/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package alchemy.nec.tree;

/**
 * Conditional expression.
 * <pre>
 * <b>if</b> (<i>condition</i>)
 *   <i>expr;</i>
 * <b>else</b>
 *   <i>expr;</i>
 * </pre>
 *
 * @author Sergey Basalaev
 */
public class IfExpr extends Expr {
	public Expr condition;
	public Expr ifexpr;
	public Expr elseexpr;

	public IfExpr(Expr condition, Expr ifexpr, Expr elseexpr) {
		this.condition = condition;
		this.ifexpr = ifexpr;
		this.elseexpr = elseexpr;
	}

	public Type rettype() {
		return ifexpr.rettype();
	}

	public void accept(ExprVisitor v, Object data) {
		v.visitIf(this, data);
	}
}
