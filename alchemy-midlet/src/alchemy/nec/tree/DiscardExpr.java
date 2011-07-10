/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package alchemy.nec.tree;

/**
 * Expression that is computed but its result is discarded.
 * @author Sergey Basalaev
 */
public class DiscardExpr extends Expr {
	public Expr expr;

	public DiscardExpr(Expr expr) {
		this.expr = expr;
	}

	public Type rettype() {
		return BuiltinType.typeNone;
	}

	public void accept(ExprVisitor v, Object data) {
		v.visitDiscard(this, data);
	}
}
