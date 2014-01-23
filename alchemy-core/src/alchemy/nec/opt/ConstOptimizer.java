/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2014, Sergey Basalaev <sbasalaev@gmail.com>
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

package alchemy.nec.opt;

import alchemy.nec.Token;
import alchemy.nec.syntax.Function;
import alchemy.nec.syntax.Scope;
import alchemy.nec.syntax.Unit;
import alchemy.nec.syntax.Var;
import alchemy.nec.syntax.expr.*;
import alchemy.nec.syntax.statement.*;
import alchemy.nec.syntax.type.BuiltinType;
import alchemy.nec.syntax.type.Type;
import alchemy.types.Float32;
import alchemy.types.Float64;
import alchemy.types.Int32;
import alchemy.types.Int64;
import alchemy.util.ArrayList;

/**
 * Simple optimizer used with {@code -O1}
 * and for constant folding.
 * Performs the following optimizations:
 * <ul>
 * <li>Constant folding (CF)</li>
 * <li>Dead code elimination (DCE)</li>
 * <li>Structural transformations (ST)</li>
 * </ul>
 * <p>
 * Visit* methods accept Scope as argument and
 * return optimized tree.
 * </p>
 * 
 * @author Sergey Basalaev
 */
public class ConstOptimizer implements ExprVisitor, StatementVisitor {

	public ConstOptimizer() { }

	public void visitUnit(Unit u) {
		ArrayList funcs = u.implementedFunctions;
		for (int i=0; i<funcs.size(); i++) {
			Function f = (Function) funcs.get(i);
			visitFunction(f);
		}
	}

	public void visitFunction(Function f) {
		f.body = (Statement) f.body.accept(this, f);
	}

	/**
	 * <pre>
	 * ST:
	 *   func.apply()  =&gt;  func
	 *   func.apply(a1 ... am).apply(b1 ... bn)  =&gt;  func.apply(a1 ... am, b1 ... bn)
	 * </pre>
	 */
	public Object visitApply(ApplyExpr apply, Object scope) {
		apply.funcExpr = (Expr) apply.funcExpr.accept(this, scope);
		Expr[] args = apply.args;
		for (int i=0; i<args.length; i++) {
			args[i] = (Expr) args[i].accept(this, scope);
		}
		if (apply.args.length == 0) {
			return apply.funcExpr;
		}
		if (apply.funcExpr.kind == Expr.EXPR_APPLY) {
			ApplyExpr innerApply = (ApplyExpr) apply.funcExpr;
			Expr[] newargs = new Expr[apply.args.length + innerApply.args.length];
			System.arraycopy(apply.args, 0, newargs, 0, apply.args.length);
			System.arraycopy(innerApply.args, 0, newargs, apply.args.length, innerApply.args.length);
			return new ApplyExpr(innerApply.funcExpr, newargs);
		}
		return apply;
	}

	public Object visitArrayElement(ArrayElementExpr aget, Object scope) {
		aget.arrayExpr = (Expr) aget.arrayExpr.accept(this, scope);
		aget.indexExpr = (Expr) aget.indexExpr.accept(this, scope);
		return aget;
	}

	public Object visitArrayLen(ArrayLenExpr alen, Object scope) {
		alen.arrayExpr = (Expr) alen.accept(this, scope);
		return alen;
	}

	/**
	 * <pre>
	 * CF:
	 *   expr + 0        =&gt;  expr
	 *   expr - 0        =&gt;  expr
	 *   expr | 0        =&gt;  expr
	 *   expr ^ 0        =&gt;  expr
	 *   expr &lt;&lt; 0       =&gt; expr
	 *   expr &gt;&gt; 0       =&gt; expr
	 *   expr &gt;&gt;&gt; 0      =&gt; expr
	 *
	 *   expr * 1        =&gt;  expr
	 *   expr / 1        =&gt;  expr
	 *
	 *   expr & -1       =&gt;  expr
	 *   expr ^ -1       =&gt;  ~expr
	 *
	 *   expr + 0L       =&gt;  expr
	 *   expr - 0L       =&gt;  expr
	 *   expr | 0L       =&gt;  expr
	 *   expr ^ 0L       =&gt;  expr
	 *
	 *   expr * 1L       =&gt;  expr
	 *   expr / 1L       =&gt;  expr
	 *
	 *   expr & -1L      =&gt;  expr
	 *   expr ^ -1L      =&gt;  ~expr
	 *
	 *   expr & true     =&gt;  expr
	 *
	 *   expr | false    =&gt;  expr
	 *
	 *   expr ^ true     =&gt;  !expr
	 *   expr ^ false    =&gt;  expr
	 *
	 *   const op const  =&gt;  const
	 * </pre>
	 */
	public Object visitBinary(BinaryExpr binary, Object scope) {
		binary.lhs = (Expr)binary.lhs.accept(this, scope);
		binary.rhs = (Expr)binary.rhs.accept(this, scope);
		// optimizing  expr op const
		if (binary.rhs.kind == Expr.EXPR_CONST) {
			Object cnst = ((ConstExpr)binary.rhs).value;
			if (cnst instanceof Int32) {
				if (cnst.equals(Int32.ZERO)) {
					switch (binary.operator) {
						case '+':
						case '-':
						case '|':
						case '^':
						case Token.LTLT:
						case Token.GTGT:
						case Token.GTGTGT: {
							return binary.lhs;
						}
						case '/':
						case '%': // refuse to optimize integer division by zero
							return binary;
					}
				} else if (cnst.equals(Int32.ONE)) {
					switch (binary.operator) {
						case '*':
						case '/':
						case '%': {
							return binary.lhs;
						}
					}
				} else if (cnst.equals(Int32.M_ONE)) {
					switch (binary.operator) {
						case '&': {
							return binary.lhs;
						}
						case '^': {
							return new UnaryExpr('~', binary.lhs);
						}
					}
				}
			} else if (cnst instanceof Int64) {
				long l = ((Int64)cnst).value;
				if (l == 0L) {
					switch (binary.operator) {
						case '+':
						case '-':
						case '|':
						case '^': {
							return binary.lhs;
						}
						case '/':
						case '%':
							// refuse to optimize integer division by zero
							return binary;
					}
				} else if (l == 1L) {
					switch (binary.operator) {
						case '*':
						case '/':
						case '%': {
							return binary.lhs;
						}
					}
				} else if (l == -1L) {
					switch (binary.operator) {
						case '&': {
							return binary.lhs;
						}
						case '^': {
							return new UnaryExpr('~', binary.lhs);
						}
					}
				}
			} else if (cnst instanceof Boolean) {
				switch (binary.operator) {
					case '&': {
						if (cnst.equals(Boolean.TRUE)) return binary.lhs;
						break;
					}
					case '|': {
						if (cnst.equals(Boolean.FALSE)) return binary.lhs;
						break;
					}
					case '^': {
						if (cnst.equals(Boolean.FALSE)) return binary.lhs;
						else return new UnaryExpr('!', binary.lhs);
					}
				}
			}
		}
		// optimizing  const op const
		if (binary.lhs.kind == Expr.EXPR_CONST && binary.rhs.kind == Expr.EXPR_CONST) {
			Object c1 = ((ConstExpr)binary.lhs).value;
			Object c2 = ((ConstExpr)binary.rhs).value;
			switch (binary.operator) {
				case '+':
					if (c1 instanceof Int32) {
						c1 = Int32.toInt32(((Int32)c1).value + ((Int32)c2).value);
					} else if (c1 instanceof Int64) {
						c1 = new Int64(((Int64)c1).value + ((Int64)c2).value);
					} else if (c1 instanceof Float32) {
						c1 = new Float32(((Float32)c1).value + ((Float32)c2).value);
					} else if (c1 instanceof Float64) {
						c1 = new Float64(((Float64)c1).value + ((Float64)c2).value);
					}
					break;
				case '-':
					if (c1 instanceof Int32) {
						c1 = Int32.toInt32(((Int32)c1).value - ((Int32)c2).value);
					} else if (c1 instanceof Int64) {
						c1 = new Int64(((Int64)c1).value - ((Int64)c2).value);
					} else if (c1 instanceof Float32) {
						c1 = new Float32(((Float32)c1).value - ((Float32)c2).value);
					} else if (c1 instanceof Float64) {
						c1 = new Float64(((Float64)c1).value - ((Float64)c2).value);
					}
					break;
				case '*':
					if (c1 instanceof Int32) {
						c1 = Int32.toInt32(((Int32)c1).value * ((Int32)c2).value);
					} else if (c1 instanceof Int64) {
						c1 = new Int64(((Int64)c1).value * ((Int64)c2).value);
					} else if (c1 instanceof Float32) {
						c1 = new Float32(((Float32)c1).value * ((Float32)c2).value);
					} else if (c1 instanceof Float64) {
						c1 = new Float64(((Float64)c1).value * ((Float64)c2).value);
					}
					break;
				case '/':
					// TODO: division by zero
					if (c1 instanceof Int32) {
						c1 = Int32.toInt32(((Int32)c1).value / ((Int32)c2).value);
					} else if (c1 instanceof Int64) {
						c1 = new Int64(((Int64)c1).value / ((Int64)c2).value);
					} else if (c1 instanceof Float32) {
						c1 = new Float32(((Float32)c1).value / ((Float32)c2).value);
					} else if (c1 instanceof Float64) {
						c1 = new Float64(((Float64)c1).value / ((Float64)c2).value);
					}
					break;
				case '%':
					// TODO: division by zero
					if (c1 instanceof Int32) {
						c1 = Int32.toInt32(((Int32)c1).value % ((Int32)c2).value);
					} else if (c1 instanceof Int64) {
						c1 = new Int64(((Int64)c1).value % ((Int64)c2).value);
					} else if (c1 instanceof Float32) {
						c1 = new Float32(((Float32)c1).value % ((Float32)c2).value);
					} else if (c1 instanceof Float64) {
						c1 = new Float64(((Float64)c1).value % ((Float64)c2).value);
					}
					break;
				case '&':
					if (c1 instanceof Int32) {
						c1 = Int32.toInt32(((Int32)c1).value & ((Int32)c2).value);
					} else if (c1 instanceof Int64) {
						c1 = new Int64(((Int64)c1).value & ((Int64)c2).value);
					} else if (c1 instanceof Boolean) {
						c1 = c1.equals(Boolean.TRUE) ? c2 : Boolean.FALSE;
					}
					break;
				case '|':
					if (c1 instanceof Int32) {
						c1 = Int32.toInt32(((Int32)c1).value | ((Int32)c2).value);
					} else if (c1 instanceof Int64) {
						c1 = new Int64(((Int64)c1).value | ((Int64)c2).value);
					} else if (c1 instanceof Boolean) {
						c1 = c1.equals(Boolean.TRUE) ? Boolean.TRUE : c2;
					}
					break;
				case '^':
					if (c1 instanceof Int32) {
						c1 = Int32.toInt32(((Int32)c1).value ^ ((Int32)c2).value);
					} else if (c1 instanceof Int64) {
						c1 = new Int64(((Int64)c1).value ^ ((Int64)c2).value);
					} else if (c1 instanceof Boolean) {
						c1 = c1.equals(c2) ? Boolean.FALSE : Boolean.TRUE;
					}
					break;
				case Token.LTLT:
					if (c1 instanceof Int32) {
						c1 = Int32.toInt32(((Int32)c1).value << ((Int32)c2).value);
					} else if (c1 instanceof Int64) {
						c1 = new Int64(((Int64)c1).value << ((Int32)c2).value);
					}
					break;
				case Token.GTGT:
					if (c1 instanceof Int32) {
						c1 = Int32.toInt32(((Int32)c1).value >> ((Int32)c2).value);
					} else if (c1 instanceof Int64) {
						c1 = new Int64(((Int64)c1).value >> ((Int32)c2).value);
					}
					break;
				case Token.GTGTGT:
					if (c1 instanceof Int32) {
						c1 = Int32.toInt32(((Int32)c1).value >>> ((Int32)c2).value);
					} else if (c1 instanceof Int64) {
						c1 = new Int64(((Int64)c1).value >>> ((Int32)c2).value);
					}
					break;
			}
			return new ConstExpr(binary.lineNumber(), binary.lhs.returnType(), c1);
		}
		return binary;
	}

	/**
	 * <pre>
	 * CF:
	 *   const.tostr()  =&gt;  "const"
	 *
	 * ST:
	 *   func.apply(a1, ..., ak)(a[k+1], ..., aN)  =&gt;  func(a1, ..., aN)
	 * </pre>
	 */
	public Object visitCall(CallExpr fcall, Object scope) {
		fcall.fload = (Expr) fcall.fload.accept(this, scope);
		for (int i=0; i<fcall.args.length; i++) {
			fcall.args[i] = (Expr)fcall.args[i].accept(this, scope);
		}
		if (fcall.fload.kind == Expr.EXPR_APPLY) {
			ApplyExpr appliedLoad = (ApplyExpr) fcall.fload;
			Expr[] newargs = new Expr[appliedLoad.args.length + fcall.args.length];
			System.arraycopy(appliedLoad.args, 0, newargs, 0, appliedLoad.args.length);
			System.arraycopy(fcall.args, 0, newargs, appliedLoad.args.length, fcall.args.length);
			fcall.fload = appliedLoad.funcExpr;
			fcall.args = newargs;
		}
		if (fcall.fload.kind == Expr.EXPR_CONST) {
			Function f = (Function)((ConstExpr)fcall.fload).value;
			if (f.signature.equals("Any.tostr") && fcall.args[0].kind == Expr.EXPR_CONST) {
				f.hits--;
				Object cnst = ((ConstExpr)fcall.args[0]).value;
				if (!(cnst instanceof Function)) {
					int line = fcall.lineNumber();
					if (fcall.args[0].returnType() == BuiltinType.CHAR) {
						return new ConstExpr(line, BuiltinType.STRING, String.valueOf((char) ((Int32)cnst).value));
					} else {
						return new ConstExpr(line, BuiltinType.STRING, String.valueOf(cnst));
					}
				}
			}
		}
		return fcall;
	}

	/**
	 * <pre>
	 * CF:
	 *   const.cast(Number)  =&gt;  const
	 * </pre>
	 */
	public Object visitCast(CastExpr cast, Object scope) {
		cast.expr = (Expr)cast.expr.accept(this, scope);
		Type fromType = cast.expr.returnType();
		Type toType = cast.toType;
		if (fromType.safeToCastTo(toType)) {
			return cast.expr;
		} else if (cast.expr.kind == Expr.EXPR_CONST && toType.kind != Type.TYPE_OBJECT) {
			Object cnst = ((ConstExpr)cast.expr).value;
			int line = cast.lineNumber();
			switch (fromType.kind) {
				case Type.TYPE_BYTE:
				case Type.TYPE_SHORT:
				case Type.TYPE_CHAR:
				case Type.TYPE_INT: {
					int i = ((Int32)cnst).value;
					switch (toType.kind) {
						case Type.TYPE_DOUBLE:
							cnst = new Float64(i);
							break;
						case Type.TYPE_FLOAT:
							cnst = new Float32(i);
							break;
						case Type.TYPE_LONG:
							cnst = new Int64(i);
							break;
						case Type.TYPE_SHORT:
							cnst = Int32.toInt32((short)i);
							break;
						case Type.TYPE_BYTE:
							cnst = Int32.toInt32((byte)i);
							break;
						case Type.TYPE_CHAR:
							cnst = Int32.toInt32((char)i);
							break;
					}
					break;
				}
				case Type.TYPE_LONG: {
					long l = ((Int64)cnst).value;
					switch (toType.kind) {
						case Type.TYPE_DOUBLE:
							cnst = new Float64(l);
							break;
						case Type.TYPE_FLOAT:
							cnst = new Float32(l);
							break;
						case Type.TYPE_INT:
							cnst = Int32.toInt32((int)l);
							break;
						case Type.TYPE_SHORT:
							cnst = Int32.toInt32((short)l);
							break;
						case Type.TYPE_BYTE:
							cnst = Int32.toInt32((byte)l);
							break;
						case Type.TYPE_CHAR:
							cnst = Int32.toInt32((char)l);
							break;
					}
					break;
				}
				case Type.TYPE_FLOAT: {
					float f = ((Float32)cnst).value;
					switch (toType.kind) {
						case Type.TYPE_DOUBLE:
							cnst = new Float64(f);
							break;
						case Type.TYPE_LONG:
							cnst = new Int64((long)f);
							break;
						case Type.TYPE_INT:
							cnst = Int32.toInt32((int)f);
							break;
						case Type.TYPE_SHORT:
							cnst = Int32.toInt32((short)f);
							break;
						case Type.TYPE_BYTE:
							cnst = Int32.toInt32((byte)f);
							break;
						case Type.TYPE_CHAR:
							cnst = Int32.toInt32((char)f);
							break;
					}
					break;
				}
				case Type.TYPE_DOUBLE: {
					double d = ((Float64)cnst).value;
					switch (toType.kind) {
						case Type.TYPE_FLOAT:
							cnst = new Float32((float)d);
							break;
						case Type.TYPE_LONG:
							cnst = new Int64((long)d);
							break;
						case Type.TYPE_INT:
							cnst = Int32.toInt32((int)d);
							break;
						case Type.TYPE_SHORT:
							cnst = Int32.toInt32((short)d);
							break;
						case Type.TYPE_BYTE:
							cnst = Int32.toInt32((byte)d);
							break;
						case Type.TYPE_CHAR:
							cnst = Int32.toInt32((char)d);
							break;
					}
					break;
				}
			}
			return new ConstExpr(line, toType, cnst);
		}
		return cast;
	}

	/**
	 * <pre>
	 * CF:
	 *   const op const  =&gt;  const
	 * 
	 *   bool == bool  =&gt;  !(bool ^ bool)
	 *   bool != bool  =&gt;  bool ^ bool
	 * 
	 * ST:
	 *   0 &lt; expr   =&gt;  expr &gt; 0
	 *   0 &gt; expr   =&gt;  expr &lt; 0
	 *   0 &lt;= expr  =&gt;  expr &gt;= 0
	 *   0 &gt;= expr  =&gt;  expr &lt;= 0
	 * </pre>
	 */
	public Object visitComparison(ComparisonExpr cmp, Object scope) {
		cmp.lhs = (Expr)cmp.lhs.accept(this, scope);
		cmp.rhs = (Expr)cmp.rhs.accept(this, scope);
		if (cmp.lhs.kind == Expr.EXPR_CONST && cmp.rhs.kind == Expr.EXPR_CONST) {
			Object c1 = ((ConstExpr)cmp.lhs).value;
			Object c2 = ((ConstExpr)cmp.rhs).value;
			switch (cmp.operator) {
				case Token.EQEQ:
					c1 = (c1.equals(c2)) ? Boolean.TRUE : Boolean.FALSE;
					break;
				case Token.NOTEQ:
					c1 = (c1.equals(c2)) ? Boolean.FALSE : Boolean.TRUE;
					break;
				case '<':
					if (c1 instanceof Int32) {
						c1 = ((Int32)c1).value < ((Int32)c2).value ? Boolean.TRUE : Boolean.FALSE;
					} else if (c1 instanceof Int64) {
						c1 = ((Int64)c1).value < ((Int64)c2).value ? Boolean.TRUE : Boolean.FALSE;
					} else if (c1 instanceof Float32) {
						c1 = ((Float32)c1).value < ((Float32)c2).value ? Boolean.TRUE : Boolean.FALSE;
					} else if (c1 instanceof Float64) {
						c1 = ((Float64)c1).value < ((Float64)c2).value ? Boolean.TRUE : Boolean.FALSE;
					}
					break;
				case '>':
					if (c1 instanceof Int32) {
						c1 = ((Int32)c1).value > ((Int32)c2).value ? Boolean.TRUE : Boolean.FALSE;
					} else if (c1 instanceof Int64) {
						c1 = ((Int64)c1).value > ((Int64)c2).value ? Boolean.TRUE : Boolean.FALSE;
					} else if (c1 instanceof Float32) {
						c1 = ((Float32)c1).value > ((Float32)c2).value ? Boolean.TRUE : Boolean.FALSE;
					} else if (c1 instanceof Float64) {
						c1 = ((Float64)c1).value > ((Float64)c2).value ? Boolean.TRUE : Boolean.FALSE;
					}
					break;
				case Token.LTEQ:
					if (c1 instanceof Int32) {
						c1 = ((Int32)c1).value <= ((Int32)c2).value ? Boolean.TRUE : Boolean.FALSE;
					} else if (c1 instanceof Int64) {
						c1 = ((Int64)c1).value <= ((Int64)c2).value ? Boolean.TRUE : Boolean.FALSE;
					} else if (c1 instanceof Float32) {
						c1 = ((Float32)c1).value <= ((Float32)c2).value ? Boolean.TRUE : Boolean.FALSE;
					} else if (c1 instanceof Float64) {
						c1 = ((Float64)c1).value <= ((Float64)c2).value ? Boolean.TRUE : Boolean.FALSE;
					}
					break;
				case Token.GTEQ:
					if (c1 instanceof Int32) {
						c1 = ((Int32)c1).value >= ((Int32)c2).value ? Boolean.TRUE : Boolean.FALSE;
					} else if (c1 instanceof Int64) {
						c1 = ((Int64)c1).value >= ((Int64)c2).value ? Boolean.TRUE : Boolean.FALSE;
					} else if (c1 instanceof Float32) {
						c1 = ((Float32)c1).value >= ((Float32)c2).value ? Boolean.TRUE : Boolean.FALSE;
					} else if (c1 instanceof Float64) {
						c1 = ((Float64)c1).value >= ((Float64)c2).value ? Boolean.TRUE : Boolean.FALSE;
					}
					break;
			}
			return new ConstExpr(cmp.lineNumber(), BuiltinType.BOOL, c1);
		} else if (cmp.lhs instanceof ConstExpr) {
			Object cnst = ((ConstExpr)cmp.lhs).value;
			if (cnst.equals(Int32.ZERO)) {
				// to not duplicate code in EAsmWriter and
				// allow it to make assumptions only on rhs
				Expr tmp = cmp.lhs;
				cmp.lhs = cmp.rhs;
				cmp.rhs = tmp;
				switch (cmp.operator) {
					case '<': cmp.operator = '>'; break;
					case '>': cmp.operator = '<'; break;
					case Token.LTEQ: cmp.operator = Token.GTEQ; break;
					case Token.GTEQ: cmp.operator = Token.LTEQ; break;
				}
				return cmp;
			}
		}
		if (cmp.lhs.returnType() == BuiltinType.BOOL && cmp.rhs.returnType() == BuiltinType.BOOL) {
			Expr binary = new BinaryExpr(cmp.lhs, '^', cmp.rhs);
			if (cmp.operator == Token.EQEQ) {
				return new UnaryExpr('!', binary);
			} else {
				return binary;
			}
		}
		return cmp;
	}

	public Object visitConst(ConstExpr cnst, Object scope) {
		return cnst;
	}

	/**
	 * <pre>
	 * CF:
	 *   "str"+const  =&gt;  "strconst"
	 * </pre>
	 */
	public Object visitConcat(ConcatExpr concat, Object scope) {
		ArrayList oldexprs = concat.exprs;
		ArrayList newexprs = new ArrayList();
		String lastLiteral = null;
		int lastLiteralLine = -1;
		for (int i=0; i<oldexprs.size(); i++) {
			Expr e = (Expr) ((Expr)oldexprs.get(i)).accept(this, scope);
			if (e.kind == Expr.EXPR_CONST && e.returnType().kind != Type.TYPE_FUNCTION) {
				boolean isChar = e.returnType().kind == Type.TYPE_CHAR;
				Object cnst = ((ConstExpr)e).value;
				String append = (isChar) ? String.valueOf((char) ((Int32)cnst).value) : cnst.toString();
				if (append.equals("")) {
					// ignore empty string
				} else if (lastLiteral == null) {
					lastLiteral = append;
					lastLiteralLine = e.lineNumber();
				} else {
					lastLiteral += cnst.toString();
				}
			} else {
				if (lastLiteral != null) {
					newexprs.add(new ConstExpr(lastLiteralLine, BuiltinType.STRING, lastLiteral));
					lastLiteral = null;
				}
				newexprs.add(e);
			}
		}
		if (lastLiteral != null) {
			newexprs.add(new ConstExpr(lastLiteralLine, BuiltinType.STRING, lastLiteral));
		}
		if (newexprs.isEmpty()) {
			return new ConstExpr(concat.lineNumber(), BuiltinType.STRING, "");
		} else if (newexprs.size() == 1) {
			return newexprs.get(0);
		} else {
			return concat;
		}
	}

	/**
	 * <pre>
	 * DCE:
	 *   if (true) e1 else e2   =&gt;  e1
	 *   if (false) e1 else e2  =&gt;  e2
	 * </pre>
	 */
	public Object visitIfElse(IfElseExpr ifexpr, Object scope) {
		ifexpr.condition = (Expr)ifexpr.condition.accept(this, scope);
		ifexpr.ifexpr = (Expr)ifexpr.ifexpr.accept(this, scope);
		ifexpr.elseexpr = (Expr)ifexpr.elseexpr.accept(this, scope);
		if (ifexpr.condition.kind == Expr.EXPR_CONST) {
			if (((ConstExpr)ifexpr.condition).value.equals(Boolean.TRUE)) {
				return ifexpr.ifexpr;
			} else {
				return ifexpr.elseexpr;
			}
		}
		return ifexpr;
	}

	public Object visitNewArray(NewArrayExpr expr, Object scope) {
		Expr[] dimensions = expr.lengthExprs;
		for (int i=0; i<dimensions.length; i++) {
			dimensions[i] = (Expr) dimensions[i].accept(this, scope);
		}
		return expr;
	}

	public Object visitNewArrayInit(NewArrayInitExpr expr, Object scope) {
		Expr[] inits = expr.initializers;
		for (int i=0; i<inits.length; i++) {
			if (inits[i] != null) {
				inits[i] = (Expr) inits[i].accept(this, scope);
			}
		}
		return expr;
	}

	public Object visitRange(RangeExpr expr, Object args) {
		expr.fromExpr = (Expr) expr.fromExpr.accept(this, args);
		expr.toExpr = (Expr) expr.toExpr.accept(this, args);
		return expr;
	}

	/**
	 * Constant propagation.
	 */
	public Object visitSequential(SequentialExpr expr, Object scope) {
		Expr[] seq = expr.seqExprs;
		int newsize = 0;
		for (int i=0; i<seq.length; i++) {
			Expr e = (Expr) seq[i].accept(this, scope);
			seq[i] = e;
			if (e.kind == Expr.EXPR_CONST) {
				expr.seqVars[i].defaultValue = ((ConstExpr)e).value;
				seq[i] = null;
			} else {
				newsize++;
			}
		}
		expr.lastExpr = (Expr) expr.lastExpr.accept(this, scope);
		if (newsize == 0) {
			return expr.lastExpr;
		} else if (newsize < seq.length) {
			Var[] newvars = new Var[newsize];
			Expr[] newseq = new Expr[newsize];
			int j=0;
			for (int i=0; i<seq.length; i++) {
				if (seq[i] != null) {
					newvars[j] = expr.seqVars[i];
					newseq[j] = seq[i];
					j++;
				}
			}
			expr.seqVars = newvars;
			expr.seqExprs = newseq;
		}
		return expr;
	}

	/**
	 * <pre>
	 * DCE:
	 *   try const catch ...  =&gt;  const
	 *   try var   catch ...  =&gt;  const
	 * </pre>
	 */
	public Object visitTryCatch(TryCatchExpr trycatch, Object scope) {
		trycatch.tryExpr = (Expr) trycatch.tryExpr.accept(this, scope);
		trycatch.catchExpr = (Expr) trycatch.catchExpr.accept(this, scope);
		int kind = trycatch.tryExpr.kind;
		if (kind == Expr.EXPR_CONST || kind == Expr.EXPR_VAR) {
			return trycatch.tryExpr;
		}
		return trycatch;
	}

	/**
	 * <pre>
	 * CF:
	 *  +expr   =&gt;  expr
	 *  -const  =&gt;  const
	 *  ~const  =&gt;  const
	 *  !const  =&gt;  const
	 * 
	 *  !!expr  =&gt;  expr
	 *  --expr  =&gt;  expr
	 *  ~~expr  =&gt;  expr
	 *
	 * ST:
	 *  !(a == b)  =&gt;  a != b
	 *  !(a != b)  =&gt;  a == b
	 *  !(a &lt; b)   =&gt;  a &gt;= b
	 *  !(a &gt; b)   =&gt;  a &lt;= b
	 *  !(a &lt;= b)  =&gt;  a &gt; b
	 *  !(a &gt;= b)  =&gt;  a &lt; b
	 *
	 *  !(if (c) a else b)  =&gt;  if (c) !a else !b
	 *  !(try a catch b)    =&gt;  try !a catch !b
	 * </pre>
	 */
	public Object visitUnary(UnaryExpr unary, Object scope) {
		// optimize subexpression
		unary.expr = (Expr)unary.expr.accept(this, scope);
		if (unary.operator == '+') {
			return unary.expr;
		}
		// optimize "op const"
		if (unary.expr.kind == Expr.EXPR_CONST) {
			Object cnst = ((ConstExpr)unary.expr).value;
			switch (unary.operator) {
				case '!':
					cnst = (cnst == Boolean.TRUE) ? Boolean.FALSE : Boolean.TRUE;
					break;
				case '-':
					if (cnst instanceof Int32) {
						int i = ((Int32)cnst).value;
						cnst = Int32.toInt32(-i);
					} else if (cnst instanceof Int64) {
						long l = ((Int64)cnst).value;
						cnst = new Int64(-l);
					} else if (cnst instanceof Float32) {
						float f = ((Float32)cnst).value;
						cnst = new Float32(-f);
					} else if (cnst instanceof Float64) {
						double d = ((Float64)cnst).value;
						cnst = new Float64(-d);
					}
					break;
				case '~':
					if (cnst instanceof Int32) {
						int i = ((Int32)cnst).value;
						cnst = Int32.toInt32(~i);
					} else if (cnst instanceof Int64) {
						long l = ((Int64)cnst).value;
						cnst = new Int64(~l);
					}
					break;
			}
			return new ConstExpr(unary.lineNumber(), unary.returnType(), cnst);
		}
		// optimize !(expr)
		if (unary.operator == '!') {
			if (unary.expr.kind == Expr.EXPR_COMPARISON) {
				final ComparisonExpr cmp = (ComparisonExpr) unary.expr;
				switch (cmp.operator) {
					case '<': cmp.operator = Token.GTEQ; break;
					case '>': cmp.operator = Token.LTEQ; break;
					case Token.LTEQ: cmp.operator = '>'; break;
					case Token.GTEQ: cmp.operator = '<'; break;
					case Token.EQEQ: cmp.operator = Token.NOTEQ; break;
					case Token.NOTEQ: cmp.operator = Token.EQEQ; break;
				}
				return cmp;
			} else if (unary.expr.kind == Expr.EXPR_IF) {
				final IfElseExpr ifelse = (IfElseExpr) unary.expr;
				ifelse.ifexpr = (Expr) new UnaryExpr('!', ifelse.ifexpr).accept(this, scope);
				ifelse.elseexpr = (Expr) new UnaryExpr('!', ifelse.elseexpr).accept(this, scope);
				return ifelse;
			} else if (unary.expr.kind == Expr.EXPR_TRYCATCH) {
				final TryCatchExpr trycatch = (TryCatchExpr) unary.expr;
				trycatch.tryExpr = (Expr) new UnaryExpr('!', trycatch.tryExpr).accept(this, scope);
				trycatch.catchExpr = (Expr) new UnaryExpr('!', trycatch.catchExpr).accept(this, scope);
				return trycatch;
			}
		}
		// optimize double unary
		if (unary.expr.kind == Expr.EXPR_UNARY && ((UnaryExpr)unary.expr).operator == unary.operator) {
			return ((UnaryExpr)unary.expr).expr;
		}
		return unary;
	}

	/** Constant/copy propagation. */
	public Object visitVar(VarExpr expr, Object scope) {
		if (expr.var.isConstant && expr.var.defaultValue != null) {
			return new ConstExpr(expr.lineNumber(), expr.var.type, expr.var.defaultValue);
		}
		return expr;
	}

	public Object visitArraySetStatement(ArraySetStatement stat, Object scope) {
		stat.arrayExpr = (Expr) stat.arrayExpr.accept(this, scope);
		stat.indexExpr = (Expr) stat.indexExpr.accept(this, scope);
		stat.assignExpr = (Expr) stat.assignExpr.accept(this, scope);
		return stat;
	}

	public Object visitAssignStatement(AssignStatement stat, Object scope) {
		stat.assignExpr = (Expr) stat.assignExpr.accept(this, scope);
		return stat;
	}

	/**
	 * <pre>
	 * ST:
	 *   { ..., stat[N], empty, stat[N+2], ... }  =&gt;  { ..., stat[N], stat[N+2], ... }
	 *   { stat }  =&gt;  stat
	 *   { }  =&gt;  empty
	 * </pre>
	 */
	public Object visitBlockStatement(BlockStatement block, Object scope) {
		ArrayList statements = block.statements;
		int i=0;
		while (i < block.statements.size()) {
			Statement stat = (Statement) block.statements.get(i);
			stat = (Statement) stat.accept(this, block);
			if (stat.kind == Statement.STAT_EMPTY) statements.remove(i);
			else statements.set(i, stat);
		}
		switch (statements.size()) {
			case 0:  return new EmptyStatement();
			case 1:  return statements.get(0);
			default: return block;
		}
	}

	public Object visitBreakStatement(BreakStatement stat, Object scope) {
		return stat;
	}

	public Object visitCompoundAssignStatement(CompoundAssignStatement stat, Object scope) {
		stat.assignExpr = (Expr) stat.assignExpr.accept(this, scope);
		return stat;
	}

	public Object visitContinueStatement(ContinueStatement stat, Object scope) {
		return stat;
	}

	public Object visitEmptyStatement(EmptyStatement stat, Object scope) {
		return stat;
	}

	/**
	 * <pre>
	 * DCE:
	 *   const;             =&gt;  empty;
	 *   var;               =&gt;  empty;
	 *   ex1 op ex1;        =&gt;  {ex1; ex2;}
	 *   op ex;             =&gt;  ex;
	 *   new [](d1,...,dN); =&gt;  {d1; ... dN;}
	 *   new []{e1,...,eN}; =&gt;  {e1; ... eN;}
	 *   array.len;         =&gt;  array;
	 *   array[i];          =&gt;  {array; i;}
	 * </pre>
	 */
	public Object visitExprStatement(ExprStatement stat, Object scope) {
		stat.expr = (Expr) stat.expr.accept(this, scope);
		switch (stat.expr.kind) {
			case Expr.EXPR_CONST:
			case Expr.EXPR_VAR:
				return new EmptyStatement();
			case Expr.EXPR_BINARY: {
				BinaryExpr binary = (BinaryExpr) stat.expr;
				BlockStatement block = new BlockStatement((Scope)scope);
				block.statements.add(new ExprStatement(binary.lhs));
				block.statements.add(new ExprStatement(binary.rhs));
				return block.accept(this, scope);
			}
			case Expr.EXPR_UNARY: {
				UnaryExpr unary = (UnaryExpr) stat.expr;
				return new ExprStatement(unary.expr).accept(this, scope);
			}
			case Expr.EXPR_NEWARRAY: {
				NewArrayExpr newarray = (NewArrayExpr) stat.expr;
				BlockStatement block = new BlockStatement((Scope)scope);
				for (int i=0; i < newarray.lengthExprs.length; i++) {
					block.statements.add(new ExprStatement(newarray.lengthExprs[i]));
				}
				return block.accept(this, scope);
			}
			case Expr.EXPR_NEWARRAY_INIT: {
				NewArrayInitExpr newarray = (NewArrayInitExpr) stat.expr;
				BlockStatement block = new BlockStatement((Scope)scope);
				for (int i=0; i < newarray.initializers.length; i++) {
					block.statements.add(new ExprStatement(newarray.initializers[i]));
				}
				return block.accept(this, scope);
			}
			case Expr.EXPR_ARRAY_LEN: {
				ArrayLenExpr alen = (ArrayLenExpr) stat.expr;
				return new ExprStatement(alen.arrayExpr).accept(this, scope);
			}
			case Expr.EXPR_ARRAY_ELEMENT: {
				ArrayElementExpr aget = (ArrayElementExpr) stat.expr;
				BlockStatement block = new BlockStatement((Scope)scope);
				block.statements.add(new ExprStatement(aget.arrayExpr));
				block.statements.add(new ExprStatement(aget.indexExpr));
				return block.accept(this, scope);
			}
			default:
				return stat;
		}
	}

	/**
	 * <pre>
	 * DCE:
	 *   if (true) st1 else st2   =&gt;  st1
	 *   if (false) st1 else st2  =&gt;  st2
	 *   if (expr) {} else st     =&gt;  if (!expr) st
	 *   if (expr) {} else {}     =&gt;  expr;
	 * </pre>
	 */
	public Object visitIfStatement(IfStatement ifelse, Object scope) {
		ifelse.condition = (Expr) ifelse.condition.accept(this, scope);
		ifelse.ifstat = (Statement) ifelse.ifstat.accept(this, scope);
		ifelse.elsestat = (Statement) ifelse.elsestat.accept(this, scope);
		if (ifelse.condition.kind == Expr.EXPR_CONST) {
			if (((ConstExpr)ifelse.condition).value.equals(Boolean.TRUE)) {
				return ifelse.ifstat;
			} else {
				return ifelse.elsestat;
			}
		}
		if (ifelse.ifstat.kind == Statement.STAT_EMPTY) {
			if (ifelse.elsestat.kind == Statement.STAT_EMPTY) {
				return new ExprStatement(ifelse.condition).accept(this, scope);
			} else {
				ifelse.condition = (Expr) new UnaryExpr('!', ifelse.condition).accept(this, scope);
				ifelse.ifstat = ifelse.elsestat;
				ifelse.elsestat = new EmptyStatement();
			}
		}
		return ifelse;
	}

	/**
	 * <pre>
	 * DCE:
	 *   do stat while (false)  =&gt;  stat
	 * </pre>
	 */
	public Object visitLoopStatement(LoopStatement stat, Object scope) {
		stat.condition = (Expr) stat.condition.accept(this, scope);
		stat.preBody = (Statement) stat.preBody.accept(this, scope);
		stat.postBody = (Statement) stat.postBody.accept(this, scope);
		if (stat.condition.kind == Expr.EXPR_CONST) {
			Object cnst = ((ConstExpr)stat.condition).value;
			if (cnst.equals(Boolean.FALSE)) {
				return stat.preBody;
			}
		}
		return stat;
	}

	public Object visitReturnStatement(ReturnStatement stat, Object scope) {
		stat.expr = (Expr) stat.expr.accept(this, scope);
		return stat;
	}

	public Object visitThrowStatement(ThrowStatement stat, Object scope) {
		stat.errCodeExpr = (Expr) stat.errCodeExpr.accept(this, scope);
		stat.errMsgExpr = (Expr) stat.errMsgExpr.accept(this, scope);
		return stat;
	}

	/**
	 * <pre>
	 * DCE:
	 *   try { } catch ...  =&gt;  empty
	 * </pre>
	 */
	public Object visitTryCatchStatement(TryCatchStatement stat, Object scope) {
		stat.tryStat = (Statement) stat.tryStat.accept(this, scope);
		stat.catchStat = (Statement) stat.catchStat.accept(this, scope);
		if (stat.tryStat.kind == Statement.STAT_EMPTY) {
			return stat.tryStat;
		}
		return stat;
	}
}
