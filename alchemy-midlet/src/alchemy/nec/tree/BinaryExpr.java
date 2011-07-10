/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package alchemy.nec.tree;

/**
 * Expression with binary operator.
 * Does not include comparison operators, for that
 * ComparisonExpr is used.
 *
 * @author Sergey Basalaev
 */
public class BinaryExpr extends Expr {

	public Expr lvalue;
	public int operator;
	public Expr rvalue;

	public BinaryExpr(Expr lvalue, int operator, Expr rvalue) {
		this.lvalue = lvalue;
		this.operator = operator;
		this.rvalue = rvalue;
	}

	public Type rettype() {
		return lvalue.rettype();
	}

	public void accept(ExprVisitor v, Object data) {
		v.visitBinary(this, data);
	}
}
