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
 * Creation of array or object and initializing its elements.
 * @author Sergey Basalaev
 */
public final class NewArrayInitExpr extends Expr {

	private final int line;
	private final Type type;
	/** Some of them may be null. */
	public final Expr[] initializers;

	public NewArrayInitExpr(int lnum, Type type, Expr[] initializers) {
		super(EXPR_NEWARRAY_INIT);
		this.line = lnum;
		this.type = type;
		this.initializers = initializers;
	}

	public Type returnType() {
		return type;
	}

	public int lineNumber() {
		return line;
	}

	public Object accept(ExprVisitor v, Object data) {
		return v.visitNewArrayInit(this, data);
	}
}
