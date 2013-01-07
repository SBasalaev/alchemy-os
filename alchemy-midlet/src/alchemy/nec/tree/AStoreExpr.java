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
 * Storing element in array.
 * <pre><i>arrayexpr</i> [ <i>indexexpr</i> ] = <i>assignexpr</i></pre>
 * @author Sergey Basalaev
 */
public class AStoreExpr extends Expr {

	public Expr arrayexpr;
	public Expr indexexpr;
	public Expr assignexpr;

	public AStoreExpr(Expr arrayexpr, Expr indexexpr, Expr assignexpr) {
		this.arrayexpr = arrayexpr;
		this.indexexpr = indexexpr;
		this.assignexpr = assignexpr;
	}

	public Type rettype() {
		return BuiltinType.NONE;
	}

	public Object accept(ExprVisitor v, Object data) {
		return v.visitAStore(this, data);
	}
}
