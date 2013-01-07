/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011-2012, Sergey Basalaev <sbasalaev@gmail.com>
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

import alchemy.core.types.Char;
import alchemy.core.types.Int;
import alchemy.nec.tree.*;
import java.util.Vector;

/**
 * Simple optimizer used with {@code -O1}.
 * Performs the following optimizations:
 * <ul>
 * <li>Constant folding (CF)</li>
 * <li>Dead code elimination (DCE)</li>
 * </ul>
 * <p>
 * Visit* methods accept Scope as argument and
 * return optimized tree.
 * </p>
 * 
 * @author Sergey Basalaev
 */
public class Optimizer implements ExprVisitor {
	
	/** Flag set if there were optimizations. */
	private boolean optimized;
	
	public void visitUnit(Unit u) {
		//visiting all functions
		do {
			optimized = false;
			for (int i=0; i<u.funcs.size(); i++) {
				visitFunc((Func)u.funcs.elementAt(i));
			}
		} while (optimized);
		//DCE: removing unused functions
		for (int i=u.funcs.size()-1; i>=0; i--) {
			Func f = (Func)u.funcs.elementAt(i);
			if (f.hits == 0) {
				u.funcs.removeElementAt(i);
			}
		}
	}
	
	public void visitFunc(Func f) {
		if (f.body != null) {
			f.body = (Expr)f.body.accept(this, f);
		}
	}

	/**
	 * <pre>
	 * CF:
	 *   new Array(expr).len         -&gt;  expr
	 *   new Array{e1, ..., eN}.len  -&gt;  {discard(e1), ..., discard(eN), N}
	 * </pre>
	 */
	public Object visitALen(ALenExpr alen, Object scope) {
		alen.arrayexpr = (Expr)alen.arrayexpr.accept(this, scope);
		if (alen.arrayexpr instanceof NewArrayExpr) {
			optimized = true;
			return ((NewArrayExpr)alen.arrayexpr).lengthexpr;
		}
		if (alen.arrayexpr instanceof NewArrayByEnumExpr) {
			optimized = true;
			final NewArrayByEnumExpr newarray = (NewArrayByEnumExpr) alen.arrayexpr;
			final BlockExpr block = new BlockExpr((Scope)scope);
			for (int i=0; i < newarray.initializers.length; i++) {
				Expr e = newarray.initializers[i];
				if (e != null) block.exprs.addElement(new DiscardExpr(e));
			}
			block.exprs.addElement(new ConstExpr(newarray.line, Int.toInt(newarray.initializers.length)));
			return block;
		}
		return alen;
	}

	public Object visitALoad(ALoadExpr aload, Object scope) {
		aload.arrayexpr = (Expr)aload.arrayexpr.accept(this, scope);
		aload.indexexpr = (Expr)aload.indexexpr.accept(this, scope);
		return aload;
	}

	public Object visitAStore(AStoreExpr astore, Object scope) {
		astore.arrayexpr = (Expr)astore.arrayexpr.accept(this, scope);
		astore.indexexpr = (Expr)astore.indexexpr.accept(this, scope);
		astore.assignexpr = (Expr)astore.assignexpr.accept(this, scope);
		return astore;
	}

	public Object visitAssign(AssignExpr assign, Object scope) {
		assign.expr = (Expr)assign.expr.accept(this, scope);
		return assign;
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
	 *   expr * 0        =&gt;  { discard(expr), 0 }
	 *   expr & 0        =&gt;  { discard(expr), 0 }
	 * 
	 *   expr * 1        =&gt;  expr
	 *   expr / 1        =&gt;  expr
	 *   expr % 1        =&gt;  expr
	 * 
	 *   expr & -1       =&gt;  expr
	 *   expr ^ -1       =&gt;  ~expr
	 * 
	 *   expr + 0L       =&gt;  expr
	 *   expr - 0L       =&gt;  expr
	 *   expr | 0L       =&gt;  expr
	 *   expr ^ 0L       =&gt;  expr
	 * 
	 *   expr * 0L       =&gt;  { discard(expr), 0L }
	 *   expr & 0L       =&gt;  { discard(expr), 0L }
	 * 
	 *   expr * 1L       =&gt;  expr
	 *   expr / 1L       =&gt;  expr
	 *   expr % 1L       =&gt;  expr
	 * 
	 *   expr & -1L      =&gt;  expr
	 *   expr ^ -1L      =&gt;  ~expr
	 * 
	 *   expr & true     =&gt;  expr
	 *   expr & false    =&gt;  { discard(expr), false }
	 * 
	 *   expr | true     =&gt;  { discard(expr), true }
	 *   expr | false    =&gt;  expr
	 * 
	 *   expr ^ true     =&gt;  !expr
	 *   expr ^ false    =&gt;  expr
	 * 
	 *   const op const  =&gt;  const
	 * </pre>
	 */
	public Object visitBinary(BinaryExpr binary, Object scope) {
		binary.lvalue = (Expr)binary.lvalue.accept(this, scope);
		binary.rvalue = (Expr)binary.rvalue.accept(this, scope);
		// optimizing  expr op const
		if (binary.rvalue instanceof ConstExpr) {
			Object cnst = ((ConstExpr)binary.rvalue).value;
			if (cnst instanceof Int) {
				if (cnst.equals(Int.ZERO)) {
					switch (binary.operator) {
						case '+':
						case '-':
						case '|':
						case '^':
						case Token.LTLT:
						case Token.GTGT:
						case Token.GTGTGT: {
							optimized = true;
							return binary.lvalue;
						}
						case '/':
						case '%': // refuse to optimize integer division by zero
							return binary;
						case '*':
						case '&': {
							optimized = true;
							BlockExpr block = new BlockExpr((Scope)scope);
							block.exprs.addElement(new DiscardExpr(binary.lvalue));
							block.exprs.addElement(binary.rvalue);
							return block;
						}
					}
				} else if (cnst.equals(Int.ONE)) {
					switch (binary.operator) {
						case '*':
						case '/':
						case '%': {
							optimized = true;
							return binary.lvalue;
						}
					}
				} else if (cnst.equals(Int.M_ONE)) {
					switch (binary.operator) {
						case '&': {
							optimized = true;
							return binary.lvalue;
						}
						case '^': {
							optimized = true;
							return new UnaryExpr('~', binary.lvalue);
						}
					}
				}
			} else if (cnst instanceof Long) {
				long l = ((Long)cnst).longValue();
				if (l == 0L) {
					switch (binary.operator) {
						case '+':
						case '-':
						case '|':
						case '^': {
							optimized = true;
							return binary.lvalue;
						}
						case '/':
						case '%': // refuse to optimize integer division by zero
							return binary;
						case '*':
						case '&': {
							optimized = true;
							BlockExpr block = new BlockExpr((Scope)scope);
							block.exprs.addElement(new DiscardExpr(binary.lvalue));
							block.exprs.addElement(binary.rvalue);
							return block;
						}
					}
				} else if (l == 1L) {
					switch (binary.operator) {
						case '*':
						case '/':
						case '%': {
							optimized = true;
							return binary.lvalue;
						}
					}
				} else if (l == -1L) {
					switch (binary.operator) {
						case '&': {
							optimized = true;
							return binary.lvalue;
						}
						case '^': {
							optimized = true;
							return new UnaryExpr('~', binary.lvalue);
						}
					}
				}
			} else if (cnst instanceof Boolean) {
				switch (binary.operator) {
					case '&': {
						optimized = true;
						if (cnst.equals(Boolean.TRUE)) return binary.lvalue;
						BlockExpr block = new BlockExpr((Scope)scope);
						block.exprs.addElement(new DiscardExpr(binary.lvalue));
						block.exprs.addElement(binary.rvalue);
						return block;
					}
					case '|': {
						optimized = true;
						if (cnst.equals(Boolean.FALSE)) return binary.lvalue;
						BlockExpr block = new BlockExpr((Scope)scope);
						block.exprs.addElement(new DiscardExpr(binary.lvalue));
						block.exprs.addElement(binary.rvalue);
						return block;
					}
					case '^': {
						optimized = true;
						if (cnst.equals(Boolean.FALSE)) return binary.lvalue;
						else return new UnaryExpr('!', binary.lvalue);
					}
				}
			}
		}
		// optimizing  const op const
		if (binary.lvalue instanceof ConstExpr && binary.rvalue instanceof ConstExpr) {
			Object c1 = ((ConstExpr)binary.lvalue).value;
			Object c2 = ((ConstExpr)binary.rvalue).value;
			optimized = true;
			switch (binary.operator) {
				case '+':
					if (c1 instanceof Int) {
						c1 = Int.toInt(((Int)c1).value + ((Int)c2).value);
					} else if (c1 instanceof Long) {
						c1 = new Long(((Long)c1).longValue() + ((Long)c2).longValue());
					} else if (c1 instanceof Float) {
						c1 = new Float(((Float)c1).floatValue() + ((Float)c2).floatValue());
					} else if (c1 instanceof Double) {
						c1 = new Double(((Double)c1).doubleValue() + ((Double)c2).doubleValue());
					}
					break;
				case '-':
					if (c1 instanceof Int) {
						c1 = Int.toInt(((Int)c1).value - ((Int)c2).value);
					} else if (c1 instanceof Long) {
						c1 = new Long(((Long)c1).longValue() - ((Long)c2).longValue());
					} else if (c1 instanceof Float) {
						c1 = new Float(((Float)c1).floatValue() - ((Float)c2).floatValue());
					} else if (c1 instanceof Double) {
						c1 = new Double(((Double)c1).doubleValue() - ((Double)c2).doubleValue());
					}
					break;
				case '*':
					if (c1 instanceof Int) {
						c1 = Int.toInt(((Int)c1).value * ((Int)c2).value);
					} else if (c1 instanceof Long) {
						c1 = new Long(((Long)c1).longValue() * ((Long)c2).longValue());
					} else if (c1 instanceof Float) {
						c1 = new Float(((Float)c1).floatValue() * ((Float)c2).floatValue());
					} else if (c1 instanceof Double) {
						c1 = new Double(((Double)c1).doubleValue() * ((Double)c2).doubleValue());
					}
					break;
				case '/':
					if (c1 instanceof Int) {
						c1 = Int.toInt(((Int)c1).value / ((Int)c2).value);
					} else if (c1 instanceof Long) {
						c1 = new Long(((Long)c1).longValue() / ((Long)c2).longValue());
					} else if (c1 instanceof Float) {
						c1 = new Float(((Float)c1).floatValue() / ((Float)c2).floatValue());
					} else if (c1 instanceof Double) {
						c1 = new Double(((Double)c1).doubleValue() / ((Double)c2).doubleValue());
					}
					break;
				case '%':
					if (c1 instanceof Int) {
						c1 = Int.toInt(((Int)c1).value % ((Int)c2).value);
					} else if (c1 instanceof Long) {
						c1 = new Long(((Long)c1).longValue() % ((Long)c2).longValue());
					} else if (c1 instanceof Float) {
						c1 = new Float(((Float)c1).floatValue() % ((Float)c2).floatValue());
					} else if (c1 instanceof Double) {
						c1 = new Double(((Double)c1).doubleValue() % ((Double)c2).doubleValue());
					}
					break;
				case '&':
					if (c1 instanceof Int) {
						c1 = Int.toInt(((Int)c1).value & ((Int)c2).value);
					} else if (c1 instanceof Long) {
						c1 = new Long(((Long)c1).longValue() & ((Long)c2).longValue());
					} else if (c1 instanceof Boolean) {
						c1 = c1.equals(Boolean.TRUE) ? c2 : Boolean.FALSE;
					}
					break;
				case '|':
					if (c1 instanceof Int) {
						c1 = Int.toInt(((Int)c1).value | ((Int)c2).value);
					} else if (c1 instanceof Long) {
						c1 = new Long(((Long)c1).longValue() | ((Long)c2).longValue());
					} else if (c1 instanceof Boolean) {
						c1 = c1.equals(Boolean.TRUE) ? Boolean.TRUE : c2;
					}
					break;
				case '^':
					if (c1 instanceof Int) {
						c1 = Int.toInt(((Int)c1).value ^ ((Int)c2).value);
					} else if (c1 instanceof Long) {
						c1 = new Long(((Long)c1).longValue() ^ ((Long)c2).longValue());
					} else if (c1 instanceof Boolean) {
						c1 = c1.equals(c2) ? Boolean.FALSE : Boolean.TRUE;
					}
					break;
				case Token.LTLT:
					if (c1 instanceof Int) {
						c1 = Int.toInt(((Int)c1).value << ((Int)c2).value);
					} else if (c1 instanceof Long) {
						c1 = new Long(((Long)c1).longValue() << ((Int)c2).value);
					}
					break;
				case Token.GTGT:
					if (c1 instanceof Int) {
						c1 = Int.toInt(((Int)c1).value >> ((Int)c2).value);
					} else if (c1 instanceof Long) {
						c1 = new Long(((Long)c1).longValue() >> ((Int)c2).value);
					}
					break;
				case Token.GTGTGT:
					if (c1 instanceof Int) {
						c1 = Int.toInt(((Int)c1).value >>> ((Int)c2).value);
					} else if (c1 instanceof Long) {
						c1 = new Long(((Long)c1).longValue() >>> ((Int)c2).value);
					}
					break;
			}
			return new ConstExpr(((ConstExpr)binary.lvalue).line, c1);
		}
		return binary;
	}

	/**
	 * <pre>
	 * CF:
	 *   { ... exprN-1; none; exprN+1; ... }   =&gt;   { ... exprN-1; exprN+1; ... }
	 *   { expr }   =&gt;   expr
	 *   {}   =&gt;   none
	 * </pre>
	 */
	public Object visitBlock(BlockExpr block, Object scope) {
		int i=0;
		while (i < block.exprs.size()) {
			Expr ex = (Expr)block.exprs.elementAt(i);
			ex = (Expr)ex.accept(this, block);
			if (ex instanceof NoneExpr) {
				block.exprs.removeElementAt(i);
			} else {
				block.exprs.setElementAt(ex, i);
				i++;
			}
		}
		switch (block.exprs.size()) {
			case 0:
				optimized = true;
				return new NoneExpr();
			case 1:
				Expr e = (Expr)block.exprs.elementAt(0);
				if (block.locals.isEmpty()) {
					optimized = true;
					return e;
				}
				return block;
			default:
				return block;
		}
	}
	
	/**
	 * <pre>
	 * CF:
	 *   cast (Number) const =&gt;  const
	 * </pre>
	 */
	public Object visitCast(CastExpr cast, Object scope) {
		cast.expr = (Expr)cast.expr.accept(this, scope);
		if (cast.expr instanceof ConstExpr && cast.toType.isSubtypeOf(BuiltinType.NUMBER)) {
			optimized = true;
			Type toType = cast.toType;
			Object cnst = ((ConstExpr)cast.expr).value;
			if (cnst instanceof Int) {
				int i = ((Int)cnst).value;
				if (toType == BuiltinType.DOUBLE) {
					cnst = new Double(i);
				} else if (toType == BuiltinType.FLOAT) {
					cnst = new Float(i);
				} else if (toType == BuiltinType.LONG) {
					cnst = new Long(i);
				} else if (toType == BuiltinType.CHAR) {
					cnst = Char.toChar((char)i);
				} else if (toType == BuiltinType.SHORT) {
					cnst = Int.toInt((short)i);
				} else if (toType == BuiltinType.BYTE) {
					cnst = Int.toInt((byte)i);
				}
			} else if (cnst instanceof Long) {
				long l = ((Long)cnst).longValue();
				if (toType == BuiltinType.DOUBLE) {
					cnst = new Double(l);
				} else if (toType == BuiltinType.FLOAT) {
					cnst = new Float(l);
				} else if (toType == BuiltinType.INT) {
					cnst = Int.toInt((int)l);
				} else if (toType == BuiltinType.CHAR) {
					cnst = Char.toChar((char)l);
				} else if (toType == BuiltinType.SHORT) {
					cnst = Int.toInt((short)l);
				} else if (toType == BuiltinType.BYTE) {
					cnst = Int.toInt((byte)l);
				}
			} else if (cnst instanceof Float) {
				float f = ((Float)cnst).floatValue();
				if (toType == BuiltinType.DOUBLE) {
					cnst = new Double(f);
				} else if (toType == BuiltinType.LONG) {
					cnst = new Long((long)f);
				} else if (toType == BuiltinType.INT) {
					cnst = Int.toInt((int)f);
				} else if (toType == BuiltinType.CHAR) {
					cnst = Char.toInt((char)f);
				} else if (toType == BuiltinType.SHORT) {
					cnst = Int.toInt((short)f);
				} else if (toType == BuiltinType.BYTE) {
					cnst = Int.toInt((byte)f);
				}
			} else if (cnst instanceof Double) {
				double d = ((Double)cnst).doubleValue();
				if (toType.equals(BuiltinType.FLOAT)) {
					cnst = new Float(d);
				} else if (toType.equals(BuiltinType.LONG)) {
					cnst = new Long((long)d);
				} else if (toType.equals(BuiltinType.INT)) {
					cnst = Int.toInt((int)d);
				} else if (toType == BuiltinType.CHAR) {
					cnst = Char.toInt((char)d);
				} else if (toType == BuiltinType.SHORT) {
					cnst = Int.toInt((short)d);
				} else if (toType == BuiltinType.BYTE) {
					cnst = Int.toInt((byte)d);
				}
			}
			return new ConstExpr(((ConstExpr)cast.expr).line, cnst);
		}
		return cast;
	}

	/**
	 * <pre>
	 * CF:
	 *   const op const  =&gt;  const
	 * 
	 * HELP COMPILER:
	 *   0 &lt; expr   =&gt;  expr &gt; 0
	 *   0 &gt; expr   =&gt;  expr &lt; 0
	 *   0 &lt;= expr  =&gt;  expr &gt;= 0
	 *   0 &gt;= expr  =&gt;  expr &lt;= 0
	 * </pre>
	 */
	public Object visitComparison(ComparisonExpr cmp, Object scope) {
		cmp.lvalue = (Expr)cmp.lvalue.accept(this, scope);
		cmp.rvalue = (Expr)cmp.rvalue.accept(this, scope);
		if (cmp.lvalue instanceof ConstExpr && cmp.rvalue instanceof ConstExpr) {
			Object c1 = ((ConstExpr)cmp.lvalue).value;
			Object c2 = ((ConstExpr)cmp.rvalue).value;
			optimized = true;
			switch (cmp.operator) {
				case Token.EQEQ:
					c1 = (c1.equals(c2)) ? Boolean.TRUE : Boolean.FALSE;
					break;
				case Token.NOTEQ:
					c1 = (c1.equals(c2)) ? Boolean.FALSE : Boolean.TRUE;
					break;
				case '<':
					if (c1 instanceof Int) {
						c1 = ((Int)c1).value < ((Int)c2).value ? Boolean.TRUE : Boolean.FALSE;
					} else if (c1 instanceof Long) {
						c1 = ((Long)c1).longValue() < ((Long)c2).longValue() ? Boolean.TRUE : Boolean.FALSE;
					} else if (c1 instanceof Float) {
						c1 = ((Float)c1).floatValue() < ((Float)c2).floatValue() ? Boolean.TRUE : Boolean.FALSE;
					} else if (c1 instanceof Double) {
						c1 = ((Double)c1).doubleValue() < ((Double)c2).doubleValue() ? Boolean.TRUE : Boolean.FALSE;
					}
					break;
				case '>':
					if (c1 instanceof Int) {
						c1 = ((Int)c1).value > ((Int)c2).value ? Boolean.TRUE : Boolean.FALSE;
					} else if (c1 instanceof Long) {
						c1 = ((Long)c1).longValue() > ((Long)c2).longValue() ? Boolean.TRUE : Boolean.FALSE;
					} else if (c1 instanceof Float) {
						c1 = ((Float)c1).floatValue() > ((Float)c2).floatValue() ? Boolean.TRUE : Boolean.FALSE;
					} else if (c1 instanceof Double) {
						c1 = ((Double)c1).doubleValue() > ((Double)c2).doubleValue() ? Boolean.TRUE : Boolean.FALSE;
					}
					break;
				case Token.LTEQ:
					if (c1 instanceof Int) {
						c1 = ((Int)c1).value <= ((Int)c2).value ? Boolean.TRUE : Boolean.FALSE;
					} else if (c1 instanceof Long) {
						c1 = ((Long)c1).longValue() <= ((Long)c2).longValue() ? Boolean.TRUE : Boolean.FALSE;
					} else if (c1 instanceof Float) {
						c1 = ((Float)c1).floatValue() <= ((Float)c2).floatValue() ? Boolean.TRUE : Boolean.FALSE;
					} else if (c1 instanceof Double) {
						c1 = ((Double)c1).doubleValue() <= ((Double)c2).doubleValue() ? Boolean.TRUE : Boolean.FALSE;
					}
					break;
				case Token.GTEQ:
					if (c1 instanceof Int) {
						c1 = ((Int)c1).value >= ((Int)c2).value ? Boolean.TRUE : Boolean.FALSE;
					} else if (c1 instanceof Long) {
						c1 = ((Long)c1).longValue() >= ((Long)c2).longValue() ? Boolean.TRUE : Boolean.FALSE;
					} else if (c1 instanceof Float) {
						c1 = ((Float)c1).floatValue() >= ((Float)c2).floatValue() ? Boolean.TRUE : Boolean.FALSE;
					} else if (c1 instanceof Double) {
						c1 = ((Double)c1).doubleValue() >= ((Double)c2).doubleValue() ? Boolean.TRUE : Boolean.FALSE;
					}
					break;
			}
			return new ConstExpr(((ConstExpr)cmp.lvalue).line, c1);
		} else if (cmp.lvalue instanceof ConstExpr) {
			Object cnst = ((ConstExpr)cmp.lvalue).value;
			if (cnst.equals(Int.ZERO)) {
				// to not duplicate code in EAsmWriter and
				// allow it to make assumptions only on rvalue
				optimized = true;
				Expr tmp = cmp.lvalue;
				cmp.lvalue = cmp.rvalue;
				cmp.rvalue = tmp;
				switch (cmp.operator) {
					case '<': cmp.operator = '>'; break;
					case '>': cmp.operator = '<'; break;
					case Token.LTEQ: cmp.operator = Token.GTEQ; break;
					case Token.GTEQ: cmp.operator = Token.LTEQ; break;
				}
				return cmp;
			}
		}
		return cmp;
	}

	/**
	 * <pre>
	 * CF:
	 *   "str"+const  =&gt;  "strconst"
	 * </pre>
	 */
	public Object visitConcat(ConcatExpr concat, Object scope) {
		Vector exprs = concat.exprs;
		for (int i=0; i<exprs.size(); i++) {
			Expr e = (Expr)((Expr)exprs.elementAt(i)).accept(this, scope);
			exprs.setElementAt(e, i);
		}
		int i=0;
		while (i < exprs.size()-1) {
			Expr e1 = (Expr)exprs.elementAt(i);
			Expr e2 = (Expr)exprs.elementAt(i+1);
			if (e1 instanceof ConstExpr && e2 instanceof ConstExpr) {
				Object o1 = ((ConstExpr)e1).value;
				Object o2 = ((ConstExpr)e2).value;
				if (!(o1 instanceof Func) && !(o2 instanceof Func)) {
					exprs.setElementAt(new ConstExpr(((ConstExpr)e1).line, String.valueOf(o1)+o2), i);
					exprs.removeElementAt(i+1);
					optimized = true;
				} else {
					i++;
				}
			} else {
				i++;
			}
		}
		if (exprs.size() == 1) {
			optimized = true;
			return exprs.elementAt(0);
		} else {
			return concat;
		}
	}

	public Object visitConst(ConstExpr cexpr, Object scope) {
		return cexpr;
	}

	/**
	 * <pre>
	 * DCE:
	 *   pop(const)            =&gt;  none
	 *   pop(var)              =&gt;  none
	 *   pop(ex1 op ex1)       =&gt;  {pop(ex1); pop(ex2)}
	 *   pop(op ex)            =&gt;  pop(ex)
	 *   pop(new [](i))        =&gt;  pop(i)
	 *   pop(new []{e1,...,eN} =&gt;  {pop(e1), ..., pop(eN)}
	 *   pop(array.len)        =&gt;  pop(array)
	 *   pop(array[i])         =&gt;  {pop(array); pop(i)}
	 * 
	 * CF: (to ease further DCE)
	 *   pop( {...; eN} )      =&gt;  {...; pop(eN)}
	 * </pre>
	 */
	public Object visitDiscard(DiscardExpr disc, Object scope) {
		disc.expr = (Expr)disc.expr.accept(this, scope);
		if (disc.expr instanceof ConstExpr || disc.expr instanceof VarExpr) {
			optimized = true;
			return new NoneExpr();
		} else if (disc.expr instanceof UnaryExpr) {
			optimized = true;
			disc.expr = ((UnaryExpr)disc.expr).expr;
			return disc;
		} else if (disc.expr instanceof BinaryExpr) {
			optimized = true;
			BinaryExpr binary = (BinaryExpr)disc.expr;
			BlockExpr block = new BlockExpr((Scope)scope);
			block.exprs.addElement(new DiscardExpr(binary.lvalue));
			block.exprs.addElement(new DiscardExpr(binary.rvalue));
			return block;
		} else if (disc.expr instanceof ComparisonExpr) {
			optimized = true;
			ComparisonExpr cmp = (ComparisonExpr)disc.expr;
			BlockExpr block = new BlockExpr((Scope)scope);
			block.exprs.addElement(new DiscardExpr(cmp.lvalue));
			block.exprs.addElement(new DiscardExpr(cmp.rvalue));
			return block;
		} else if (disc.expr instanceof BlockExpr) {
			optimized = true;
			BlockExpr block = (BlockExpr)disc.expr;
			disc.expr = (Expr)block.exprs.lastElement();
			block.exprs.setElementAt(disc, block.exprs.size()-1);
			return block;
		} else if (disc.expr instanceof ALenExpr) {
			optimized = true;
			disc.expr = ((ALenExpr)disc.expr).arrayexpr;
			return disc;
		} else if (disc.expr instanceof ALoadExpr) {
			optimized = true;
			ALoadExpr aload = (ALoadExpr)disc.expr;
			BlockExpr block = new BlockExpr((Scope)scope);
			block.exprs.addElement(new DiscardExpr(aload.arrayexpr));
			block.exprs.addElement(new DiscardExpr(aload.indexexpr));
			return block;
		} else if (disc.expr instanceof CastExpr) {
			optimized = true;
			disc.expr = ((CastExpr)disc.expr).expr;
			return disc;
		} else if (disc.expr instanceof ConcatExpr) {
			optimized = true;
			Vector exprs = ((ConcatExpr)disc.expr).exprs;
			BlockExpr block = new BlockExpr((Scope)scope);
			for (int i=0; i<exprs.size(); i++) {
				block.exprs.addElement(new DiscardExpr((Expr)exprs.elementAt(i)));
			}
			return block;
		} else if (disc.expr instanceof NewArrayExpr) {
			optimized = true;
			disc.expr = ((NewArrayExpr)disc.expr).lengthexpr;
			return disc;
		} else if (disc.expr instanceof NewArrayByEnumExpr) {
			optimized = true;
			Expr[] exprs = ((NewArrayByEnumExpr)disc.expr).initializers;
			BlockExpr block = new BlockExpr((Scope)scope);
			for (int i=0; i<exprs.length; i++) {
				if (exprs[i] != null) block.exprs.addElement(new DiscardExpr(exprs[i]));
			}
			return block;
		}
		return disc;
	}

	/**
	 * <pre>
	 * CF:
	 *   do expr; while (false)  =&gt;  expr;
	 * </pre>
	 */
	public Object visitDoWhile(DoWhileExpr wexpr, Object scope) {
		wexpr.condition = (Expr)wexpr.condition.accept(this, scope);
		wexpr.body = (Expr)wexpr.body.accept(this, scope);
		if (wexpr.condition instanceof ConstExpr) {
			Object cnst = ((ConstExpr)wexpr.condition).value;
			if (cnst.equals(Boolean.FALSE)) {
				optimized = true;
				return wexpr.body;
			}
		}
		return wexpr;
	}

	/**
	 * <pre>
	 * CF:
	 *   const.tostr()  =>  "const"
	 * </pre>
	 */
	public Object visitFCall(FCallExpr fcall, Object scope) {
		fcall.fload = (Expr)fcall.fload.accept(this, scope);
		for (int i=0; i<fcall.args.length; i++) {
			fcall.args[i] = (Expr)fcall.args[i].accept(this, scope);
		}
		if (fcall.fload instanceof ConstExpr) {
			Func f = (Func)((ConstExpr)fcall.fload).value;
			if (f.signature.equals("Any.tostr") && fcall.args[0] instanceof ConstExpr) {
				f.hits--;
				Object cnst = ((ConstExpr)fcall.args[0]).value;
				if (!(cnst instanceof Func)) {
					optimized = true;
					return new ConstExpr(((ConstExpr)fcall.args[0]).line, String.valueOf(cnst));
				}
			}
/* Inlining functions will be a part of -O2
			if (f.body != null && (f.body instanceof ConstExpr || f.body instanceof NoneExpr)) {
				// we don't need to call function but we still
				// need to calculate its arguments
				f.hits--;
				BlockExpr block = new BlockExpr((Scope)scope);
				for (int i=0; i<fcall.args.length; i++) {
					block.exprs.addElement(new DiscardExpr(fcall.args[i]));
				}
				block.exprs.addElement(f.body);
				optimized = true;
				return block.accept(this, scope);
			}
*/
		}
		return fcall;
	}

	/**
	 * <pre>
	 * DCE:
	 *   if (true) e1 else e2   =&gt;  e1
	 *   if (false) e1 else e2  =&gt;  e2
	 *
	 * CF:
	 *   if (cond) { } else expr  =&gt;  if (!cond) expr
	 *   if (cond) { } else { }   =&gt;  cond
	 * </pre>
	 */
	public Object visitIf(IfExpr ifexpr, Object scope) {
		ifexpr.condition = (Expr)ifexpr.condition.accept(this, scope);
		ifexpr.ifexpr = (Expr)ifexpr.ifexpr.accept(this, scope);
		ifexpr.elseexpr = (Expr)ifexpr.elseexpr.accept(this, scope);
		if (ifexpr.condition instanceof ConstExpr) {
			optimized = true;
			if (((ConstExpr)ifexpr.condition).value.equals(Boolean.TRUE)) {
				return ifexpr.ifexpr;
			} else {
				return ifexpr.elseexpr;
			}
		}
		if (ifexpr.ifexpr instanceof NoneExpr) {
			optimized = true;
			if (ifexpr.elseexpr instanceof NoneExpr) {
				return ifexpr.condition;
			} else {
				ifexpr.condition = new UnaryExpr('!', ifexpr.condition);
				Expr empty = ifexpr.ifexpr;
				ifexpr.ifexpr = ifexpr.elseexpr;
				ifexpr.elseexpr = empty;
			}
		}
/* Makes line numbers inconsistent, it is better to handle "if (!expr)" by EAsmWriter
		if (ifexpr.condition instanceof UnaryExpr) {
			optimized = true;
			ifexpr.condition = ((UnaryExpr)ifexpr.condition).expr;
			Expr tmp = ifexpr.ifexpr;
			ifexpr.ifexpr = ifexpr.elseexpr;
			ifexpr.elseexpr = tmp;
			return ifexpr;
		}
*/
		return ifexpr;
	}

	public Object visitNewArray(NewArrayExpr newarray, Object scope) {
		newarray.lengthexpr = (Expr)newarray.lengthexpr.accept(this, scope);
		return newarray;
	}

	public Object visitNewArrayByEnum(NewArrayByEnumExpr newarray, Object scope) {
		for (int i=0; i < newarray.initializers.length; i++) {
			if (newarray.initializers[i] != null)
			newarray.initializers[i] = (Expr)newarray.initializers[i].accept(this, scope);
		}
		return newarray;
	}

	public Object visitNone(NoneExpr none, Object scope) {
		return none;
	}

	/**
	 * <pre>
	 * DCE:
	 *   switch (const) { ... }  =&gt;  branch
	 *
	 * CF:
	 *   switch { else: expr }  =&gt;  expr
	 * </pre>
	 */
	public Object visitSwitch(SwitchExpr swexpr, Object scope) {
		// optimize subtrees
		swexpr.indexexpr = (Expr)swexpr.indexexpr.accept(this, scope);
		for (int i=0; i<swexpr.exprs.size(); i++) {
			Expr e = (Expr)swexpr.exprs.elementAt(i);
			swexpr.exprs.setElementAt(e.accept(this, scope), i);
		}
		if (swexpr.elseexpr != null) swexpr.elseexpr = (Expr)swexpr.elseexpr.accept(this, scope);
		// optimize switch (const)
		if (swexpr.indexexpr instanceof ConstExpr) {
			int choice = ((Int)((ConstExpr)swexpr.indexexpr).value).value;
			optimized = true;
			for (int br=0; br < swexpr.keys.size(); br++) {
				final int[] indices = (int[])swexpr.keys.elementAt(br);
				for (int i=0; i < indices.length; i++) {
					if (indices[i] == choice) return swexpr.exprs.elementAt(br);
				}
			}
			if (swexpr.elseexpr != null) return swexpr.elseexpr;
			else return new NoneExpr();
		}
		// optimize if there is only else branch
		if (swexpr.keys.isEmpty()) {
			optimized = true;
			if (swexpr.elseexpr != null) return swexpr.elseexpr;
			else return new NoneExpr();
		}
		return swexpr;
	}

	/**
	 * <pre>
	 * DCE:
	 *   try { } catch { ... }  =&gt;  none
	 * </pre>
	 */
	public Object visitTryCatch(TryCatchExpr trycatch, Object scope) {
		trycatch.tryexpr = (Expr)trycatch.tryexpr.accept(this, scope);
		trycatch.catchexpr = (Expr)trycatch.catchexpr.accept(this, scope);
		if (trycatch.tryexpr instanceof NoneExpr) {
			optimized = true;
			return trycatch.tryexpr;
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
	 *  !(a == b)  =&gt;  a != b
	 *  !(a != b)  =&gt;  a == b
	 *  !(a &lt; b)   =&gt;  a &gt;= b
	 *  !(a &gt; b)   =&gt;  a &lt;= b
	 *  !(a &lt;= b)  =&gt;  a &gt; b
	 *  !(a &gt;= b)  =&gt;  a &lt; b
	 * </pre>
	 */
	public Object visitUnary(UnaryExpr unary, Object scope) {
		// optimize subexpression
		unary.expr = (Expr)unary.expr.accept(this, scope);
		if (unary.operator == '+') {
			optimized = true;
			return unary.expr;
		}
		// optimize "op const"
		if (unary.expr instanceof ConstExpr) {
			Object cnst = ((ConstExpr)unary.expr).value;
			switch (unary.operator) {
				case '!':
					if (cnst.equals(Boolean.TRUE)) cnst = Boolean.FALSE;
					else cnst = Boolean.TRUE;
					break;
				case '-':
					if (cnst instanceof Int) {
						int i = ((Int)cnst).value;
						cnst = Int.toInt(-i);
					} else if (cnst instanceof Long) {
						long l = ((Long)cnst).longValue();
						cnst = new Long(-l);
					} else if (cnst instanceof Float) {
						float f = ((Float)cnst).floatValue();
						cnst = new Float(-f);
					} else if (cnst instanceof Double) {
						double d = ((Double)cnst).doubleValue();
						cnst = new Double(-d);
					}
					break;
				case '~':
					if (cnst instanceof Int) {
						int i = ((Int)cnst).value;
						cnst = Int.toInt(~i);
					} else if (cnst instanceof Long) {
						long l = ((Long)cnst).longValue();
						cnst = new Long(~l);
					}
					break;
			}
			optimized = true;
			return new ConstExpr(((ConstExpr)unary.expr).line, cnst);
		}
		// optimize !(expr cmp expr)
		if (unary.expr instanceof ComparisonExpr) {
			optimized = true;
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
		}
		return unary;
	}
	
	public Object visitVar(VarExpr vexpr, Object scope) {
		return vexpr;
	}
	
	/**
	 * <pre>
	 * DCE:
	 *   while (false) expr;   =&gt;   none
	 * </pre>
	 */
	public Object visitWhile(WhileExpr wexpr, Object scope) {
		wexpr.condition = (Expr)wexpr.condition.accept(this, scope);
		wexpr.body = (Expr)wexpr.body.accept(this, scope);
		if (wexpr.condition instanceof ConstExpr) {
			Object cnst = ((ConstExpr)wexpr.condition).value;
			optimized = true;
			if (cnst.equals(Boolean.FALSE)) {
				return new NoneExpr();
			}
		}
		return wexpr;
	}
}
