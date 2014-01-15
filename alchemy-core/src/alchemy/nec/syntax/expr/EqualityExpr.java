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

import alchemy.nec.syntax.Function;
import alchemy.nec.syntax.type.BuiltinType;
import alchemy.nec.syntax.type.Type;

/**
 * Equality expression.
 * <pre>lhs == rhs</pre>
 *
 * Valid operators are == and !=.
 * If <i>eqmethod</i> is not null then
 * this expression will be compiled as safe
 * method call. For == this will be
 * <pre>
 * <b>if</b> (lhs == <b>null</b> || rhs == <b>null</b>)
 *   rhs == lhs
 * <b>else</b>
 *   eqmethod(lhs, rhs)
 * </pre>
 * For != this will be
 * <pre>
 * <b>if</b> (lhs == <b>null</b> || rhs == <b>null</b>)
 *   rhs != lhs
 * <b>else</b>
 *   !eqmethod(lhs, rhs)
 * </pre>
 *
 * @author Sergey Basalaev
 */
public final class EqualityExpr extends Expr {
	public final Function eqmethod;
	public Expr lhs;
	public int operator;
	public Expr rhs;

	public EqualityExpr(Expr lhs, int operator, Expr rhs) {
		super(EXPR_EQUALITY);
		this.eqmethod = null;
		this.lhs = lhs;
		this.operator = operator;
		this.rhs = rhs;
	}

	public EqualityExpr(Function eqmethod, Expr lhs, int operator, Expr rhs) {
		super(EXPR_EQUALITY);
		this.eqmethod = eqmethod;
		this.lhs = lhs;
		this.operator = operator;
		this.rhs = rhs;
	}

	public int lineNumber() {
		return lhs.lineNumber();
	}

	public Type returnType() {
		return BuiltinType.BOOL;
	}

	public Object accept(ExprVisitor v, Object args) {
		return v.visitEquality(this, args);
	}
}
