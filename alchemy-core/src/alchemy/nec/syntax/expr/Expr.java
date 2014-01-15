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
 * Single expression in Ether code.
 * @author Sergey Basalaev
 */
public abstract class Expr {
	public static final int EXPR_CONST = 0;
	public static final int EXPR_BINARY = 1;
	public static final int EXPR_CALL = 2;
	public static final int EXPR_CAST = 3;
	public static final int EXPR_COMPARISON = 4;
	public static final int EXPR_CONCAT = 5;
	public static final int EXPR_IF = 6;
	public static final int EXPR_UNARY = 7;
	public static final int EXPR_VAR = 8;
	public static final int EXPR_ARRAY_ELEMENT = 9;
	public static final int EXPR_ARRAY_LEN = 10;
	public static final int EXPR_EQUALITY = 11;
	public static final int EXPR_PROPERTY = 12;
	public static final int EXPR_ARRAYLIKE = 13;

	/** Kind of this expression, one of EXPR_* constants. */
	public final int kind;

	protected Expr(int kind) {
		this.kind = kind;
	}

	/** Number of the starting line of this expression. */
	public abstract int lineNumber();

	/** Returns type of the result of this expression. */
	public abstract Type returnType();

	/** Tests whether this expression can appear on the left-hand side of assignment. */
	public boolean isLvalue() {
		return false;
	}

	public abstract Object accept(ExprVisitor v, Object args);
}
