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

import alchemy.nec.syntax.type.BuiltinType;
import alchemy.nec.syntax.type.Type;

/**
 * Array length expression.
 * <pre><i>arrayExpr</i>.len</pre>
 *
 * @author Sergey Basalaev
 */
public final class ArrayLenExpr extends Expr {

	public Expr arrayExpr;

	public ArrayLenExpr(Expr arrayExpr) {
		super(EXPR_ARRAY_LEN);
		this.arrayExpr = arrayExpr;
	}

	public ArrayLenExpr(int kind) {
		super(kind);
	}

	public int lineNumber() {
		return arrayExpr.lineNumber();
	}

	public Type returnType() {
		return BuiltinType.INT;
	}

	public Object accept(ExprVisitor v, Object args) {
		return v.visitArrayLen(this, args);
	}
}
