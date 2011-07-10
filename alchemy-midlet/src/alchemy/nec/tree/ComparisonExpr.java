/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package alchemy.nec.tree;

/**
 * Comparison expression.
 * @author Sergey Basalaev
 */
public class ComparisonExpr extends BinaryExpr {

	public ComparisonExpr(Expr lvalue, int operator, Expr rvalue) {
		super(lvalue, operator, rvalue);
	}

	public Type rettype() {
		return BuiltinType.typeBool;
	}

	public void accept(ExprVisitor v, Object data) {
		v.visitComparison(this, data);
	}
}
