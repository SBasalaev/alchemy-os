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

import alchemy.core.Int;
import alchemy.nec.tree.*;
import java.util.Vector;

/**
 * Simple optimizer.
 * Folds constants and eliminates dead code.
 * <p>
 * Visiting methods accept Scope as argument and
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

	public Object visitALen(ALenExpr alen, Object scope) {
		alen.arrayexpr = (Expr)alen.arrayexpr.accept(this, scope);
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
	 * CF:
	 *   const op const  =>  const
	 */
	public Object visitBinary(BinaryExpr binary, Object scope) {
		binary.lvalue = (Expr)binary.lvalue.accept(this, scope);
		binary.rvalue = (Expr)binary.rvalue.accept(this, scope);
		if (binary.lvalue instanceof ConstExpr && binary.rvalue instanceof ConstExpr) {
			Object c1 = ((ConstExpr)binary.lvalue).value;
			Object c2 = ((ConstExpr)binary.rvalue).value;
			optimized = true;
			switch (binary.operator) {
				case '+':
					if (c1 instanceof Int) {
						c1 = new Int(((Int)c1).value + ((Int)c2).value);
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
						c1 = new Int(((Int)c1).value - ((Int)c2).value);
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
						c1 = new Int(((Int)c1).value * ((Int)c2).value);
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
						c1 = new Int(((Int)c1).value / ((Int)c2).value);
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
						c1 = new Int(((Int)c1).value % ((Int)c2).value);
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
						c1 = new Int(((Int)c1).value & ((Int)c2).value);
					} else if (c1 instanceof Long) {
						c1 = new Long(((Long)c1).longValue() & ((Long)c2).longValue());
					} else if (c1 instanceof Boolean) {
						c1 = c1.equals(Boolean.TRUE) ? c2 : Boolean.FALSE;
					}
					break;
				case '|':
					if (c1 instanceof Int) {
						c1 = new Int(((Int)c1).value | ((Int)c2).value);
					} else if (c1 instanceof Long) {
						c1 = new Long(((Long)c1).longValue() | ((Long)c2).longValue());
					} else if (c1 instanceof Boolean) {
						c1 = c1.equals(Boolean.TRUE) ? Boolean.TRUE : c2;
					}
					break;
				case '^':
					if (c1 instanceof Int) {
						c1 = new Int(((Int)c1).value ^ ((Int)c2).value);
					} else if (c1 instanceof Long) {
						c1 = new Long(((Long)c1).longValue() ^ ((Long)c2).longValue());
					} else if (c1 instanceof Boolean) {
						c1 = c1.equals(c2) ? Boolean.FALSE : Boolean.TRUE;
					}
					break;
				case Token.LTLT:
					if (c1 instanceof Int) {
						c1 = new Int(((Int)c1).value << ((Int)c2).value);
					} else if (c1 instanceof Long) {
						c1 = new Long(((Long)c1).longValue() << ((Int)c2).value);
					}
					break;
				case Token.GTGT:
					if (c1 instanceof Int) {
						c1 = new Int(((Int)c1).value >> ((Int)c2).value);
					} else if (c1 instanceof Long) {
						c1 = new Long(((Long)c1).longValue() >> ((Int)c2).value);
					}
					break;
				case Token.GTGTGT:
					if (c1 instanceof Int) {
						c1 = new Int(((Int)c1).value >>> ((Int)c2).value);
					} else if (c1 instanceof Long) {
						c1 = new Long(((Long)c1).longValue() >>> ((Int)c2).value);
					}
					break;
			}
			return new ConstExpr(binary.lvalue.line, c1);
		}
		return binary;
	}

	/**
	 * CF:
	 *   { ... exprN-1; none; exprN+1; ... }   =>   { ... exprN-1; exprN+1; ... }
	 *   { expr }   =>   expr
	 *   {}   =>   none
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
				return new NoneExpr(block.line);
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
	 * CF:
	 *   cast (Number) const  =>  const
	 */
	public Object visitCast(CastExpr cast, Object scope) {
		cast.expr = (Expr)cast.expr.accept(this, scope);
		if (cast.expr instanceof ConstExpr && cast.toType.isSubtypeOf(BuiltinType.NUMBER)) {
			optimized = true;
			Type toType = cast.toType;
			Object cnst = ((ConstExpr)cast.expr).value;
			if (cnst instanceof Int) {
				int i = ((Int)cnst).value;
				if (toType.equals(BuiltinType.DOUBLE)) {
					cnst = new Double(i);
				} else if (toType.equals(BuiltinType.FLOAT)) {
					cnst = new Float(i);
				} else if (toType.equals(BuiltinType.LONG)) {
					cnst = new Long(i);
				}
			} else if (cnst instanceof Long) {
				long l = ((Long)cnst).longValue();
				if (toType.equals(BuiltinType.DOUBLE)) {
					cnst = new Double(l);
				} else if (toType.equals(BuiltinType.FLOAT)) {
					cnst = new Float(l);
				} else if (toType.equals(BuiltinType.INT)) {
					cnst = new Int((int)l);
				}
			} else if (cnst instanceof Float) {
				float f = ((Float)cnst).floatValue();
				if (toType.equals(BuiltinType.DOUBLE)) {
					cnst = new Double(f);
				} else if (toType.equals(BuiltinType.LONG)) {
					cnst = new Long((long)f);
				} else if (toType.equals(BuiltinType.INT)) {
					cnst = new Int((int)f);
				}
			} else if (cnst instanceof Double) {
				double d = ((Double)cnst).doubleValue();
				if (toType.equals(BuiltinType.FLOAT)) {
					cnst = new Float(d);
				} else if (toType.equals(BuiltinType.LONG)) {
					cnst = new Long((long)d);
				} else if (toType.equals(BuiltinType.INT)) {
					cnst = new Int((int)d);
				}
			}
			return new ConstExpr(cast.line, cnst);
		}
		return cast;
	}

	/**
	 * CF:
	 *  const op const  =>  const
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
					if (c1 == null) {
						c1 = (c2 == null) ? Boolean.TRUE : Boolean.FALSE;
					} else {
						c1 = (c1.equals(c2)) ? Boolean.TRUE : Boolean.FALSE;
					}
					break;
				case Token.NOTEQ:
					if (c1 == null) {
						c1 = (c2 == null) ? Boolean.FALSE : Boolean.TRUE;
					} else {
						c1 = (c1.equals(c2)) ? Boolean.FALSE : Boolean.TRUE;
					}
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
			return new ConstExpr(cmp.lvalue.line, c1);
		} else if (cmp.lvalue instanceof ConstExpr) {
			Object cnst = ((ConstExpr)cmp.lvalue).value;
			if (cnst == null || cnst.equals(Int.ZERO)) {
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
	 * CF:
	 *   "str"+const  =>  "strconst"
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
					exprs.setElementAt(new ConstExpr(e1.line, String.valueOf(o1)+o2), i);
					exprs.removeElementAt(i+1);
				} else {
					i++;
				}
			} else {
				i++;
			}
		}
		return concat;
	}

	public Object visitConst(ConstExpr cexpr, Object data) {
		return cexpr;
	}

	/**
	 * DCE: (unused operations)
	 *   const;       =>  none
	 *   var;         =>  none
	 *   ex1 op ex1;  =>  {ex1; ex2;}
	 *   op ex;       =>  ex;
	 *   array.len;   =>  array;
	 *   array[i];    =>  {array; i;}
	 * CF: (to ease further DCE)
	 *   {...; eN};   =>  {...; eN; }
	 */
	public Object visitDiscard(DiscardExpr disc, Object scope) {
		disc.expr = (Expr)disc.expr.accept(this, scope);
		if (disc.expr instanceof ConstExpr || disc.expr instanceof VarExpr) {
			optimized = true;
			return new NoneExpr(disc.line);
		} else if (disc.expr instanceof UnaryExpr) {
			optimized = true;
			disc.expr = ((UnaryExpr)disc.expr).expr;
			return disc;
		} else if (disc.expr instanceof BinaryExpr) {
			optimized = true;
			BinaryExpr binary = (BinaryExpr)disc.expr;
			BlockExpr block = new BlockExpr(binary.line, (Scope)scope);
			block.exprs.addElement(new DiscardExpr(binary.lvalue));
			block.exprs.addElement(new DiscardExpr(binary.rvalue));
			return block;
		} else if (disc.expr instanceof ComparisonExpr) {
			optimized = true;
			ComparisonExpr cmp = (ComparisonExpr)disc.expr;
			BlockExpr block = new BlockExpr(cmp.line, (Scope)scope);
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
			BlockExpr block = new BlockExpr(aload.line, (Scope)scope);
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
			BlockExpr block = new BlockExpr(disc.line, (Scope)scope);
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
			BlockExpr block = new BlockExpr(disc.line, (Scope)scope);
			for (int i=0; i<exprs.length; i++) {
				if (exprs[i] != null) block.exprs.addElement(new DiscardExpr(exprs[i]));
			}
			return block;
		}
		return disc;
	}

	/**
	 * CF:
	 *  do expr; while (false)  =>  expr;
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
	 * CF:
	 *   const.tostr()  =>  "const"
	 *   remove calls to empty and constant functions
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
					return new ConstExpr(fcall.args[0].line, String.valueOf(cnst));
				}
			}
			if (f.body != null && (f.body instanceof ConstExpr || f.body instanceof NoneExpr)) {
				// we don't need to call function but we still
				// need to calculate its arguments
				f.hits--;
				BlockExpr block = new BlockExpr(fcall.line, (Scope)scope);
				for (int i=0; i<fcall.args.length; i++) {
					block.exprs.addElement(new DiscardExpr(fcall.args[i]));
				}
				block.exprs.addElement(f.body);
				optimized = true;
				return block.accept(this, scope);
			}
		}
		return fcall;
	}

	/**
	 * DCE:
	 *  if (true) e1 else e2   =>  e1
	 *  if (false) e1 else e2  =>  e2
	 * CF:
	 *  if (!expr) e1 else e2  =>  if (expr) e2 else e1
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
		if (ifexpr.condition instanceof UnaryExpr) {
			optimized = true;
			ifexpr.condition = ((UnaryExpr)ifexpr.condition).expr;
			Expr tmp = ifexpr.ifexpr;
			ifexpr.ifexpr = ifexpr.elseexpr;
			ifexpr.elseexpr = tmp;
			return ifexpr;
		}
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

	public Object visitSwitch(SwitchExpr swexpr, Object scope) {
		swexpr.indexexpr = (Expr)swexpr.indexexpr.accept(this, scope);
		for (int i=0; i<swexpr.exprs.size(); i++) {
			Expr e = (Expr)swexpr.exprs.elementAt(i);
			swexpr.exprs.setElementAt(e.accept(this, scope), i);
		}
		if (swexpr.elseexpr != null) swexpr.elseexpr = (Expr)swexpr.elseexpr.accept(this, scope);
		return swexpr;
	}

	public Object visitTryCatch(TryCatchExpr trycatch, Object scope) {
		trycatch.tryexpr = (Expr)trycatch.tryexpr.accept(this, scope);
		trycatch.catchexpr = (Expr)trycatch.catchexpr.accept(this, scope);
		return trycatch;
	}

	/**
	 * CF:
	 *  +expr   =>  expr
	 *  -const  =>  const
	 *  ~const  =>  const
	 *  !const  =>  const
	 */
	public Object visitUnary(UnaryExpr unary, Object scope) {
		unary.expr = (Expr)unary.expr.accept(this, scope);
		if (unary.operator == '+') {
			optimized = true;
			return unary.expr;
		}
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
						cnst = new Int(-i);
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
						cnst = new Int(~i);
					} else if (cnst instanceof Long) {
						long l = ((Long)cnst).longValue();
						cnst = new Long(~l);
					}
					break;
			}
			optimized = true;
			return new ConstExpr(unary.expr.line, cnst);
		}
		return unary;
	}
	
	public Object visitVar(VarExpr vexpr, Object scope) {
		return vexpr;
	}
	
	/**
	 * DCE:
	 *  while (false) expr;   =>   none
	 */
	public Object visitWhile(WhileExpr wexpr, Object scope) {
		wexpr.condition = (Expr)wexpr.condition.accept(this, scope);
		wexpr.body = (Expr)wexpr.body.accept(this, scope);
		if (wexpr.condition instanceof ConstExpr) {
			Object cnst = ((ConstExpr)wexpr.condition).value;
			optimized = true;
			if (cnst.equals(Boolean.FALSE)) {
				return new NoneExpr(wexpr.line);
			} else if (cnst.equals(Boolean.TRUE)) {
				// do-while is shorter when writing native code
				return new DoWhileExpr(wexpr.line, wexpr.condition, wexpr.body);
			}
		}
		return wexpr;
	}
}
