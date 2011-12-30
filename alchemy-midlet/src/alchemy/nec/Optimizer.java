package alchemy.nec;

import alchemy.nec.tree.*;

/**
 * Simple optimizer.
 * Folds constants and eliminates dead code.
 * @author Sergey Basalaev
 */
class Optimizer implements ExprVisitor {

	public void visitUnit(Unit u) {
		for (int i=0; i<u.funcs.size(); i++) {
			visitFunc((Func)u.funcs.elementAt(i));
		}
	}

	public void visitFunc(Func f) {
		if (f.body != null) {
			f.body = (Expr)f.body.accept(this, null);
		}
	}

	public Object visitALen(ALenExpr alen, Object data) {
		alen.arrayexpr = (Expr)alen.arrayexpr.accept(this, data);
		return alen;
	}

	public Object visitALoad(ALoadExpr aload, Object data) {
		aload.arrayexpr = (Expr)aload.arrayexpr.accept(this, data);
		aload.indexexpr = (Expr)aload.indexexpr.accept(this, data);
		return aload;
	}

	public Object visitAStore(AStoreExpr astore, Object data) {
		astore.arrayexpr = (Expr)astore.arrayexpr.accept(this, data);
		astore.indexexpr = (Expr)astore.indexexpr.accept(this, data);
		astore.assignexpr = (Expr)astore.assignexpr.accept(this, data);
		return astore;
	}

	public Object visitAssign(AssignExpr assign, Object data) {
		assign.expr = (Expr)assign.expr.accept(this, data);
		return assign;
	}

	/**
	 * CF:
	 *   const op const   =>   const
	 */
	public Object visitBinary(BinaryExpr binary, Object data) {
		binary.lvalue = (Expr)binary.lvalue.accept(this, data);
		binary.rvalue = (Expr)binary.rvalue.accept(this, data);
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
							return binary;
						}
						break;
					case '%':
						if (((Integer)rval).intValue() != 0) {
							lval = new Integer(((Integer)lval).intValue() % ((Integer)rval).intValue());
						} else {
							return binary;
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
							return binary;
						}
						break;
					case '%':
						if (((Long)rval).longValue() != 0) {
							lval = new Long(((Long)lval).longValue() % ((Long)rval).longValue());
						} else {
							return binary;
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
			return new ConstExpr(lval);
		}
		return binary;
	}

	/**
	 * CF:
	 *   { ... exprN-1; none; exprN+1; ... }   =>   { ... exprN-1; exprN+1; ... }
	 *   { expr }   =>   expr
	 *   {}   =>   none
	 */
	public Object visitBlock(BlockExpr block, Object data) {
		int i=0;
		while (i < block.exprs.size()) {
			Expr ex = (Expr)block.exprs.elementAt(i);
			ex = (Expr)ex.accept(this, data);
			if (ex instanceof NoneExpr) {
				block.exprs.removeElementAt(i);
			} else {
				block.exprs.setElementAt(ex, i);
				i++;
			}
		}
		switch (block.exprs.size()) {
			case 0:
				return new NoneExpr();
			case 1:
				Expr e = (Expr)block.exprs.elementAt(0);
				if (block.locals.isEmpty()) {
					return e;
				}
			default:
				return block;
		}
	}

	public Object visitCast(CastExpr cast, Object data) {
		cast.expr = (Expr)cast.expr.accept(this, data);
		if (cast.expr instanceof CastExpr) {
			return new CastExpr(cast.toType, ((CastExpr)cast.expr).expr);
		}
		return cast;
	}

	/**
	 * CF:
	 *   cast(type) number   =>   castednumber
	 */
	public Object visitCastPrimitive(CastPrimitiveExpr cast, Object data) {
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
			return new ConstExpr(cnst);
		}
		return cast;
	}

	public Object visitConst(ConstExpr cexpr, Object data) {
		return cexpr;
	}

	/**
	 * DCE:
	 *   const;   =>   none
	 *   var;     =>   none
	 */
	public Object visitDiscard(DiscardExpr disc, Object data) {
		disc.expr = (Expr)disc.expr.accept(this, data);
		if (disc.expr instanceof ConstExpr || disc.expr instanceof VarExpr) {
			return new NoneExpr();
		}
		return disc;
	}

	public Object visitFCall(FCallExpr fcall, Object data) {
		fcall.fload = (Expr)fcall.fload.accept(this, data);
		for (int i=0; i<fcall.args.length; i++) {
			fcall.args[i] = (Expr)fcall.args[i].accept(this, data);
		}
		return fcall;
	}

	/**
	 * DCE:
	 *   if (true)  expr1 else expr2   =>   expr1
	 *   if (false) expr1 else expr2   =>   expr2
	 * CF:
	 *   if (if? true else false) expr1 else expr2  =>  if? expr1 else expr2
	 *   // the latter is common due to implementation of comparison
	 */
	public Object visitIf(IfExpr expr, Object data) {
		//optimize children
		expr.condition = (Expr)expr.condition.accept(this, data);
		expr.ifexpr = (Expr)expr.ifexpr.accept(this, data);
		expr.elseexpr = (Expr)expr.elseexpr.accept(this, data);
		//test condition
		if (expr.condition instanceof ConstExpr) {
			Object cond = ((ConstExpr)expr.condition).value;
			switch (expr.type) {
				case IfExpr.TRUE:
					if (Boolean.TRUE.equals(cond)) {
						return expr.ifexpr;
					} else {
						return expr.elseexpr;
					}
				case IfExpr.FALSE:
					if (Boolean.FALSE.equals(cond)) {
						return expr.ifexpr;
					} else {
						return expr.elseexpr;
					}
				case IfExpr.NULL:
					if (cond == null) {
						return expr.ifexpr;
					} else {
						return expr.elseexpr;
					}
				case IfExpr.NOTNULL:
					if (cond != null) {
						return expr.ifexpr;
					} else {
						return expr.elseexpr;
					}
				case IfExpr.ZERO:
					if (((Integer)cond).intValue() == 0) {
						return expr.ifexpr;
					} else {
						return expr.elseexpr;
					}
				case IfExpr.NOTZERO:
					if (((Integer)cond).intValue() != 0) {
						return expr.ifexpr;
					} else {
						return expr.elseexpr;
					}
				case IfExpr.NEG:
					if (((Integer)cond).intValue() < 0) {
						return expr.ifexpr;
					} else {
						return expr.elseexpr;
					}
				case IfExpr.NOTNEG:
					if (((Integer)cond).intValue() >= 0) {
						return expr.ifexpr;
					} else {
						return expr.elseexpr;
					}
				case IfExpr.POS:
					if (((Integer)cond).intValue() > 0) {
						return expr.ifexpr;
					} else {
						return expr.elseexpr;
					}
				case IfExpr.NOTPOS:
					if (((Integer)cond).intValue() <= 0) {
						return expr.ifexpr;
					} else {
						return expr.elseexpr;
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
		return expr;
	}

	public Object visitNewArray(NewArrayExpr newarray, Object data) {
		newarray.lengthexpr = (Expr)newarray.lengthexpr.accept(this, data);
		return newarray;
	}

	public Object visitNone(NoneExpr none, Object data) {
		return none;
	}

	/** CF:
	 *   !const   =>   const
	 *   -const   =>   const
	 */
	public Object visitUnary(UnaryExpr unary, Object data) {
		unary.expr = (Expr)unary.expr.accept(this, data);
		if (unary.expr instanceof ConstExpr) {
			Object cnst = ((ConstExpr)unary.expr).value;
			if (unary.operator == '!') {
				return new ConstExpr(cnst.equals(Boolean.TRUE) ? Boolean.FALSE : Boolean.TRUE);
			} else if (unary.operator == '-') {
				if (cnst instanceof Integer) {
					int oldval = ((Integer)cnst).intValue();
					return new ConstExpr(new Integer(-oldval));
				} else if (cnst instanceof Long) {
					long oldval = ((Long)cnst).longValue();
					return new ConstExpr(new Long(-oldval));
				} else if (cnst instanceof Float) {
					float oldval = ((Float)cnst).floatValue();
					return new ConstExpr(new Float(-oldval));
				} else if (cnst instanceof Double) {
					double oldval = ((Double)cnst).doubleValue();
					return new ConstExpr(new Double(-oldval));
				}
			}
		}
		return unary;
	}

	public Object visitVar(VarExpr vexpr, Object data) {
		return vexpr;
	}

	/**
	 * DCE:
	 *  while (false) expr;   =>   none
	 */
	public Object visitWhile(WhileExpr wexpr, Object data) {
		//optimize children
		wexpr.condition = (Expr)wexpr.condition.accept(this, data);
		wexpr.body = (Expr)wexpr.body.accept(this, data);
		//test condition
		if (wexpr.condition instanceof ConstExpr) {
			ConstExpr boolConst = (ConstExpr)wexpr.condition;
			if (boolConst.value.equals(Boolean.FALSE)) {
				return new NoneExpr();
			} //TODO: while(true) cycle
		}
		return wexpr;
	}
}
