/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011-2013, Sergey Basalaev <sbasalaev@gmail.com>
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

package alchemy.nec.tree;

/**
 * Expression with binary operator.
 * Valid binary operators are <code>+</code>,
 * <code>-</code>, <code>*</code>, <code>/</code>,
 * <code>%</code>, <code>&amp;</code>, <code>|</code>,
 * <code>^</code>, <code>TT_LTLT</code>, <code>TT_GTGT</code>.
 *
 * @author Sergey Basalaev
 */
public class BinaryExpr extends Expr {

	public Expr lvalue;
	public int operator;
	public Expr rvalue;

	public BinaryExpr(Expr lvalue, int operator, Expr rvalue) {
		this.lvalue = lvalue;
		this.operator = operator;
		this.rvalue = rvalue;
	}

	public Type rettype() {
		return lvalue.rettype();
	}

	public Object accept(ExprVisitor v, Object data) {
		return v.visitBinary(this, data);
	}
}
