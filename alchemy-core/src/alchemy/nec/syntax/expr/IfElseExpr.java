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
 * Conditional expression.
 * <pre>
 * <b>if</b> (condition)
 *   ifexpr
 * <b>else</b>
 *   elseexpr
 * </pre>
 * 
 * @author Sergey Basalaev
 */
public final class IfElseExpr extends Expr {
	
	public Expr condition;
	public Expr ifexpr;
	public Expr elseexpr;

	public IfElseExpr(Expr condition, Expr ifexpr, Expr elseexpr) {
		super(EXPR_IF);
		this.condition = condition;
		this.ifexpr = ifexpr;
		this.elseexpr = elseexpr;
	}

	public Type returnType() {
		return ifexpr.returnType();
	}

	public int lineNumber() {
		return condition.lineNumber();
	}

	public Object accept(ExprVisitor v, Object args) {
		return v.visitIfElse(this, args);
	}
}
