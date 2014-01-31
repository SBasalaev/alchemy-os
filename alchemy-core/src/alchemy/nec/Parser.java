/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011-2014, Sergey Basalaev <sbasalaev@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package alchemy.nec;

import alchemy.nec.opt.ConstOptimizer;
import alchemy.fs.Filesystem;
import alchemy.io.ConnectionInputStream;
import alchemy.io.IO;
import alchemy.io.UTFReader;
import alchemy.nec.syntax.*;
import alchemy.nec.syntax.expr.*;
import alchemy.nec.syntax.statement.*;
import alchemy.nec.syntax.type.*;
import alchemy.types.*;
import alchemy.util.ArrayList;
import alchemy.util.HashMap;
import alchemy.util.Strings;
import java.io.IOException;

/**
 * Parser for Ether language revision 2.2.
 * @author Sergey Basalaev
 */
public class Parser {

	private final CompilerEnv env;
	private final ConstOptimizer constOptimizer;
	private final FlowAnalyzer flowAnalyzer;
	private Unit unit;

	/** Set of all files that were already parsed. */
	private ArrayList finishedFiles = new ArrayList();
	/** Stack of files we are currently parsing. */
	private ArrayList files = new ArrayList();
	/** Current tokenizer */
	private Tokenizer t;

	public Parser(CompilerEnv env) {
		this.env = env;
		this.constOptimizer = new ConstOptimizer(env);
		this.flowAnalyzer = new FlowAnalyzer(env);
	}

	public Unit parseUnit(String file) {
		Unit u = new Unit();
		this.unit = u;
		try {
			u.addType(BuiltinType.ANY);
			u.addType(BuiltinType.ARRAY);
			u.addType(BuiltinType.BOOL);
			u.addType(BuiltinType.BYTE);
			u.addType(BuiltinType.CHAR);
			u.addType(BuiltinType.DOUBLE);
			u.addType(BuiltinType.ERROR);
			u.addType(BuiltinType.FLOAT);
			u.addType(BuiltinType.FUNCTION);
			u.addType(BuiltinType.INT);
			u.addType(BuiltinType.LONG);
			u.addType(BuiltinType.SHORT);
			u.addType(BuiltinType.STRING);
			parseFile("/inc/builtin.eh");
			if (env.hasOption(CompilerEnv.F_COMPAT21)) {
				if (Filesystem.exists("/inc/compat/")) {
					env.io.setEnv("INCPATH", "/inc/compat:" + env.io.getEnv("INCPATH"));
				} else {
					env.warn("/inc/compat", 1, CompilerEnv.W_ERROR, "Compatibility headers are not installed.");
					return null;
				}
			}
			parseFile(file);
		} catch (IOException ioe) {
			IO.println(env.io.stderr, "I/O error while reading " + files.last() + ": " + ioe.getMessage());
		} catch (ParseException pe) {
			warn(CompilerEnv.W_ERROR, pe.getMessage());
		} catch (Exception e) {
			// bug in compiler
			String details = "At: " + files.last() + ':' + t.lineNumber()
					+ "\nLast token: " + t;
			env.exceptionHappened("Parser", details, e);
		}
		return unit;
	}

	/**
	 * Finds file referenced in 'use' directive.
	 * Checked files are (in that order):
	 *   ./name
	 *   ./name.eh
	 *   $INCPATH/name
	 *   $INCPATH/name.eh
	 */
	private String resolveFile(String name) throws ParseException {
		if (name.length() == 0) {
			throw new ParseException("Empty string in 'use'");
		}
		String f = env.io.toFile(name);
		if (Filesystem.exists(f) && !Filesystem.isDirectory(f)) return f;
		f = env.io.toFile(name+".eh");
		if (Filesystem.exists(f)) return f;
		if (name.charAt(0) != '/') {
			String[] incpath = Strings.split(env.io.getEnv("INCPATH"), ':', true);
			for (int i=0; i<incpath.length; i++) {
				f = env.io.toFile(incpath[i]+'/'+name);
				if (Filesystem.exists(f) && !Filesystem.isDirectory(f)) return f;
				f = env.io.toFile(incpath[i]+'/'+name+".eh");
				if (Filesystem.exists(f)) return f;
			}
		}
		throw new ParseException("File not found: "+name);
	}

	private void parseFile(String file) throws ParseException, IOException {
		// do nothing if this file was already processed
		if (finishedFiles.contains(file)) return;

		// if file is already in stack we have cyclic inclusion
		if (files.contains(file)) {
			StringBuffer sb = new StringBuffer("Cyclic inclusion");
			for (int i=0; i<files.size(); i++) {
				sb.append("\n from ").append(files.get(i));
			}
			throw new ParseException(sb.toString());
		}

		//push file into stack and set fields
		Tokenizer oldt = t;
		String olddir = env.io.getCurrentDirectory();
		files.add(file);
		env.io.setCurrentDirectory(Filesystem.fileParent(file));
		ConnectionInputStream in = new ConnectionInputStream(Filesystem.read(file));
		env.io.addConnection(in);
		t = new Tokenizer(env, file, new UTFReader(in));

		// do parsing
		while (t.nextToken() != Token.EOF) {
			switch (t.ttype) {
				case ';': // skip those
					break;
				case Token.TYPE: {
					parseTypeDef();
					break;
				}
				case Token.USE: { // include another file
					if (t.nextToken() != Token.QUOTED)
							throw new ParseException("String literal expected after 'use'");
						parseFile(resolveFile(t.svalue));
					}
					break;
				case Token.DEF: { // parse function
					// parse definition and compare with previous definitions
					Function func = parseFunctionDef(unit);
					Var fvar = unit.getVar(func.signature);
					if (fvar == null) {
						fvar = new Var(func.signature, func.type);
						fvar.isConstant = true;
						fvar.defaultValue = func;
						unit.addVar(fvar);
					} else if (!fvar.type.equals(func.type)) {
						if (fvar.type.kind == Type.TYPE_FUNCTION)
							throw new ParseException("Definition of function "+func.signature+" conflicts with previous definition.");
						else
							throw new ParseException("Variable "+func.signature+" is already defined");
					}
					Function oldfunc = (Function) fvar.defaultValue;
					oldfunc.args = func.args;
					func = oldfunc;

					if (func.isConstructor) {
						// also create .<init>(), it may be used by subclasses
						Type owner = func.type.returnType;
						String initName = owner + ".<init>";
						Function initFunc;
						Var initVar = unit.getVar(initName);
						if (initVar == null) {
							initFunc = new Function(unit, owner + ".<init>");
							initFunc.args = new Var[func.args.length+1];
							initFunc.args[0] = new Var("this", owner);
							System.arraycopy(func.args, 0, initFunc.args, 1, func.args.length);
							Type[] argtypes = new Type[initFunc.args.length];
							for (int i=0; i<initFunc.args.length; i++) {
								argtypes[i] = initFunc.args[i].type;
							}
							initFunc.type = new FunctionType(BuiltinType.NONE, argtypes);
							initVar = new Var(initFunc.signature, initFunc.type);
							initVar.isConstant = true;
							initVar.defaultValue = initFunc;
							unit.addVar(initVar);
						} else {
							initFunc = (Function) initVar.defaultValue;
						}

						// parse body
						int next = t.nextToken();
						t.pushBack();
						if (next == '=' || next == '{') {
							// generate .new() and parse .init()
							if (func.body != null)
								throw new ParseException("Function " + func.signature + " is already implemented");
							if (!(owner instanceof ObjectType))
								throw new ParseException("Cannot create constructor of " + owner);
							initFunc.source = func.source = Filesystem.fileName((String)files.last());
							unit.implementedFunctions.add(func);
							unit.implementedFunctions.add(initFunc);
							func.hits++;
							initFunc.hits++;
							func.body = generateConstructor((ObjectType)owner, func, initFunc);
							initFunc.body = parseInitBody(owner, initFunc);
							// add return if missing
							if (flowAnalyzer.visitFunction(initFunc) == flowAnalyzer.NEXT) {
								BlockStatement block = new BlockStatement(initFunc);
								block.statements.add(initFunc.body);
								block.statements.add(new ReturnStatement(
										new ConstExpr(t.lineNumber(), BuiltinType.NULL, Null.NULL)));
								initFunc.body = block;
							}
						}
					} else {
						// parse body
						int next = t.nextToken();
						t.pushBack();
						if (next == '=' || next == '{') {
							if (func.body != null)
								throw new ParseException("Function " + func.signature + " is already implemented");
							func.body = parseFunctionBody(func);
							func.hits++;
							func.source = Filesystem.fileName((String)files.last());
							unit.implementedFunctions.add(func);
							// add return if missing
							if (flowAnalyzer.visitFunction(func) == flowAnalyzer.NEXT) {
								if (func.type.returnType == BuiltinType.NONE) {
									BlockStatement block = new BlockStatement(func);
									block.statements.add(func.body);
									block.statements.add(new ReturnStatement(
											new ConstExpr(t.lineNumber(), BuiltinType.NULL, Null.NULL)));
									func.body = block;
								} else if (func.body.kind == Statement.STAT_BLOCK) {
									BlockStatement block = (BlockStatement) func.body;
									if (block.statements.size() > 0) {
										Statement last = (Statement) block.statements.last();
										if (last.kind == Statement.STAT_EXPR) {
											Expr expr = cast(((ExprStatement)last).expr, func.type.returnType);
											if (!env.hasOption(CompilerEnv.F_COMPAT21)) {
												warn(CompilerEnv.W_RETURN, "'return' is not stated explicitely");
											}
											block.statements.set(block.statements.size()-1, new ReturnStatement(expr));
										}
									}
								} else {
									warn(CompilerEnv.W_ERROR, "Missing return statement");
								}
							}
						}
					}
					break;
				}
				case Token.VAR:
				case Token.CONST: { // parse global variable or constant
					boolean isConst = t.ttype == Token.CONST;
					if (t.nextToken() != Token.WORD)
						throw new ParseException("Variable name expected");
					String varname = t.svalue;
					if (unit.getVar(varname) != null)
						warn(CompilerEnv.W_ERROR, "Variable " + varname + " is already defined");
					Type vartype = null;
					Expr varvalue = null;
					// parsing type
					if (t.nextToken() == ':') {
						vartype = parseType(unit);
					} else {
						t.pushBack();
					}
					// parsing value
					if (t.nextToken() == '=') {
						varvalue = (Expr) parseExpr(unit).accept(constOptimizer, unit);
						if (varvalue.kind != Expr.EXPR_CONST)
							throw new ParseException("Constant expression expected");
						if (vartype == null) {
							vartype = varvalue.returnType();
							if (vartype == BuiltinType.NULL) vartype = BuiltinType.ANY;
						} else {
							varvalue = (Expr)cast(varvalue, vartype).accept(constOptimizer, unit);
						}
					} else {
						t.pushBack();
					}
					// defining variable
					if (vartype == null) {
						throw new ParseException("Type of "+varname+" is not defined");
					}
					Var v = new Var(varname, vartype);
					if (isConst) {
						v.isConstant = true;
						if (varvalue == null) {
							throw new ParseException("Constant "+varname+" is not initialized");
						}
					}
					if (varvalue != null) {
						v.defaultValue = ((ConstExpr)varvalue).value;
					} else {
						v.defaultValue = defaultValue(vartype);
					}
					unit.addVar(v);
					break;
				}
				default:
					throw new ParseException(t.toString() + " unexpected here");
			}
		}

		// pop file from stack and revert fields
		t = oldt;
		env.io.setCurrentDirectory(olddir);
		finishedFiles.add(files.last());
		files.remove(-1);
		in.close();
		env.io.removeConnection(in);
	}

	private void parseTypeDef() throws IOException, ParseException {
		// reading name and parent of type
		if (t.nextToken() != Token.WORD)
			throw new ParseException("Type name expected after 'type'");
		String typename = t.svalue;
		Type parent;
		if (t.nextToken() == '<') {
			if (t.nextToken() != Token.WORD)
				throw new ParseException("Type name expected after '<'");
			String parentName = t.svalue;
			parent = unit.getType(parentName);
			if (parent == null)
				throw new ParseException("Type " + parentName + " is not defined");
		} else {
			t.pushBack();
			parent = BuiltinType.ANY;
		}

		ObjectType newType;
		if (parent == BuiltinType.ANY) {
			newType = new ObjectType(typename, null);
		} else if (parent instanceof ObjectType) {
			newType = new ObjectType(typename, (ObjectType)parent);
		} else {
			throw new ParseException("Cannot create subtype of " + parent);
		}

		Type prevType = unit.getType(typename);
		if (prevType != null) {
			if (!(prevType instanceof ObjectType) || !parent.equals(prevType.superType()))
				throw new ParseException("Definition of " + typename + " conflicts with previous definition");
			newType = (ObjectType)prevType;
		} else {
			unit.addType(newType);
		}

		if (t.nextToken() == '{') {
			if (newType.fields != null)
				throw new ParseException("Type " + typename + " is already defined");
			ArrayList fields = new ArrayList();
			ArrayList fieldNames = new ArrayList();
			if (newType.parent != null) {
				Var[] parentFields = newType.parent.fields;
				if (parentFields == null)
					throw new ParseException("Cannot extend " + newType.parent + " since its structure is not known");
				for (int i=0; i<parentFields.length; i++) {
					fields.add(parentFields[i]);
					fieldNames.add(parentFields[i].name);
				}
			}
			boolean first = true;
			while (t.nextToken() != '}') {
				t.pushBack();
				if (first) first = false;
				else expect(',');
				if (t.nextToken() != Token.WORD)
					throw new ParseException("Field name expected, got " + t);
				String fieldName = t.svalue;
				if (fieldNames.contains(t.svalue))
					throw new ParseException("Field " + typename + '.' + fieldName + " is already defined");
				expect(':');
				Type fieldType = parseType(unit);
				Object fieldValue = null;
				if (t.nextToken() == '=') {
					Expr defExpr = (Expr) cast(parseExpr(unit), fieldType).accept(constOptimizer, unit);
					if (defExpr.kind != Expr.EXPR_CONST) {
						warn(CompilerEnv.W_ERROR, "Constant expression expected");
					} else {
						fieldValue = ((ConstExpr)defExpr).value;
					}
				} else {
					t.pushBack();
					fieldValue = defaultValue(fieldType);
				}
				Var field = new Var(fieldName, fieldType);
				field.defaultValue = fieldValue;
				fields.add(field);
				fieldNames.add(fieldName);
			}
			newType.fields = new Var[fields.size()];
			fields.copyInto(newType.fields);
		} else {
			t.pushBack();
		}
	}

	private Function parseFunctionDef(Scope scope) throws IOException, ParseException {
		// parse name
		if (t.nextToken() != Token.WORD && t.ttype != Token.THROW)
			throw new ParseException("Function name expected, got "+t);
		String sig = t.svalue;
		Type owner = null;
		boolean isConstructor = false;
		if (t.nextToken() == '.') {
			owner = scope.getType(sig);
			if (owner == null) {
				warn(CompilerEnv.W_ERROR, "Type " + sig + " is not defined");
				owner = BuiltinType.ANY;
			}
			if (t.nextToken() == Token.NEW) {
				isConstructor = true;
			} else if (t.ttype != Token.WORD) {
				throw new ParseException("Function name expected, got "+t);
			}
			sig = owner.name + '.' + t.svalue;
		} else {
			t.pushBack();
		}
		Function func = new Function(scope, sig);
		func.isPublic = true;
		func.isConstructor = isConstructor;

		// parse argument list
		expect('(');
		ArrayList arglist = new ArrayList();
		ArrayList names = new ArrayList();
		if (owner != null && !isConstructor) {
			arglist.add(new Var("this", owner));
			names.add("this");
		}
		boolean first = true;
		boolean defaults = false;
		while (t.nextToken() != ')') {
			t.pushBack();
			if (first) first = false;
			else expect(',');
			if (t.nextToken() != Token.WORD)
				throw new ParseException("Variable name expected, got "+t);
			String varname = t.svalue;
			if (names.contains(varname))
				warn(CompilerEnv.W_ERROR, "Variable " + varname + " is already defined");
			names.add(varname);
			expect(':');
			Type vartype = parseType(func);
			Var var = new Var(varname, vartype);
			arglist.add(var);
			if (t.nextToken() == '=') {
				defaults = true;
				Expr expr = (Expr) cast(parseExpr(scope), vartype).accept(constOptimizer, scope);
				if (expr instanceof ConstExpr) {
					var.defaultValue = ((ConstExpr)expr).value;
				} else {
					throw new ParseException("Constant expression expected");
				}
			} else if (defaults) {
				throw new ParseException("No default provided for argument " + varname);
			} else {
				t.pushBack();
			}
		}

		// parse return type
		Type rettype;
		if (t.nextToken() == ':') {
			rettype = parseType(func);
		} else {
			t.pushBack();
			rettype = BuiltinType.NONE;
		}
		if (func.isConstructor) {
			if (rettype == BuiltinType.NONE) {
				rettype = owner;
			} else if (!rettype.equals(owner)) {
				warn(CompilerEnv.W_ERROR, "Constructor returns value other than " + owner);
			}
		}

		// fill function fields
		Var[] args = new Var[arglist.size()];
		arglist.copyInto(args);
		func.args = args;
		FunctionType ftype = new FunctionType(rettype, new Type[args.length]);
		for (int i=args.length-1; i>=0; i--) {
			ftype.argtypes[i] = args[i].type;
		}
		func.type = ftype;

		// semantic checks on main function
		if (sig.equals("main")) {
			if (args.length != 1) {
				warn(CompilerEnv.W_MAIN, "main() arguments are incorrect, should be ([String])");
			} else {
				Type argtype = args[0].type;
				Type shouldbe = new ArrayType(BuiltinType.STRING);
				if (!argtype.equals(shouldbe)) {
					warn(CompilerEnv.W_MAIN, "main() arguments are incorrect, should be ([String])");
				}
			}
			if (rettype != BuiltinType.INT && rettype != BuiltinType.NONE) {
				warn(CompilerEnv.W_MAIN, "main() return value is incorrect, should be Int or <none>");
			}
		}

		// semantic checks on certain methods
		if (owner != null) {
			String methodname = sig.substring(sig.lastIndexOf('.')+1);
			if (methodname.equals("eq") &&
					(rettype != BuiltinType.BOOL || args.length != 2 || !ftype.argtypes[1].equals(owner)))
				warn(CompilerEnv.W_OVERRIDES, "Method " + sig + " cannot be used as override for equality operators");
			else if (methodname.equals("cmp") &&
					(rettype != BuiltinType.INT || args.length != 2 || !ftype.argtypes[1].equals(owner)))
				warn(CompilerEnv.W_OVERRIDES, "Method " + sig + " cannot be used as override for comparison operators");
			else if (methodname.equals("tostr") &&
					(rettype != BuiltinType.STRING || args.length != 1))
				warn(CompilerEnv.W_OVERRIDES, "Method " + sig + " cannot be used for conversion to a String");
		}

		return func;
	}

	/**
	 * Generates body of .new() method.
	 * Body has the following structure:
	 * <pre>
	 * def Type.new(...): Type {
	 *   Type this = new Type {  }
	 *   this.<init>(...)
	 *   return this
	 * }
	 * </pre>
	 */
	private Statement generateConstructor(ObjectType owner, Function newFunc, Function initFunc) throws ParseException {
		if (owner.fields == null)
			throw new ParseException("Cannot create constructor of " + owner + " since its structure is not known");
		BlockStatement block = new BlockStatement(newFunc);
		Var thisVar = new Var("this", owner);
		thisVar.isConstant = true;
		thisVar.hits = 2;
		block.addVar(thisVar);
		// create object and initialize default fields
		int line = t.lineNumber();
		Expr[] initializers = new Expr[owner.fields.length];
		for (int i=0; i<initializers.length; i++) {
			if (owner.fields[i].defaultValue != null) {
				initializers[i] = new ConstExpr(line, owner.fields[i].type, owner.fields[i].defaultValue);
			}
		}
		block.statements.add(new AssignStatement(thisVar, new NewArrayInitExpr(line, owner, initializers)));
		// call .<init>()
		Expr[] initArgs = new Expr[1 + newFunc.args.length];
		initArgs[0] = new VarExpr(line, thisVar);
		for (int i=0; i<newFunc.args.length; i++) {
			initArgs[i+1] = new VarExpr(line, newFunc.args[i]);
		}
		block.statements.add(new ExprStatement(new CallExpr(line, initFunc, initArgs)));
		// return
		block.statements.add(new ReturnStatement(new VarExpr(line, thisVar)));
		return block;
	}

	/**
	 * Parses body of constructor.
	 * The first statement of constructor might be call to super().
	 */
	private Statement parseInitBody(Type owner, Function f) throws IOException, ParseException {
		Function superInit = findMethod(owner.superType(), "<init>");
		if (superInit == null) {
			return parseFunctionBody(f);
		} else {
			boolean autoInit = superInit.args.length == 1;
			BlockStatement block = new BlockStatement(f);
			int line = t.lineNumber();
			Expr getThis = new VarExpr(line, f.args[0]);
			if (t.nextToken() == '=') {
				if (t.nextToken() == Token.SUPER) {
					expect('(');
					Expr superCall = parseFunctionCall(block, new ConstExpr(line, superInit.type, superInit), getThis);
					block.statements.add(new ExprStatement(superCall));
				} else if (autoInit) {
					t.pushBack();
					Expr superCall = new CallExpr(line, superInit, new Expr[] { getThis });
					block.statements.add(new ExprStatement(superCall));
					block.statements.add(new ExprStatement(parseExpr(block)));
				} else {
					throw new ParseException("Missing super() call");
				}
				block.statements.add(new ReturnStatement(new ConstExpr(t.lineNumber(), BuiltinType.NULL, Null.NULL)));
			} else { // token == '{'
				if (t.nextToken() == Token.SUPER) {
					expect('(');
					Expr superCall = parseFunctionCall(block, new ConstExpr(line, superInit.type, superInit), getThis);
					block.statements.add(new ExprStatement(superCall));
				} else if (autoInit) {
					t.pushBack();
					Expr superCall = new CallExpr(line, superInit, new Expr[] { getThis });
					block.statements.add(new ExprStatement(superCall));
				} else {
					throw new ParseException("Missing super() call");
				}
				while (t.nextToken() != '}') {
					t.pushBack();
					block.statements.add(parseStatement(block));
				}
			}
			return block;
		}
	}

	private Statement parseFunctionBody(Function f) throws IOException, ParseException {
		if (t.nextToken() == '=') {
			if (f.type.returnType == BuiltinType.NONE) {
				BlockStatement block = new BlockStatement(f);
				block.statements.add(new ExprStatement(parseExpr(block)));
				block.statements.add(new ReturnStatement(new ConstExpr(t.lineNumber(), BuiltinType.NULL, Null.NULL)));
				return block;
			} else {
				return new ReturnStatement(cast(parseExpr(f), f.type.returnType));
			}
		} else { // token == '{'
			t.pushBack();
			return parseStatement(f);
		}
	}

	private Statement parseStatement(Scope scope) throws IOException, ParseException {
		switch (t.nextToken()) {
			case ';':
				return new EmptyStatement();
			case '{': {
				BlockStatement block = new BlockStatement(scope);
				while (t.nextToken() != '}') {
					t.pushBack();
					block.statements.add(parseStatement(block));
				}
				if (block.statements.isEmpty()) return new EmptyStatement();
				return block;
			}
			case Token.BREAK: {
				return new BreakStatement(t.lineNumber());
			}
			case Token.CONTINUE: {
				return new ContinueStatement(t.lineNumber());
			}
			case Token.IF: {
				expect('(');
				Expr condition = cast(parseExpr(scope), BuiltinType.BOOL);
				expect(')');
				Statement ifstat = parseStatement(scope);
				Statement elsestat;
				if (ifstat.kind == Statement.STAT_EMPTY)
					warn(CompilerEnv.W_EMPTY, "Empty statement after 'if'");
				if (t.nextToken() == Token.ELSE) {
					elsestat = parseStatement(scope);
					if (elsestat.kind == Statement.STAT_EMPTY)
						warn(CompilerEnv.W_EMPTY, "Empty statement after 'else'");
				} else {
					t.pushBack();
					elsestat = new EmptyStatement();
				}
				return new IfStatement(condition, ifstat, elsestat);
			}
			case Token.RETURN: {
				Type rettype = scope.enclosingFunction().type.returnType;
				Expr returnExpr;
				if (rettype.kind == Type.TYPE_NONE) {
					returnExpr = new ConstExpr(t.lineNumber(), BuiltinType.NULL, Null.NULL);
				} else {
					returnExpr = cast(parseExpr(scope), rettype);
				}
				if (t.nextToken() != ';') t.pushBack();
				return new ReturnStatement(returnExpr);
			}
			case Token.DO: {
				Statement body = parseStatement(scope);
				expect(Token.WHILE);
				expect('(');
				Expr condition = cast(parseExpr(scope), BuiltinType.BOOL);
				expect(')');
				return new LoopStatement(body, condition, new EmptyStatement());
			}
			case Token.WHILE: {
				expect('(');
				Expr condition;
				Statement prestat = parseStatement(scope);
				if (t.nextToken() == ',') {
					condition = cast(parseExpr(scope), BuiltinType.BOOL);
				} else {
					t.pushBack();
					if (prestat.kind != Statement.STAT_EXPR)
						throw new ParseException("Bool expression expected");
					condition = cast(((ExprStatement)prestat).expr, BuiltinType.BOOL);
					prestat = new EmptyStatement();
				}
				expect(')');
				return new LoopStatement(prestat, condition, parseStatement(scope));
			}
			case Token.TRY: {
				Statement tryStat = parseStatement(scope);
				Var catchVar = null;
				Statement catchStat;
				expect(Token.CATCH);
				if (t.nextToken() == '(') {
					if (t.nextToken() == Token.VAR) {
						// parse as 'catch (var error) { catchBlock }'
						if (t.nextToken() != Token.WORD)
							throw new ParseException("Variable name expected");
						catchVar = new Var(t.svalue, BuiltinType.ERROR);
						expect(')');
						BlockStatement catchBlock = new BlockStatement(scope);
						if (catchBlock.addVar(catchVar))
							warn(CompilerEnv.W_HIDDEN, "Variable " + catchVar.name + " hides another variable with the same name");
						catchStat = parseStatement(catchBlock);
						catchBlock.statements.add(catchStat);
					} else {
						// rewind to 'catch { catchBlock }'
						t.pushBack();
						catchStat = parseStatement(scope);
						expect(')');
					}
				} else {
					t.pushBack();
					catchStat = parseStatement(scope);
				}
				return new TryCatchStatement(tryStat, catchVar, catchStat);
			}
			case Token.VAR:
			case Token.CONST: {
				if (!(scope instanceof BlockStatement)) {
					scope = new BlockStatement(scope);
				}
				BlockStatement block = (BlockStatement) scope;
				boolean isConst = t.ttype == Token.CONST;
				if (t.nextToken() != Token.WORD)
					throw new ParseException("Variable name expected, got " + t);
				String varname = t.svalue;
				if (block.vars.get(varname) != null)
					warn(CompilerEnv.W_ERROR, "Variable " + varname + " is already defined");
				Type vartype = null;
				Expr varvalue = null;
				if (t.nextToken() == ':') {
					vartype = parseType(scope);
				} else {
					t.pushBack();
				}
				if (t.nextToken() == '=') {
					varvalue = parseExpr(scope);
					if (vartype == null) {
						vartype = varvalue.returnType();
					} else {
						varvalue = cast(varvalue, vartype);
					}
				} else {
					t.pushBack();
					if (vartype == null)
						vartype = BuiltinType.ANY;
					if (varvalue == null) {
						Object dflt = defaultValue(vartype);
						if (dflt == null) dflt = Null.NULL;
						varvalue = new ConstExpr(t.lineNumber(), vartype, dflt);
					}
				}
				if (vartype == BuiltinType.NULL)
					vartype = BuiltinType.ANY;
				else if (vartype == BuiltinType.NONE)
					throw new ParseException("Cannot create variable of type <none>");
				Var var = new Var(varname, vartype);
				var.isConstant = isConst;
				if (varvalue.kind == Expr.EXPR_CONST) {
					var.defaultValue = ((ConstExpr)varvalue).value;
				}
				if (block.addVar(var))
					warn(CompilerEnv.W_HIDDEN, "Variable " + varname + " hides another variable with the same name");
				return new AssignStatement(var, varvalue);
			}
			case Token.FOR:
				return parseForLoop(scope);
			case Token.SWITCH:
				return parseSwitchStatement(scope);
			case Token.THROW: {
				expect('(');
				Expr errCode;
				Expr errMsg;
				if (t.nextToken() == ')') {
					errCode = new ConstExpr(t.lineNumber(), BuiltinType.INT, Int32.ONE);
					errMsg = new ConstExpr(t.lineNumber(), BuiltinType.STRING, Null.NULL);
				} else {
					t.pushBack();
					errCode = cast(parseExpr(scope), BuiltinType.INT);
					if (t.nextToken() == ',') {
						errMsg = cast(parseExpr(scope), BuiltinType.STRING);
					} else {
						t.pushBack();
						errMsg = new ConstExpr(t.lineNumber(), BuiltinType.STRING, Null.NULL);
					}
					expect(')');
				}
				if (t.nextToken() != ';') t.pushBack();
				return new ThrowStatement(errCode, errMsg);
			}
			default: {
				t.pushBack();
				Statement stat = parseExprStatement(scope);
				if (t.nextToken() != ';') t.pushBack();
				return stat;
			}
		}
	}

	/**
	 * Parses for loops.
	 * <br/>
	 * Classic for:
	 * <pre>
	 * for (init, condition, increment) body
	 * </pre>
	 * we parse as
	 * <pre>
	 * BlockStatement {
	 *   init
	 *   ForLoopStatement {
	 *     condition
	 *     increment
	 *     body
	 *   }
	 * }
	 * </pre>
	 *
	 * Loops over collections are translated as:
	 * <pre>
	 * for (var i in from..to)  =&gt;  for (var i=from, i &lt;= to, i+=1)
	 * for (var i in range)     =&gt;  for (var i=range.from, i &lt;= range.to, i+=1)
	 * for (var i in array)     =&gt;  for (var #=0, # &lt; array.len, #+=1) { i = array[#], ... }
	 */
	private Statement parseForLoop(Scope scope) throws IOException, ParseException {
		expect('(');
		BlockStatement forBlock = new BlockStatement(scope);
		if (t.nextToken() != Token.VAR) {
			// old style 'for (init, cond, incr)'
			t.pushBack();
			forBlock.statements.add(parseExprStatement(forBlock));
			expect(',');
			Expr condition = cast(parseExpr(forBlock), BuiltinType.BOOL);
			expect(',');
			Statement incr = parseExprStatement(forBlock);
			expect(')');
			Statement forBody = parseStatement(forBlock);
			forBlock.statements.add(new ForLoopStatement(condition, incr, forBody));
			return forBlock;
		}
		// starts with 'for (var name ...'
		if (t.nextToken() != Token.WORD)
			throw new ParseException("Variable name expected");
		String varname = t.svalue;
		Type vartype = null;
		if (t.nextToken() == ':') {
			vartype = parseType(forBlock);
		} else {
			t.pushBack();
		}
		if (t.nextToken() == '=') {
			// still classic for loop
			Expr initExpr = parseExpr(forBlock);
			if (vartype == null) {
				vartype = initExpr.returnType();
			} else {
				initExpr = cast(initExpr, vartype);
			}
			Var var = new Var(varname, vartype);
			if (forBlock.addVar(var)) {
				warn(CompilerEnv.W_HIDDEN, "Variable " + varname + " hides another variable with the same name");
			}
			forBlock.statements.add(new AssignStatement(var, initExpr));
			expect(',');
			Expr condition = cast(parseExpr(forBlock), BuiltinType.BOOL);
			expect(',');
			Statement incr = parseStatement(forBlock);
			expect(')');
			forBlock.statements.add(new ForLoopStatement(condition, incr, parseStatement(forBlock)));
			return forBlock;
		}
		t.pushBack();
		// for loop over collection
		expect(Token.IN);
		Expr collection = parseExpr(forBlock);
		Type collType = collection.returnType();
		expect(')');
		switch (collType.kind) {
			case Type.TYPE_INTRANGE:
			case Type.TYPE_LONGRANGE: {
				// init is 'loopVar = from; var #to = to'
				Type itemType = (collType.kind == Type.TYPE_INTRANGE) ? BuiltinType.INT : BuiltinType.LONG;
				if (vartype == null) vartype = itemType;
				Var loopVar = new Var(varname, vartype);
				loopVar.hits = 2; // in comparison and increment
				if (forBlock.addVar(loopVar)) {
					warn(CompilerEnv.W_HIDDEN, "Variable " + varname + " hides another variable with the same name");
				}
				Var toVar = new Var("#to", vartype);
				toVar.isConstant = true;
				toVar.hits = 1; // in comparison
				forBlock.addVar(toVar);
				if (collection.kind == Expr.EXPR_RANGE) {
					forBlock.statements.add(new AssignStatement(loopVar, cast(((RangeExpr)collection).fromExpr, vartype)));
					forBlock.statements.add(new AssignStatement(toVar, cast(((RangeExpr)collection).toExpr, vartype)));
				} else {
					Var rangeVar = new Var("#range", collType);
					rangeVar.isConstant = true;
					rangeVar.hits = 2; // to load .from and .to
					Expr rangeLoad = new VarExpr(-1, rangeVar);
					Expr getFrom = new ArrayElementExpr(rangeLoad, new ConstExpr(-1, BuiltinType.INT, Int32.ZERO), itemType);
					Expr getTo = new ArrayElementExpr(rangeLoad, new ConstExpr(-1, BuiltinType.INT, Int32.ONE), itemType);
					// create subBlock in which #range will live
					BlockStatement subBlock = new BlockStatement(forBlock);
					subBlock.addVar(rangeVar);
					subBlock.statements.add(new AssignStatement(rangeVar, collection));
					subBlock.statements.add(new AssignStatement(loopVar, cast(getFrom, vartype)));
					subBlock.statements.add(new AssignStatement(toVar, cast(getTo, vartype)));
					forBlock.statements.add(subBlock);
				}
				Expr loadVar = new VarExpr(-1, loopVar);
				Expr loadTo = new VarExpr(-1, toVar);
				// condition is 'var <= toConst'
				Expr condition  = new ComparisonExpr(loadVar, Token.LTEQ, loadTo);
				// increment is 'var += 1'
				Expr plusOne;
				switch (vartype.kind) {
					case Type.TYPE_INT:
						plusOne = new ConstExpr(-1, BuiltinType.INT, Int32.ONE);
						break;
					case Type.TYPE_LONG:
						plusOne = new ConstExpr(-1, BuiltinType.LONG, new Int64(1L));
						break;
					default:
						throw new ParseException("Variable " + varname + " must be Int or Long");
				}
				Statement increment = new CompoundAssignStatement(loopVar, Token.PLUSEQ, plusOne);
				forBlock.statements.add(new ForLoopStatement(condition, increment, parseStatement(forBlock)));
				return forBlock;
			}
			case Type.TYPE_ARRAY: {
				Type itemType = ((ArrayType)collType).elementType;
				if (vartype == null) vartype = itemType;
				// init is 'var #array = collection; var #index = 0; var #len = #array.len'
				Var arrayVar = new Var("#array", collection.returnType());
				Var indexVar = new Var("#index", BuiltinType.INT);
				Var lenVar = new Var("#len", BuiltinType.INT);
				arrayVar.isConstant = true;
				lenVar.isConstant = true;
				arrayVar.hits = 2; // in init and body
				indexVar.hits = 2; // in body and condition
				lenVar.hits = 1;   // in condition
				forBlock.addVar(arrayVar);
				forBlock.addVar(indexVar);
				forBlock.addVar(lenVar);
				Expr getArray = new VarExpr(-1, arrayVar);
				Expr getIndex = new VarExpr(-1, indexVar);
				Expr getLen = new VarExpr(-1, lenVar);
				forBlock.statements.add(new AssignStatement(arrayVar, collection));
				forBlock.statements.add(new AssignStatement(indexVar, new ConstExpr(-1, BuiltinType.INT, Int32.ZERO)));
				if (collection.kind == Expr.EXPR_NEWARRAY_INIT) {
					arrayVar.hits = 1;
					int len = ((NewArrayInitExpr)collection).initializers.length;
					forBlock.statements.add(new AssignStatement(lenVar, new ConstExpr(-1, BuiltinType.INT, Int32.toInt32(len))));
				} else {
					forBlock.statements.add(new AssignStatement(lenVar, new ArrayLenExpr(getArray)));
				}
				// condition is '#index < #len'
				Expr condition = new ComparisonExpr(getIndex, '<', getLen);
				// increment is '#index += 1'
				Statement increment = new CompoundAssignStatement(indexVar, Token.PLUSEQ, new ConstExpr(-1, BuiltinType.INT, Int32.ONE));
				// add 'var loopVar = #array[#index]' to body
				BlockStatement body = new BlockStatement(forBlock);
				Var loopVar = new Var(varname, vartype);
				body.addVar(loopVar);
				Expr arrayItem = cast(new ArrayElementExpr(getArray, getIndex, itemType), vartype);
				body.statements.add(new AssignStatement(loopVar, arrayItem));
				body.statements.add(parseStatement(body));
				forBlock.statements.add(new ForLoopStatement(condition, increment, body));
				return forBlock;
			}
			default:
				throw new ParseException("Type " + collType + " is not iterable");
		}
	}

	private Statement parseSwitchStatement(Scope scope) throws IOException, ParseException {
		expect('(');
		Expr keyExpr = parseExpr(scope);
		Type keyType = keyExpr.returnType();
		boolean intSwitch;
		if (keyType == BuiltinType.STRING) {
			intSwitch = false;
		} else if (keyType == BuiltinType.INT || keyType == BuiltinType.BYTE
		        || keyType == BuiltinType.CHAR || keyType == BuiltinType.SHORT) {
			intSwitch = true;
			keyExpr = cast(keyExpr, BuiltinType.INT);
			keyType = BuiltinType.INT;
		} else {
			throw new ParseException("Switch over " + keyType + " values is not supported");
		}
		expect(')');
		ArrayList keys = new ArrayList();
		ArrayList keySets = new ArrayList();
		ArrayList statements = new ArrayList();
		Statement elseStat = null;
		expect('{');
		while (t.nextToken() != '}') {
			// reading possible else branch
			if (t.ttype == Token.ELSE) {
				expect(':');
				if (elseStat != null)
					warn(CompilerEnv.W_ERROR, "Duplicate else case");
				elseStat = parseStatement(scope);
				continue;
			}

			// reading set of keys
			ArrayList set = new ArrayList();
			boolean first = true;
			do {
				t.pushBack();
				if (first) first = false;
				else expect(',');
				Expr caseExpr = parseExpr(scope);
				if (intSwitch && caseExpr.kind == Expr.EXPR_RANGE) {
					Expr case1 = (Expr) cast(((RangeExpr)caseExpr).fromExpr, BuiltinType.INT).accept(constOptimizer, scope);
					Expr case2 = (Expr) cast(((RangeExpr)caseExpr).toExpr, BuiltinType.INT).accept(constOptimizer, scope);
					if (case1.kind != Expr.EXPR_CONST || case2.kind != Expr.EXPR_CONST)
						throw new ParseException("Constant expression expected");
					int from = ((Int32)((ConstExpr)case1).value).value;
					int to = ((Int32)((ConstExpr)case1).value).value;
					if (from > to)
						warn(CompilerEnv.W_ERROR, "Invalid range " + from + ".." + to);
					for (int i=from; i<=to; i++) {
						Int32 I = Int32.toInt32(i);
						if (keys.contains(I)) warn(CompilerEnv.W_ERROR, "Duplicate switch case " + i);
						keys.add(I);
						set.add(I);
					}
				} else {
					caseExpr = (Expr) cast(caseExpr, keyType).accept(constOptimizer, scope);
					if (caseExpr.kind != Expr.EXPR_CONST)
						throw new ParseException("Constant expression expected");
					Object key = ((ConstExpr)caseExpr).value;
					if (keys.contains(key)) warn(CompilerEnv.W_ERROR, "Duplicate switch case " + key);
					keys.add(key);
					set.add(key);
				}
			} while (t.nextToken() != ':');
			// reading branch
			keySets.add(set);
			statements.add(parseStatement(scope));
		}
		if (elseStat == null) elseStat = new EmptyStatement();
		// return switch
		if (intSwitch) {
			int branchCount = statements.size();
			Statement[] statArray = new Statement[branchCount];
			statements.copyInto(statArray);
			int[][] keySetArray = new int[branchCount][];
			for (int i=0; i<branchCount; i++) {
				ArrayList set = (ArrayList) keySets.get(i);
				keySetArray[i] = new int[set.size()];
				set.copyInto(keySetArray[i]);
			}
			return new SwitchStatement(keyExpr, keySetArray, statArray, elseStat);
		} else {
			// string switch we implement as two consequent switches
			// first is switch over hashcodes which returns branch number
			BlockStatement switchBlock = new BlockStatement(scope);
			Var strVar = new Var("#string", BuiltinType.STRING);
			strVar.hits = 1;
			strVar.isConstant = true;
			Var indexVar = new Var("#index", BuiltinType.INT);
			indexVar.hits = 1;
			switchBlock.addVar(strVar);
			switchBlock.addVar(indexVar);
			switchBlock.statements.add(new AssignStatement(strVar, keyExpr));
			switchBlock.statements.add(new AssignStatement(indexVar, new ConstExpr(-1, BuiltinType.INT, Int32.M_ONE)));
			// create first switch
			Expr strExpr = new VarExpr(-1, strVar);
			ArrayList hashes = new ArrayList();
			ArrayList checkStatements = new ArrayList();
			for (int branchIndex = 0; branchIndex < keySets.size(); branchIndex++) {
				ArrayList set = (ArrayList) keySets.get(branchIndex);
				for (int keyIdx=0; keyIdx < set.size(); keyIdx++) {
					String key = (String) set.get(keyIdx);
					Int32 hash = Int32.toInt32(key.hashCode());
					int hashIdx = hashes.indexOf(hash);
					if (hashIdx < 0) {
						hashIdx = hashes.size();
						hashes.add(hash);
						checkStatements.add(new EmptyStatement());
					}
					Statement checkStat = (Statement) checkStatements.get(hashIdx);
					checkStat = new IfStatement(
							new ComparisonExpr(strExpr, Token.EQEQ, new ConstExpr(-1, BuiltinType.STRING, key)),
							new AssignStatement(indexVar, new ConstExpr(-1, BuiltinType.INT, Int32.toInt32(branchIndex))),
							checkStat);
					strVar.hits++;
					checkStatements.set(hashIdx, checkStat);
				}
			}
			int[][] keySetArray = new int[hashes.size()][];
			for (int i=0; i<keySetArray.length; i++) {
				keySetArray[i] = new int[] { ((Int32)hashes.get(i)).value };
			}
			Statement[] statArray = new Statement[hashes.size()];
			checkStatements.copyInto(statArray);
			Expr getHash = new CallExpr(-1, unit.getFunction("String.hash"), new Expr[] { strExpr });
			switchBlock.statements.add(new SwitchStatement(getHash, keySetArray, statArray, new EmptyStatement()));
			// create second switch
			keySetArray = new int[statements.size()][];
			for (int i=0; i<keySetArray.length; i++) {
				keySetArray[i] = new int[] { i };
			}
			statArray = new Statement[statements.size()];
			statements.copyInto(statArray);
			switchBlock.statements.add(new SwitchStatement(new VarExpr(-1, indexVar), keySetArray, statArray, elseStat));
			return switchBlock;
		}
	}

	private Expr parseSwitchExpr(Scope scope) throws IOException, ParseException {
		expect('(');
		Expr keyExpr = parseExpr(scope);
		Type keyType = keyExpr.returnType();
		boolean intSwitch;
		if (keyType == BuiltinType.STRING) {
			intSwitch = false;
		} else if (keyType == BuiltinType.INT || keyType == BuiltinType.BYTE
		        || keyType == BuiltinType.CHAR || keyType == BuiltinType.SHORT) {
			intSwitch = true;
			keyExpr = cast(keyExpr, BuiltinType.INT);
			keyType = BuiltinType.INT;
		} else {
			throw new ParseException("Switch over " + keyType + " values is not supported");
		}
		expect(')');
		ArrayList keys = new ArrayList();
		ArrayList keySets = new ArrayList();
		ArrayList exprs = new ArrayList();
		Expr elseExpr = null;
		expect('{');
		while (t.nextToken() != '}') {
			// reading possible else branch
			if (t.ttype == Token.ELSE) {
				expect(':');
				if (elseExpr != null)
					warn(CompilerEnv.W_ERROR, "Duplicate else case");
				elseExpr = parseExpr(scope);
				if (t.nextToken() != ';') t.pushBack();
				continue;
			}

			// reading set of keys
			ArrayList set = new ArrayList();
			boolean first = true;
			do {
				t.pushBack();
				if (first) first = false;
				else expect(',');
				Expr caseExpr = parseExpr(scope);
				if (intSwitch && caseExpr.kind == Expr.EXPR_RANGE) {
					Expr case1 = (Expr) cast(((RangeExpr)caseExpr).fromExpr, BuiltinType.INT).accept(constOptimizer, scope);
					Expr case2 = (Expr) cast(((RangeExpr)caseExpr).toExpr, BuiltinType.INT).accept(constOptimizer, scope);
					if (case1.kind != Expr.EXPR_CONST || case2.kind != Expr.EXPR_CONST)
						throw new ParseException("Constant expression expected");
					int from = ((Int32)((ConstExpr)case1).value).value;
					int to = ((Int32)((ConstExpr)case1).value).value;
					if (from > to)
						warn(CompilerEnv.W_ERROR, "Invalid range " + from + ".." + to);
					for (int i=from; i<=to; i++) {
						Int32 I = Int32.toInt32(i);
						if (keys.contains(I)) warn(CompilerEnv.W_ERROR, "Duplicate switch case " + i);
						keys.add(I);
						set.add(I);
					}
				} else {
					caseExpr = (Expr) cast(caseExpr, keyType).accept(constOptimizer, scope);
					if (caseExpr.kind != Expr.EXPR_CONST)
						throw new ParseException("Constant expression expected");
					Object key = ((ConstExpr)caseExpr).value;
					if (keys.contains(key)) warn(CompilerEnv.W_ERROR, "Duplicate switch case " + key);
					keys.add(key);
					set.add(key);
				}
			} while (t.nextToken() != ':');
			// reading branch
			keySets.add(set);
			exprs.add(parseExpr(scope));
			if (t.nextToken() != ';') t.pushBack();
		}

		// calculating return type
		if (elseExpr == null) {
			throw new ParseException("Missing else branch");
		}
		Type rettype = elseExpr.returnType();
		for (int i=exprs.size()-1; i>=0; i--) {
			rettype = binaryCastType(rettype, ((Expr)exprs.get(i)).returnType());
		}
		elseExpr = cast(elseExpr, rettype);
		for (int i=exprs.size()-1; i>=0; i--) {
			exprs.set(i, cast((Expr)exprs.get(i), rettype));
		}

		// return switch
		if (intSwitch) {
			int branchCount = exprs.size();
			Expr[] exprArray = new Expr[branchCount];
			exprs.copyInto(exprArray);
			int[][] keySetArray = new int[branchCount][];
			for (int i=0; i<branchCount; i++) {
				ArrayList set = (ArrayList) keySets.get(i);
				keySetArray[i] = new int[set.size()];
				set.copyInto(keySetArray[i]);
			}
			return new SwitchExpr(keyExpr, keySetArray, exprArray, elseExpr);
		} else {
			// string switch we implement as two consequent switches
			// first is switch over hashcodes which returns branch number
			Var strVar = new Var("#string", BuiltinType.STRING);
			strVar.hits = 1;
			strVar.isConstant = true;
			// create first switch
			Expr strExpr = new VarExpr(-1, strVar);
			ArrayList hashes = new ArrayList();
			ArrayList checkExprs = new ArrayList();
			for (int branchIndex = 0; branchIndex < keySets.size(); branchIndex++) {
				ArrayList set = (ArrayList) keySets.get(branchIndex);
				for (int keyIdx=0; keyIdx < set.size(); keyIdx++) {
					String key = (String) set.get(keyIdx);
					Int32 hash = Int32.toInt32(key.hashCode());
					int hashIdx = hashes.indexOf(hash);
					if (hashIdx < 0) {
						hashIdx = hashes.size();
						hashes.add(hash);
						checkExprs.add(new ConstExpr(-1, BuiltinType.INT, Int32.M_ONE));
					}
					Expr checkExpr = (Expr) checkExprs.get(hashIdx);
					checkExpr = new IfElseExpr(
							new ComparisonExpr(strExpr, Token.EQEQ, new ConstExpr(-1, BuiltinType.STRING, key)),
							new ConstExpr(-1, BuiltinType.INT, Int32.toInt32(branchIndex)),
							checkExpr);
					strVar.hits++;
					checkExprs.set(hashIdx, checkExpr);
				}
			}
			int[][] keySetArray = new int[hashes.size()][];
			for (int i=0; i<keySetArray.length; i++) {
				keySetArray[i] = new int[] { ((Int32)hashes.get(i)).value };
			}
			Expr[] exprArray = new Expr[hashes.size()];
			checkExprs.copyInto(exprArray);
			Expr getHash = new CallExpr(-1, unit.getFunction("String.hash"), new Expr[] { strExpr });
			Expr innerSwitch = new SwitchExpr(getHash, keySetArray, exprArray, new ConstExpr(-1, BuiltinType.INT, Int32.M_ONE));
			// create second switch
			keySetArray = new int[exprs.size()][];
			for (int i=0; i<keySetArray.length; i++) {
				keySetArray[i] = new int[] { i };
			}
			exprArray = new Expr[exprs.size()];
			exprs.copyInto(exprArray);
			Expr outerSwitch = new SwitchExpr(innerSwitch, keySetArray, exprArray, elseExpr);
			return new SequentialExpr(new Var[] { strVar }, new Expr[] { keyExpr }, outerSwitch);
		}
	}

	/** Parses assignments and expression statements. */
	private Statement parseExprStatement(Scope scope) throws IOException, ParseException {
		Expr expr = parseExpr(scope);
		int assignOp = t.nextToken();
		if (assignOp == '=') {
			Expr rhs = parseExpr(scope);
			switch (expr.kind) {
				case Expr.EXPR_VAR: {
					VarExpr lhs = (VarExpr) expr;
					if (lhs.var.isConstant)
						throw new ParseException("Cannot assign to constant " + lhs.var.name);
					rhs = cast(rhs, lhs.var.type);
					return new AssignStatement(lhs.var, rhs);
				}
				case Expr.EXPR_ARRAY_ELEMENT: {
					ArrayElementExpr lhs = (ArrayElementExpr) expr;
					rhs = cast(rhs, lhs.returnType());
					return new ArraySetStatement(lhs.arrayExpr, lhs.indexExpr, rhs);
				}
				case Expr.EXPR_PROPERTY: {
					// setter(objectExpr, rhs)
					PropertyLvalue lhs = (PropertyLvalue) expr;
					rhs = cast(rhs, lhs.setter.type.argtypes[1]);
					return new ExprStatement(new CallExpr(
							lhs.lineNumber(), lhs.setter,
							new Expr[] { lhs.objectExpr, rhs }));
				}
				case Expr.EXPR_ARRAYLIKE: {
					// setter(objectExpr, indexExprs..., rhs )
					ArrayLikePropertyLvalue lhs = (ArrayLikePropertyLvalue) expr;
					int idxsize = lhs.indexExprs.length;
					Expr[] setterArgs = new Expr[idxsize + 2];
					setterArgs[0] = lhs.objectExpr;
					setterArgs[idxsize+1] = cast(rhs, lhs.setter.type.argtypes[idxsize+1]);
					for (int i=0; i<idxsize; i++) {
						setterArgs[i+1] = cast(lhs.indexExprs[i], lhs.setter.type.argtypes[i+1]);
					}
					return new ExprStatement(new CallExpr(lhs.lineNumber(), lhs.setter, setterArgs));
				}
				default:
					throw new ParseException("Cannot assign to given expression");
			}
		} else if (Token.isAssignment(assignOp)) {
			Expr rhs = parseExpr(scope);
			switch (expr.kind) {
				case Expr.EXPR_VAR: {
					VarExpr lhs = (VarExpr) expr;
					if (lhs.var.isConstant)
						throw new ParseException("Cannot assign to constant " + lhs.var.name);
					Expr binary = makeBinaryExpr(lhs, Token.getBinaryOperator(assignOp), rhs);
					if (binary.kind == Expr.EXPR_BINARY) {
						if (assignOp == Token.LTLTEQ || assignOp == Token.GTGTEQ || assignOp == Token.GTGTGTEQ) {
							rhs = cast(rhs, BuiltinType.INT);
						} else {
							rhs = cast(rhs, lhs.var.type);
						}
						return new CompoundAssignStatement(lhs.var, assignOp, rhs);
					} else {
						return new AssignStatement(lhs.var, cast(binary, lhs.var.type));
					}
				}
				case Expr.EXPR_ARRAY_ELEMENT: {
					// var #lvalue = arrayExpr
					// var #0 = indexExpr
					// #array[#index] = #array[#index] * rhs
					ArrayElementExpr lhs = (ArrayElementExpr) expr;
					Var arrVar = new Var("#array", lhs.arrayExpr.returnType());
					arrVar.hits = 2;
					arrVar.isConstant = true;
					Var idxVar = new Var("#index", lhs.indexExpr.returnType());
					idxVar.hits = 2;
					idxVar.isConstant = true;
					Expr arrExpr = new VarExpr(-1, arrVar);
					Expr idxExpr = new VarExpr(-1, idxVar);
					rhs = makeBinaryExpr(
							new ArrayElementExpr(arrExpr, idxExpr, lhs.returnType()),
							Token.getBinaryOperator(assignOp), rhs);

					BlockStatement block = new BlockStatement(scope);
					block.addVar(arrVar);
					block.addVar(idxVar);
					block.statements.add(new AssignStatement(arrVar, lhs.arrayExpr));
					block.statements.add(new AssignStatement(idxVar, lhs.indexExpr));
					block.statements.add(new ArraySetStatement(arrExpr, idxExpr, cast(rhs, lhs.returnType())));
					return block;
				}
				case Expr.EXPR_PROPERTY: {
					// var #object = objectExpr
					// setter (#object, getter(#object) * rhs)
					PropertyLvalue lhs = (PropertyLvalue) expr;
					Var objVar = new Var("#object", lhs.objectExpr.returnType());
					objVar.isConstant = true;
					objVar.hits = 2;
					Expr objExpr = new VarExpr(-1, objVar);
					Expr getterCall = new CallExpr(lhs.lineNumber(), lhs.getter, new Expr[] { objExpr });
					rhs = makeBinaryExpr(getterCall, Token.getBinaryOperator(assignOp), rhs);
					Expr setterCall = new CallExpr(
							lhs.lineNumber(), lhs.setter,
							new Expr[] { objExpr, cast(rhs, lhs.setter.type.argtypes[1]) });
					return new ExprStatement(new SequentialExpr(
							new Var[] { objVar }, new Expr[] { lhs.objectExpr }, setterCall));
				}
				case Expr.EXPR_ARRAYLIKE: {
					// var #object = objectExpr
					// var #index0 = indexExprs[0]
					// ...
					// var #indexN = indexExprs[N]
					// setter (#object, #0...#N , getter(#object, #0...#N) * rhs)
					ArrayLikePropertyLvalue lhs = (ArrayLikePropertyLvalue) expr;
					int idxsize = lhs.indexExprs.length;
					Var[] seqVars = new Var[idxsize+1];
					Expr[] seqExprs = new Expr[idxsize+1];
					seqVars[0] = new Var("#object", lhs.objectExpr.returnType());
					seqVars[0].hits  = 2;
					seqExprs[0] = lhs.objectExpr;
					for (int i=0; i<idxsize; i++) {
						seqVars[i+1] = new Var("#index" + i, lhs.indexExprs[i].returnType());
						seqVars[i+1].isConstant = true;
						seqVars[i+1].hits = 2;
						seqExprs[i+1] = lhs.indexExprs[i];
					}
					Expr[] getterArgs = new Expr[idxsize + 1];
					for (int i=0; i<idxsize+1; i++) {
						getterArgs[i] = cast(new VarExpr(-1, seqVars[i]), lhs.getter.type.argtypes[i]);
					}
					CallExpr getterCall = new CallExpr(lhs.lineNumber(), lhs.getter, getterArgs);
					rhs = makeBinaryExpr(getterCall, Token.getBinaryOperator(assignOp), rhs);
					Expr[] setterArgs = new Expr[idxsize + 2];
					for (int i=0; i<idxsize+1; i++) {
						setterArgs[i] = cast(new VarExpr(-1, seqVars[i]), lhs.setter.type.argtypes[i]);
					}
					setterArgs[idxsize+1] = cast(rhs, lhs.setter.type.argtypes[idxsize+1]);
					CallExpr setterCall = new CallExpr(lhs.lineNumber(), lhs.setter, setterArgs);
					return new ExprStatement(new SequentialExpr(seqVars, seqExprs, setterCall));
				}
				default:
					throw new ParseException("Cannot assign to given expression");
			}
		} else {
			t.pushBack();
			return new ExprStatement(expr);
		}
	}

	/**
	 * Parses expression part after '(' (function call).
	 *
	 * <p>
	 * Does special type checks and type casts for some functions.
	 * <dl>
	 * <dt>{@code Function.curry}</dt>
	 * <dd>checks if argument is acceptable, computes returned type</dd>
	 *
	 * <dt>{@code acopy}</dt>
	 * <dd>checks if array elements are assignment compatible</dd>
	 *
	 * <dt>{@code StrBuf.append(Char)}</dt>
	 * <dd>replaces by StrBuf.addch(Char)</dd>
	 *
	 * <dt>{@code StrBuf.insert(at, Char)}</dt>
	 * <dd>replaces by StrBuf.insch(at, Char)</dd>
	 *
	 * <dt>{@code print(obj)}, {@code println(obj)}, {@code OStream.print(obj)}, {@code OStream.println(obj)}</dt>
	 * <dd>replaces argument by {@code obj.tostr()}</dd>
	 *
	 * </dl>
	 */
	private Expr parseFunctionCall(Scope scope, Expr fload, Expr firstarg) throws IOException, ParseException {
		if (fload.returnType().kind != Type.TYPE_FUNCTION)
			throw new ParseException("Applying () to non-function expression");
		FunctionType ftype = (FunctionType)fload.returnType();
		// parse arguments
		ArrayList vargs = new ArrayList();
		if (firstarg != null) vargs.add(firstarg);
		boolean first = true;
		while (t.nextToken() != ')') {
			t.pushBack();
			if (first) first = false;
			else expect(',');
			vargs.add(parseExpr(scope));
		}
		// add default argument values
		if (vargs.size() < ftype.argtypes.length && fload.kind == Expr.EXPR_CONST) {
			Function f = (Function) ((ConstExpr)fload).value;
			for (int i=vargs.size(); i < ftype.argtypes.length; i++) {
				Var v = f.args[i];
				if (v.defaultValue != null) vargs.add(new ConstExpr(-1, v.type, v.defaultValue));
			}
		}
		if (ftype.argtypes.length != vargs.size()) {
			if (fload.kind == Expr.EXPR_CONST) {
				Function f = (Function) ((ConstExpr)fload).value;
				throw new ParseException("Wrong number of arguments in call to "+f.signature+"()");
			} else {
				throw new ParseException("Wrong number of arguments in function call");
			}
		}
		// cast arguments to needed types
		Expr[] args = new Expr[vargs.size()];
		for (int i=0; i<args.length; i++) {
			args[i] = cast((Expr)vargs.get(i), ftype.argtypes[i]);
		}
		// special processing for some functions
		if (fload.kind == Expr.EXPR_CONST) {
			Function f = (Function) ((ConstExpr)fload).value;
			if (f.signature.equals("acopy") && args[2].returnType().kind == Type.TYPE_ARRAY) {
				ArrayType toarray = (ArrayType)args[2].returnType();
				if (args[0].returnType().kind == Type.TYPE_ARRAY) {
					ArrayType fromarray = (ArrayType)args[0].returnType();
					if (toarray.elementType.safeToCastTo(fromarray.elementType)
						&& !toarray.elementType.equals(fromarray.elementType)) {
						warn(CompilerEnv.W_TYPECAST, "Unsafe type cast when copying from "+fromarray+" to "+toarray);
					} else if (!toarray.elementType.safeToCastTo(fromarray.elementType)) {
						warn(CompilerEnv.W_ERROR, "Cast to the incompatible type when copying from "+fromarray+" to "+toarray);
					}
				} else if (toarray.elementType != BuiltinType.ANY) {
					warn(CompilerEnv.W_TYPECAST, "Unsafe type cast when copying from Array to "+toarray);
				}
			} else if (f.signature.equals("StrBuf.append") && args[1].returnType() == BuiltinType.CHAR) {
				f.hits--;
				Function addch = unit.getFunction("StrBuf.addch");
				addch.hits++;
				return new CallExpr(fload.lineNumber(), addch, args);
			} else if (f.signature.equals("StrBuf.insert") && args[2].returnType() == BuiltinType.CHAR) {
				f.hits--;
				Function insch = unit.getFunction("StrBuf.insch");
				insch.hits++;
				return new CallExpr(fload.lineNumber(), insch, args);
			} else if ((f.signature.equals("print") || f.signature.equals("println")) &&
					args[0].returnType() != BuiltinType.STRING) {
				Function tostr = findMethod(args[0].returnType(), "tostr");
				tostr.hits++;
				args[0] = new CallExpr(fload.lineNumber(), tostr, new Expr[] { args[0] });
			} else if ((f.signature.equals("OStream.print") || f.signature.equals("OStream.println")) &&
					args[1].returnType() != BuiltinType.STRING) {
				Function tostr = findMethod(args[0].returnType(), "tostr");
				tostr.hits++;
				args[1] = new CallExpr(fload.lineNumber(), tostr, new Expr[] { args[1] });
			}
		}
		return new CallExpr(fload, args);
	}

	private Expr parseDot(Scope scope, Expr expr) throws IOException, ParseException {
		int lnum = t.lineNumber();

		// parse value.cast(Type)
		if (t.nextToken() == Token.CAST) {
			Type fromType = expr.returnType();
			expect('(');
			Type toType = parseType(scope);
			expect(')');
			if (toType.equals(fromType)) {
				warn(CompilerEnv.W_CAST, "Unnecessary cast to the same type");
				return expr;
			}
			if (fromType.safeToCastTo(toType)) {
				warn(CompilerEnv.W_CAST, "Unnecessary cast from " + fromType + " to " + toType);
				return expr;
			}
			return cast(expr, toType, true);
		}

		if (t.ttype != Token.WORD)
			throw new ParseException("Identifier expected after '.'");
		String member = t.svalue;
		Type type = expr.returnType();

		// object field or emulated field
		switch (type.kind) {
			case Type.TYPE_ARRAY: {
				if (member.equals("len")) {
					return new ArrayLenExpr(expr);
				}
				break;
			}
			case Type.TYPE_INTRANGE:
			case Type.TYPE_LONGRANGE: {
				Type itemType = (type.kind == Type.TYPE_INTRANGE) ? BuiltinType.INT : BuiltinType.LONG;
				if (member.equals("from")) {
					return new ArrayElementExpr(expr, new ConstExpr(lnum, BuiltinType.INT, Int32.ZERO), type);
				} else if (member.equals("to")) {
					return new ArrayElementExpr(expr, new ConstExpr(lnum, BuiltinType.INT, Int32.ONE), type);
				}
				break;
			}
			case Type.TYPE_FUNCTION: {
				if (member.equals("apply") || (env.hasOption(CompilerEnv.F_COMPAT21) && member.equals("curry"))) {
					if (member.equals("curry")) {
						warn(CompilerEnv.W_DEPRECATED, "'curry' keyword is deprecated. Use Function.apply for partial argument application.");
					}
					expect('(');
					ArrayList args = new ArrayList();
					boolean first = true;
					while (t.nextToken() != ')') {
						if (first) first = false;
						else expect(',');
						args.add(parseExpr(scope));
					}
					FunctionType ftype = (FunctionType) type;
					if (args.size() > ftype.argtypes.length)
						throw new ParseException("Number of arguments in apply() exceeds arity of function");
					Expr[] argExprs = new Expr[args.size()];
					for (int i=0; i < argExprs.length; i++) {
						argExprs[i] = cast((Expr)args.get(i), ftype.argtypes[i]);
					}
					return new ApplyExpr(expr, argExprs);
				}
				break;
			}
			case Type.TYPE_OBJECT: {
				if (type instanceof ObjectType) { // FIXME: get rid of instanceof
					// searching named field
					Var[] fields = ((ObjectType)type).fields;
					int index = -1;
					if (fields != null) {
						for (int i=0; i<fields.length; i++) {
							if (fields[i].name.equals(member)) {
								index = i;
								break;
							}
						}
					}
					if (index >= 0) {
						ConstExpr indexexpr = new ConstExpr(lnum, BuiltinType.INT, Int32.toInt32(index));
						ArrayElementExpr ldexpr = new ArrayElementExpr(expr, indexexpr, fields[index].type);
						return ldexpr;
					}
				}
			}
		}

		// object method
		Function method = findMethod(type, member);
		if (method != null) {
			method.hits++;
			if (t.nextToken() == '(') {
				return parseFunctionCall(scope, new ConstExpr(lnum, method.type, method), expr);
			} else {
				t.pushBack();
				return new ApplyExpr(new ConstExpr(lnum, method.type, method), new Expr[] { expr });
			}
		}

		// object property
		Function getter = findGetter(type, member);
		Function setter = findSetter(type, member);
		int operator = t.nextToken();
		t.pushBack();
		if (operator != '=') {
			if (getter == null)
				throw new ParseException("Type " + type + " has no member named " + member);
			if (getter.type.argtypes.length != 1)
				throw new ParseException("Function " + getter.signature + " cannot be used as property getter.");
			getter.hits++;
		}
		if (Token.isAssignment(operator)) {
			if (setter == null)
				throw new ParseException("Type " + type + " has no member named " + member);
			if (setter.type.argtypes.length != 2)
				throw new ParseException("Function " + setter.signature + " cannot be used as property setter.");
			setter.hits++;
			return new PropertyLvalue(expr, member, getter, setter);
		} else {
			return new CallExpr(expr.lineNumber(), getter, new Expr[] { expr });
		}
	}

	/**
	 * Parses expression part after '['.
	 */
	private Expr parseBrackets(Scope scope, Expr arexpr) throws IOException, ParseException {
		// parse first expression and check if ':' follows
		int lnum = t.lineNumber();
		Type artype = arexpr.returnType();
		ArrayList indices = new ArrayList();
		int tok = t.nextToken();
		t.pushBack();
		if (tok == ':') {
			indices.add(new ConstExpr(0, BuiltinType.INT, Int32.ZERO));
		} else {
			indices.add(parseExpr(scope));
		}

		// parse range expression
		if (t.nextToken() == ':') {
			// parse second range argument
			tok = t.nextToken();
			t.pushBack();
			if (tok == ']') { // implicit end
				// FIXME: arexpr used twice here
				Function lenMethod = findMethod(artype, "len");
				if (lenMethod == null || lenMethod.type.argtypes.length != 1 || lenMethod.type.returnType != BuiltinType.INT)
					throw new ParseException("Operator [:] cannot be applied to " + artype + ", no suitable len()");
				lenMethod.hits++;
				indices.add(new CallExpr(t.lineNumber(), lenMethod, new Expr[] { arexpr }));
			} else {
				indices.add(cast(parseExpr(scope), BuiltinType.INT));
			}
			expect(']');
			// return range expression
			Function rangeMethod = findMethod(artype, "range");
			if (rangeMethod == null || rangeMethod.type.argtypes.length != 3 ||
			   rangeMethod.type.argtypes[1] != BuiltinType.INT || rangeMethod.type.argtypes[2] != BuiltinType.INT)
				throw new ParseException("Operator [:] cannot be applied to " + artype);
			rangeMethod.hits++;
			return new CallExpr(lnum, rangeMethod, new Expr[] {arexpr, (Expr)indices.get(0), (Expr)indices.get(1)});
		}
		t.pushBack();

		// parse remaining indices
		while (t.nextToken() != ']') {
			t.pushBack();
			expect(',');
			indices.add(parseExpr(scope));
		}

		// if array then convert to a chain of array gets
		if (artype.kind == Type.TYPE_ARRAY) {
			Expr getExpr = arexpr;
			while (indices.size() > 0) {
				artype = getExpr.returnType();
				if (artype.kind != Type.TYPE_ARRAY) {
					warn(CompilerEnv.W_ERROR, "Number of arguments in [] exceeds dimension of array");
					return getExpr;
				}
				Expr indexExpr = cast((Expr)indices.first(), BuiltinType.INT);
				indices.remove(0);
				getExpr = new ArrayElementExpr(getExpr, indexExpr, ((ArrayType)getExpr.returnType()).elementType);
			}
			return getExpr;
		}

		// finally use get() / set()
		Function getter = findMethod(artype, "get");
		Function setter = findMethod(artype, "set");
		int operator = t.nextToken();
		t.pushBack();
		if (Token.isAssignment(operator)) {
			if (setter == null || setter.type.argtypes.length != 2+indices.size())
					throw new ParseException("Operator []= cannot be applied to " + artype);
			setter.hits++;
			if (operator != '=') {
				if (getter == null || getter.type.argtypes.length != 1+indices.size())
					throw new ParseException("Operator [] cannot be applied to " + artype);
				for (int i=1; i<getter.type.argtypes.length; i++) {
					if (!getter.type.argtypes[i].safeToCastTo(setter.type.argtypes[i]))
						throw new ParseException("argument types of get() and set() are incompatible for " + artype);
				}
				getter.hits++;
			}
			Expr[] indexExprs = new Expr[indices.size()];
			for (int i=0; i<indexExprs.length; i++) {
				Expr index = (Expr) indices.get(i);
				indexExprs[i] = cast(index, (getter != null) ? getter.type.argtypes[i+1] : setter.type.argtypes[i+1]);
			}
			return new ArrayLikePropertyLvalue(arexpr, indexExprs, getter, setter);
		} else {
			if (getter == null || getter.type.argtypes.length != 1+indices.size())
				throw new ParseException("Operator [] cannot be applied to " + artype);
			getter.hits++;
			Expr[] args = new Expr[1 + indices.size()];
			args[0] = arexpr;
			for (int i=1; i < args.length; i++) {
				Expr index = (Expr) indices.get(i-1);
				args[i] = cast(index, getter.type.argtypes[i]);
			}
			return new CallExpr(arexpr.lineNumber(), getter, args);
		}
	}

	private Expr parsePostfix(Scope scope, Expr expr) throws IOException, ParseException {
		while (true) {
			switch (t.nextToken()) {
				case '(':
					expr = parseFunctionCall(scope, expr, null);
					break;
				case '[':
					expr = parseBrackets(scope, expr);
					break;
				case '.':
					expr = parseDot(scope, expr);
					break;
				default:
					t.pushBack();
					return expr;
			}
		}
	}

	/**
	 * Binary operators arranged by priority. In groups of four.
	 */
	private static int[] priorops = {
		'^', 0, 0, 0,
		Token.BARBAR, '|', 0, 0,
		Token.AMPAMP, '&', 0, 0,
		Token.LTEQ, Token.GTEQ, '<', '>',
		Token.EQEQ, Token.NOTEQ, 0, 0,
		Token.IN, 0, 0, 0,
		Token.RANGE, 0, 0, 0,
		Token.LTLT, Token.GTGT, Token.GTGTGT, 0,
		'+', '-', 0, 0,
		'*', '/', '%', 0
	};

	private int getPriority(Int32 operator) {
		int op = operator.value;
		for (int i=0; i<priorops.length; i++) {
			if (priorops[i] == op) return i/4;
		}
		return -1;
	}

	private Expr parseExpr(Scope scope) throws IOException, ParseException {
		ArrayList exprs = new ArrayList();
		ArrayList operators = new ArrayList();
		while (true) {
			exprs.add(parsePostfix(scope, parseExprAtom(scope)));
			int opchar = t.nextToken();
			if (Token.isOperator(opchar)) {
				operators.add(Int32.toInt32(opchar));
			} else {
				t.pushBack();
				break;
			}
		}
		while (!operators.isEmpty()) {
			int index = 0;
			int priority = 0;
			for (int i = 0; i < operators.size(); i++) {
				int pr = getPriority((Int32)operators.get(i));
				if (pr > priority) {
					priority = pr;
					index = i;
				}
			}
			int op = ((Int32)operators.get(index)).value;
			Expr left = (Expr)exprs.get(index);
			Expr right = (Expr)exprs.get(index+1);
			Expr newexpr = makeBinaryExpr(left, op, right);
			exprs.set(index, newexpr);
			exprs.remove(index+1);
			operators.remove(index);
		}
		return (Expr)exprs.first();
	}

	private Expr parseExprAtom(Scope scope) throws IOException, ParseException {
		int ttype = t.nextToken();
		int line = t.lineNumber();
		switch (ttype) {
			case Token.CHAR:
				return new ConstExpr(line, BuiltinType.CHAR, Int32.toInt32(t.ivalue));
			case Token.INT:
				return new ConstExpr(line, BuiltinType.INT, Int32.toInt32(t.ivalue));
			case Token.LONG:
				return new ConstExpr(line, BuiltinType.LONG, new Int64(t.lvalue));
			case Token.FLOAT:
				return new ConstExpr(line, BuiltinType.FLOAT, new Float32(t.fvalue));
			case Token.DOUBLE:
				return new ConstExpr(line, BuiltinType.DOUBLE, new Float64(t.dvalue));
			case Token.QUOTED:
				return new ConstExpr(line, BuiltinType.STRING, t.svalue);
			case Token.FALSE:
				return new ConstExpr(line, BuiltinType.BOOL, Boolean.FALSE);
			case Token.TRUE:
				return new ConstExpr(line, BuiltinType.BOOL, Boolean.TRUE);
			case Token.NULL:
				return new ConstExpr(line, BuiltinType.NULL, Null.NULL);
			case Token.THROW:
			case Token.WORD: {
				Var var = scope.getVar(t.svalue);
				if (var == null)
					throw new ParseException("Variable " + t.svalue + " is not defined");
				if (var.isConstant && var.defaultValue != null) {
					Object cnst = var.defaultValue;
					if (cnst instanceof Function) {
						((Function)cnst).hits++;
					}
					return new ConstExpr(line, var.type, var.defaultValue);
				} else {
					var.hits++;
					return new VarExpr(line, var);
				}
			}
			case '(': {
				Expr expr = parseExpr(scope);
				expect(')');
				return expr;
			}
			case '[': {
				// reading array elements
				ArrayList exprs = new ArrayList();
				boolean first = true;
				while (t.nextToken() != ']') {
					t.pushBack();
					if (first) first = false;
					else expect(',');
					if (t.nextToken() == ']') break;
					else t.pushBack();
					exprs.add(parseExpr(scope));
				}
				// calculating common type
				Type eltype = BuiltinType.NULL;
				for (int i=0; i<exprs.size(); i++) {
					Expr e = (Expr)exprs.get(i);
					eltype = binaryCastType(eltype, e.returnType());
				}
				if (eltype == BuiltinType.NULL)
					eltype = BuiltinType.ANY;
				else if (eltype == BuiltinType.NONE)
					throw new ParseException("Cannot create array of <none>");
				// building expression
				Expr[] init = new Expr[exprs.size()];
				for (int i=0; i<init.length; i++) {
					init[i] = cast( (Expr)exprs.get(i), eltype);
				}
				return new NewArrayInitExpr(line, new ArrayType(eltype), init);
			}
			case '+': {
				Expr sub = parsePostfix(scope, parseExprAtom(scope));
				Type type = sub.returnType();
				if (type.isNumeric()) return sub;
				throw new ParseException("Operator "+Token.toString(ttype)+" cannot be applied to "+type);
			}
			case '-': {
				Expr sub = parsePostfix(scope, parseExprAtom(scope));
				Type type = sub.returnType();
				if (type.isNumeric())
					return new UnaryExpr(ttype, sub);
				Function method = findMethod(type, "minus");
				if (method != null && method.type.argtypes.length == 1) {
					method.hits++;
					return new CallExpr(sub.lineNumber(), method, new Expr[] { sub });
				}
				throw new ParseException("Operator "+(char)ttype+" cannot be applied to "+type);
			}
			case '!': {
				Expr sub = parsePostfix(scope, parseExprAtom(scope));
				Type type = sub.returnType();
				if (type == BuiltinType.BOOL) {
					return new UnaryExpr(ttype, sub);
				}
				Function method = findMethod(type, "not");
				if (method != null && method.type.argtypes.length == 1) {
					method.hits++;
					return new CallExpr(sub.lineNumber(), method, new Expr[] { sub });
				}
				throw new ParseException("Operator "+Token.toString(ttype)+" cannot be applied to "+type);
			}
			case '~': {
				Expr sub = parsePostfix(scope, parseExprAtom(scope));
				Type type = sub.returnType();
				if (type == BuiltinType.BYTE || type == BuiltinType.SHORT || type == BuiltinType.CHAR) {
					sub = cast(sub, BuiltinType.INT);
					type = BuiltinType.INT;
				}
				if (type == BuiltinType.INT || type == BuiltinType.LONG) {
					return new UnaryExpr(ttype, sub);
				}
				throw new ParseException("Operator "+Token.toString(ttype)+" cannot be applied to "+type);
			}
			case '{':
				throw new ParseException("Blocks cannot be used as expressions anymore");
			case Token.NEW: {
				Type type = parseType(scope);
				// parse array constructor
				if (type.kind == Type.TYPE_ARRAY) {
					switch (t.nextToken()) {
						case '(': {
							ArrayList lengths = new ArrayList();
							boolean first = true;
							do {
								if (first) {
									first = false;
								} else {
									t.pushBack();
									expect(',');
								}
								lengths.add(cast(parseExpr(scope), BuiltinType.INT));
							} while (t.nextToken() != ')');
							int arrayDim = 0;
							Type elType = type;
							while (elType.kind == Type.TYPE_ARRAY) {
								arrayDim++;
								elType = ((ArrayType)elType).elementType;
							}
							if (arrayDim < lengths.size())
								warn(CompilerEnv.W_ERROR, "Number of sizes exceeds array dimension");
							Expr[] lengthExprs = new Expr[lengths.size()];
							lengths.copyInto(lengthExprs);
							return new NewArrayExpr(line, type, lengthExprs);
						}
						case '{': {
							Type elementType = ((ArrayType)type).elementType;
							ArrayList initializers = new ArrayList();
							boolean first = true;
							while (t.nextToken() != '}') {
								t.pushBack();
								if (first) first = false;
								else expect(',');
								initializers.add(cast(parseExpr(scope), elementType));
							}
							Expr[] initExprs = new Expr[initializers.size()];
							initializers.copyInto(initExprs);
							return new NewArrayInitExpr(line, type, initExprs);
						}
						default:
							throw new ParseException("Expected '(' or '{' in array constructor");
					}
				}

				// use .new() method
				Function newMethod = findMethod(type, "new");
				if (newMethod != null) {
					expect('(');
					return parseFunctionCall(scope, new ConstExpr(line, newMethod.type, newMethod), null);
				}

				// use default constructor
				if (type != BuiltinType.ANY && findMethod(type.superType(), "new") != null) {
					warn(CompilerEnv.W_ERROR, "Type " + type + " has no constructor but parent type " + type.superType() + " has");
				}
				if (!(type instanceof ObjectType) || ((ObjectType)type).fields == null) {
					throw new ParseException("Cannot use default constructor, structure of " + type + " is not defined");
				}
				Var[] fields = ((ObjectType)type).fields;
				Expr[] initializers = new Expr[fields.length];
				if (t.nextToken()== '(') {
					for (int i=0; i<initializers.length; i++) {
						if (i != 0) expect(',');
						initializers[i] = cast(parseExpr(scope), fields[i].type);
					}
					expect(')');
				} else {
					t.pushBack();
					for (int i=0; i<initializers.length; i++) {
						if (fields[i].defaultValue != null) {
							initializers[i] = new ConstExpr(line, fields[i].type, fields[i].defaultValue);
						}
					}
					expect('{');
					boolean first = true;
					while (t.nextToken() != '}') {
						t.pushBack();
						if (first) first = false;
						else expect(',');
						if (t.nextToken() != Token.WORD)
							throw new ParseException("Field name expected, got " + t);
						String fieldName = t.svalue;
						int i=fields.length-1;
						while (i >= 0) {
							if (fields[i].name.equals(fieldName)) break;
							i--;
						}
						if (i < 0) throw new ParseException("Type " + type + " has no field " + fieldName);
						expect('=');
						initializers[i] = cast(parseExpr(scope), fields[i].type);
					}
				}
				return new NewArrayInitExpr(line, type, initializers);
			}
			case Token.IF: {
				expect('(');
				Expr condition = cast(parseExpr(scope), BuiltinType.BOOL);
				expect(')');
				Expr trueExpr = parseExpr(scope);
				expect(Token.ELSE);
				Expr falseExpr = parseExpr(scope);
				Type binaryType = binaryCastType(trueExpr.returnType(), falseExpr.returnType());
				trueExpr = cast(trueExpr, binaryType);
				falseExpr = cast(falseExpr, binaryType);
				return new IfElseExpr(condition, trueExpr, falseExpr);
			}
			case Token.SWITCH: {
				return parseSwitchExpr(scope);
			}
			case Token.CAST: {
				if (env.hasOption(CompilerEnv.F_COMPAT21)) {
					warn(CompilerEnv.W_DEPRECATED, "In Ether 2.2 you should use syntax (expr).cast(type)");
				} else {
					warn(CompilerEnv.W_ERROR, "Use (expr).cast(type) for type cast");
				}
				expect('(');
				Type toType = parseType(scope);
				expect(')');
				return cast(parseExprAtom(scope), toType, true);
			}
			case Token.SUPER: {
				Var thisVar = scope.getVar("this");
				Expr superExpr = new VarExpr(line, thisVar);
				if (thisVar == null)
					warn(CompilerEnv.W_ERROR, "'this' outside of method");
				Type superType = thisVar.type.superType();
				if (superType == null) {
					warn(CompilerEnv.W_ERROR, "Type " + thisVar.type + " does not have a parent type");
				} else {
					superExpr = new CastExpr(superExpr, superType);
				}
				return superExpr;
			}
			case Token.TRY: {
				Expr tryExpr = parseExpr(scope);
				expect(Token.CATCH);
				Expr catchExpr = parseExpr(scope);
				return new TryCatchExpr(tryExpr, catchExpr);
			}
			case Token.DEF: {
				// anonymous function
				Function lambda = new Function(unit, "#lambda");
				// parse argument list
				expect('(');
				ArrayList args = new ArrayList();
				ArrayList names = new ArrayList();
				boolean first = true;
				while (t.nextToken() != ')') {
					t.pushBack();
					if (first) first = false;
					else expect(',');
					if (t.nextToken() != Token.WORD)
						throw new ParseException("Argument name expected, got " + t);
					String argname = t.svalue;
					if (names.contains(argname))
						warn(CompilerEnv.W_ERROR, "Variable " + argname + " is already defined");
					names.add(argname);
					expect(':');
					Type argtype = parseType(lambda);
					Var arg = new Var(argname, argtype);
					args.add(arg);
					if (t.nextToken() == '=') {
						Expr defValue = (Expr) cast(parseExpr(lambda), argtype).accept(constOptimizer, lambda);
						if (defValue.kind != Expr.EXPR_CONST)
							throw new ParseException("Constant expression expected");
						arg.defaultValue = ((ConstExpr)defValue).value;
					} else {
						t.pushBack();
					}
				}
				lambda.args = new Var[args.size()];
				args.copyInto(lambda.args);
				Type returnType = null;
				if (t.nextToken() == ':') {
					returnType = parseType(lambda);
				} else {
					t.pushBack();
				}
				// parse function body
				expect('=');
				ClosureScope closure = new ClosureScope(lambda, scope);
				Expr body = parseExpr(closure);
				if (returnType == null) {
					returnType = body.returnType();
				} else {
					body = cast(body, returnType);
				}
				lambda.body = new ReturnStatement(body);
				// add closure variables
				int argsCount = lambda.args.length;
				int addedVarsCount = closure.enclosedVars.size();
				if (addedVarsCount > 0) {
					Var[] newArgs = new Var[argsCount + addedVarsCount];
					System.arraycopy(lambda.args, 0, newArgs, 0, argsCount);
					Object[] newVarNames = closure.enclosedVars.keys();
					for (int i=0; i<addedVarsCount; i++) {
						newArgs[argsCount + i] = (Var) closure.enclosedVars.get(newVarNames[i]);
					}
					lambda.args = newArgs;
				}
				// fill remaining function fields
				lambda.hits = 1;
				lambda.source = files.last().toString();
				Type[] argTypes = new Type[lambda.args.length];
				for (int i=0; i<argTypes.length; i++) {
					argTypes[i] = lambda.args[i].type;
				}
				lambda.type = new FunctionType(returnType, argTypes);
				unit.implementedFunctions.add(lambda);
				// return value
				Expr lambdaLoad = new ConstExpr(line, lambda.type, lambda);
				if (addedVarsCount > 0) {
					Expr[] varLoaders = new Expr[addedVarsCount];
					for (int i=0; i < addedVarsCount; i++) {
						varLoaders[i] = new VarExpr(line, scope.getVar(lambda.args[argsCount + i].name));
					}
					lambdaLoad = new ApplyExpr(lambdaLoad, varLoaders);
				}
				return lambdaLoad;
			}
			default:
				throw new ParseException(t.toString() + " unexpected here");
		}
	}

	/** Parses type expression. */
	private Type parseType(Scope scope) throws IOException, ParseException {
		switch (t.nextToken()) {
			case Token.WORD: { // scalar type
				Type type = scope.getType(t.svalue);
				return type;
			}
			case '(': { // function type
				ArrayList arglist = new ArrayList();
				boolean first = true;
				while (t.nextToken() != ')') {
					t.pushBack();
					if (first) first = false;
					else expect(',');
					arglist.add(parseType(scope));
				}
				Type rettype;
				if (t.nextToken() == ':') {
					rettype = parseType(scope);
				} else {
					t.pushBack();
					rettype = BuiltinType.NONE;
				}
				Type[] argtypes = new Type[arglist.size()];
				arglist.copyInto(argtypes);
				return new FunctionType(rettype, argtypes);
			}
			case '[': { // array type
				Type elementType = parseType(scope);
				expect(']');
				return new ArrayType(elementType);
			}
			default:
				throw new ParseException(t.toString() + " unexpected here");
		}
	}

	private Expr makeBinaryExpr(Expr left, int op, Expr right) throws ParseException {
		Type ltype = left.returnType();
		Type rtype = right.returnType();
		Type btype = binaryCastType(ltype, rtype);
		// operations on primitive types and special cases
		switch (op) {
			case Token.GTGT:
			case Token.LTLT:
			case Token.GTGTGT:
				if (btype == BuiltinType.INT || btype == BuiltinType.LONG) {
					if (ltype == BuiltinType.BYTE || ltype == BuiltinType.SHORT || ltype == BuiltinType.CHAR) {
						ltype = BuiltinType.INT;
						left = cast(left, ltype);
					}
					if (rtype == BuiltinType.BYTE || rtype == BuiltinType.SHORT || rtype == BuiltinType.CHAR) {
						rtype = BuiltinType.INT;
						right = cast(right, rtype);
					}
					if (rtype == BuiltinType.INT && (ltype == BuiltinType.LONG || ltype == BuiltinType.INT))
						return new BinaryExpr(left, op, right);
				}
				break;
			case '<':
			case '>':
			case Token.LTEQ:
			case Token.GTEQ:
				if (ltype.isNumeric() && rtype.isNumeric()) {
					return new ComparisonExpr(cast(left,btype), op, cast(right,btype));
				}
				// ex.: for '<' returns 'left.cmp(right) < 0'
				Function cmpmethod = findMethod(ltype, "cmp");
				if (cmpmethod != null && cmpmethod.type.returnType == BuiltinType.INT &&
						cmpmethod.type.argtypes.length == 2 && rtype.safeToCastTo(cmpmethod.type.argtypes[1])) {
					cmpmethod.hits++;
					Expr call = new CallExpr(left.lineNumber(), cmpmethod, new Expr[] {left, right});
					return new ComparisonExpr(call, op, new ConstExpr(-1, BuiltinType.INT, Int32.ZERO));
				}
				break;
			case Token.EQEQ:
			case Token.NOTEQ:
				if (btype == BuiltinType.ANY && ltype != BuiltinType.ANY && rtype != BuiltinType.ANY) {
					throw new ParseException("Incomparable types " + ltype + " and " + rtype);
				}
				if (ltype != BuiltinType.NULL && rtype != BuiltinType.NULL) {
					Function eqmethod = findMethod(ltype, "eq");
					if (eqmethod != null && eqmethod.type.returnType == BuiltinType.BOOL &&
							eqmethod.type.argtypes.length == 2 && rtype.safeToCastTo(eqmethod.type.argtypes[1])) {
						// var #left = left
						// var #right = right
						// /* for == */
						// if (#left == null) #right == null else ( if (#right == null) false else #left.eq(#right) )
						Var[] seqVars = new Var[2];
						seqVars[0] = new Var("#left", left.returnType());
						seqVars[0].isConstant = true;
						seqVars[0].hits = 2;
						seqVars[1] = new Var("#right", right.returnType());
						seqVars[1].isConstant = true;
						seqVars[1].hits = 3;
						Expr[] seqExprs = new Expr[] { left, right };
						left = new VarExpr(-1, seqVars[0]);
						right = new VarExpr(-1, seqVars[1]);
						Expr nullExpr = new ConstExpr(-1, BuiltinType.NULL, Null.NULL);
						Expr leftIsNull = new ComparisonExpr(left, Token.EQEQ, nullExpr);
						Expr rightIsNull = new ComparisonExpr(right, Token.EQEQ, nullExpr);
						Expr eqCall = new CallExpr(seqExprs[0].lineNumber(), eqmethod, new Expr[] { left, right });
						Expr secondIf = new IfElseExpr(rightIsNull, new ConstExpr(-1, BuiltinType.BOOL, Boolean.FALSE), eqCall);
						Expr firstIf = new IfElseExpr(leftIsNull, rightIsNull, secondIf);
						if (op == Token.NOTEQ) {
							firstIf = new UnaryExpr('!', firstIf);
						}
						return new SequentialExpr(seqVars, seqExprs, firstIf);
					}
				}
				return new ComparisonExpr(cast(left,btype), op, cast(right,btype));
			case Token.AMPAMP: {
				// return 'if (left) right else false'
				if (ltype != BuiltinType.BOOL || rtype != BuiltinType.BOOL)
					throw new ParseException("Operator "+Token.toString(op)+" cannot be applied to "+ltype+", "+rtype);
				return new IfElseExpr(left, right, new ConstExpr(-1, BuiltinType.BOOL, Boolean.FALSE));
			}
			case Token.BARBAR: {
				// return 'if (left) true else right'
				if (ltype != BuiltinType.BOOL || rtype != BuiltinType.BOOL)
					throw new ParseException("Operator "+Token.toString(op)+" cannot be applied to "+ltype+", "+rtype);
				return new IfElseExpr(left, new ConstExpr(-1, BuiltinType.BOOL, Boolean.TRUE), right);
			}
			case '+':
			case '-':
			case '*':
			case '/':
			case '%':
				if (ltype == BuiltinType.STRING && op == '+' && rtype != BuiltinType.NONE) {
					// if type defines overriden tostr(), use it
					if (rtype != BuiltinType.CHAR) {
						Function tostr = findMethod(rtype, "tostr");
						if (!tostr.signature.equals("Any.tostr") && tostr.type.returnType == BuiltinType.STRING && tostr.type.argtypes.length == 1) {
							tostr.hits++;
							right = new CallExpr(left.lineNumber(), tostr, new Expr[] { right });
						}
					}
					if (left.kind == Expr.EXPR_CONCAT) {
						((ConcatExpr)left).exprs.add(right);
						return left;
					} else {
						ConcatExpr cexpr = new ConcatExpr();
						cexpr.exprs.add(left);
						cexpr.exprs.add(right);
						return cexpr;
					}
				} else if (btype.isNumeric()) {
					return new BinaryExpr(cast(left,btype), op, cast(right,btype));
				}
				break;
			case '^':
			case '&':
			case '|':
				if (btype == BuiltinType.BOOL || btype == BuiltinType.INT || btype == BuiltinType.LONG) {
					return new BinaryExpr(cast(left,btype), op, cast(right,btype));
				}
				break;
			case Token.RANGE:
				if (btype == BuiltinType.INT || btype == BuiltinType.LONG) {
					return new RangeExpr(cast(left, btype), cast(right, btype));
				}
				break;
			case Token.IN: {
				if (rtype == BuiltinType.INTRANGE || rtype == BuiltinType.LONGRANGE) {
					left = cast(left, rtype == BuiltinType.INTRANGE ? BuiltinType.INT : BuiltinType.LONG);
					Var[] seqVars = new Var[] { new Var("#value", BuiltinType.INT) };
					seqVars[0].isConstant = true;
					seqVars[0].hits = 2;
					Expr[] seqExprs = new Expr[] { left };
					Expr leftVar = new VarExpr(-1, seqVars[0]);
					Expr comparison = new IfElseExpr(
							new ComparisonExpr(leftVar, Token.GTEQ, ((RangeExpr)right).fromExpr),
							new ConstExpr(-1, BuiltinType.BOOL, Boolean.TRUE),
							new ComparisonExpr(leftVar, Token.LTEQ, ((RangeExpr)right).toExpr));
					return new SequentialExpr(seqVars, seqExprs, comparison);
				}
				Function method = findMethod(rtype, "contains");
				if (method != null && method.type.argtypes.length == 2) {
					// using sequential since order of arguments is reversed,
					// i. e. 'A in B' becomes 'B.contains(A)'
					Var[] seqVars = new Var[] {	new Var("#item", ltype), new Var("#object", rtype)};
					Expr[] seqExprs = new Expr[] { left, right };
					VarExpr objExpr = new VarExpr(-1, seqVars[1]);
					VarExpr itemExpr = new VarExpr(-1, seqVars[0]);
					return new SequentialExpr(seqVars, seqExprs,
							new CallExpr(left.lineNumber(), method, new Expr[] {objExpr, itemExpr}));
				}
				throw new ParseException("Operator "+Token.toString(op)+" cannot be applied to "+ltype+", "+rtype);
			}
		}
		// searching method that overrides operator
		String methodname = null;
		switch (op) {
			case Token.LTLT: methodname = "shl"; break;
			case Token.GTGT: methodname = "shr"; break;
			case Token.GTGTGT: methodname = "ushr"; break;
			case Token.RANGE: methodname = "rangeTo"; break;
			case '+': methodname = "add"; break;
			case '-': methodname = "sub"; break;
			case '*': methodname = "mul"; break;
			case '/': methodname = "div"; break;
			case '%': methodname = "mod"; break;
			case '^': methodname = "xor"; break;
			case '&': methodname = "and"; break;
			case '|': methodname = "or"; break;
		}
		Function method = null;
		if (methodname != null) method = findMethod(ltype, methodname);
		if (method != null && method.type.argtypes.length == 2) {
			method.hits++;
			right = cast(right, method.type.argtypes[1]);
			return new CallExpr(left.lineNumber(), method, new Expr[] { left, right });
		}
		throw new ParseException("Operator "+Token.toString(op)+" cannot be applied to "+ltype+", "+rtype);
	}

	/**
	 * Computes type to which operands of binary operator should be cast.
	 */
	private Type binaryCastType(Type ltype, Type rtype) {
		if (ltype == BuiltinType.NULL) return rtype;
		if (rtype == BuiltinType.NULL) return ltype;
		if (ltype.isNumeric() && rtype.isNumeric()) {
			Type ctype = BuiltinType.INT;
			if (ltype == BuiltinType.DOUBLE || rtype == BuiltinType.DOUBLE)
				ctype = BuiltinType.DOUBLE;
			else if (ltype == BuiltinType.FLOAT || rtype == BuiltinType.FLOAT)
				ctype = BuiltinType.FLOAT;
			else if (ltype == BuiltinType.LONG || rtype == BuiltinType.LONG)
				ctype = BuiltinType.LONG;
			return ctype;
		}
		return Type.commonSuperType(ltype, rtype);
	}

	private Function findGetter(Type ownertype, String name) throws ParseException {
		String gettername = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
		Function getter = findMethod(ownertype, gettername);
		if (getter == null && env.hasOption(CompilerEnv.F_COMPAT21)) {
			getter = findMethod(ownertype, "get_" + name);
		}
		return getter;
	}

	private Function findSetter(Type ownertype, String name) throws ParseException {
		String gettername = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
		Function getter = findMethod(ownertype, gettername);
		if (getter == null && env.hasOption(CompilerEnv.F_COMPAT21)) {
			getter = findMethod(ownertype, "set_" + name);
		}
		return getter;
	}

	private Function findMethod(Type ownertype, String name) throws ParseException {
		Type stype = ownertype;
		while (stype != null) {
			Var mvar = unit.getVar(stype.toString()+'.'+name);
			if (mvar != null) {
				if (mvar.isConstant && mvar.type.kind == Type.TYPE_FUNCTION) {
					return (Function) mvar.defaultValue;
				} else {
					throw new ParseException("Cannot use variable " + mvar.name + " as method");
				}
			}
			stype = stype.superType();
		}
		return null;
	}

	private Expr cast(Expr expr, Type toType) throws ParseException {
		return cast(expr, toType, false);
	}

	private Expr cast(Expr expr, Type toType, boolean silent) throws ParseException {
		Type fromType = expr.returnType();
		// safe casts
		if (fromType.equals(toType)) {
			return expr;
		}
		if (fromType == BuiltinType.NONE) {
			throw new ParseException("Cannot convert from " + fromType + " to " + toType);
		}
		if (fromType.safeToCastTo(toType)) {
			return expr;
		}
		if (toType == BuiltinType.ANY) {
			return expr;
		}
		if (fromType.isNumeric() && toType.isNumeric()) {
			return new CastExpr(expr, toType);
		}
		if (fromType == BuiltinType.NULL && !toType.isNumeric() && toType.kind != Type.TYPE_BOOL) {
			return new CastExpr(expr, toType);
		}

		// unsafe casts
		if (toType.safeToCastTo(fromType) || fromType == BuiltinType.ANY) {
			if (!silent) {
				warn(CompilerEnv.W_TYPECAST, "Unsafe type cast from " + fromType + " to " + toType +
					"\n Use explicit cast() to suppress this message");
			}
			return new CastExpr(expr, toType);
		}

		throw new ParseException("Cannot convert from " + fromType + " to " + toType);
	}

	private Object defaultValue(Type type) {
		switch (type.kind) {
			case Type.TYPE_BOOL:
				return Boolean.FALSE;
			case Type.TYPE_BYTE:
			case Type.TYPE_CHAR:
			case Type.TYPE_SHORT:
			case Type.TYPE_INT:
				return Int32.ZERO;
			case Type.TYPE_LONG:
				return new Int64(0);
			case Type.TYPE_FLOAT:
				return new Float32(0);
			case Type.TYPE_DOUBLE:
				return new Float64(0);
			default:
				return null;
		}
	}

	/** Reads next token and if it is not the given, throws exception. */
	private void expect(int ttype) throws ParseException, IOException {
		if (t.nextToken() != ttype) {
			throw new ParseException("Expected '" + Token.toString(ttype) + "', got " + t.toString());
		}
	}

	private void warn(int category, String message) {
		env.warn((String)files.last(), t.lineNumber(), category, message);
	}

	private class ClosureScope implements Scope {
		private Scope parent;
		private Function lambda;

		public HashMap enclosedVars = new HashMap();

		public ClosureScope(Function lambda, Scope parent) {
			this.parent = parent;
			this.lambda = lambda;
		}

		public Function enclosingFunction() {
			return lambda;
		}

		public Type getType(String name) {
			return lambda.getType(name);
		}

		public Var getVar(String name) {
			Var var = lambda.getVar(name);
			if (var == null) {
				var = (Var) enclosedVars.get(name);
			}
			if (var == null) {
				var = parent.getVar(name);
				if (var != null && unit.getVar(name) != var && !var.isConstant)
					warn(CompilerEnv.W_ERROR, "Variable " + name + " is not constant");
				var = new Var(var.name, var.type);
				enclosedVars.set(name, var);
			}
			return var;
		}
	}
}
