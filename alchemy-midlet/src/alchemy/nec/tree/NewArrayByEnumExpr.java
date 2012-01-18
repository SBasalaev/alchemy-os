package alchemy.nec.tree;

/**
 * Creation of array and structure by enumerating its elements.
 * @author Sergey Basalaev
 */
public class NewArrayByEnumExpr extends Expr {

	private final Type type;
	public final Expr[] initializers;

	public NewArrayByEnumExpr(Type type, Expr[] initializers) {
		this.type = type;
		this.initializers = initializers;
	}
	
	public Type rettype() {
		return type;
	}

	public Object accept(ExprVisitor v, Object data) {
		return v.visitNewArrayByEnum(this, data);
	}
	
}
