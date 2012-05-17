/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011-2012, Sergey Basalaev <sbasalaev@gmail.com>
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
 * Loading element from array or structure.
 * <pre><i>arrayexpr</i> [ <i>indexexpr</i> ]</pre>
 * @author Sergey Basalaev
 */
public class ALoadExpr extends Expr {

	public final Type returnType;
	public Expr arrayexpr;
	public Expr indexexpr;

	public ALoadExpr(Expr arrayexpr, Expr indexexpr, Type type) {
		this.arrayexpr = arrayexpr;
		this.indexexpr = indexexpr;
		this.returnType = type;
	}

	public Type rettype() {
		return returnType;
	}

	public Object accept(ExprVisitor v, Object data) {
		return v.visitALoad(this, data);
	}
}
