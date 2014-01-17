/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package alchemy.nec.syntax.expr;

import alchemy.nec.syntax.Function;
import alchemy.nec.syntax.type.BuiltinType;
import alchemy.nec.syntax.type.Type;

/**
 * Object property.
 * <pre>objectExpr.propertyName</pre>
 *
 * Upon compilation getter and setter methods
 * are used to access property.
 *
 * @author Sergey Basalaev
 */
public final class PropertyLvalue extends Expr {
	public Expr objectExpr;
	public String propertyName;
	public Function getter;
	public Function setter;

	public PropertyLvalue(Expr objectExpr, String propertyName, Function getter, Function setter) {
		super(EXPR_PROPERTY);
		this.objectExpr = objectExpr;
		this.propertyName = propertyName;
		this.getter = getter;
		this.setter = setter;
	}

	public int lineNumber() {
		return objectExpr.lineNumber();
	}

	public Type returnType() {
		return (getter != null) ? getter.type.returnType : BuiltinType.NONE;
	}

	public Object accept(ExprVisitor v, Object args) {
		throw new RuntimeException("Lvalue appears in the final tree");
	}
}
