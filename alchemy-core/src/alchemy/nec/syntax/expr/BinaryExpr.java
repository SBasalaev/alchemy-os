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
 * Expression with binary operator.
 * <pre><i>expr</i> + <i>expr</i></pre>
 * Valid binary operators are <code>+</code>,
 * <code>-</code>, <code>*</code>, <code>/</code>,
 * <code>%</code>, <code>&amp;</code>, <code>|</code>,
 * <code>^</code>, <code>TT_LTLT</code>, <code>TT_GTGT</code>.
 *
 * @author Sergey Basalaev
 */
public final class BinaryExpr extends Expr {
	public int operator;
	public Expr lhs;
	public Expr rhs;

	public BinaryExpr(Expr lhs, int operator, Expr rhs) {
		super(EXPR_BINARY);
		this.lhs = lhs;
		this.operator = operator;
		this.rhs = rhs;
	}

	public Type returnType() {
		return lhs.returnType();
	}

	public int lineNumber() {
		return lhs.lineNumber();
	}

	public Object accept(ExprVisitor v, Object args) {
		return v.visitBinary(this, args);
	}
}
