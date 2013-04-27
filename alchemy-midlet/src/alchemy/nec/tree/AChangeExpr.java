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
 * Changing element in array using operator assignment.
 * <pre>
 * <i>array</i>[<i>index</i>] += <i>expr</i>
 * </pre>
 * 
 * @author Sergey Basalaev
 */
public class AChangeExpr extends Expr {
	
	public Expr arrayexpr;
	public Expr indexexpr;
	public final Type elementType;
	public final int operator;
	public Expr rvalue;

	public AChangeExpr(Expr arrayexpr, Expr indexexpr, Type elementType, int operator, Expr rvalue) {
		this.arrayexpr = arrayexpr;
		this.indexexpr = indexexpr;
		this.elementType = elementType;
		this.operator = operator;
		this.rvalue = rvalue;
	}
	
	public Type rettype() {
		return BuiltinType.NONE;
	}

	public int lineNumber() {
		return arrayexpr.lineNumber();
	}

	public Object accept(ExprVisitor v, Object data) {
		return v.visitAChange(this, data);
	}
}