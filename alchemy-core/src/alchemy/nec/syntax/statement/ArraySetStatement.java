/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package alchemy.nec.syntax.statement;

import alchemy.nec.syntax.expr.Expr;

/**
 * Setting array element or object field.
 * @author Sergey Basalaev
 */
public final class ArraySetStatement extends Statement {

	public Expr arrayExpr;
	public Expr indexExpr;
	public Expr assignExpr;

	public ArraySetStatement(Expr arrayExpr, Expr indexExpr, Expr assignExpr) {
		super(STAT_ARRAYSET);
		this.arrayExpr = arrayExpr;
		this.indexExpr = indexExpr;
		this.assignExpr = assignExpr;
	}

	public int lineNumber() {
		return arrayExpr.lineNumber();
	}

	public Object accept(StatementVisitor v, Object args) {
		return v.visitArraySetStatement(this, args);
	}
}
