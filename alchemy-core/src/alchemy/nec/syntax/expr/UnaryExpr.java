/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011-2014, Sergey Basalaev <sbasalaev@gmail.com>
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

import alchemy.nec.syntax.type.Type;

/**
 * Unary operator preceding expression.
 * <pre>- expr</pre>
 * Valid operators are '-', '~', '!'.
 * 
 * @author Sergey Basalaev
 */
public final class UnaryExpr extends Expr {

	public final int operator;
	public Expr expr;

	public UnaryExpr(int operator, Expr expr) {
		super(EXPR_UNARY);
		this.operator = operator;
		this.expr = expr;
	}

	public Type returnType() {
		return expr.returnType();
	}

	public int lineNumber() {
		return expr.lineNumber();
	}

	public Object accept(ExprVisitor v, Object args) {
		return v.visitUnary(this, args);
	}
}
