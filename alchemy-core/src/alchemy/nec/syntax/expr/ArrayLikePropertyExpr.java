/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2014, Sergey Basalaev <sbasalaev@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package alchemy.nec.syntax.expr;

import alchemy.nec.syntax.Function;
import alchemy.nec.syntax.type.Type;

/**
 * Expression for overriden [] operator.
 * <pre>objectExpr <b>[</b> indexExprs[0], ..., indexExprs[N] <b>]</b></pre>
 *
 * @author Sergey Basalaev
 */
public final class ArrayLikePropertyExpr extends Expr {

	public Expr objectExpr;
	public Expr[] indexExprs;
	public Function getter;
	public Function setter;

	public ArrayLikePropertyExpr(Expr objectExpr, Expr[] indexExprs, Function getter, Function setter) {
		super(EXPR_ARRAYLIKE);
		this.objectExpr = objectExpr;
		this.indexExprs = indexExprs;
		this.getter = getter;
		this.setter = setter;
	}

	public int lineNumber() {
		return objectExpr.lineNumber();
	}

	public Type returnType() {
		return getter.type.returnType;
	}

	public boolean isLvalue() {
		return true;
	}

	public Object accept(ExprVisitor v, Object args) {
		return v.visitArrayLikeProperty(this, args);
	}
}
