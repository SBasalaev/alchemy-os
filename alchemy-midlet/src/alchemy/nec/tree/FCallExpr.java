/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011  Sergey Basalaev <sbasalaev@gmail.com>
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
 * Function call.
 * <pre><i>func</i>(<i>arg0</i>, ..., <i>argN</i>)</pre>
 * 
 * @author Sergey Basalaev
 */
public class FCallExpr extends Expr {

	/** Expression used to load reference to a function. */
	public Expr fload;
	/** Argument expressions. */
	public Expr[] args;

	public FCallExpr(Expr fload, Expr[] args) {
		this.fload = fload;
		this.args = args;
	}

	public Type rettype() {
		FunctionType ftype = (FunctionType)fload.rettype();
		return ftype.rettype;
	}

	public Object accept(ExprVisitor v, Object data) {
		return v.visitFCall(this, data);
	}
}
