/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package alchemy.nec.tree;

/**
 * Loop expression.
 * <pre>
 * <b>while</b> (<i>condition</i>) <i>expr</i>;
 * </pre>
 * @author Sergey Basalaev
 */
public class WhileExpr extends Expr {
	public Expr condition;
	public Expr body;

	public WhileExpr(Expr condition, Expr body) {
		this.condition = condition;
		this.body = body;
	}

	public Type rettype() {
		return BuiltinType.typeNone;
	}

	public void accept(ExprVisitor v, Object data) {
		v.visitWhile(this, data);
	}
}
