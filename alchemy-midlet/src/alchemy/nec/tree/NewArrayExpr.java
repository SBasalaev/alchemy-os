package alchemy.nec.tree;

/**
 * Creation of new array or structure.
 * @author Sergey Basalaev
 */
public class NewArrayExpr extends Expr {

	private final Type type;
	public Expr lengthexpr;

	public NewArrayExpr(Type type, Expr lengthexpr) {
		this.type = type;
		this.lengthexpr = lengthexpr;
	}

	public Type rettype() {
		return type;
	}

	public void accept(ExprVisitor v, Object data) {
		v.visitNewArray(this, data);
	}
}
