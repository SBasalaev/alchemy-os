/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package alchemy.nec.tree;

/**
 * Unary operator preceding expression.
 * 
 * @author Sergey Basalaev
 */
public class UnaryExpr extends Expr {
	public int operator;
	public Expr expr;

	public UnaryExpr(int operator, Expr expr) {
		this.operator = operator;
		this.expr = expr;
	}

	public Type rettype() {
		return expr.rettype();
	}

	public void accept(ExprVisitor v, Object data) {
		v.visitUnary(this, data);
	}
}
