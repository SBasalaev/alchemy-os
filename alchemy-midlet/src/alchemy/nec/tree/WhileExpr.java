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
 * Loop expression.
 * <pre>
 * <b>while</b> (<i>condition</i>) <i>expr</i>;
 * </pre>
 * @author Sergey Basalaev
 */
public class WhileExpr extends Expr {
	public Expr condition;
	public Expr body;

	public WhileExpr(Expr condition, Expr body) {
		this.condition = condition;
		this.body = body;
	}

	public Type rettype() {
		return BuiltinType.typeNone;
	}

	public void accept(ExprVisitor v, Object data) {
		v.visitWhile(this, data);
	}
}
