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

import alchemy.core.Process;
import alchemy.core.Int;
import alchemy.fs.FSManager;
import alchemy.fs.Filesystem;
import alchemy.nec.tree.*;
import alchemy.util.IO;
import alchemy.util.UTFReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

/**
 * Parses Ether language.
 * @author Sergey Basalaev
 */
public class Parser {

	// deprecated symbols
	private static final Hashtable deprecated = new Hashtable();

	static {
		deprecated.put("bacopy", "Use acopy for all arrays.");
		deprecated.put("cacopy", "Use acopy for all arrays.");
	}

	// Warning categories
	private static final int W_ERROR = -1;
	private static final int W_TYPESAFE = 0;
	private static final int W_MAIN = 1;
	private static final int W_OPERATORS = 2;
	private static final int W_CAST = 3;
	private static final int W_HIDDEN = 4;
	private static final int W_DEPRECATED = 5;
	private static final int W_INCLUDED = 6;
	
	static final String[] WARN_STRINGS = {
		"typesafe", "main", "operators", "cast",
		"hidden", "deprecated", "included"};

	private int Wmask; /* Enabled warnings. */

	// eXperimental feature categories
	
	private static final int X_ARRAYOPT = 1;
	private static final int X_IINC = 2;
	
	static final String[] X_STRINGS = {"arrayopt", "iinc"};

	private int Xmask; /* Enabled experimental features. */

	private final Process p;
	private final int target;
	private Tokenizer t;
	private Unit unit;

	/** Files in the process of parsing. */
	private Stack files = new Stack();
	/** Files that are already parsed. */
	private Vector parsed = new Vector();
	
	private final Optimizer constOptimizer = new Optimizer();
	
	public Parser(Process p, int target, int optlevel, int Wmask, int Xmask) {
		this.p = p;
		this.target = target;
		if (optlevel > 1) Xmask |= X_ARRAYOPT | X_IINC;
		this.Wmask = Wmask;
		this.Xmask = Xmask;
	}

	public Unit parse(String source) {
		unit = new Unit();
		try {
			// adding builtin types
			unit.putType(BuiltinType.ANY);
			unit.putType(BuiltinType.BYTE);
			unit.putType(BuiltinType.CHAR);
			unit.putType(BuiltinType.SHORT);
			unit.putType(BuiltinType.INT);
			unit.putType(BuiltinType.LONG);
			unit.putType(BuiltinType.FLOAT);
			unit.putType(BuiltinType.DOUBLE);
			unit.putType(BuiltinType.BOOL);
			unit.putType(BuiltinType.STRING);
			unit.putType(BuiltinType.ARRAY);
			unit.putType(BuiltinType.FUNCTION);
			unit.putType(BuiltinType.STRUCTURE);
			unit.putType(BuiltinType.ERROR);
			// adding builtin functions
			parseFile("/inc/builtin.eh");
			// parsing
			parseFile(source);
		} catch (ParseException pe) {
			warn(W_ERROR, pe.getMessage());
			return null;
		} catch (IOException ioe) {
			IO.println(p.stderr, "I/O error: "+ioe);
			return null;
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
		if (name.length() == 0) throw new ParseException("Empty string in 'use'");
		String f = p.toFile(name);
		if (FSManager.fs().exists(f) && !FSManager.fs().isDirectory(f)) return f;
		f = p.toFile(name+".eh");
		if (FSManager.fs().exists(f)) return f;
		if (name.charAt(0) != '/') {
			String[] incpath = IO.split(p.getEnv("INCPATH"), ':');
			for (int i=0; i<incpath.length; i++) {
				f = p.toFile(incpath[i]+'/'+name);
				if (FSManager.fs().exists(f) && !FSManager.fs().isDirectory(f)) return f;
				f = p.toFile(incpath[i]+'/'+name+".eh");
				if (FSManager.fs().exists(f)) return f;
			}
		}
		throw new ParseException("File not found: "+name);
	}
	
	private void parseFile(String file) throws ParseException, IOException {
		//do nothing if this file is already parsed
		if (parsed.contains(file)) return;
		//if file is in stack we have cyclic inclusion
		if (files.contains(file)) {
			StringBuffer sb = new StringBuffer("Cyclic inclusion");
			for (int i=0; i<files.size(); i++) {
				sb.append("\n from ").append(files.elementAt(i));
			}
			throw new ParseException(sb.toString());
		}
		//push file in stack
		Tokenizer oldt = t;
		String olddir = p.getCurDir();
		files.push(file);
		p.setCurDir(Filesystem.fparent(file));
		InputStream filein = FSManager.fs().read(file);
		ByteArrayInputStream in = new ByteArrayInputStream(IO.readFully(filein));
		p.addStream(filein);
		filein.close();
		p.removeStream(filein);
		UTFReader fred = new UTFReader(in);
		t = new Tokenizer(fred);
		//parse
		while (t.nextToken() != Token.EOF) {
			if (t.ttype == ';') {
				// do nothing
			} else if (t.ttype != Token.KEYWORD) {
				throw new ParseException(t+" unexpected here.");
			} else if (t.svalue.equals("use")) {
				if (t.nextToken() != Token.QUOTED)
					throw new ParseException("String literal expected after 'use'");
				String next = resolveFile(t.svalue);
				parseFile(next);
			} else if (t.svalue.equals("type")) {
				if (t.nextToken() != Token.WORD)
					throw new ParseException("Type name expected after 'type'");
				String typename = t.svalue;
				if (typename.equals("Any"))
					throw new ParseException("Type Any is already defined.");
				Type prevtype = unit.getType(typename);
				if (t.nextToken() == ';') { // forward declaration
					if (prevtype == null) unit.putType(new NamedType(typename, null));
				} else if (prevtype != null && prevtype.superType() != null) {
					throw new ParseException("Type "+typename+" is already defined.");
				}
				t.pushBack();
				switch (t.nextToken()) {
					case ';':
						break;
					case '<': { // defining subtype
						Type superType = parseType(unit);
						if (superType instanceof BuiltinType && superType != BuiltinType.ANY)
							throw new ParseException("Cannot make a subtype of builtin type.");
						Type type;
						if (t.nextToken() == '{') {
							type = parseStruct(typename, superType);
						} else {
							t.pushBack();
							type = new NamedType(typename, superType);
						}
						unit.putType(type);
						break;
					}
					case '{': { // structure type
						unit.putType(parseStruct(typename, BuiltinType.STRUCTURE));
						break;
					}
					default:
						throw new ParseException(t.toString()+" unexpected here");
				}
			} else if (t.svalue.equals("var") || t.svalue.equals("const")) {
				boolean isConst = t.svalue.equals("const");
				if (t.nextToken() != Token.WORD)
					throw new ParseException("Variable name expected");
				String varname = t.svalue;
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
					if (!(varvalue instanceof ConstExpr))
						throw new ParseException("Constant expression expected");
					if (vartype == null) {
						vartype = varvalue.rettype();
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
					v.isConst = true;
					if (varvalue == null) {
						throw new ParseException("Constant "+varname+" is not initialized");
					}
				}
				if (varvalue != null) {
					v.constValue = ((ConstExpr)varvalue).value;
				} else if (vartype == BuiltinType.BOOL) {
					v.constValue = Boolean.FALSE;
				} else if (vartype == BuiltinType.INT || vartype == BuiltinType.SHORT
				        || vartype == BuiltinType.BYTE || vartype == BuiltinType.CHAR) {
					v.constValue = Int.ZERO;
				} else if (vartype == BuiltinType.LONG) {
					v.constValue = new Long(0l);
				} else if (vartype == BuiltinType.FLOAT) {
					v.constValue = new Float(0f);
				} else if (vartype == BuiltinType.DOUBLE) {
					v.constValue = new Double(0d);
				}
				unit.addVar(v);
				if (!isConst && files.size() > 1)
					warn(W_INCLUDED, "Global variable " + v.name + " in included file");
			} else if (t.svalue.equals("def")) {
				Func fdef = parseFuncDef();
				Var fvar = unit.getVar(fdef.signature);
				if (fvar == null) {
					fvar = new Var(fdef.signature, fdef.type);
					fvar.isConst = true;
					fvar.constValue = fdef;
					unit.addVar(fvar);
				} else if (!fvar.type.equals(fdef.type)) {
					if (fvar.type instanceof FunctionType)
						throw new ParseException("Definition of function "+fdef.signature+" conflicts with previous definition.");
					else
						throw new ParseException("Variable "+fdef.signature+" is already defined");
				}
				Func prev = unit.getFunc(fdef.signature);
				if (prev == null) unit.funcs.addElement(fdef);
				switch (t.nextToken()) {
					case ';':
						break;
					case '{':
					case '=':
						if (prev != null && prev.body != null)
							throw new ParseException("Function "+fdef.signature+" is already defined.");
						if (prev != null) {
							prev.locals = fdef.locals; //actual names for impl.
							fdef = prev;
						}
						if (t.ttype != '=') {
							t.pushBack();
						}
						if (fdef.isConstructor) {
							int lnum  = t.lineNumber();
							Type rettype = unit.getType(fdef.type.rettype.toString());
							if (!(rettype instanceof StructureType))
								throw new ParseException("Constructors can only be defined for structures");
							StructureType rtype = (StructureType) rettype;
							// defining <init>
							Func init = makeInitDef(fdef);
							boolean isBlock = t.nextToken() == '{';
							if (!isBlock) t.pushBack();
							Expr superinit = new NoneExpr();
							boolean supercalled = t.nextToken() == Token.KEYWORD && t.svalue.equals("super");
							if (supercalled) {
								expect('(');
								Type stype = rtype.superType();
								if (stype == BuiltinType.STRUCTURE) {
									expect(')');
								} else {
									Func supermethod = findMethod(stype, "new");
									if (supermethod == null)
										throw new ParseException("Method " + stype +".new not defined");
									supermethod = makeInitDef(supermethod);
									superinit = parseFCall(init, new ConstExpr(lnum, supermethod),
											new VarExpr(lnum, (Var) init.locals.firstElement()));
								}
							} else {
								t.pushBack();
								Type stype = rtype.superType();
								if (stype != BuiltinType.STRUCTURE) {
									Func supermethod = findMethod(stype, "new");
									if (supermethod == null)
										throw new ParseException("Method " + stype + ".new not defined");
									supermethod = makeInitDef(supermethod);
									if (supermethod.type.args.length > 1)
										throw new ParseException("super() call expected");
									superinit = new FCallExpr(new ConstExpr(lnum, supermethod),
											new Expr[]{ new VarExpr(lnum, (Var) init.locals.firstElement()) });
								}
							}
							BlockExpr initblock = new BlockExpr(init);
							initblock.exprs.addElement(superinit);
							if (isBlock) {
								initblock.exprs.addElement(cast(parseBlock(initblock), BuiltinType.NONE));
							} else if (!supercalled) {
								initblock.exprs.addElement(cast(parseExpr(initblock), BuiltinType.NONE));
							}
							init.body = initblock;
							unit.funcs.addElement(init);
							// defining constructor body
							//
							// def Type.new(...): Type {
							//   var this = new Type { }
							//   this.<init>(...)
							//   this
							// }
							BlockExpr block = new BlockExpr(fdef);
							Var th = new Var("this", rtype);
							block.addVar(th);

							Expr[] implicit = new Expr[rtype.fields.length];
							for (int i=0; i < implicit.length; i++) {
								if (rtype.fields[i].constValue != null)
									implicit[i] = new ConstExpr(lnum, rtype.fields[i].constValue);
							}
							block.exprs.addElement(new AssignExpr(th, new NewArrayByEnumExpr(lnum, rtype, implicit)));

							Expr[] explicit = new Expr[fdef.locals.size()+1];
							explicit[0] = new VarExpr(lnum, th);
							for (int i=1; i < explicit.length; i++) {
								explicit[i] = new VarExpr(lnum, (Var)fdef.locals.elementAt(i-1));
							}
							block.exprs.addElement(new FCallExpr(new ConstExpr(lnum, init), explicit));

							block.exprs.addElement(new VarExpr(lnum, th));
							fdef.body = block;
							fdef.hits++;
							fdef.source = Filesystem.fname((String)files.peek());
						} else {
							fdef.body = cast(parseExpr(fdef), fdef.type.rettype);
							fdef.hits++;
							fdef.source = Filesystem.fname((String)files.peek());
						}
						if (files.size() > 1)
							warn(W_INCLUDED, "Function " + fdef.signature + " is implemented in included file");
						break;
					default:
						throw new ParseException(t.toString()+" unexpected here");
				}
			}
		}
		//move file to parsed
		t = oldt;
		p.setCurDir(olddir);
		parsed.addElement(files.pop());
	}
	
	/** Creates Type.&lt;init&gt; from Type.new */
	private Func makeInitDef(Func constructor) {
		Type rtype = constructor.type.rettype;
		Func init = new Func(unit);
		init.hits++;
		init.signature = rtype.toString() + ".<init>";
		init.source = Filesystem.fname((String)files.peek());
		Type[] initargs = new Type[constructor.type.args.length + 1];
		initargs[0] = rtype;
		System.arraycopy(constructor.type.args, 0, initargs, 1, initargs.length-1);
		init.type = new FunctionType(BuiltinType.NONE, initargs);
		init.locals = new Vector();
		init.locals.addElement(new Var("this", rtype));
		for (int i=0; i < constructor.locals.size(); i++) {
			Var var = (Var) constructor.locals.elementAt(i);
			init.locals.addElement(var.clone());
		}
		return init;
	}
	
	private StructureType parseStruct(String name, Type parent) throws ParseException, IOException {
		if (parent instanceof NamedType && parent.superType() == null)
			parent = unit.getType(parent.toString());
		StructureType struct = new StructureType(name, parent);
		Vector fields = new Vector();
		if (parent instanceof StructureType) {
			final Var[] pfields = ((StructureType)parent).fields;
			for (int i=0; i < pfields.length; i++) {
				fields.addElement(pfields[i]);
			}
		} else if (parent != BuiltinType.STRUCTURE) {
			throw new ParseException("Type " + name + " is not a structure");
		}
		boolean first = true;
		while (t.nextToken() != '}') {
			t.pushBack();
			if (first) first = false;
			else expect(',');
			if (t.nextToken() != Token.WORD)
				throw new ParseException("Field name expected, got "+t);
			String fieldname = t.svalue;
			expect(':');
			Type vartype = parseType(unit);
			Var var = new Var(fieldname, vartype);
			var.index = fields.size();
			fields.addElement(var);
			if (t.nextToken() == '=') {
				Expr varvalue = (Expr) cast(parseExpr(unit), vartype).accept(constOptimizer, unit);
				if (!(varvalue instanceof ConstExpr))
					throw new ParseException("Constant expression expected");
				var.constValue = ((ConstExpr)varvalue).value;
			} else {
				t.pushBack();
				if (vartype == BuiltinType.BOOL) {
					var.constValue = Boolean.FALSE;
				} else if (vartype == BuiltinType.INT || vartype == BuiltinType.SHORT
				        || vartype == BuiltinType.BYTE || vartype == BuiltinType.CHAR) {
					var.constValue = Int.ZERO;
				} else if (vartype == BuiltinType.LONG) {
					var.constValue = new Long(0l);
				} else if (vartype == BuiltinType.FLOAT) {
					var.constValue = new Float(0f);
				} else if (vartype == BuiltinType.DOUBLE) {
					var.constValue = new Double(0d);
				}
			}
		}
		struct.fields = new Var[fields.size()];
		for (int i=fields.size()-1; i>=0; i--) {
			struct.fields[i] = ((Var)fields.elementAt(i));
		}
		return struct;
	}
	
	/**
	 * Parses type expression.
	 */
	private Type parseType(Scope scope) throws ParseException, IOException {
		switch (t.nextToken()) {
			case Token.WORD: { //scalar type
				Type type = scope.getType(t.svalue);
				if (t.svalue.equals("BArray")) {
					warn(W_DEPRECATED, "Type BArray is deprecated, use [Byte]");
					return new ArrayType(BuiltinType.BYTE);
				}
				if (t.svalue.equals("CArray")) {
					warn(W_DEPRECATED, "Type CArray is deprecated, use [Char]");
					return new ArrayType(BuiltinType.CHAR);
				}
				if (type == null) {
					throw new ParseException("Undefined type "+t);
				}
				return type;
			}
			case '(': { //function type
				Vector argtypes = new Vector();
				while (t.nextToken() != ')') {
					t.pushBack();
					if (!argtypes.isEmpty()) expect(',');
					argtypes.addElement(parseType(scope));
				}
				Type rettype;
				if (t.nextToken() == ':') {
					rettype = parseType(scope);
				} else {
					t.pushBack();
					rettype = BuiltinType.NONE;
				}
				FunctionType type = new FunctionType(rettype, new Type[argtypes.size()]);
				for (int i=argtypes.size()-1; i>=0; i--) {
					type.args[i] = (Type)argtypes.elementAt(i);
				}
				return type;
			}
			case '[': { // array type
				Type elementType = parseType(scope);
				expect(']');
				return new ArrayType(elementType);
			}
			default:
				throw new ParseException(t.toString()+" unexpected here");
		}
	}

	/**
	 * Parses definition of function (without a body).
	 */
	private Func parseFuncDef() throws ParseException, IOException {
		Func func = new Func(unit);
		//parsing def
		if (t.nextToken() != Token.WORD)
			throw new ParseException("Function name expected, got "+t);
		String str = t.svalue;
		String fname;
		NamedType methodholder = null;
		if (t.nextToken() == '.') {
			methodholder = unit.getType(str);
			if (methodholder == null)
				throw new ParseException("Type "+str+" is not defined");
			if (t.nextToken() == Token.KEYWORD && t.svalue.equals("new"))
				func.isConstructor = true;
			else if (t.ttype != Token.WORD)
				throw new ParseException("Function name expected, got "+t);
			fname = methodholder.toString()+'.'+t.svalue;
		} else {
			t.pushBack();
			fname = str;
		}
		expect('(');
		Vector args = new Vector();
		if (!func.isConstructor && methodholder != null) {
			args.addElement(new Var("this", methodholder));
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
			expect(':');
			Type vartype = parseType(func);
			Var var = new Var(varname, vartype);
			args.addElement(var);
			if (t.nextToken() == '=') {
				defaults = true;
				Expr expr = (Expr) cast(parseExpr(unit), vartype).accept(constOptimizer, unit);
				if (expr instanceof ConstExpr) {
					var.constValue = ((ConstExpr)expr).value;
				} else {
					throw new ParseException("Constant expression expected");
				}
			} else if (defaults) {
				throw new ParseException("No default provided for argument " + varname);
			} else {
				t.pushBack();
			}
		}
		Type rettype;
		if (t.nextToken() == ':') {
			rettype = parseType(func);
		} else {
			t.pushBack();
			rettype = (func.isConstructor) ? methodholder : BuiltinType.NONE;
		}
		//populating fields
		func.locals = args;
		FunctionType ftype = new FunctionType(rettype, new Type[args.size()]);
		for (int i=args.size()-1; i>=0; i--) {
			ftype.args[i] = ((Var)args.elementAt(i)).type;
		}
		func.signature = fname;
		func.type = ftype;
		// semantic checks
		if (func.isConstructor && !methodholder.equals(rettype))
			warn(W_OPERATORS, "Constructor returns value of different type than " + methodholder);
		if (fname.equals("main")) {
			if (args.size() != 1) {
				warn(W_MAIN, "Incorrect number of arguments in main(), should be ([String])");
			} else {
				Type argtype = ((Var)args.elementAt(0)).type;
				Type shouldbe = new ArrayType(BuiltinType.STRING);
				if (!argtype.isSupertypeOf(shouldbe)) {
					warn(W_MAIN, "Incompatible argument type in main()");
				}
				if (!argtype.equals(shouldbe)) {
					warn(W_MAIN, "Argument of main() should be of type [String]");
				}
			}
			if (rettype != BuiltinType.INT && rettype != BuiltinType.NONE) {
				warn(W_MAIN, "Incompatible return type in main(), should be Int or <none>");
			}
		}
		if (methodholder != null) {
			String methodname = fname.substring(fname.lastIndexOf('.')+1);
			if (methodname.equals("eq") &&
					(rettype != BuiltinType.BOOL || ftype.args.length != 2 || !ftype.args[1].equals(methodholder)))
				warn(W_OPERATORS, "Method " + fname + " cannot be used as override for equality operators");
			else if (methodname.equals("cmp") &&
					(rettype != BuiltinType.INT || args.size() != 2 || !ftype.args[1].equals(methodholder)))
				warn(W_OPERATORS, "Method " + fname + " cannot be used as override for comparison operators");
			else if (methodname.equals("tostr") &&
					(rettype != BuiltinType.STRING || ftype.args.length != 1))
				warn(W_OPERATORS, "Method " + fname + " cannot be used as override for Any.tostr()");
		}
		return func;
	}
	
	/**
	 * Binary operators arranged by priority. In groups of four.
	 */
	private static int[] priorops = {
			// if word operators are to appear they have the lowest priority
			Token.KEYWORD, Token.WORD, 0, 0,
			'^', 0, 0, 0,
			Token.BARBAR, '|', 0, 0,
			Token.AMPAMP, '&', 0, 0,
			Token.LTEQ, Token.GTEQ, '<', '>',
			Token.EQEQ, Token.NOTEQ, 0, 0,
			Token.LTLT, Token.GTGT, Token.GTGTGT, 0,
			'+', '-', 0, 0,
			'*', '/', '%', 0
		};

	private int getPriority(Int operator) {
		int op = operator.value;
		for (int i=0; i<priorops.length; i++) {
			if (priorops[i] == op) return i/4;
		}
		return -1;
	}
	
	/**
	 * Parses sequence of expressions delimitered with operators.
	 */
	private Expr parseExpr(Scope scope) throws IOException, ParseException {
		Vector exprs = new Vector();
		Vector operators = new Vector();
		while (true) {
			exprs.addElement(parsePostfix(scope, parseExprNoop(scope)));
			int opchar = t.nextToken();
			if ("+-/*%^&|<>".indexOf(opchar) >= 0 || (opchar <= -20 && opchar >= -30)) {
				operators.addElement(Int.toInt(opchar));
			} else {
				t.pushBack();
				break;
			}
		}
		while (!operators.isEmpty()) {
			int index = 0;
			int priority = 0;
			for (int i = 0; i < operators.size(); i++) {
				int pr = getPriority((Int)operators.elementAt(i));
				if (pr > priority) {
					priority = pr;
					index = i;
				}
			}
			int op = ((Int)operators.elementAt(index)).value;
			Expr left = (Expr)exprs.elementAt(index);
			Expr right = (Expr)exprs.elementAt(index+1);
			Expr newexpr = makeBinaryExpr(left, op, right);
			exprs.setElementAt(newexpr, index);
			exprs.removeElementAt(index+1);
			operators.removeElementAt(index);
		}
		return (Expr)exprs.elementAt(0);
	}

	private Expr parsePostfix(Scope scope, Expr expr) throws ParseException, IOException {
		Expr rtexpr = expr;
		while (true) {
			switch (t.nextToken()) {
				case '(':
					rtexpr = parseFCall(scope, rtexpr, null);
					break;
				case '[':
					rtexpr = parseBrackets(scope, rtexpr);
					break;
				case '.':
					rtexpr = parseDot(scope, rtexpr);
					break;
				default:
					t.pushBack();
					return rtexpr;
			}
		}
	}
	
	/**
	 * Parses expression without binary operators
	 */
	private Expr parseExprNoop(Scope scope) throws ParseException, IOException {
		int ttype = t.nextToken();
		int lnum = t.lineNumber();
		switch (ttype) {
			case '+': {
				Expr sub = parsePostfix(scope, parseExprNoop(scope));
				Type type = sub.rettype();
				if (type.isSubtypeOf(BuiltinType.NUMBER))
					return sub;
				throw new ParseException("Operator "+(char)ttype+" cannot be applied to "+type);
			}
			case '-': {
				Expr sub = parsePostfix(scope, parseExprNoop(scope));
				Type type = sub.rettype();
				if (type.isSubtypeOf(BuiltinType.NUMBER))
					return new UnaryExpr(ttype, sub);
				Func method = findMethod(type, "minus");
				if (method != null && method.type.args.length == 1) {
					method.hits++;
					return new FCallExpr(new ConstExpr(sub.lineNumber(), method), new Expr[] { sub });
				}
				throw new ParseException("Operator "+(char)ttype+" cannot be applied to "+type);
			}
			case '~': {
				Expr sub = parsePostfix(scope, parseExprNoop(scope));
				Type type = sub.rettype();
				if (type == BuiltinType.BYTE || type == BuiltinType.SHORT || type == BuiltinType.CHAR) {
					sub = cast(sub, BuiltinType.INT);
					type = BuiltinType.INT;
				}
				if (type == BuiltinType.INT || type == BuiltinType.LONG) {
					return new UnaryExpr(ttype, sub);
				}
				throw new ParseException("Operator "+(char)ttype+" cannot be applied to "+type);
			}
			case '!': {
				Expr sub = parsePostfix(scope, parseExprNoop(scope));
				Type type = sub.rettype();
				if (type == BuiltinType.BOOL) {
					return new UnaryExpr(ttype, sub);
				}
				Func method = findMethod(type, "not");
				if (method != null && method.type.args.length == 1) {
					method.hits++;
					return new FCallExpr(new ConstExpr(sub.lineNumber(), method), new Expr[] { sub });
				}
				throw new ParseException("Operator "+(char)ttype+" cannot be applied to "+type);
			}
			case '{':
				return parseBlock(scope);
			case '(': {
				Expr expr = parseExpr(scope);
				expect(')');
				return expr;
			}
			case '[': {
				// reading elements
				Vector exprs = new Vector();
				while (t.nextToken() != ']') {
					t.pushBack();
					if (!exprs.isEmpty()) expect(',');
					exprs.addElement(parseExpr(scope));
				}
				// calculating common type
				Type eltype = BuiltinType.NULL;
				for (int i=0; i<exprs.size(); i++) {
					Expr e = (Expr)exprs.elementAt(i);
					eltype = binaryCastType(eltype, e.rettype());
				}
				if (eltype == BuiltinType.NULL)
					eltype = BuiltinType.ANY;
				else if (eltype == BuiltinType.NONE)
					throw new ParseException("Cannot create array of <none>.");
				// building expression
				Expr[] init = new Expr[exprs.size()];
				for (int i=0; i<init.length; i++) {
					init[i] = cast( (Expr)exprs.elementAt(i), eltype);
				}
				return new NewArrayByEnumExpr(lnum, new ArrayType(eltype), init);
			}
			case Token.CHAR:
				return new CharConstExpr(lnum, Int.toInt(t.ivalue));
			case Token.INT:
				return new ConstExpr(lnum, Int.toInt(t.ivalue));
			case Token.LONG:
				return new ConstExpr(lnum, new Long(t.lvalue));
			case Token.FLOAT:
				return new ConstExpr(lnum, new Float(t.fvalue));
			case Token.DOUBLE:
				return new ConstExpr(lnum, new Double(t.dvalue));
			case Token.QUOTED:
				return new ConstExpr(lnum, t.svalue);
			case Token.BOOL:
				return new ConstExpr(lnum, (t.svalue.equals("true") ? Boolean.TRUE : Boolean.FALSE));
			case Token.KEYWORD:
				return parseKeyword(scope, t.svalue);
			case Token.WORD: {
				String str = t.svalue;
				if (deprecated.get(str) != null) {
					warn(W_DEPRECATED, "Function " + str + " is deprecated. " + deprecated.get(str));
				}
				Var var = scope.getVar(str);
				if (var == null) throw new ParseException("Variable "+str+" is not defined");
				// making get expression
				Expr vexpr;
				if (var.isConst && var.constValue != null) {
					Object cnst = var.constValue;
					if (cnst instanceof Func) {
						((Func)cnst).hits++;
					}
					vexpr = new ConstExpr(lnum, var.constValue);
				} else if (scope.isLocal(var.name)) {
					vexpr = new VarExpr(lnum, var);
				} else {
					// convert to  cast(type)getstatic("var#hash")
					if (var.constValue != null) {
						Func getstaticdef = unit.getFunc("getstaticdef");
						getstaticdef.hits++;
						vexpr = new CastExpr(
							var.type,
							new FCallExpr(new ConstExpr(lnum, getstaticdef),
							new Expr[] { new ConstExpr(lnum, var.name+'#'+Integer.toHexString(var.hashCode())),
							             new ConstExpr(lnum, var.constValue)}));
					} else {
						Func getstatic = unit.getFunc("getstatic");
						getstatic.hits++;
						vexpr = new CastExpr(
							var.type,
							new FCallExpr(new ConstExpr(lnum, getstatic),
							new Expr[] { new ConstExpr(lnum, var.name+'#'+Integer.toHexString(var.hashCode())) }));
					}
				}
				if (Token.isAssignment(t.nextToken())) {
					if (var.isConst)
						throw new ParseException("Cannot assign to constant "+var.name);
					int operator = t.ttype;
					Expr value = cast(makeAssignRval(vexpr, operator, parseExpr(scope)), var.type);
					if (scope.isLocal(var.name)) {
						if ((Xmask & X_IINC) != 0 && var.type == BuiltinType.INT && value instanceof BinaryExpr) {
							BinaryExpr bin = (BinaryExpr)value;
							if (bin.lvalue == vexpr && bin.rvalue instanceof ConstExpr && (bin.operator == '+' || bin.operator == '-')) {
								int incr = ((Int)((ConstExpr)bin.rvalue).value).value;
								if (bin.operator == '-') incr = -incr;
								if (incr >= Byte.MIN_VALUE && incr <= Byte.MAX_VALUE) {
									return new IincExpr(lnum, var, incr);
								}
							}
						}
						return new AssignExpr(var, value);
					} else {
						// convert to  setstatic("var#hash", value)
						Func setstatic = unit.getFunc("setstatic");
						setstatic.hits++;
						return new FCallExpr(
							new ConstExpr(lnum, setstatic),
							new Expr[] { new ConstExpr(lnum, var.name+'#'+Integer.toHexString(var.hashCode())), value });
					}
				} else {
					t.pushBack();
					return vexpr;
				}
			}
			default:
				throw new ParseException(t.toString()+" unexpected here");
		}
	}
	
	private Expr parseKeyword(Scope scope, String keyword) throws IOException, ParseException {
		int lnum = t.lineNumber();
		if (keyword.equals("cast")) {
			expect('(');
			Type toType = parseType(scope);
			expect(')');
			Expr expr = parseExpr(scope);
			if (toType.equals(expr.rettype())) {
				warn(W_CAST, "Unnecessary cast to the same type");
			} else if (toType.isSupertypeOf(expr.rettype())) {
				warn(W_CAST, "Unnecessary cast to the supertype");
			}
			if (expr.rettype().isSupertypeOf(toType)) {
				return new CastExpr(toType, expr);
			}
			return cast(expr, toType);
		} else if (keyword.equals("null")) {
			return new ConstExpr(lnum, Null.NULL);
		} else if (keyword.equals("while")) {
			expect('(');
			Expr cond = cast(parseExpr(scope), BuiltinType.BOOL);
			expect(')');
			Expr body = cast(parseExpr(scope), BuiltinType.NONE);
			return new WhileExpr(cond, body);
		} else if (keyword.equals("do")) {
			Expr body = cast(parseExpr(scope), BuiltinType.NONE);
			if (t.nextToken() != Token.KEYWORD || !t.svalue.equals("while"))
				throw new ParseException("'while' expected after 'do <expr>'");
			expect('(');
			Expr cond = cast(parseExpr(scope), BuiltinType.BOOL);
			expect(')');
			return new DoWhileExpr(cond, body);
		} else if (keyword.equals("for")) {
			expect('(');
			BlockExpr forblock = new BlockExpr(scope);
			BlockExpr forbody = new BlockExpr(forblock);
			Expr init = cast(parseExpr(forblock), BuiltinType.NONE);
			expect(',');
			Expr cond = cast(parseExpr(forblock), BuiltinType.BOOL);
			expect(',');
			Expr incr = cast(parseExpr(forbody), BuiltinType.NONE);
			expect(')');
			Expr body = cast(parseExpr(forbody), BuiltinType.NONE);
			forbody.exprs.addElement(body);
			forbody.exprs.addElement(incr);
			forblock.exprs.addElement(init);
			forblock.exprs.addElement(new WhileExpr(cond, forbody));
			return forblock;
		} else if (keyword.equals("if")) {
			expect('(');
			Expr cond = cast(parseExpr(scope), BuiltinType.BOOL);
			expect(')');
			Expr ifexpr = parseExpr(scope);
			Expr elseexpr;
			if (t.nextToken() != Token.KEYWORD || !t.svalue.equals("else")) {
				t.pushBack();
				elseexpr = new NoneExpr();
			} else {
				elseexpr = parseExpr(scope);
			}
			Type btype = binaryCastType(ifexpr.rettype(), elseexpr.rettype());
			return new IfExpr(cond, cast(ifexpr, btype), cast(elseexpr, btype));
		} else if (keyword.equals("switch")) {
			expect('(');
			// do not cast, other numeric type may be put here by mistake
			Expr indexexpr = parseExpr(scope);
			Type indextype = indexexpr.rettype();
			if (indextype != BuiltinType.INT && indextype != BuiltinType.SHORT &&
			    indextype != BuiltinType.BYTE && indextype != BuiltinType.CHAR)
				throw new ParseException("Index of switch must be Int");
			expect(')');
			expect('{');
			// parsing switch body
			Expr elseexpr = null;
			Vector keys = new Vector(); // int[]
			Vector keysunique = new Vector(); // Int
			Vector exprs = new Vector(); // Expr
			while (t.nextToken() != '}') {
				if (t.ttype == ';') continue;
				else if (t.ttype == Token.KEYWORD && t.svalue.equals("else")) {
					if (elseexpr != null)
						throw new ParseException("else branch is already defined in this switch");
					expect(':');
					elseexpr = parseExpr(scope);
				} else {
					Vector branchkeyv = new Vector();
					do {
						t.pushBack();
						if (!branchkeyv.isEmpty()) expect(',');
						Expr branchindex = (Expr) cast(parseExpr(scope), BuiltinType.INT).accept(constOptimizer, scope);
						if (!(branchindex instanceof ConstExpr))
							throw new ParseException("Constant expression expected.");
						Int idx = (Int)((ConstExpr)branchindex).value;
						if (keysunique.contains(idx))
							throw new ParseException("branch for "+idx+" is already defined in this switch");
						branchkeyv.addElement(idx);
						keysunique.addElement(idx);
					} while (t.nextToken() != ':');
					int[] branchkeys = new int[branchkeyv.size()];
					for (int i=0; i<branchkeys.length; i++) {
						Int idx = (Int)branchkeyv.elementAt(i);
						branchkeys[i] = idx.value;
					}
					keys.addElement(branchkeys);
					exprs.addElement(parseExpr(scope));
				}
			}
			// obtaining common type
			Type type;
			if (elseexpr != null) {
				type = elseexpr.rettype();
			} else if (!exprs.isEmpty()) {
				type = ((Expr)exprs.firstElement()).rettype();
			} else {
				throw new ParseException("switch body is empty");
			}
			for (int i=0; i<exprs.size(); i++) {
				Expr e = (Expr)exprs.elementAt(i);
				type = binaryCastType(type, e.rettype());
			}
			// casting all to common type
			if (elseexpr != null) elseexpr = cast(elseexpr, type);
			for (int i=0; i<exprs.size(); i++) {
				Expr e = (Expr)exprs.elementAt(i);
				exprs.setElementAt(cast(e, type), i);
			}
			SwitchExpr swexpr = new SwitchExpr();
			swexpr.indexexpr = indexexpr;
			swexpr.elseexpr = elseexpr;
			swexpr.keys = keys;
			swexpr.exprs = exprs;
			swexpr.rettype = type;
			return swexpr;
		} else if (keyword.equals("var") || keyword.equals("const")) {
			boolean isConst = keyword.equals("const");
			if (t.nextToken() != Token.WORD)
				throw new ParseException("Identifier expected after 'var'");
			String varname = t.svalue;
			Type vartype = null;
			Expr varvalue = null;
			// parsing type
			if (t.nextToken() == ':') {
				vartype = parseType(scope);
			} else {
				t.pushBack();
			}
			// parsing value
			if (t.nextToken() == '=') {
				varvalue = parseExpr(scope);
				if (vartype == null) {
					vartype = varvalue.rettype();
					if (vartype == BuiltinType.NONE)
						throw new ParseException("Cannot convert from <none> to Any");
					if (vartype == BuiltinType.NULL)
						vartype = BuiltinType.ANY;
				} else {
					varvalue = cast(varvalue, vartype);
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
				v.isConst = true;
				if (varvalue == null) {
					throw new ParseException("Constant "+varname+" is not initialized");
				}
			}
			// initializing primitive type
			if (vartype instanceof BuiltinType && varvalue == null) {
				if (vartype == BuiltinType.BOOL) {
					varvalue = new ConstExpr(lnum, Boolean.FALSE);
				} else if (vartype == BuiltinType.INT || vartype == BuiltinType.SHORT
				        || vartype == BuiltinType.BYTE || vartype == BuiltinType.CHAR) {
					varvalue = new ConstExpr(lnum, Int.ZERO);
				} else if (vartype == BuiltinType.LONG) {
					varvalue = new ConstExpr(lnum, new Long(0l));
				} else if (vartype == BuiltinType.FLOAT) {
					varvalue = new ConstExpr(lnum, new Float(0f));
				} else if (vartype == BuiltinType.DOUBLE) {
					varvalue = new ConstExpr(lnum, new Double(0d));
				}
			}
			// adding variable and returning expression
			if (scope.addVar(v)) {
				warn(W_HIDDEN, "Variable "+v.name+" hides another variable with the same name");
			}
			if (varvalue != null) {
				return new AssignExpr(v, varvalue);
			} else {
				return new NoneExpr();
			}
		} else if (keyword.equals("new")) {
			Type type = parseType(scope);
			if (type instanceof NamedType) {
				if (t.nextToken() == '{') {
					// extended structure constructor
					if (!(type instanceof StructureType))
						throw new ParseException("Extended constructor but type " + type + " is not a structure");
					boolean first = true;
					StructureType struct = (StructureType)type;
					Expr[] init = new Expr[struct.fields.length];
					// parse explicit initializers
					while (t.nextToken() != '}') {
						t.pushBack();
						if (first) first = false;
						else expect(',');
						if (t.nextToken() != Token.WORD)
							throw new ParseException("Identifier expected in structure constructor");
						int index = struct.fields.length-1;
						while (index >= 0 && !struct.fields[index].name.equals(t.svalue)) index--;
						if (index < 0)
							throw new ParseException("Type "+type+" has no member named "+t.svalue);
						expect('=');
						init[index] = cast(parseExpr(scope), struct.fields[index].type);
					}
					// add implicit initializers
					for (int i=0; i < init.length; i++) {
						if (init[i] == null && struct.fields[i].constValue != null) {
							init[i] = new ConstExpr(t.lineNumber(), struct.fields[i].constValue);
						}
					}
					return new NewArrayByEnumExpr(lnum, type, init);
				} else if (t.ttype == '(') {
					Func newmethod = findMethod(type, "new");
					if (newmethod == null) {
						// default structure constructor
						if (!(type instanceof StructureType))
							throw new ParseException("Type " + type + " has no 'new' method");
						boolean first = true;
						StructureType struct = (StructureType)type;
						Expr[] init = new Expr[struct.fields.length];
						for (int i=0; i < init.length; i++) {
							if (first) first = false;
							else expect(',');
							init[i] = cast(parseExpr(scope), struct.fields[i].type);
						}
						expect(')');
						return new NewArrayByEnumExpr(lnum, type, init);
					} else {
						// calling Type.new
						return parseFCall(scope, new ConstExpr(lnum, newmethod), null);
					}
				} else {
					throw new ParseException(t.toString() + " unexpected here");
				}
			} else if (type instanceof ArrayType) {
				if (t.nextToken() == '(') {
					// new array of given size
					Expr lenexpr = cast(parseExpr(scope), BuiltinType.INT);
					expect(')');
					return new NewArrayExpr(lnum, type, lenexpr);
				} else if (t.ttype == '{') {
					// new array with given elements
					Vector vinit = new Vector();
					Type eltype = ((ArrayType)type).elementType();
					while (t.nextToken() != '}') {
						t.pushBack();
						if (!vinit.isEmpty()) expect(',');
						Expr e = cast(parseExpr(scope), eltype);
						vinit.addElement(e);
					}
					Expr[] init = new Expr[vinit.size()];
					vinit.copyInto(init);
					return new NewArrayByEnumExpr(lnum, type, init);
				} else {
					throw new ParseException("'(' or '{' expected in constructor");
				}
			} else {
				throw new ParseException("Type "+ type +" has no 'new' method");
			}
		} else if (keyword.equals("def")) {
			// anonymous function
			// TODO: I probably need to use scope here instead
			Func func = new Func(unit);
			func.source = Filesystem.fname((String)files.peek());
			// parsing args
			expect('(');
			Vector args = new Vector();
			boolean first = true;
			while (t.nextToken() != ')') {
				t.pushBack();
				if (first) first = false;
				else expect(',');
				if (t.nextToken() != Token.WORD)
					throw new ParseException("Variable name expected, got "+t);
				String varname = t.svalue;
				expect(':');
				Type vartype = parseType(func);
				args.addElement(new Var(varname, vartype));
			}
			Type rettype;
			if (t.nextToken() == ':') {
				rettype = parseType(func);
			} else {
				t.pushBack();
				rettype = BuiltinType.NONE;
			}
			//populating fields
			func.locals = args;
			FunctionType ftype = new FunctionType(rettype, new Type[args.size()]);
			for (int i=args.size()-1; i>=0; i--) {
				ftype.args[i] = ((Var)args.elementAt(i)).type;
			}
			int lambdanum = 1;
			while (unit.getFunc(scope.funcName()+'$'+lambdanum) != null) lambdanum++;
			func.signature = scope.funcName()+'$'+lambdanum;
			func.type = ftype;
			func.hits++;
			switch (t.nextToken()) {
				case '=':
					func.body = cast(parseExpr(func), rettype);
					break;
				case '{':
					func.body = cast(parseBlock(func), rettype);
					break;
				default:
					throw new ParseException("Function body expected, got "+t);
			}
			unit.funcs.addElement(func);
			return new ConstExpr(lnum, func);
		} else if (keyword.equals("try")) {
			Expr tryexpr = parseExpr(scope);
			if (t.nextToken() != Token.KEYWORD || !t.svalue.equals("catch"))
				throw new ParseException("'catch' expected after 'try <expr>'");
			Var v = null;
			BlockExpr catchblock = new BlockExpr(scope);
			if (t.nextToken() == '(') {
				if (t.nextToken() != Token.KEYWORD || !t.svalue.equals("var"))
					throw new ParseException("'var' expected");
				if (t.nextToken() != Token.WORD)
					throw new ParseException("Identifier expected");
				v = new Var(t.svalue, BuiltinType.ERROR);
				if (catchblock.addVar(v)) {
					warn(W_HIDDEN, "Variable "+v.name+" hides another variable with the same name");
				}
				expect(')');
			} else {
				t.pushBack();
			}
			Expr catchexpr = parseExpr(catchblock);
			Type commontype = binaryCastType(tryexpr.rettype(), catchexpr.rettype());
			catchblock.exprs.addElement(cast(catchexpr, commontype));
			TryCatchExpr trycatch = new TryCatchExpr();
			trycatch.tryexpr = cast(tryexpr, commontype);
			trycatch.catchexpr = catchblock;
			trycatch.catchvar = v;
			return trycatch;
		} else if (keyword.equals("super")) {
			Var th = scope.getVar("this");
			if (th == null)
				throw new ParseException("Variable " + this + " not found");
			Type stype = th.type.superType();
			if (stype == null) stype = BuiltinType.ANY;
			return new CastExpr(stype, new VarExpr(lnum, th));
		} else {
			throw new ParseException(t.toString()+" unexpected here");
		}
	}
	
	/**
	 * Parses expression part after '(' (function application).
	 *
	 * <p>
	 * Does special type checkings and type casts for some functions.
	 * <dl>
	 * <dt>{@code Function.curry}</dt>
	 * <dd>checks if argument is acceptable, computes returned type</dd>
	 *
	 * <dt>{@code Structure.clone}</dt>
	 * <dd>the returned type is the same as of argument</dd>
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
	private Expr parseFCall(Scope scope, Expr fload, Expr firstarg) throws IOException, ParseException {
		if (!(fload.rettype() instanceof FunctionType))
			throw new ParseException("Applying () to non-function expression");
		FunctionType ftype = (FunctionType)fload.rettype();
		// parse arguments
		Vector vargs = new Vector();
		if (firstarg != null) vargs.addElement(firstarg);
		boolean first = true;
		while (t.nextToken() != ')') {
			t.pushBack();
			if (first) first = false;
			else expect(',');
			vargs.addElement(parseExpr(scope));
		}
		// add default argument values
		if (vargs.size() < ftype.args.length && fload instanceof ConstExpr) {
			Func f = (Func) ((ConstExpr)fload).value;
			for (int i=vargs.size(); i < ftype.args.length; i++) {
				Var v = (Var) f.locals.elementAt(i);
				if (v.constValue != null) vargs.addElement(new ConstExpr(-1, v.constValue));
			}
		}
		if (ftype.args.length != vargs.size()) {
			if (fload instanceof ConstExpr) {
				Func f = (Func) ((ConstExpr)fload).value;
				throw new ParseException("Wrong number of arguments in call to "+f.signature+"()");
			} else {
				throw new ParseException("Wrong number of arguments in function call");
			}
		}
		// cast arguments to needed types
		Expr[] args = new Expr[vargs.size()];
		for (int i=0; i<args.length; i++) {
			args[i] = cast((Expr)vargs.elementAt(i), ftype.args[i]);
		}
		// special processing for some functions
		if (fload instanceof ConstExpr) {
			Func f = (Func) ((ConstExpr)fload).value;
			if (f.signature.equals("Function.curry") && args[0].rettype() instanceof FunctionType) {
				return makeCurry(args[0], args[1]);
			} else if (f.signature.equals("Structure.clone") && args[0].rettype() instanceof StructureType) {
				return new CastExpr(args[0].rettype(), new FCallExpr(fload, args));
			} else if (f.signature.equals("acopy") && args[2].rettype() instanceof ArrayType) {
				ArrayType toarray = (ArrayType)args[2].rettype();
				if (args[0].rettype() instanceof ArrayType) {
					ArrayType fromarray = (ArrayType)args[0].rettype();
					if (toarray.elementType().isSubtypeOf(fromarray.elementType())
						&& !toarray.elementType().equals(fromarray.elementType())) {
						warn(W_TYPESAFE, "Unsafe type cast when copying from "+fromarray+" to "+toarray);
					} else if (!toarray.elementType().isSupertypeOf(fromarray.elementType())) {
						throw new ParseException("Cast to the incompatible type when copying from "+fromarray+" to "+toarray);
					}
				} else if (toarray.elementType() != BuiltinType.ANY) {
					warn(W_TYPESAFE, "Unsafe type cast when copying from Array to "+toarray);
				}
			} else if (f.signature.equals("StrBuf.append") && args[1].rettype().equals(BuiltinType.CHAR)) {
				f.hits--;
				Func addch = unit.getFunc("StrBuf.addch");
				addch.hits++;
				return new FCallExpr(new ConstExpr(fload.lineNumber(), addch), args);
			} else if (f.signature.equals("StrBuf.insert") && args[2].rettype().equals(BuiltinType.CHAR)) {
				f.hits--;
				Func insch = unit.getFunc("StrBuf.insch");
				insch.hits++;
				return new FCallExpr(new ConstExpr(fload.lineNumber(), insch), args);
			} else if ((f.signature.equals("print") || f.signature.equals("println")) &&
					!args[0].rettype().equals(BuiltinType.STRING)) {
				Func tostr = findMethod(args[0].rettype(), "tostr");
				tostr.hits++;
				args[0] = new FCallExpr(new ConstExpr(fload.lineNumber(), tostr), new Expr[] { args[0] });
			} else if ((f.signature.equals("OStream.print") || f.signature.equals("OStream.println")) &&
					!args[1].rettype().equals(BuiltinType.STRING)) {
				Func tostr = findMethod(args[0].rettype(), "tostr");
				tostr.hits++;
				args[1] = new FCallExpr(new ConstExpr(fload.lineNumber(), tostr), new Expr[] { args[1] });
			}
		}
		return new FCallExpr(fload, args);
	}
	
	/**
	 * Parses expression part after '['.
	 */
	private Expr parseBrackets(Scope scope, Expr arexpr) throws IOException, ParseException {
		int lnum = t.lineNumber();
		Type artype = arexpr.rettype();
		if (t.nextToken() == ':') { // range with implicit start
			if (t.nextToken() == ']') {
				return arexpr;
			} else {
				t.pushBack();
				Expr endexpr = cast(parseExpr(scope), BuiltinType.INT);
				expect(']');
				Func method = findMethod(artype, "range");
				if (method == null || method.type.args.length != 3 ||
				   method.type.args[1] != BuiltinType.INT || method.type.args[2] != BuiltinType.INT)
						throw new ParseException("Operator [:] cannot be applied to " + artype);
				Expr startexpr = new ConstExpr(lnum, Int.ZERO);
				method.hits++;
				return new FCallExpr(new ConstExpr(lnum, method), new Expr[] {arexpr, startexpr, endexpr});
			}
		} else {
			t.pushBack();
			Expr indexexpr = parseExpr(scope);
			if (t.nextToken() == ':') {
				if (t.nextToken() == ']') { // range with implicit end
					Func method = findMethod(artype, "len");
					if (method == null || method.type.args.length != 1 || method.type.rettype != BuiltinType.INT)
						throw new ParseException("Operator [:] cannot be applied to " + artype + ", no suitable len()");
					method.hits++;
					Expr endexpr = new FCallExpr(new ConstExpr(lnum, method), new Expr[] {arexpr});
					method = findMethod(artype, "range");
					if (method == null || method.type.args.length != 3 ||
					   method.type.args[1] != BuiltinType.INT || method.type.args[2] != BuiltinType.INT)
						throw new ParseException("Operator [:] cannot be applied to " + artype);
					indexexpr = cast(indexexpr, BuiltinType.INT);
					method.hits++;
					return new FCallExpr(new ConstExpr(lnum, method), new Expr[] {arexpr, indexexpr, endexpr});
				} else {
					t.pushBack();
					Expr endexpr = cast(parseExpr(scope), BuiltinType.INT);
					expect(']');
					Func method = findMethod(artype, "range");
					if (method == null || method.type.args.length != 3 ||
					   method.type.args[1] != BuiltinType.INT || method.type.args[2] != BuiltinType.INT)
						throw new ParseException("Operator [:] cannot be applied to " + artype);
					method.hits++;
					return new FCallExpr(new ConstExpr(lnum, method), new Expr[] {arexpr, indexexpr, endexpr});
				}
			} else { // not a range
				t.pushBack();
				expect(']');
				if (artype instanceof ArrayType) {
					// array getter or setter
					indexexpr = cast(indexexpr, BuiltinType.INT);
					Type eltype = ((ArrayType)artype).elementType();
					Expr getexpr = new ALoadExpr(arexpr, indexexpr, eltype);
					if (Token.isAssignment(t.nextToken())) {
						int assignop = t.ttype;
						Expr rexpr = cast(makeAssignRval(getexpr, assignop, parseExpr(scope)), eltype);
						if ((Xmask & X_ARRAYOPT) != 0 && assignop != '=' && rexpr instanceof BinaryExpr) {
							// in this case we can produce more optimized code
							final BinaryExpr bin = (BinaryExpr)rexpr;
							return new AChangeExpr(arexpr, indexexpr, eltype, bin.operator, bin.rvalue);
						} else {
							return new AStoreExpr(arexpr, indexexpr, rexpr);
						}
					} else {
						t.pushBack();
						return getexpr;
					}
				} else {
					// get() or set() function
					int operator = t.nextToken();
					Func getmethod;
					Expr getexpr = null;
					if (operator != '=') {
						getmethod = findMethod(artype, "get");
						if (getmethod == null || getmethod.type.args.length != 2)
							throw new ParseException("Operator [] cannot be applied to " + artype);
						indexexpr = cast(indexexpr, getmethod.type.args[1]);
						getmethod.hits++;
						getexpr = new FCallExpr(new ConstExpr(lnum, getmethod), new Expr[] {arexpr, indexexpr});
					}
					if (Token.isAssignment(operator)) {
						Func setmethod = findMethod(artype, "set");
						if (setmethod == null || setmethod.type.args.length != 3)
							throw new ParseException("Operator []= cannot be applied to " + artype);
						indexexpr = cast(indexexpr, setmethod.type.args[1]);
						Expr rexpr = cast(makeAssignRval(getexpr, operator, parseExpr(scope)), setmethod.type.args[2]);
						setmethod.hits++;
						return new FCallExpr(new ConstExpr(lnum, setmethod), new Expr[] {arexpr, indexexpr, rexpr});
					} else {
						t.pushBack();
						return getexpr;
					}
				}
			}
		}
	}
	
	private Func findMethod(Type ownertype, String name) throws ParseException {
		Type stype = ownertype;
		while (stype != null) {
			Var mvar = unit.getVar(stype.toString()+'.'+name);
			if (mvar != null) {
				if (mvar.isConst && mvar.constValue instanceof Func) {
					return (Func) mvar.constValue;
				} else {
					throw new ParseException("Cannot use variable " + mvar.name + " as method");
				}
			}
			stype = stype.superType();
		}
		return null;
	}
	
	private Expr parseDot(Scope scope, Expr expr) throws IOException, ParseException {
		int lnum = t.lineNumber();
		// parse value.cast(Type)
		if (t.nextToken() == Token.KEYWORD && t.svalue.equals("cast")) {
			expect('(');
			Type toType = parseType(scope);
			expect(')');
			if (toType.equals(expr.rettype())) {
				warn(W_CAST, "Unnecessary cast to the same type");
				return expr;
			}
			if (toType.isSupertypeOf(expr.rettype())) {
				warn(W_CAST, "Unnecessary cast to the supertype");
				return expr;
			}
			if (expr.rettype().isSupertypeOf(toType)) {
				return new CastExpr(toType, expr);
			}
			return cast(expr, toType);
		}
		if (t.ttype != Token.WORD && t.ttype != Token.KEYWORD)
			throw new ParseException("Identifier expected after '.'");
		String member = t.svalue;
		Type type = expr.rettype();
		if (type instanceof NamedType && type.superType() == null)
			type = unit.getType(type.toString());
		if (type instanceof ArrayType) {
			if (member.equals("len")) {
				return new ALenExpr(expr);
			}
		} else if (type instanceof StructureType) {
			Var[] fields = ((StructureType)type).fields;
			int index = -1;
			for (int i=0; i<fields.length; i++) {
				if (fields[i].name.equals(member)) {
					index = i;
					break;
				}
			}
			if (index >= 0) {
				ConstExpr indexexpr = new ConstExpr(lnum, Int.toInt(index));
				ALoadExpr ldexpr = new ALoadExpr(expr, indexexpr, fields[index].type);
				if (Token.isAssignment(t.nextToken())) {
					int assignop = t.ttype;
					Expr rexpr = cast(makeAssignRval(ldexpr, assignop, parseExpr(scope)), fields[index].type);
					if ((Xmask & X_ARRAYOPT) != 0 && rexpr instanceof BinaryExpr && ((BinaryExpr)rexpr).lvalue == ldexpr) {
						// in this case we can produce more optimized code
						final BinaryExpr bin = (BinaryExpr)rexpr;
						return new AChangeExpr(expr, indexexpr, fields[index].type, bin.operator, bin.rvalue);
					} else {
						return new AStoreExpr(expr, indexexpr, rexpr);
					}
				} else {
					t.pushBack();
					return ldexpr;
				}
			}
		}
		// neither Array.len nor structure field
		// trying to find method
		Func method = findMethod(type, member);
		if (method != null) {
			method.hits++;
			if (t.nextToken() == '(') {
				// applying method
				return parseFCall(scope, new ConstExpr(lnum, method), expr);
			} else {
				t.pushBack();
				// creating partially applied function
				return makeCurry(new ConstExpr(lnum, method), expr);
			}
		}
		// no such method, trying getter and setter
		Func setter = findMethod(type, "set_" + member);
		Func getter = findMethod(type, "get_" + member);
		Expr getexpr = null;
		int operator = t.nextToken();
		if (operator != '=') {
			if (getter == null)
				throw new ParseException("Getter for " + type + "." + member + " not found.");
			if (getter.type.args.length != 1)
				throw new ParseException("Getter for " + type + "." + member + " must accept no arguments");
			getter.hits++;
			getexpr = new FCallExpr(new ConstExpr(lnum, getter), new Expr[] { expr });
		}
		if (Token.isAssignment(operator)) {
			if (setter == null)
				throw new ParseException("Setter for " + type + "." + member + " not found.");
			if (setter.type.args.length != 2)
				throw new ParseException("Setter for " + type + "." + member + " must accept one argument");
			setter.hits++;
			Expr setexpr = cast(makeAssignRval(getexpr, operator, parseExpr(scope)), setter.type.args[1]);
			return new FCallExpr(new ConstExpr(lnum, setter), new Expr[] { expr, setexpr });
		} else {
			t.pushBack();
			return getexpr;
		}
	}

	private Expr parseBlock(Scope scope) throws ParseException, IOException {
		BlockExpr block = new BlockExpr(scope);
		Expr lastexpr = null;
		while (t.nextToken() != '}') {
			if (t.ttype == ';') continue;
			t.pushBack();
			lastexpr = parseExpr(block);
			if (lastexpr.rettype() == BuiltinType.NONE)
				block.exprs.addElement(lastexpr);
			else
				block.exprs.addElement(new DiscardExpr(lastexpr));
		}
		if (block.exprs.isEmpty()) {
			return new NoneExpr();
		} else {
			//not to discard value of last expression
			block.exprs.setElementAt(lastexpr, block.exprs.size()-1);
			return block;
		}
	}
	
	/** Checks if given operator can be applied to given expressions
	 * and returns binary expression.
	 */
	private Expr makeBinaryExpr(Expr left, int op, Expr right) throws ParseException {
		Type ltype = left.rettype();
		Type rtype = right.rettype();
		Type btype = binaryCastType(ltype, rtype);
		if (btype == BuiltinType.NONE)
			throw new ParseException("Operator "+opstring(op)+" cannot be applied to "+ltype+","+rtype);
		// if built-in operators apply, return them
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
				if (ltype.isSubtypeOf(BuiltinType.NUMBER) && rtype.isSubtypeOf(BuiltinType.NUMBER)) {
					return new ComparisonExpr(cast(left,btype), op, cast(right,btype));
				}
				Func cmpmethod = findMethod(ltype, "cmp");
				if (cmpmethod != null && cmpmethod.type.rettype == BuiltinType.INT &&
						cmpmethod.type.args.length == 2 && cmpmethod.type.args[1].isSupertypeOf(rtype)) {
					cmpmethod.hits++;
					Expr fcall = new FCallExpr(new ConstExpr(left.lineNumber(), cmpmethod), new Expr[] {left, right});
					return new ComparisonExpr(fcall, op, new ConstExpr(-1, Int.ZERO));
				}
				break;
			case Token.EQEQ:
			case Token.NOTEQ:
				if (btype == BuiltinType.ANY && ltype != BuiltinType.ANY && rtype != BuiltinType.ANY) {
					throw new ParseException("Incomparable types " + ltype + " and " + rtype);
				}
				// if (left == null) right == null else left.eq(right)
				if (ltype != BuiltinType.NULL && rtype != BuiltinType.NULL) {
					Func eqmethod = findMethod(ltype, "eq");
					if (eqmethod != null && eqmethod.type.rettype == BuiltinType.BOOL &&
							eqmethod.type.args.length == 2 && eqmethod.type.args[1].isSupertypeOf(rtype)) {
						eqmethod.hits++;
						Expr fcall = new FCallExpr(new ConstExpr(left.lineNumber(), eqmethod), new Expr[] {left, right});
						Expr nullCmp = new ComparisonExpr(left, Token.EQEQ, new ConstExpr(-1, Null.NULL));
						if (op == Token.EQEQ) {
							return new IfExpr(
									nullCmp,
									new ComparisonExpr(right, Token.EQEQ, new ConstExpr(-1, Null.NULL)),
									fcall);
						} else {
							return new IfExpr(
									nullCmp,
									new ComparisonExpr(right, Token.NOTEQ, new ConstExpr(-1, Null.NULL)),
									new UnaryExpr('!', fcall));
						}
					}
				}
				return new ComparisonExpr(cast(left,btype), op, cast(right,btype));
			case Token.AMPAMP: {
				if (ltype != BuiltinType.BOOL || rtype != BuiltinType.BOOL)
					throw new ParseException("Operator "+opstring(op)+" cannot be applied to "+ltype+","+rtype);
				return new IfExpr(left, right, new ConstExpr(-1, Boolean.FALSE));
			}
			case Token.BARBAR: {
				if (ltype != BuiltinType.BOOL || rtype != BuiltinType.BOOL)
					throw new ParseException("Operator "+opstring(op)+" cannot be applied to "+ltype+","+rtype);
				return new IfExpr(left, new ConstExpr(-1, Boolean.TRUE), right);
			}
			case '+':
			case '-':
			case '*':
			case '/':
			case '%':
				if (ltype == BuiltinType.STRING && op == '+' && rtype != BuiltinType.NONE) {
					// string concatenation
					if (rtype != BuiltinType.CHAR) {
						Func tostr = findMethod(rtype, "tostr");
						if (!tostr.signature.equals("Any.tostr") && tostr.type.rettype == BuiltinType.STRING && tostr.type.args.length == 1) {
							tostr.hits++;
							right = new FCallExpr(new ConstExpr(left.lineNumber(), tostr), new Expr[] { right });
						}
					}
					if (left instanceof ConcatExpr) {
						((ConcatExpr)left).exprs.addElement(right);
						return left;
					} else {
						ConcatExpr cexpr = new ConcatExpr();
						cexpr.exprs.addElement(left);
						cexpr.exprs.addElement(right);
						return cexpr;
					}
				} else if (btype.isSubtypeOf(BuiltinType.NUMBER)) {
					return new BinaryExpr(cast(left,btype), op, cast(right,btype));
				}
				break;
			case '^':
			case '&':
			case '|': {
				if (btype == BuiltinType.BOOL || btype == BuiltinType.INT || btype == BuiltinType.LONG) {
					return new BinaryExpr(cast(left,btype), op, cast(right,btype));
				}
			}
		}
		// searching method that overloads operator
		String methodname = null;
		switch (op) {
			case Token.LTLT: methodname = "shl"; break;
			case Token.GTGT: methodname = "shr"; break;
			case Token.GTGTGT: methodname = "ushr"; break;
			case '+': methodname = "add"; break;
			case '-': methodname = "sub"; break;
			case '*': methodname = "mul"; break;
			case '/': methodname = "div"; break;
			case '%': methodname = "mod"; break;
			case '^': methodname = "xor"; break;
			case '&': methodname = "and"; break;
			case '|': methodname = "or"; break;
		}
		Func method = null;
		if (methodname != null) method = findMethod(ltype, methodname);
		if (method != null && method.type.args.length == 2 && method.type.args[1].isSupertypeOf(rtype)) {
			method.hits++;
			return new FCallExpr(new ConstExpr(left.lineNumber(), method), new Expr[] { left, right });
		}
		throw new ParseException("Operator "+opstring(op)+" cannot be applied to "+ltype+","+rtype);
	}

	private Expr makeCurry(Expr fload, Expr argument) throws ParseException {
		Func curry = unit.getFunc("Function.curry");
		curry.hits++;
		FCallExpr expr = new FCallExpr(new ConstExpr(fload.lineNumber(), curry), new Expr[] { fload, argument });
		if (!(fload.rettype() instanceof FunctionType)) {
			warn(W_TYPESAFE, "Function.curry is not type safe since actual function type is unknown");
		}
		// extra special for f.curry
		if (fload instanceof ConstExpr
				&& ((Func)((ConstExpr)fload).value).signature.equals("Function.curry")
				&& argument.rettype() instanceof FunctionType) {
			FunctionType ftype = (FunctionType)argument.rettype();
			if (ftype.args.length == 0)
				throw new ParseException("Cannot curry function that takes no arguments");
			FunctionType redftype = new FunctionType(ftype.rettype, new Type[ftype.args.length-1]);
			System.arraycopy(ftype.args, 1, redftype.args, 0, redftype.args.length);
			FunctionType newftype = new FunctionType(redftype, new Type[] { ftype.args[0] });
			return new CastExpr(newftype, expr);
		}
		// testing whether function accepts arguments
		FunctionType oldftype = (FunctionType)fload.rettype();
		if (oldftype.args.length == 0)
			throw new ParseException("Cannot curry function that takes no arguments");
		// testing whether the second argument can be accepted
		try {
			cast(argument, oldftype.args[0]);
		} catch (ParseException pe) {
			throw new ParseException("Cannot curry with given argument: "+pe.getMessage());
		}
		// creating new type
		FunctionType newftype = new FunctionType(oldftype.rettype, new Type[oldftype.args.length-1]);
		System.arraycopy(oldftype.args, 1, newftype.args, 0, newftype.args.length);
		return new CastExpr(newftype, expr);
	}
	
	private Expr makeAssignRval(Expr get, int operator, Expr right) throws ParseException {
		if (operator == '=') {
			return right;
		} else {
			return makeBinaryExpr(get, Token.getAssignOperator(operator), right);
		}
	}

	/**
	 * Casts expression to the specified type.
	 */
	private Expr cast(Expr expr, Type toType) throws ParseException {
		Type fromType = expr.rettype();
		if (fromType == BuiltinType.NONE) {
			if (toType == BuiltinType.NONE) return expr;
			else throw new ParseException("Cannot convert from <none> to "+toType);
		}
		if (toType == BuiltinType.NONE) {
			return new DiscardExpr(expr);
		}
		if (fromType instanceof NamedType && fromType.superType() == null) {
			fromType = unit.getType(fromType.toString());
		}
		if (toType instanceof NamedType && toType.superType() == null) {
			toType = unit.getType(toType.toString());
		}
		if (toType.isSupertypeOf(fromType) || toType == BuiltinType.ANY) {
			return expr;
		}
		if (toType.isSubtypeOf(fromType)) {
			warn(W_TYPESAFE, "Unsafe type cast from "+fromType+" to "+toType);
			return new CastExpr(toType, expr);
		}
		if (fromType.isSubtypeOf(BuiltinType.NUMBER) && toType.isSubtypeOf(BuiltinType.NUMBER)) {
			return new CastExpr(toType, expr);
		}
		if (expr instanceof NewArrayByEnumExpr && fromType instanceof ArrayType && toType instanceof ArrayType) {
			Type fromElemType = ((ArrayType)fromType).elementType();
			Type toElemType = ((ArrayType)toType).elementType();
			if (toElemType.isSupertypeOf(fromElemType) || ((NewArrayByEnumExpr)expr).initializers.length == 0)
				return new NewArrayByEnumExpr(expr.lineNumber(), toType, ((NewArrayByEnumExpr)expr).initializers);
		}
		throw new ParseException("Cannot convert from "+fromType+" to "+toType);
	}
	
	/**
	 * Computes return type of binary operator.
	 */
	private Type binaryCastType(Type ltype, Type rtype) {
		if (ltype == BuiltinType.NULL) return rtype;
		if (rtype == BuiltinType.NULL) return ltype;
		if (ltype.isSubtypeOf(BuiltinType.NUMBER) && rtype.isSubtypeOf(BuiltinType.NUMBER)) {
			Type ctype = BuiltinType.INT;
			if (ltype == BuiltinType.DOUBLE || rtype == BuiltinType.DOUBLE)
				ctype = BuiltinType.DOUBLE;
			else if (ltype == BuiltinType.FLOAT || rtype == BuiltinType.FLOAT)
				ctype = BuiltinType.FLOAT;
			else if (ltype == BuiltinType.LONG || rtype == BuiltinType.LONG)
				ctype = BuiltinType.LONG;
			return ctype;
		}
		return Type.commonSupertype(ltype, rtype);
	}

	/** Reads next token and if it is not the given character, throws exception. */
	private void expect(char ttype) throws ParseException, IOException {
		if (t.nextToken() != ttype) {
			throw new ParseException("Expected '"+String.valueOf(ttype)+"', got "+t);
		}
	}

	/** Returns operator string by ttype. */
	private String opstring(int ttype) {
		if (ttype > 0) return String.valueOf((char)ttype);
		switch (ttype) {
			case Token.EQEQ: return "==";
			case Token.GTEQ: return ">=";
			case Token.GTGT: return ">>";
			case Token.GTGTGT: return ">>>";
			case Token.LTEQ: return "<=";
			case Token.LTLT: return "<<";
			case Token.NOTEQ: return "!=";
			case Token.AMPAMP: return "&&";
			case Token.BARBAR: return "||";
			default: return String.valueOf(ttype);
		}
	}

	/** Prints warning on stderr. */
	private void warn(int category, String msg) {
		if (category == W_ERROR || ((1 << category) & Wmask) != 0) {
			StringBuffer output = new StringBuffer();
			output.append(files.peek()).append(':').append(t.lineNumber());
			if (category == W_ERROR) output.append(": [Error]");
			else output.append(": [Warning ").append(WARN_STRINGS[category]).append(']');
			output.append("\n ").append(msg);
			IO.println(p.stderr, output);
		}
	}
}
