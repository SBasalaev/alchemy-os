/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package alchemy.nec.syntax.expr;

import alchemy.nec.syntax.Scope;
import alchemy.nec.syntax.Var;
import alchemy.nec.syntax.type.Type;

/**
 * Helper expression for some constructs.
 * Results from expressions in sequence are
 * stored in local variables. Result of the last
 * expression is returned.
 *
 * @author Sergey Basalaev
 */
public final class SequentialExpr extends Expr {

	public Var[] seqVars;
	public Expr[] seqExprs;
	public Expr lastExpr;

	public SequentialExpr(Var[] seqVars, Expr[] seqExprs, Expr lastExpr) {
		super(EXPR_SEQUENTIAL);
		this.seqVars = seqVars;
		this.seqExprs = seqExprs;
		this.lastExpr = lastExpr;
	}

	public int lineNumber() {
		return seqExprs[0].lineNumber();
	}

	public Type returnType() {
		return lastExpr.returnType();
	}

	public Object accept(ExprVisitor v, Object args) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
