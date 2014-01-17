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
 * Comparison expression.
 * <pre>lhs &lt; rhs</pre>
 * Valid operators are &lt;, &gt;, &lt=, &gt;=, ==, !=.
 * 
 * @author Sergey Basalaev
 */
public final class ComparisonExpr extends Expr {
	public int operator;
	public Expr lhs;
	public Expr rhs;

	public ComparisonExpr(Expr lhs, int operator, Expr rhs) {
		super(EXPR_COMPARISON);
		this.lhs = lhs;
		this.operator = operator;
		this.rhs = rhs;
	}

	public Type returnType() {
		return BuiltinType.BOOL;
	}

	public int lineNumber() {
		return lhs.lineNumber();
	}

	public Object accept(ExprVisitor v, Object args) {
		return v.visitComparison(this, args);
	}
}
