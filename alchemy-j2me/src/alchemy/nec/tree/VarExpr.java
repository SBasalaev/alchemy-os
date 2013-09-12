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
 * Variable expression.
 * @author Sergey Basalaev
 */
public class VarExpr extends Expr {

	public Var var;
	private final int line;

	public VarExpr(int lnum, Var v) {
		this.line = lnum;
		this.var = v;
	}

	public Type rettype() {
		return var.type;
	}

	public int lineNumber() {
		return line;
	}

	public Object accept(ExprVisitor v, Object data) {
		return v.visitVar(this, data);
	}
}
