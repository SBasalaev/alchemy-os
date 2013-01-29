/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011-2013, Sergey Basalaev <sbasalaev@gmail.com>
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

import alchemy.core.Context;
import alchemy.core.types.Int;
import alchemy.fs.FSManager;
import alchemy.fs.Filesystem;
import alchemy.nec.tree.*;
import alchemy.util.IO;
import alchemy.util.UTFReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;
import java.util.Vector;

/**
 * Parses Ether language.
 * @author Sergey Basalaev
 */
public class Parser {
	
	// Warning categories
	private static final int W_ERROR = -1;
	private static final int W_TYPESAFE = 0;
	private static final int W_MAIN = 1;
	private static final int W_CAST = 2;
	private static final int W_VARS = 3;
	private static final int W_DEPRECATED = 4;
	
	static final String[] WARN_STRINGS = {"typesafe", "main", "cast", "vars", "deprecated"};

	// eXperimental feature categories
	private static final int X_TRY = 1;
	
	static final String[] X_STRINGS = {"try"};
	
	private final Context c;
	private final int target;
	private Tokenizer t;
	private Unit unit;
	private int Wmask; /* Enabled warnings. */
	private int Xmask; /* Enabled experimental features. */

	/** Files in the process of parsing. */
	private Stack files = new Stack();
	/** Files that are already parsed. */
	private Vector parsed = new Vector();
	
	public Parser(Context c, int target, int Wmask, int Xmask) {
		this.c = c;
		this.target = target;
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
			parseFile("/res/nec/embed.eh");
			// parsing
			parseFile(source);
		} catch (ParseException pe) {
			warn(W_ERROR, pe.getMessage());
			return null;
		} catch (IOException ioe) {
			IO.println(c.stderr, "I/O error: "+ioe);
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
		String f = c.toFile(name);
		if (FSManager.fs().exists(f)) return f;
		f = c.toFile(name+".eh");
		if (FSManager.fs().exists(f)) return f;
		if (name.charAt(0) != '/') {
			String[] incpath = IO.split(c.getEnv("INCPATH"), ':');
			for (int i=0; i<incpath.length; i++) {
				f = c.toFile(incpath[i]+'/'+name);
				if (FSManager.fs().exists(f)) return f;
				f = c.toFile(incpath[i]+'/'+name+".eh");
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
		String olddir = c.getCurDir();
		files.push(file);
		c.setCurDir(Filesystem.fparent(file));
		InputStream in = FSManager.fs().read(file);
		c.addStream(in);
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
			} else if (t.svalue.equals("const")) {
				if (t.nextToken() != Token.IDENTIFIER)
					throw new ParseException("Constant name expected after 'const'");
				String name = t.svalue;
				expect('=');
				Expr expr = (Expr)parseExpr(unit).accept(new Optimizer(), unit);
				if (!(expr instanceof ConstExpr))
					throw new ParseException("Constant expression expected");
				Var cnst = new Var(name, expr.rettype());
				cnst.isConst = true;
				cnst.constValue = ((ConstExpr)expr).value;
				unit.addVar(cnst);
			} else if (t.svalue.equals("type")) {
				if (t.nextToken() != Token.IDENTIFIER)
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
			} else if (t.svalue.equals("var")) {
				if (t.nextToken() != Token.IDENTIFIER)
					throw new ParseException("Variable name expected after 'var'");
				String varname = t.svalue;
				expect(':');
				Type vartype = parseType(unit);
				unit.addVar(new Var(varname, vartype));
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
						Expr body;
						if (t.ttype == '{') {
							body = parseBlock(fdef);
						} else {
							body = parseExpr(fdef);
						}
						fdef.body = cast(body, fdef.type.rettype);
						fdef.hits++;
						fdef.source = Filesystem.fname((String)files.peek());
						break;
					default:
						throw new ParseException(t.toString()+" unexpected here");
				}
			}
		}
		//move file to parsed
		in.close();
		c.removeStream(in);
		t = oldt;
		c.setCurDir(olddir);
		parsed.addElement(files.pop());
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
			if (t.nextToken() != Token.IDENTIFIER)
				throw new ParseException("Field name expected, got "+t);
			String fieldname = t.svalue;
			expect(':');
			Type vartype = parseType(unit);
			Var var = new Var(fieldname, vartype);
			var.index = fields.size();
			fields.addElement(var);
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
			case Token.IDENTIFIER: { //scalar type
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
	 * Parses definition of function (without the body).
	 */
	private Func parseFuncDef() throws ParseException, IOException {
		Func func = new Func(unit);
		//parsing def
		if (t.nextToken() != Token.IDENTIFIER)
			throw new ParseException("Function name expected, got "+t);
		String str = t.svalue;
		String fname;
		NamedType methodholder = null;
		if (t.nextToken() == '.') {
			methodholder = unit.getType(str);
			if (methodholder == null)
				throw new ParseException("Type "+str+" is not defined");
			if (t.nextToken() != Token.IDENTIFIER)
				throw new ParseException("Function name expected, got "+t);
			fname = methodholder.toString()+'.'+t.svalue;
		} else {
			t.pushBack();
			fname = str;
		}
		expect('(');
		Vector args = new Vector();
		if (methodholder != null) {
			args.addElement(new Var("this", methodholder));
		}
		boolean first = true;
		while (t.nextToken() != ')') {
			t.pushBack();
			if (first) first = false;
			else expect(',');
			if (t.nextToken() != Token.IDENTIFIER)
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
		func.signature = fname;
		func.type = ftype;
		//some checkings
		if (fname.equals("main")) {
			if (args.size() != 1) {
				warn(W_MAIN, "Incorrect number of arguments in main(), should be ([String])");
			} else {
				Type argtype = ((Var)args.elementAt(0)).type;
				Type shouldbe = new ArrayType(BuiltinType.STRING);
				if (!argtype.isSupertypeOf(shouldbe)) {
					throw new ParseException("Incompatible argument type in main()");
				}
				if (!argtype.equals(shouldbe)) {
					warn(W_MAIN, "argument of main() should be of type [String]");
				}
			}
			if (rettype != BuiltinType.INT && rettype != BuiltinType.NONE) {
				warn(W_MAIN, "Incompatible return type in main(), should be Int or <none>.");
			}
		}
		return func;
	}
	
	/**
	 * Binary operators arranged by priority. In groups of four.
	 */
	private static int[] priorops = {
			// if word operators are to appear they have the lowest priority
			Token.KEYWORD, Token.IDENTIFIER, 0, 0,
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
			if (opchar == ';') break;
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
				int p = getPriority((Int)operators.elementAt(i));
				if (p > priority) {
					priority = p;
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
			case ';':
				return new NoneExpr();
			case '+':
			case '-': {
				Expr sub = parsePostfix(scope, parseExprNoop(scope));
				Type type = sub.rettype();
				if (!type.isSubtypeOf(BuiltinType.NUMBER))
					throw new ParseException("Operator "+(char)ttype+" cannot be applied to "+type);
				return new UnaryExpr(ttype, sub);				
			}
			case '~': {
				Expr sub = parsePostfix(scope, parseExprNoop(scope));
				Type type = sub.rettype();
				if (type != BuiltinType.INT && type != BuiltinType.LONG)
					throw new ParseException("Operator "+(char)ttype+" cannot be applied to "+type);
				return new UnaryExpr(ttype, sub);				
			}
			case '!': {
				Expr sub = parsePostfix(scope, parseExprNoop(scope));
				Type type = sub.rettype();
				if (type != BuiltinType.BOOL)
					throw new ParseException("Operator "+(char)ttype+" cannot be applied to "+type);
				return new UnaryExpr(ttype, sub);				
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
			case Token.IDENTIFIER: {
				String str = t.svalue;
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
					Func getstatic = unit.getFunc("getstatic");
					getstatic.hits++;
					vexpr = new CastExpr(
							var.type,
							new FCallExpr(new ConstExpr(lnum, getstatic),
							new Expr[] { new ConstExpr(lnum, var.name+'#'+Integer.toHexString(var.hashCode())) }));
				}
				if (Token.isAssignment(t.nextToken())) {
					if (var.isConst)
						throw new ParseException("Cannot assign to constant "+var.name);
					int operator = t.ttype;
					Expr value = cast(makeAssignRval(vexpr, operator, parseExpr(scope)), var.type);
					if (scope.isLocal(var.name)) {
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
				//warn(W_TYPESAFE, "Unsafe type cast from "+expr.rettype()+" to "+toType);
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
			if (indexexpr.rettype() != BuiltinType.INT)
				throw new ParseException("Index of switch must be Int");
			expect(')');
			expect('{');
			// parsing switch body
			Expr elseexpr = null;
			Vector keys = new Vector(); // int[]
			Vector keysunique = new Vector(); // Int
			Vector exprs = new Vector(); // Expr
			while (t.nextToken() != '}') {
				if (t.ttype == Token.KEYWORD && t.svalue.equals("else")) {
					if (elseexpr != null)
						throw new ParseException("else branch is already defined in this switch");
					expect(':');
					elseexpr = parseExpr(scope);
				} else {
					Vector branchkeyv = new Vector();
					do {
						t.pushBack();
						if (!branchkeyv.isEmpty()) expect(',');
						Expr branchindex = (Expr)parseExpr(scope).accept(new Optimizer(), scope);
						if (!(branchindex instanceof ConstExpr))
							throw new ParseException("Constant expression expected in switch key");
						if (branchindex.rettype() != BuiltinType.INT)
							throw new ParseException("switch key is required to be integer");
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
			return swexpr;
		} else if (keyword.equals("var") || keyword.equals("const")) {
			boolean isConst = keyword.equals("const");
			if (t.nextToken() != Token.IDENTIFIER)
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
			if (scope.addVar(v)) {
				warn(W_VARS, "Variable "+v.name+" hides another variable with the same name");
			}
			if (varvalue != null) {
				return new AssignExpr(v, varvalue);
			} else {
				return new NoneExpr();
			}
		} else if (keyword.equals("new")) {
			Type type = parseType(scope);
			if (type instanceof StructureType) {
				StructureType struct = (StructureType)type;
				Expr[] init = new Expr[struct.fields.length];
				boolean first = true;
				switch (t.nextToken()) {
					case '{':
						while (t.nextToken() != '}') {
							t.pushBack();
							if (first) first = false;
							else expect(',');
							if (t.nextToken() != Token.IDENTIFIER)
								throw new ParseException("Identifier expected in structure constructor");
							int index = struct.fields.length-1;
							while (index >= 0 && !struct.fields[index].name.equals(t.svalue)) index--;
							if (index < 0)
								throw new ParseException("Type "+type+" has no member named "+t.svalue);
							expect('=');
							init[index] = cast(parseExpr(scope), struct.fields[index].type);
						}
						break;
					case '(':
						for (int i=0; i < init.length; i++) {
							if (first) first = false;
							else expect(',');
							init[i] = cast(parseExpr(scope), struct.fields[i].type);
						}
						expect(')');
						break;
					default:
						throw new ParseException("'(' or '{' expected in structure constructor");
				}
				return new NewArrayByEnumExpr(lnum, type, init);
			} else if (type instanceof ArrayType) {
				if (t.nextToken() == '(') {
					Expr lenexpr = cast(parseExpr(scope), BuiltinType.INT);
					expect(')');
					return new NewArrayExpr(lnum, type, lenexpr);
				} else if (t.ttype == '{') {
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
				throw new ParseException("Applying 'new' to neither array nor structure");
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
				if (t.nextToken() != Token.IDENTIFIER)
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
			if ((Xmask & X_TRY) == 0)
				throw new ParseException("Experimental try/catch support is disabled. Use -Xtry to enable it.");
			Expr tryexpr = parseExpr(scope);
			if (t.nextToken() != Token.KEYWORD || !t.svalue.equals("catch"))
				throw new ParseException("'catch' expected after 'try <expr>'");
			Var v = null;
			BlockExpr catchblock = new BlockExpr(scope);
			if (t.nextToken() == '(') {
				if (t.nextToken() != Token.KEYWORD || !t.svalue.equals("var"))
					throw new ParseException("'var' expected");
				if (t.nextToken() != Token.IDENTIFIER)
					throw new ParseException("Identifier expected");
				v = new Var(t.svalue, BuiltinType.ERROR);
				if (catchblock.addVar(v)) {
					warn(W_VARS, "Variable "+v.name+" hides another variable with the same name");
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
		} else {
			throw new ParseException(t.toString()+" unexpected here");
		}
	}
	
	/**
	 * Parses expression part after '(' (function application).
	 */
	private Expr parseFCall(Scope scope, Expr fload, Expr firstarg) throws IOException, ParseException {
		if (!(fload.rettype() instanceof FunctionType))
			throw new ParseException("Applying () to non-function expression");
		FunctionType ftype = (FunctionType)fload.rettype();
		Vector vargs = new Vector();
		if (firstarg != null) vargs.addElement(firstarg);
		boolean first = true;
		while (t.nextToken() != ')') {
			t.pushBack();
			if (first) first = false;
			else expect(',');
			vargs.addElement(parseExpr(scope));
		}
		if (ftype.args.length != vargs.size()) {
			if (fload instanceof ConstExpr) {
				Func f = (Func)((ConstExpr)fload).value;
				throw new ParseException("Wrong number of arguments in call to "+f.signature+"()");
			} else {
				throw new ParseException("Wrong number of arguments in function call");
			}
		}
		Expr[] args = new Expr[vargs.size()];
		for (int i=0; i<args.length; i++) {
			args[i] = cast((Expr)vargs.elementAt(i), ftype.args[i]);
		}
		if (fload instanceof ConstExpr) {
			return makeFCall(((ConstExpr)fload).line, (Func)((ConstExpr)fload).value, args);
		} else {
			return new FCallExpr(fload, args);
		}
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
				if (method == null)
					throw new ParseException("Method "+artype+".range not found");
				if (method.type.args.length != 3 ||
				   method.type.args[1] != BuiltinType.INT ||
				   method.type.args[2] != BuiltinType.INT)
					throw new ParseException("Method "+artype+".range must be (Int,Int) to use [] notation");
				Expr startexpr = new ConstExpr(lnum, Int.ZERO);
				method.hits++;
				return makeFCall(lnum, method, new Expr[] {arexpr, startexpr, endexpr});
			}
		} else {
			t.pushBack();
			Expr indexexpr = parseExpr(scope);
			if (t.nextToken() == ':') {
				if (t.nextToken() == ']') { // range with implicit end
					Func method = findMethod(artype, "len");
					if (method == null)
						throw new ParseException("Method "+artype+".len not found");
					if (method.type.args.length != 1 || method.type.rettype != BuiltinType.INT)
						throw new ParseException("Method "+artype+".len must be ():Int to use [] notation");
					method.hits++;
					Expr endexpr = makeFCall(lnum, method, new Expr[] {arexpr});
					method = findMethod(artype, "range");
					if (method == null)
						throw new ParseException("Method "+artype+".range not found");
					if (method.type.args.length != 3 ||
					   method.type.args[1] != BuiltinType.INT ||
					   method.type.args[2] != BuiltinType.INT)
						throw new ParseException("Method "+artype+".range must be (Int,Int) to use [] notation");
					indexexpr = cast(indexexpr, BuiltinType.INT);
					method.hits++;
					return makeFCall(lnum, method, new Expr[] {arexpr, indexexpr, endexpr});
				} else {
					t.pushBack();
					Expr endexpr = cast(parseExpr(scope), BuiltinType.INT);
					expect(']');
					Func method = findMethod(artype, "range");
					if (method == null)
						throw new ParseException("Method "+artype+".range not found");
					if (method.type.args.length != 3 ||
					   method.type.args[1] != BuiltinType.INT ||
					   method.type.args[2] != BuiltinType.INT)
						throw new ParseException("Method "+artype+".range must be (Int,Int) to use [] notation");
					method.hits++;
					return makeFCall(lnum, method, new Expr[] {arexpr, indexexpr, endexpr});
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
						Expr rexpr = makeAssignRval(getexpr, t.ttype, parseExpr(scope));
						return new AStoreExpr(arexpr, indexexpr, cast(rexpr, eltype));
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
						if (getmethod == null)
							throw new ParseException("Method "+artype+".get not found");
						if (getmethod.type.args.length != 2)
							throw new ParseException("Method "+artype+".get must accept one argument to use [] notation");
						indexexpr = cast(indexexpr, getmethod.type.args[1]);
						getmethod.hits++;
						getexpr = makeFCall(lnum, getmethod, new Expr[] {arexpr, indexexpr});
					}
					if (Token.isAssignment(operator)) {
						Func setmethod = findMethod(artype, "set");
						if (setmethod == null)
							throw new ParseException("Method "+artype+".set not found");
						if (setmethod.type.args.length != 3)
							throw new ParseException("Method "+artype+".set must accept two arguments to use [] notation");
						indexexpr = cast(indexexpr, setmethod.type.args[1]);
						Expr rexpr = cast(makeAssignRval(getexpr, operator, parseExpr(scope)), setmethod.type.args[2]);
						setmethod.hits++;
						return makeFCall(lnum, setmethod, new Expr[] {arexpr, indexexpr, rexpr});
					} else {
						t.pushBack();
						return getexpr;
					}
				}
			}
		}
	}
	
	private Func findMethod(Type type, String name) throws ParseException {
		Type stype = type;
		while (stype != null) {
			Var mvar = unit.getVar(stype.toString()+'.'+name);
			if (mvar != null) {
				if (mvar.isConst && mvar.constValue instanceof Func)
					return (Func)mvar.constValue;
				else
					throw new ParseException("Cannot use variable as method");
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
		if (t.ttype != Token.IDENTIFIER && t.ttype != Token.KEYWORD)
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
					Expr rexpr = cast(makeAssignRval(ldexpr, t.ttype, parseExpr(scope)), fields[index].type);
					return new AStoreExpr(expr, indexexpr, rexpr);
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
				return parseFCall(scope, new ConstExpr(lnum, method), expr);
			} else {
				t.pushBack();
				// creating partially applied function
				Func curry = unit.getFunc("Function.curry");
				curry.hits++;
				Expr[] args = new Expr[2];
				args[0] = new ConstExpr(lnum, method);
				args[1] = expr;
				return makeFCall(lnum, curry, args);
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
			getexpr = makeFCall(lnum, getter, new Expr[] { expr });
		}
		if (Token.isAssignment(operator)) {
			if (setter == null)
				throw new ParseException("Setter for " + type + "." + member + " not found.");
			if (setter.type.args.length != 2)
				throw new ParseException("Setter for " + type + "." + member + " must accept one argument");
			setter.hits++;
			Expr setexpr = cast(makeAssignRval(getexpr, operator, parseExpr(scope)), setter.type.args[1]);
			return makeFCall(lnum, setter, new Expr[] { expr, setexpr });
		} else {
			t.pushBack();
			return getexpr;
		}
	}

	private Expr parseBlock(Scope scope) throws ParseException, IOException {
		BlockExpr block = new BlockExpr(scope);
		Expr lastexpr = null;
		while (t.nextToken() != '}') {
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
		switch (op) {
			case Token.GTGT:
			case Token.LTLT:
			case Token.GTGTGT: {
				if (ltype == BuiltinType.BYTE || ltype == BuiltinType.SHORT || ltype == BuiltinType.CHAR) {
					ltype = BuiltinType.INT;
					left = cast(left, ltype);
				}
				if (rtype == BuiltinType.BYTE || rtype == BuiltinType.SHORT || rtype == BuiltinType.CHAR) {
					rtype = BuiltinType.INT;
					right = cast(right, ltype);
				}
				if (ltype != BuiltinType.INT && ltype != BuiltinType.LONG || rtype != BuiltinType.INT)
						throw new ParseException("Operator "+opstring(op)+" cannot be applied to "+ltype+","+rtype);
				return new BinaryExpr(left, op, right);
			}
			case '<':
			case '>':
			case Token.LTEQ:
			case Token.GTEQ: {
				if (!(ltype.isSubtypeOf(BuiltinType.NUMBER)) || !(rtype.isSubtypeOf(BuiltinType.NUMBER)))
					throw new ParseException("Operator "+opstring(op)+" cannot be applied to "+ltype+","+rtype);
				Type btype = binaryCastType(ltype, rtype);
				return new ComparisonExpr(cast(left,btype), op, cast(right,btype));
			}
			case Token.EQEQ:
			case Token.NOTEQ: {
				Type btype = binaryCastType(ltype, rtype);
				return new ComparisonExpr(cast(left,btype), op, cast(right,btype));
			}
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
			case '%': {
				if (ltype == BuiltinType.STRING && op == '+') {
					// string concatenation
					right = cast(right, BuiltinType.ANY);
					if (left instanceof ConcatExpr) {
						((ConcatExpr)left).exprs.addElement(right);
						return left;
					} else {
						ConcatExpr cexpr = new ConcatExpr();
						cexpr.exprs.addElement(left);
						cexpr.exprs.addElement(right);
						return cexpr;
					}
				} else {
					Type btype = binaryCastType(ltype, rtype);
					if (!(btype.isSubtypeOf(BuiltinType.NUMBER)))
						throw new ParseException("Operator "+opstring(op)+" cannot be applied to "+ltype+","+rtype);
					return new BinaryExpr(cast(left,btype), op, cast(right,btype));
				}
			}
			case '^':
			case '&':
			case '|': {
				Type btype = binaryCastType(ltype, rtype);
				if (btype != BuiltinType.BOOL && btype != BuiltinType.INT && btype != BuiltinType.LONG)
					throw new ParseException("Operator "+opstring(op)+" cannot be applied to "+ltype+","+rtype);
				return new BinaryExpr(cast(left,btype), op, cast(right,btype));
			}
			default: {
				//should not happen, but...
				throw new ParseException("There is a bug in compiler."
					+" Please report it with your source code and"
					+"the following error message: unusual operator "+opstring(op));
			}
		}
	}

	/**
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
	 * </dl>
	 */
	private Expr makeFCall(int lnum, Func f, Expr[] args) throws ParseException {
		FCallExpr expr = new FCallExpr(new ConstExpr(lnum, f), args);
		if (f.signature.equals("Function.curry") && args[0].rettype() instanceof FunctionType) {
			// extra special for f.curry
			if (args[0] instanceof ConstExpr
					&& ((Func)((ConstExpr)args[0]).value).signature.equals("Function.curry")
					&& args[1].rettype() instanceof FunctionType) {
				FunctionType ftype = (FunctionType)args[1].rettype();
				if (ftype.args.length == 0)
					throw new ParseException("Cannot curry function that takes no arguments");
				FunctionType redftype = new FunctionType(ftype.rettype, new Type[ftype.args.length-1]);
				System.arraycopy(ftype.args, 1, redftype.args, 0, redftype.args.length);
				FunctionType newftype = new FunctionType(redftype, new Type[] { ftype.args[0] });
				return new CastExpr(newftype, expr);
			}
			FunctionType oldftype = (FunctionType)args[0].rettype();
			if (oldftype.args.length == 0)
				throw new ParseException("Cannot curry function that takes no arguments");
			// testing whether the second argument can be accepted
			try {
				cast(args[1], oldftype.args[0]);
			} catch (ParseException pe) {
				throw new ParseException("Cannot curry with given argument: "+pe.getMessage());
			}
			// creating new type
			FunctionType newftype = new FunctionType(oldftype.rettype, new Type[oldftype.args.length-1]);
			System.arraycopy(oldftype.args, 1, newftype.args, 0, newftype.args.length);
			return new CastExpr(newftype, expr);
		} else if (f.signature.equals("Structure.clone") && args[0].rettype() instanceof StructureType) {
			return new CastExpr(args[0].rettype(), expr);
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
		}
		return expr;
	}
	
	private Expr makeAssignRval(Expr get, int operator, Expr right) throws ParseException {
		switch (operator) {
			case '=':
				return right;
			case Token.PLUSEQ:
				return makeBinaryExpr(get, '+', right);
			case Token.MINUSEQ:
				return makeBinaryExpr(get, '-', right);
			case Token.STAREQ:
				return makeBinaryExpr(get, '*', right);
			case Token.SLASHEQ:
				return makeBinaryExpr(get, '/', right);
			case Token.PERCENTEQ:
				return makeBinaryExpr(get, '%', right);
			case Token.BAREQ:
				return makeBinaryExpr(get, '|', right);
			case Token.AMPEQ:
				return makeBinaryExpr(get, '&', right);
			case Token.HATEQ:
				return makeBinaryExpr(get, '^', right);
			case Token.LTLTEQ:
				return makeBinaryExpr(get, Token.LTLT, right);
			case Token.GTGTEQ:
				return makeBinaryExpr(get, Token.GTGT, right);
			case Token.GTGTGTEQ:
				return makeBinaryExpr(get, Token.GTGTGT, right);
			default:
				throw new ParseException("Unexpected operator type "+operator);
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
			IO.println(c.stderr, output);
		}
	}
}
