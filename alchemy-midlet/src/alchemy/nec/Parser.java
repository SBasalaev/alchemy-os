/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011  Sergey Basalaev <sbasalaev@gmail.com>
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
import alchemy.fs.File;
import alchemy.util.I18N;
import alchemy.nec.tree.*;
import alchemy.util.IO;
import alchemy.util.UTFReader;
import java.io.IOException;
import java.util.Stack;
import java.util.Vector;

/**
 *
 * @author Sergey Basalaev
 */
public class Parser {

	private final Context c;
	private final int target;

	/** Files that are currently parsed. */
	private Stack files = new Stack();
	/** Files that are already parsed. */
	private Vector parsed = new Vector();

	private Tokenizer t;

	private Unit unit;

	public Parser(Context c, int target) {
		this.c = c;
		this.target = target;
	}

	public Unit parse(File source) {
		//initializing
		unit = new Unit();
		unit.putType("Any", BuiltinType.typeAny);
		unit.putType("Int", BuiltinType.typeInt);
		unit.putType("Long", BuiltinType.typeLong);
		unit.putType("Float", BuiltinType.typeFloat);
		unit.putType("Double", BuiltinType.typeDouble);
		unit.putType("Bool", BuiltinType.typeBool);
		unit.putType("String", BuiltinType.typeString);
		unit.putType("Array", BuiltinType.typeArray);
		unit.putType("BArray", BuiltinType.typeBArray);
		unit.putType("CArray", BuiltinType.typeCArray);
		//parsing
		try {
			parseFile(new File("/res/nec/embed.eh"));
			parseFile(source);
		} catch (ParseException pe) {
			error(pe.getMessage());
			return null;
		} catch (IOException ioe) {
			IO.println(c.stderr, I18N._("I/O error: {0}", ioe));
			return null;
		}
		//removing unused imports
		for (int i=unit.funcs.size()-1; i>=0; i--) {
			Func f = (Func)unit.funcs.elementAt(i);
			if (f.body == null && f.hits == 0) {
				unit.funcs.removeElementAt(i);
			}
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
	private File resolveFile(String name) throws ParseException {
		if (name.length() == 0) throw new ParseException(I18N._("Empty string in 'use'"));
		File f = c.toFile(name);
		if (c.fs().exists(f)) return f;
		f = c.toFile(name+".eh");
		if (c.fs().exists(f)) return f;
		if (name.charAt(0) != '/') {
			String[] incpath = IO.split(c.getEnv("INCPATH"), ':');
			for (int i=0; i<incpath.length; i++) {
				f = c.toFile(incpath[i]+'/'+name);
				if (c.fs().exists(f)) return f;
				f = c.toFile(incpath[i]+'/'+name+".eh");
				if (c.fs().exists(f)) return f;
			}
		}
		throw new ParseException(I18N._("Module not found: {0}", name));
	}

	private void parseFile(File file) throws ParseException, IOException {
		//do nothing if this file is already parsed
		if (parsed.contains(file)) return;
		//if file is in stack we have cyclic inclusion
		if (files.contains(file)) {
			StringBuffer sb = new StringBuffer(I18N._("Cyclic inclusion"));
			for (int i=0; i<files.size(); i++) {
				sb.append(I18N._("{0} from {1}", "\n", "")).append(files.elementAt(i));
			}
			throw new ParseException(sb.toString());
		}
		//push file in stack
		Tokenizer oldt = t;
		File olddir = c.getCurDir();
		files.push(file);
		c.setCurDir(file.parent());
		UTFReader fred = new UTFReader(c.fs().read(file));
		t = new Tokenizer(fred);
		//parse
		try {
			while (t.nextToken() != Tokenizer.TT_EOF) {
				switch (t.ttype) {
					case Tokenizer.TT_KEYWORD:
						if (t.svalue.equals("use")) {
							if (t.nextToken() != Tokenizer.TT_QUOTED)
								throw new ParseException(I18N._("String literal expected after 'use'"));
							File next = resolveFile(t.svalue);
							if (t.nextToken() != ';') t.pushBack();
							parseFile(next);
						} else if (t.svalue.equals("type")) {
							if (t.nextToken() != Tokenizer.TT_IDENTIFIER)
								throw new ParseException(I18N._("Type name expected after 'type'"));
							String alias = t.svalue;
							Type type = unit.getType(alias);
							if (type == null) {
								unit.putType(alias, new NamedType(alias));
							} else if (type.getClass() != NamedType.class) {
								throw new ParseException(I18N._("Type {0} is already defined", alias));
							}
							switch (t.nextToken()) {
								case ';': // forward declaration
									break;
								case '=': // type alias
									unit.putType(alias, parseType(unit));
									if (t.nextToken() != ';') t.pushBack();
									break;
								case '{': { // structure type
									StructureType struct = new StructureType(alias);
									Vector fields = new Vector();
									while (t.nextToken() != '}') {
										t.pushBack();
										if (!fields.isEmpty()) expect(',');
										if (t.nextToken() != Tokenizer.TT_IDENTIFIER)
											throw new ParseException(I18N._("Field name expected, got {0}", t));
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
									unit.putType(alias, struct);
									break;
								}
								default:
									throw new ParseException(I18N._("{0} unexpected here", t));
							}
						} else if (t.svalue.equals("def")) {
							Func fdef = parseFuncDef();
							Func prev = unit.getFunc(fdef.asVar.name);
							if (prev != null && !fdef.asVar.type.equals(prev.asVar.type))
								throw new ParseException(I18N._("Definition of function {0} conflicts with previous definition.", fdef.asVar.name));
							if (prev == null) unit.funcs.addElement(fdef);
							switch (t.nextToken()) {
								case ';':
									break;
								case '{':
								case '=':
									if (prev != null && prev.body != null)
										throw new ParseException(I18N._("Function {0} is already defined.", fdef.asVar.name));
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
									Type rettype = ((FunctionType)fdef.asVar.type).rettype;
									fdef.body = cast(body, rettype);
									// if exported increase hits
									if (fdef.asVar.name.charAt(0) != '_') fdef.hits++;
									break;
								default:
									throw new ParseException(I18N._("{0} unexpected here", t));
							}
						} else {
							throw new ParseException(I18N._("{0} unexpected here", t));
						}
						break;
					case ';':
						break;
					default:
						throw new ParseException(I18N._("{0} unexpected here", t));
				}
			}
		} finally {
			fred.close();
		}
		//move file to parsed
		t = oldt;
		c.setCurDir(olddir);
		parsed.addElement(files.pop());
	}

	private Func parseFuncDef() throws ParseException, IOException {
		Func func = new Func(unit);
		//parsing def
		if (t.nextToken() != Tokenizer.TT_IDENTIFIER)
			throw new ParseException(I18N._("Function name expected, got {0}", t));
		String fname = t.svalue;
		expect('(');
		Vector args = new Vector();
		while (t.nextToken() != ')') {
			t.pushBack();
			if (!args.isEmpty()) expect(',');
			if (t.nextToken() != Tokenizer.TT_IDENTIFIER)
				throw new ParseException(I18N._("Variable name expected, got {0}", t));
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
			rettype = BuiltinType.typeNone;
		}
		//populating fields
		func.locals = args;
		FunctionType ftype = new FunctionType();
		ftype.rettype = rettype;
		ftype.args = new Type[args.size()];
		for (int i=args.size()-1; i>=0; i--) {
			ftype.args[i] = ((Var)args.elementAt(i)).type;
		}
		func.asVar = new Var(fname, ftype);
		//some checkings
		if (fname.equals("main")) {
			if (args.size() != 1) {
				warn(I18N._("Incorrect number of arguments in main()"));
			}
			if (args.size() > 0) {
				Var arg0 = (Var)args.elementAt(0);
				if (!arg0.type.equals(BuiltinType.typeAny) && !arg0.type.equals(BuiltinType.typeArray)) {
					warn(I18N._("Incompatible argument type in main()"));
				}
			}
			if (!rettype.equals(BuiltinType.typeInt) && !rettype.equals(BuiltinType.typeNone)) {
				warn(I18N._("Incompatible return type in main()"));
			}
		}
		return func;
	}

	/**
	 * Parses type expression.
	 */
	private Type parseType(Scope scope) throws ParseException, IOException {
		int ttype = t.nextToken();
		//scalar type or alias
		if (ttype == Tokenizer.TT_IDENTIFIER) {
			Type type = scope.getType(t.svalue);
			if (type == null) throw new ParseException(I18N._("Undefined type {0}", t));
			return type;
		}
		//function type
		if (ttype == '(') {
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
				rettype = BuiltinType.typeNone;
			}
			FunctionType type = new FunctionType();
			type.rettype = rettype;
			type.args = new Type[argtypes.size()];
			for (int i=argtypes.size()-1; i>=0; i--) {
				type.args[i] = (Type)argtypes.elementAt(i);
			}
			return type;
		}
		throw new ParseException(I18N._("{0} unexpected here", t));
	}

	private Expr parseBlock(Scope scope) throws ParseException, IOException {
		BlockExpr block = new BlockExpr(scope);
		Expr lastexpr = null;
		while (t.nextToken() != '}') {
			t.pushBack();
			lastexpr = parseExpr(block);
			if (lastexpr.rettype().equals(BuiltinType.typeNone))
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

	/**
	 * Binary operators arranged by priority. In groups of four.
	 */
	private static int[] priorops = {
			// if word operators are to appear they have the lowest priority
			Tokenizer.TT_KEYWORD, Tokenizer.TT_IDENTIFIER, 0, 0,
			'^', 0, 0, 0,
			Tokenizer.TT_BARBAR, '|', 0, 0,
			Tokenizer.TT_AMPAMP, '&', 0, 0,
			Tokenizer.TT_LTEQ, Tokenizer.TT_GTEQ, '<', '>',
			Tokenizer.TT_EQEQ, Tokenizer.TT_NOTEQ, 0, 0,
			Tokenizer.TT_LTLT, Tokenizer.TT_GTGT, 0, 0,
			'+', '-', 0, 0,
			'*', '/', '%', 0
		};

	private int getPriority(Integer operator) {
		int op = operator.intValue();
		for (int i=0; i<priorops.length; i++) {
			if (priorops[i] == op) return i/4;
		}
		return -1;
	}

	/**
	 * Parses sequence of expressions delimitered with operators.
	 */
	private Expr parseExpr(Scope scope) throws ParseException, IOException {
		Vector exprs = new Vector();
		Vector operators = new Vector();
		while (true) {
			exprs.addElement(parsePostfix(scope, parseExprNoop(scope)));
			int opchar = t.nextToken();
			if (opchar == ';') break;
			if ("+-*/%^&|<>".indexOf(opchar) >= 0 || opchar <= -20) {
				operators.addElement(new Integer(opchar));
			} else {
				t.pushBack();
				break;
			}
		}
		while (!operators.isEmpty()) {
			int index = 0;
			int priority = 0;
			for (int i = 0; i < operators.size(); i++) {
				int p = getPriority((Integer)operators.elementAt(i));
				if (p > priority) {
					priority = p;
					index = i;
				}
			}
			int op = ((Integer)operators.elementAt(index)).intValue();
			Expr left = (Expr)exprs.elementAt(index);
			Type ltype = left.rettype();
			Expr right = (Expr)exprs.elementAt(index+1);
			Type rtype = right.rettype();
			Expr newexpr;
			if (op == Tokenizer.TT_GTGT || op == Tokenizer.TT_LTLT || op == Tokenizer.TT_GTGTGT) {
				if (!ltype.equals(BuiltinType.typeInt) && !ltype.equals(BuiltinType.typeLong)
						|| !rtype.equals(BuiltinType.typeInt))
					throw new ParseException(I18N._("Operator {0} cannot be applied to {1},{2}", opstring(op), ltype, rtype));
				newexpr = new BinaryExpr(left, op, right);
			} else if (op == '<' || op == '>' || op == Tokenizer.TT_LTEQ || op == Tokenizer.TT_GTEQ) {
				if (!(ltype.getClass() == BuiltinType.class) || ((BuiltinType)ltype).numIndex() < 0
				  || !(rtype.getClass() == BuiltinType.class) || ((BuiltinType)rtype).numIndex() < 0)
					throw new ParseException(I18N._("Operator {0} cannot be applied to {1},{2}", opstring(op), ltype, rtype));
				Type btype = binaryCastType(ltype, rtype);
				Expr cmpexpr = new BinaryExpr(cast(left,btype), '=', cast(right,btype));
				int iftype;
				switch (op) {
					case '<':               iftype = IfExpr.NEG;    break;
					case '>':               iftype = IfExpr.POS;    break;
					case Tokenizer.TT_LTEQ: iftype = IfExpr.NOTPOS; break;
					case Tokenizer.TT_GTEQ: iftype = IfExpr.NOTNEG; break;
					default: iftype = -1;
				}
				newexpr = new IfExpr(cmpexpr, iftype, new ConstExpr(Boolean.TRUE), new ConstExpr(Boolean.FALSE));
			} else if (op == Tokenizer.TT_EQEQ || op == Tokenizer.TT_NOTEQ) {
				Type btype = binaryCastType(ltype, rtype);
				newexpr = new IfExpr(
						new BinaryExpr(cast(left,btype), '=', cast(right,btype)),
						op == Tokenizer.TT_EQEQ ? IfExpr.ZERO : IfExpr.NOTZERO,
						new ConstExpr(Boolean.TRUE), new ConstExpr(Boolean.FALSE));
			} else if (op == Tokenizer.TT_AMPAMP) {
				if (!ltype.equals(BuiltinType.typeBool) || !rtype.equals(BuiltinType.typeBool))
					throw new ParseException(I18N._("Operator && cannot be applied to {0},{1}", ltype, rtype));
				newexpr = new IfExpr(left, IfExpr.TRUE, right, new ConstExpr(Boolean.FALSE));
			} else if (op == Tokenizer.TT_BARBAR) {
				if (!ltype.equals(BuiltinType.typeBool) || !rtype.equals(BuiltinType.typeBool))
					throw new ParseException(I18N._("Operator || cannot be applied to {0},{1}", ltype, rtype));
				newexpr = new IfExpr(left, IfExpr.TRUE, new ConstExpr(Boolean.TRUE), right);
			} else if ("+-*/%".indexOf(op) >= 0) {
				if (ltype.equals(BuiltinType.typeString)) {
					// string concatenation, does strcat(left, to_str(right))
					if (!right.rettype().equals(BuiltinType.typeString)) {
						Func to_str = unit.getFunc("to_str");
						to_str.hits++;
						right = new FCallExpr(new ConstExpr(to_str), new Expr[] { cast(right, BuiltinType.typeAny) });
					}
					Func strcat = unit.getFunc("strcat");
					strcat.hits++;
					newexpr = new FCallExpr(new ConstExpr(strcat), new Expr[] { left, right });
				} else {
					Type btype = binaryCastType(ltype, rtype);
					if (!(btype.getClass() == BuiltinType.class) || ((BuiltinType)btype).numIndex() < 0)
						throw new ParseException(I18N._("Operator {0} cannot be applied to {1},{2}", opstring(op), ltype, rtype));
					newexpr = new BinaryExpr(cast(left,btype), op, cast(right,btype));
				}
			} else if ("^&|".indexOf(op) >= 0) {
				Type btype = binaryCastType(ltype, rtype);
				if (!btype.equals(BuiltinType.typeBool) && !btype.equals(BuiltinType.typeInt) && !btype.equals(BuiltinType.typeLong))
					throw new ParseException(I18N._("Operator {0} cannot be applied to {1},{2}", opstring(op), ltype, rtype));
				newexpr = new BinaryExpr(cast(left,btype), op, cast(right,btype));
			} else {
				//should not happen, but...
				throw new ParseException(I18N._("Error while parsing expression, please report."));
			}
			exprs.setElementAt(newexpr, index);
			exprs.removeElementAt(index+1);
			operators.removeElementAt(index);
		}
		return (Expr)exprs.elementAt(0);
	}

	/**
	 * Applies postfix operators (), [] and . to expression.
	 */
	private Expr parsePostfix(Scope scope, Expr expr) throws ParseException, IOException {
		while (true) {
			int ttype = t.nextToken();
			if (ttype == '(') {
				if (!(expr.rettype().getClass() == FunctionType.class))
					throw new ParseException(I18N._("Applying () to non-function expression"));
				FunctionType ftype = (FunctionType)expr.rettype();
				Vector vargs = new Vector();
				while (t.nextToken() != ')') {
					t.pushBack();
					if (!vargs.isEmpty()) expect(',');
					vargs.addElement(parseExpr(scope));
				}
				if (ftype.args.length != vargs.size())
					throw new ParseException(I18N._("Wrong number of arguments in function call"));
				Expr[] args = new Expr[vargs.size()];
				for (int i=0; i<args.length; i++) {
					args[i] = cast((Expr)vargs.elementAt(i), ftype.args[i]);
				}
				expr = new FCallExpr(expr, args);
				continue;
			} else if (ttype == '[') {
				Type arrtype = expr.rettype();
				if (!arrtype.equals(BuiltinType.typeArray)
				 && !arrtype.equals(BuiltinType.typeBArray)
				 && !arrtype.equals(BuiltinType.typeCArray)) {
					throw new ParseException(I18N._("Applying [] to non-array expression"));
				}
				Expr indexexpr = cast(parseExpr(scope), BuiltinType.typeInt);
				expect(']');
				if (t.nextToken() == '=') {
					Expr assignexpr = parseExpr(scope);
					if (!arrtype.equals(BuiltinType.typeArray)) {
						assignexpr = cast(assignexpr, BuiltinType.typeInt);
					} else {
						assignexpr = cast(assignexpr, BuiltinType.typeAny);
					}
					expr = new AStoreExpr(expr, indexexpr, assignexpr);
				} else {
					t.pushBack();
					if (arrtype.equals(BuiltinType.typeArray)) {
						expr = new ALoadExpr(expr, indexexpr, BuiltinType.typeAny);
					} else {
						expr = new ALoadExpr(expr, indexexpr, BuiltinType.typeInt);
					}
					continue;
				}
			} else if (ttype == '.') {
				if (t.nextToken() != Tokenizer.TT_IDENTIFIER)
					throw new ParseException(I18N._("Identifier expected after '.'"));
				String member = t.svalue;
				Type type = expr.rettype();
				if (type.equals(BuiltinType.typeArray)
				 || type.equals(BuiltinType.typeBArray)
				 || type.equals(BuiltinType.typeCArray)) {
					if (member.equals("len")) {
						expr = new ALenExpr(expr);
						continue;
					}
				} else if (type.getClass() == StructureType.class) {
					Var[] fields = ((StructureType)type).fields;
					int index = -1;
					for (int i=0; i<fields.length; i++) {
						if (fields[i].name.equals(member)) {
							index = i;
							break;
						}
					}
					if (index >= 0) {
						ConstExpr indexexpr = new ConstExpr(new Integer(index));
						if (t.nextToken() == '=') {
							Expr assignexpr = cast(parseExpr(scope), fields[index].type);
							expr = new AStoreExpr(expr, indexexpr, assignexpr);
							continue;
						} else {
							t.pushBack();
							expr = new ALoadExpr(expr, indexexpr, fields[index].type);
							continue;
						}
					}
				}
				throw new ParseException(I18N._("Type {0} has no member named {1}", type, member));
			} else {
				t.pushBack();
				return expr;
			}
		}
	}

	/**
	 * Parses expression without binary operators.
	 */
	private Expr parseExprNoop(Scope scope) throws ParseException, IOException {
		int ttype = t.nextToken();
		switch (ttype) {
			case ';':
				return new NoneExpr();
			case '!':
				return new UnaryExpr(ttype, cast(parsePostfix(scope, parseExprNoop(scope)), BuiltinType.typeBool));
			case '~': {
				Expr sub = parsePostfix(scope, parseExprNoop(scope));
				Type type = sub.rettype();
				if (type.equals(BuiltinType.typeInt))
					return new BinaryExpr(sub, '^', new ConstExpr(new Integer(-1)));
				else if (type.equals(BuiltinType.typeLong))
					return new BinaryExpr(sub, '^', new ConstExpr(new Long(-1l)));
				else
					throw new ParseException(I18N._("Operator ~ cannot be applied to {0}", type));
			}
			case '-': {
				Expr sub = parsePostfix(scope, parseExprNoop(scope));
				Type type = sub.rettype();
				if (!type.equals(BuiltinType.typeInt)
				 && !type.equals(BuiltinType.typeLong)
				 && !type.equals(BuiltinType.typeFloat)
				 && !type.equals(BuiltinType.typeDouble))
					throw new ParseException(I18N._("Operator - cannot be applied to {0}", type));
				return new UnaryExpr(ttype, sub);
			}
			case '{':
				return parseBlock(scope);
			case '(': {
				Expr expr = parseExpr(scope);
				expect(')');
				return expr;
			}
			case Tokenizer.TT_INT:
				return new ConstExpr(new Integer(t.ivalue));
			case Tokenizer.TT_LONG:
				return new ConstExpr(new Long(t.lvalue));
			case Tokenizer.TT_FLOAT:
				return new ConstExpr(new Float(t.fvalue));
			case Tokenizer.TT_DOUBLE:
				return new ConstExpr(new Double(t.dvalue));
			case Tokenizer.TT_QUOTED:
				return new ConstExpr(t.svalue);
			case Tokenizer.TT_BOOL:
				return new ConstExpr((t.svalue.equals("true") ? Boolean.TRUE : Boolean.FALSE));
			case Tokenizer.TT_KEYWORD:
				if (t.svalue.equals("cast")) {
					return parseCast(scope);
				} else if (t.svalue.equals("null")) {
					return new ConstExpr(null);
				} else if (t.svalue.equals("while")) {
					expect('(');
					Expr cond = cast(parseExpr(scope), BuiltinType.typeBool);
					expect(')');
					Expr body = cast(parseExpr(scope), BuiltinType.typeNone);
					return new WhileExpr(cond, body);
				} else if (t.svalue.equals("if")) {
					expect('(');
					Expr cond = cast(parseExpr(scope), BuiltinType.typeBool);
					expect(')');
					Expr ifexpr = parseExpr(scope);
					Expr elseexpr;
					if (t.nextToken() != Tokenizer.TT_KEYWORD || !t.svalue.equals("else")) {
						t.pushBack();
						elseexpr = new NoneExpr();
					} else {
						elseexpr = parseExpr(scope);
					}
					Type btype = binaryCastType(ifexpr.rettype(), elseexpr.rettype());
					return new IfExpr(cond, IfExpr.TRUE, cast(ifexpr,btype), cast(elseexpr,btype));
				} else if (t.svalue.equals("var")) {
					if (t.nextToken() != Tokenizer.TT_IDENTIFIER)
						throw new ParseException(I18N._("Identifier expected after var"));
					String varname = t.svalue;
					Type vartype = null;
					if (t.nextToken() == ':') {
						vartype = parseType(scope);
					} else {
						t.pushBack();
					}
					if (t.nextToken() == ';') {
						if (vartype != null) {
							Var v = new Var(varname, vartype);
							if (scope.addVar(v)) {
								warn(I18N._("Variable {0} hides another variable with the same name", v.name));
							}
							return new NoneExpr();
						} else {
							throw new ParseException(I18N._("Type of {0} is not defined", varname));
						}
					} else {
						t.pushBack();
					}
					if (t.nextToken() == '=') {
						Expr value = parseExpr(scope);
						if (vartype == null) {
							vartype = value.rettype();
							if (vartype.equals(BuiltinType.typeNone))
								throw new ParseException(I18N._("Cannot convert from none to Any"));
						} else {
							value = cast(value, vartype);
						}
						Var v = new Var(varname, vartype);
						if (scope.addVar(v)) {
							warn(I18N._("Variable {0} hides another variable with the same name", v.name));
						}
						return new AssignExpr(v, value);
					} else {
						throw new ParseException(I18N._("{0} unexpected here", t));
					}
				} else if (t.svalue.equals("new")) {
					Type type = parseType(scope);
					if (type.getClass() == StructureType.class) {
						expect('(');
						StructureType struct = (StructureType)type;
						Expr[] init = new Expr[struct.fields.length];
						boolean first = true;
						while (t.nextToken() != ')') {
							t.pushBack();
							if (first) first = false; else expect(',');
							if (t.nextToken() != Tokenizer.TT_IDENTIFIER) {
								throw new ParseException(I18N._("Identifier expected in structure constructor"));
							}
							int index = struct.fields.length-1;
							while (index >= 0 && !struct.fields[index].name.equals(t.svalue)) index--;
							if (index < 0) {
								throw new ParseException(I18N._("Type {0} has no member named {1}", type, t.svalue));
							}
							expect('=');
							init[index] = cast(parseExpr(scope), struct.fields[index].type);
						}
						return new NewArrayByEnumExpr(type, init);
					} else if (type.equals(BuiltinType.typeArray)
					        || type.equals(BuiltinType.typeBArray)
							|| type.equals(BuiltinType.typeCArray)) {
						if (t.nextToken() == '(') {
							Expr lenexpr = cast(parseExpr(scope), BuiltinType.typeInt);
							expect(')');
							return new NewArrayExpr(type, lenexpr);
						} else if (t.ttype == '{') {
							Vector vinit = new Vector();
							while (t.nextToken() != '}') {
								t.pushBack();
								if (!vinit.isEmpty()) expect(',');
								Expr e = parseExpr(scope);
								if (type.equals(BuiltinType.typeArray)) {
									e = cast(e, BuiltinType.typeAny);
								} else {
									e = cast(e, BuiltinType.typeInt);
								}
								vinit.addElement(e);
							}
							Expr[] init = new Expr[vinit.size()];
							vinit.copyInto(init);
							return new NewArrayByEnumExpr(type, init);
						} else {
							throw new ParseException(I18N._("'(' or '{' expected in constructor"));
						}
					} else {
						throw new ParseException(I18N._("Applying 'new' to neither array nor structure"));
					}
				} else if (t.svalue.equals("for")) {
					expect('(');
					BlockExpr forblock = new BlockExpr(scope);
					BlockExpr forbody = new BlockExpr(forblock);
					Expr init = cast(parseExpr(forblock), BuiltinType.typeNone);
					expect(',');
					Expr cond = cast(parseExpr(forblock), BuiltinType.typeBool);
					expect(',');
					Expr incr = cast(parseExpr(forbody), BuiltinType.typeNone);
					expect(')');
					Expr body = cast(parseExpr(forbody), BuiltinType.typeNone);
					forbody.exprs.addElement(body);
					forbody.exprs.addElement(incr);
					forblock.exprs.addElement(init);
					forblock.exprs.addElement(new WhileExpr(cond, forbody));
					return forblock;
				} else {
					throw new ParseException(I18N._("{0} unexpected here", t));
				}
			case Tokenizer.TT_IDENTIFIER:
				String str = t.svalue;
				Var var = scope.getVar(str);
				if (var == null) throw new ParseException(I18N._("Variable {0} is not defined", str));
				switch (t.nextToken()) {
					case '=': {
						if (!scope.isLocal(str))
							throw new ParseException(I18N._("Cannot assign to constant {0}", str));
						Expr value = cast(parseExpr(scope), var.type);
						return new AssignExpr(var, value);
					}
					default: {
						t.pushBack();
						if (scope.isLocal(str)) {
							return new VarExpr(var);
						} else {
							Func f = unit.getFunc(str);
							f.hits++;
							return new ConstExpr(f);
						}
					}
				}
			default:
				throw new ParseException(I18N._("{0} unexpected here", t));
		}
	}

	/**
	 * Parses cast expression. Called after 'cast' token.
	 */
	private Expr parseCast(Scope scope) throws ParseException, IOException {
		expect('(');
		Type toType = parseType(scope);
		expect(')');
		Expr expr = parseExpr(scope);
		if (toType.equals(expr.rettype())) {
			warn(I18N._("Cast to the same type"));
		}
		if (toType.equals(BuiltinType.typeAny)) {
			warn(I18N._("Cast to Any"));
		}
		if (expr.rettype().equals(BuiltinType.typeAny)) {
			return new CastExpr(toType, expr);
		}
		return cast(expr, toType);
	}

	private Expr cast(Expr expr, Type toType) throws ParseException {
		Type fromType = expr.rettype();
		if (fromType.equals(toType)) return expr;
		if (toType.equals(BuiltinType.typeAny) && !fromType.equals(BuiltinType.typeNone)) {
			return new CastExpr(BuiltinType.typeAny, expr);
		}
		if (toType.equals(BuiltinType.typeNone)) {
			return new DiscardExpr(expr);
		}
		if (toType.getClass() == BuiltinType.class) {
			return castPrimitive(expr, toType);
		}
		if (expr.getClass() == ConstExpr.class && ((ConstExpr)expr).value == null) {
			return new CastExpr(toType, expr);
		}
		throw new ParseException(I18N._("Cannot convert from {0} to {1}", fromType, toType));
	}

	private Expr castPrimitive(Expr expr, Type toType) throws ParseException {
		Type fromType = expr.rettype();
		if (fromType.equals(toType)) return expr;
		if (fromType.equals(BuiltinType.typeInt)) {
			if (toType.equals(BuiltinType.typeLong)) return new CastPrimitiveExpr(expr, CastPrimitiveExpr.I2L);
			if (toType.equals(BuiltinType.typeFloat)) return new CastPrimitiveExpr(expr, CastPrimitiveExpr.I2F);
			if (toType.equals(BuiltinType.typeDouble)) return new CastPrimitiveExpr(expr, CastPrimitiveExpr.I2D);
		}
		if (fromType.equals(BuiltinType.typeLong)) {
			if (toType.equals(BuiltinType.typeInt)) return new CastPrimitiveExpr(expr, CastPrimitiveExpr.L2I);
			if (toType.equals(BuiltinType.typeFloat)) return new CastPrimitiveExpr(expr, CastPrimitiveExpr.L2F);
			if (toType.equals(BuiltinType.typeDouble)) return new CastPrimitiveExpr(expr, CastPrimitiveExpr.L2D);
		}
		if (fromType.equals(BuiltinType.typeFloat)) {
			if (toType.equals(BuiltinType.typeLong)) return new CastPrimitiveExpr(expr, CastPrimitiveExpr.F2L);
			if (toType.equals(BuiltinType.typeInt)) return new CastPrimitiveExpr(expr, CastPrimitiveExpr.F2I);
			if (toType.equals(BuiltinType.typeDouble)) return new CastPrimitiveExpr(expr, CastPrimitiveExpr.F2D);
		}
		if (fromType.equals(BuiltinType.typeDouble)) {
			if (toType.equals(BuiltinType.typeLong)) return new CastPrimitiveExpr(expr, CastPrimitiveExpr.D2L);
			if (toType.equals(BuiltinType.typeFloat)) return new CastPrimitiveExpr(expr, CastPrimitiveExpr.D2F);
			if (toType.equals(BuiltinType.typeInt)) return new CastPrimitiveExpr(expr, CastPrimitiveExpr.D2I);
		}
		throw new ParseException(I18N._("Cannot convert from {0} to {1}", fromType, toType));
	}

	/**
	 * Computes type to which LHS and RHS of operator should be cast.
	 * <ul>
	 *   <li>
	 *     If types are the same then that type is returned.
	 *   </li><li>
	 *     if one of types is none then returned type is none
	 *   </li><li>
	 *     If both are numbers then returned type is first
	 *     occured in the list: Double, Float, Long, Int.
	 *   </li><li>
	 *     Otherwise returned type is 'Any'.
	 *   </li>
	 * </ul>
	 */
	private Type binaryCastType(Type ltype, Type rtype) {
		if (ltype.equals(rtype)) return ltype;
		if (ltype.equals(BuiltinType.typeNone) || rtype.equals(BuiltinType.typeNone))
			return BuiltinType.typeNone;
		if (ltype.getClass() == BuiltinType.class && rtype.getClass() == BuiltinType.class) {
			int lindex = ((BuiltinType)ltype).numIndex();
			int rindex = ((BuiltinType)rtype).numIndex();
			if (lindex < 0 || rindex < 0) return BuiltinType.typeAny;
			switch (lindex > rindex ? lindex : rindex) {
				case 1: return BuiltinType.typeInt;
				case 2: return BuiltinType.typeLong;
				case 3: return BuiltinType.typeFloat;
				case 4: return BuiltinType.typeDouble;
			}
		}
		return BuiltinType.typeAny;
	}

	/** Returns operator string by ttype. */
	private String opstring(int ttype) {
		if (ttype > 0) return String.valueOf((char)ttype);
		switch (ttype) {
			case Tokenizer.TT_EQEQ: return "==";
			case Tokenizer.TT_GTEQ: return ">=";
			case Tokenizer.TT_GTGT: return ">>";
			case Tokenizer.TT_GTGTGT: return ">>>";
			case Tokenizer.TT_LTEQ: return "<=";
			case Tokenizer.TT_LTLT: return "<<";
			case Tokenizer.TT_NOTEQ: return "!=";
			default: return null;
		}
	}

	/** Reads next token and if it is not the given character, throws exception. */
	private void expect(char ttype) throws ParseException, IOException {
		if (t.nextToken() != ttype) {
			throw new ParseException(I18N._("Expected '{0}', got {1}", String.valueOf(ttype), t));
		}
	}

	/** Prints error on stderr. */
	private void error(String msg) {
		IO.println(c.stderr, files.peek().toString() + ':' + t.lineNumber()
				+ ": "+I18N._("[Error]")+"\n "+msg);
	}

	/** Prints warning on stderr. */
	private void warn(String msg) {
		IO.println(c.stderr, files.peek().toString() + ':' + t.lineNumber()
				+ ": "+I18N._("[Warning]")+"\n "+msg);
	}
}
