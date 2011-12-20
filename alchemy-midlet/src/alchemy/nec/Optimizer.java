package alchemy.nec;

import alchemy.nec.tree.*;

/**
 * Simple optimizer.
 * Folds constants and eliminates dead code.
 * @author Sergey Basalaev
 */
class Optimizer implements ExprVisitor {

	private Expr optimized = null;

	public void visitUnit(Unit u) {
		for (int i=0; i<u.funcs.size(); i++) {
			visitFunc((Func)u.funcs.elementAt(i));
		}
	}

	public void visitFunc(Func f) {
		if (f.body != null) {
			f.body.accept(this, null);
			if (optimized != null) f.body = optimized;
		}
	}

	public void visitALen(ALenExpr alen, Object data) {
		alen.arrayexpr.accept(this, data);
		optimized = null;
	}

	public void visitALoad(ALoadExpr aload, Object data) {
		aload.arrayexpr.accept(this, data);
		aload.indexexpr.accept(this, data);
		optimized = null;
	}

	public void visitAStore(AStoreExpr astore, Object data) {
		astore.arrayexpr.accept(this, data);
		astore.indexexpr.accept(this, data);
		astore.assignexpr.accept(this, data);
	}

	public void visitAssign(AssignExpr assign, Object data) {
		assign.expr.accept(this, data);
		optimized = null;
	}

	/**
	 * CF:
	 *   const op const   =>   const
	 */
	public void visitBinary(BinaryExpr binary, Object data) {
		binary.lvalue.accept(this, data);
		if (optimized != null) binary.lvalue = optimized;
		binary.rvalue.accept(this, data);
		if (optimized != null) binary.rvalue = optimized;
		if (binary.lvalue instanceof ConstExpr && binary.rvalue instanceof ConstExpr) {
			Object lval = ((ConstExpr)binary.lvalue).value;
			Object rval = ((ConstExpr)binary.rvalue).value;
			if (lval instanceof Integer) {
				switch (binary.operator) {
					case '+':
						lval = new Integer(((Integer)lval).intValue() + ((Integer)rval).intValue());
						break;
					case '-':
						lval = new Integer(((Integer)lval).intValue() - ((Integer)rval).intValue());
						break;
					case '*':
						lval = new Integer(((Integer)lval).intValue() * ((Integer)rval).intValue());
						break;
					case '/':
						if (((Integer)rval).intValue() != 0) {
							lval = new Integer(((Integer)lval).intValue() / ((Integer)rval).intValue());
						} else {
							optimized = null;
							return;
						}
						break;
					case '%':
						if (((Integer)rval).intValue() != 0) {
							lval = new Integer(((Integer)lval).intValue() % ((Integer)rval).intValue());
						} else {
							optimized = null;
							return;
						}
						break;
					case '=':
						if (lval.equals(rval)) {
							lval = new Integer(0);
						} else if (((Integer)lval).intValue() < ((Integer)rval).intValue()) {
							lval = new Integer(-1);
						} else {
							lval = new Integer(1);
						}
						break;
					case '&':
						lval = new Integer(((Integer)lval).intValue() & ((Integer)rval).intValue());
						break;
					case '|':
						lval = new Integer(((Integer)lval).intValue() | ((Integer)rval).intValue());
						break;
					case '^':
						lval = new Integer(((Integer)lval).intValue() ^ ((Integer)rval).intValue());
						break;
					case Tokenizer.TT_LTLT:
						lval = new Integer(((Integer)lval).intValue() << ((Integer)rval).intValue());
						break;
					case Tokenizer.TT_GTGT:
						lval = new Integer(((Integer)lval).intValue() >> ((Integer)rval).intValue());
						break;
					case Tokenizer.TT_GTGTGT:
						lval = new Integer(((Integer)lval).intValue() >>> ((Integer)rval).intValue());
						break;
				}
			} else if (lval instanceof Long) {
				switch (binary.operator) {
					case '+':
						lval = new Long(((Long)lval).longValue() + ((Long)rval).longValue());
						break;
					case '-':
						lval = new Long(((Long)lval).longValue() - ((Long)rval).longValue());
						break;
					case '*':
						lval = new Long(((Long)lval).longValue() * ((Long)rval).longValue());
						break;
					case '/':
						if (((Long)rval).longValue() != 0) {
							lval = new Long(((Long)lval).longValue() / ((Long)rval).longValue());
						} else {
							optimized = null;
							return;
						}
						break;
					case '%':
						if (((Long)rval).longValue() != 0) {
							lval = new Long(((Long)lval).longValue() % ((Long)rval).longValue());
						} else {
							optimized = null;
							return;
						}
						break;
					case '=':
						if (lval.equals(rval)) {
							lval = new Integer(0);
						} else if (((Long)lval).longValue() < ((Long)rval).longValue()) {
							lval = new Integer(-1);
						} else {
							lval = new Integer(1);
						}
						break;
					case '&':
						lval = new Long(((Long)lval).longValue() & ((Long)rval).longValue());
						break;
					case '|':
						lval = new Long(((Long)lval).longValue() | ((Long)rval).longValue());
						break;
					case '^':
						lval = new Long(((Long)lval).longValue() ^ ((Long)rval).longValue());
						break;
					case Tokenizer.TT_LTLT:
						lval = new Long(((Long)lval).longValue() << ((Integer)rval).intValue());
						break;
					case Tokenizer.TT_GTGT:
						lval = new Long(((Long)lval).longValue() >> ((Integer)rval).intValue());
						break;
					case Tokenizer.TT_GTGTGT:
						lval = new Long(((Long)lval).longValue() >>> ((Integer)rval).intValue());
						break;
				}
			} else if (lval instanceof Float) {
				switch (binary.operator) {
					case '+':
						lval = new Float(((Float)lval).floatValue() + ((Float)rval).floatValue());
						break;
					case '-':
						lval = new Float(((Float)lval).floatValue() - ((Float)rval).floatValue());
						break;
					case '*':
						lval = new Float(((Float)lval).floatValue() * ((Float)rval).floatValue());
						break;
					case '/':
						lval = new Float(((Float)lval).floatValue() / ((Float)rval).floatValue());
						break;
					case '%':
						lval = new Float(((Float)lval).floatValue() % ((Float)rval).floatValue());
						break;
					case '=':
						if (lval.equals(rval)) {
							lval = new Integer(0);
						} else if (((Float)lval).longValue() < ((Float)rval).longValue()) {
							lval = new Integer(-1);
						} else {
							lval = new Integer(1);
						}
						break;
				}
			} else if (lval instanceof Double) {
				switch (binary.operator) {
					case '+':
						lval = new Double(((Double)lval).doubleValue() + ((Double)rval).doubleValue());
						break;
					case '-':
						lval = new Double(((Double)lval).doubleValue() - ((Double)rval).doubleValue());
						break;
					case '*':
						lval = new Double(((Double)lval).doubleValue() * ((Double)rval).doubleValue());
						break;
					case '/':
						lval = new Double(((Double)lval).doubleValue() / ((Double)rval).doubleValue());
						break;
					case '%':
						lval = new Double(((Double)lval).doubleValue() % ((Double)rval).doubleValue());
						break;
					case '=':
						if (lval.equals(rval)) {
							lval = new Integer(0);
						} else if (((Long)lval).longValue() < ((Long)rval).longValue()) {
							lval = new Integer(-1);
						} else {
							lval = new Integer(1);
						}
						break;
				}
			} else if (lval instanceof Boolean) {
				switch (binary.operator) {
					case '&':
						lval = (((Boolean)lval).booleanValue() & ((Boolean)rval).booleanValue()
								? Boolean.TRUE : Boolean.FALSE);
						break;
					case '|':
						lval = (((Boolean)lval).booleanValue() | ((Boolean)rval).booleanValue()
								? Boolean.TRUE : Boolean.FALSE);
						break;
					case '^':
						lval = (((Boolean)lval).booleanValue() ^ ((Boolean)rval).booleanValue()
								? Boolean.TRUE : Boolean.FALSE);
						break;
					case '=':
						if (lval.equals(rval)) {
							lval = new Integer(0);
						} else {
							lval = new Integer(1);
						}
						break;
				}
			}
			optimized = new ConstExpr(lval);
			return;
		}
		optimized = null;
	}

	/**
	 * CF:
	 *   { ... exprN-1; none; exprN+1; ... }   =>   { ... exprN-1; exprN+1; ... }
	 *   { expr }   =>   expr
	 *   {}   =>   none
	 */
	public void visitBlock(BlockExpr block, Object data) {
		int i=0;
		while (i < block.exprs.size()) {
			Expr ex = (Expr)block.exprs.elementAt(i);
			ex.accept(this, data);
			if (optimized != null) ex = optimized;
			if (ex instanceof NoneExpr) {
				block.exprs.removeElementAt(i);
			} else {
				block.exprs.setElementAt(ex, i);
				i++;
			}
		}
		switch (block.exprs.size()) {
			case 0:
				optimized = new NoneExpr();
				return;
			case 1:
				Expr e = (Expr)block.exprs.elementAt(0);
				if (block.locals.isEmpty()) {
					optimized = e;
					return;
				}
		}
		optimized = null;
	}

	public void visitCast(CastExpr cast, Object data) {
		cast.expr.accept(this, data);
		if (optimized != null) cast.expr = optimized;
		if (cast.expr instanceof CastExpr) {
			optimized = new CastExpr(cast.toType, ((CastExpr)cast.expr).expr);
			return;
		}
		optimized = null;
	}

	/**
	 * CF:
	 *   cast(type) number   =>   castednumber
	 */
	public void visitCastPrimitive(CastPrimitiveExpr cast, Object data) {
		cast.expr.accept(this, data);
		if (cast.expr instanceof ConstExpr) {
			Object cnst = ((ConstExpr)cast.expr).value;
			switch (cast.casttype) {
				case CastPrimitiveExpr.I2L:
					cnst = new Long(((Integer)cnst).longValue());
					break;
				case CastPrimitiveExpr.I2F:
					cnst = new Float(((Integer)cnst).floatValue());
					break;
				case CastPrimitiveExpr.I2D:
					cnst = new Double(((Integer)cnst).doubleValue());
					break;
				case CastPrimitiveExpr.L2I:
					cnst = new Integer((int)((Long)cnst).longValue());
					break;
				case CastPrimitiveExpr.L2F:
					cnst = new Float(((Long)cnst).floatValue());
					break;
				case CastPrimitiveExpr.L2D:
					cnst = new Double(((Long)cnst).doubleValue());
					break;
				case CastPrimitiveExpr.F2I:
					cnst = new Integer(((Float)cnst).intValue());
					break;
				case CastPrimitiveExpr.F2L:
					cnst = new Long(((Float)cnst).longValue());
					break;
				case CastPrimitiveExpr.F2D:
					cnst = new Double(((Float)cnst).doubleValue());
					break;
				case CastPrimitiveExpr.D2I:
					cnst = new Integer(((Double)cnst).intValue());
					break;
				case CastPrimitiveExpr.D2L:
					cnst = new Long(((Double)cnst).longValue());
					break;
				case CastPrimitiveExpr.D2F:
					cnst = new Float(((Double)cnst).floatValue());
					break;
			}
			optimized = new ConstExpr(cnst);
			return;
		}
		optimized = null;
	}

	public void visitConst(ConstExpr cexpr, Object data) {
		optimized = null;
	}

	/**
	 * DCE:
	 *   const;   =>   none
	 *   var;     =>   none
	 */
	public void visitDiscard(DiscardExpr disc, Object data) {
		disc.expr.accept(this, data);
		if (optimized != null) disc.expr = optimized;
		if (disc.expr instanceof ConstExpr || disc.expr instanceof VarExpr) {
			optimized = new NoneExpr();
			return;
		}
		optimized = null;
	}

	public void visitFCall(FCallExpr fcall, Object data) {
		fcall.fload.accept(this, data);
		if (optimized != null) fcall.fload = optimized;
		for (int i=0; i<fcall.args.length; i++) {
			fcall.args[i].accept(this, data);
			if (optimized != null) fcall.args[i] = optimized;
		}
		optimized = null;
	}

	/**
	 * DCE:
	 *   if (true)  expr1 else expr2   =>   expr1
	 *   if (false) expr1 else expr2   =>   expr2
	 * CF:
	 *   if (if? true else false) expr1 else expr2  =>  if? expr1 else expr2
	 *   // the latter is common due to implementation of comparison
	 */
	public void visitIf(IfExpr expr, Object data) {
		//optimize children
		expr.condition.accept(this, data);
		if (optimized != null) expr.condition = optimized;
		expr.ifexpr.accept(this, data);
		if (optimized != null) expr.ifexpr = optimized;
		expr.elseexpr.accept(this, data);
		if (optimized != null) expr.elseexpr = optimized;
		//test condition
		if (expr.condition instanceof ConstExpr) {
			Object cond = ((ConstExpr)expr.condition).value;
			switch (expr.type) {
				case IfExpr.TRUE:
					if (Boolean.TRUE.equals(cond)) {
						optimized = expr.ifexpr;
						return;
					} else {
						optimized = expr.elseexpr;
						return;
					}
				case IfExpr.FALSE:
					if (Boolean.FALSE.equals(cond)) {
						optimized = expr.ifexpr;
						return;
					} else {
						optimized = expr.elseexpr;
						return;
					}
				case IfExpr.NULL:
					if (cond == null) {
						optimized = expr.ifexpr;
						return;
					} else {
						optimized = expr.elseexpr;
						return;
					}
				case IfExpr.NOTNULL:
					if (cond != null) {
						optimized = expr.ifexpr;
						return;
					} else {
						optimized = expr.elseexpr;
						return;
					}
				case IfExpr.ZERO:
					if (((Integer)cond).intValue() == 0) {
						optimized = expr.ifexpr;
						return;
					} else {
						optimized = expr.elseexpr;
						return;
					}
				case IfExpr.NOTZERO:
					if (((Integer)cond).intValue() != 0) {
						optimized = expr.ifexpr;
						return;
					} else {
						optimized = expr.elseexpr;
						return;
					}
				case IfExpr.NEG:
					if (((Integer)cond).intValue() < 0) {
						optimized = expr.ifexpr;
						return;
					} else {
						optimized = expr.elseexpr;
						return;
					}
				case IfExpr.NOTNEG:
					if (((Integer)cond).intValue() >= 0) {
						optimized = expr.ifexpr;
						return;
					} else {
						optimized = expr.elseexpr;
						return;
					}
				case IfExpr.POS:
					if (((Integer)cond).intValue() > 0) {
						optimized = expr.ifexpr;
						return;
					} else {
						optimized = expr.elseexpr;
						return;
					}
				case IfExpr.NOTPOS:
					if (((Integer)cond).intValue() <= 0) {
						optimized = expr.ifexpr;
						return;
					} else {
						optimized = expr.elseexpr;
						return;
					}
			}
		} else if (expr.condition instanceof IfExpr) {
			IfExpr innerif = (IfExpr)expr.condition;
			if (innerif.ifexpr instanceof ConstExpr
			 && ((ConstExpr)innerif.ifexpr).value == Boolean.TRUE
			 && innerif.elseexpr instanceof ConstExpr
			 && ((ConstExpr)innerif.elseexpr).value == Boolean.FALSE) {
				expr.condition = innerif.condition;
				expr.type = innerif.type;
			}
		}
		optimized = null;
	}

	public void visitNewArray(NewArrayExpr newarray, Object data) {
		newarray.lengthexpr.accept(this, data);
		if (optimized != null) newarray.lengthexpr = optimized;
		optimized = null;
	}

	public void visitNone(NoneExpr none, Object data) {
		optimized = null;
	}

	/** CF:
	 *   !const   =>   const
	 *   -const   =>   const
	 */
	public void visitUnary(UnaryExpr unary, Object data) {
		unary.expr.accept(this, data);
		if (optimized != null) unary.expr = optimized;
		if (unary.expr instanceof ConstExpr) {
			Object cnst = ((ConstExpr)unary.expr).value;
			if (unary.operator == '!') {
				optimized = new ConstExpr(cnst.equals(Boolean.TRUE) ? Boolean.FALSE : Boolean.TRUE);
				return;
			} else if (unary.operator == '-') {
				if (cnst instanceof Integer) {
					int oldval = ((Integer)cnst).intValue();
					optimized = new ConstExpr(new Integer(-oldval));
					return;
				} else if (cnst instanceof Long) {
					long oldval = ((Long)cnst).longValue();
					optimized = new ConstExpr(new Long(-oldval));
					return;
				} else if (cnst instanceof Float) {
					float oldval = ((Float)cnst).floatValue();
					optimized = new ConstExpr(new Float(-oldval));
					return;
				} else if (cnst instanceof Double) {
					double oldval = ((Double)cnst).doubleValue();
					optimized = new ConstExpr(new Double(-oldval));
					return;
				}
			}
		}
		optimized = null;
	}

	public void visitVar(VarExpr vexpr, Object data) {
		optimized = null;
	}

	/**
	 * DCE:
	 *  while (false) expr;   =>   none
	 */
	public void visitWhile(WhileExpr wexpr, Object data) {
		//optimize children
		wexpr.condition.accept(this, data);
		if (optimized != null) wexpr.condition = optimized;
		wexpr.body.accept(this, data);
		if (optimized != null) wexpr.body = optimized;
		//test condition
		if (wexpr.condition instanceof ConstExpr) {
			ConstExpr boolConst = (ConstExpr)wexpr.condition;
			if (boolConst.value.equals(Boolean.FALSE)) {
				optimized = new NoneExpr();
				return;
			} //TODO: while(true) cycle
		}
		optimized = null;
	}
}
