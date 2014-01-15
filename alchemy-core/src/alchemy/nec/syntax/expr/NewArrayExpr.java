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
 * Creation of new array or object.
 * <pre><b>new</b> [arrayType](lengthExprs[0], ..., lengthExprs[N])</pre>
 *
 * @author Sergey Basalaev
 */
public final class NewArrayExpr extends Expr {

	private final int line;
	private final Type type;
	public final Expr[] lengthExprs;

	public NewArrayExpr(int lnum, Type arrayType, Expr[] lengthExprs) {
		super(EXPR_NEWARRAY);
		this.line = lnum;
		this.type = arrayType;
		this.lengthExprs = lengthExprs;
	}

	public Type returnType() {
		return type;
	}

	public int lineNumber() {
		return line;
	}

	public Object accept(ExprVisitor v, Object args) {
		return v.visitNewArray(this, args);
	}
}
