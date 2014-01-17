/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package alchemy.nec.syntax.expr;

import alchemy.nec.syntax.type.FunctionType;
import alchemy.nec.syntax.type.Type;

/**
 * Partial application of function arguments.
 * <pre>funcExpr.<b>apply</b>(args[0], ..., args[N])</pre>
 *
 * @author Sergey Basalaev
 */
public final class ApplyExpr extends Expr {

	public Expr funcExpr;
	public final Expr[] args;
	private final Type type;

	public ApplyExpr(Expr funcExpr, Expr[] arguments) {
		super(EXPR_APPLY);
		this.funcExpr = funcExpr;
		this.args = arguments;
		// computing return type
		FunctionType ftype = (FunctionType) funcExpr.returnType();
		Type[] argTypes = new Type[ftype.argtypes.length - arguments.length];
		if (argTypes.length > 0) {
			System.arraycopy(ftype.argtypes, arguments.length, argTypes, 0, argTypes.length);
		}
		this.type = new FunctionType(ftype.returnType, argTypes);
	}

	public int lineNumber() {
		return funcExpr.lineNumber();
	}

	public Type returnType() {
		return type;
	}

	public Object accept(ExprVisitor v, Object args) {
		return v.visitApply(this, args);
	}
}
