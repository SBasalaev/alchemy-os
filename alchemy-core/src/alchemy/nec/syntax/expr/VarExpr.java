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

import alchemy.nec.syntax.Var;
import alchemy.nec.syntax.type.Type;

/**
 * Variable expression.
 * @author Sergey Basalaev
 */
public final class VarExpr extends Expr {

	private final int line;
	public final Var var;

	public VarExpr(int line, Var v) {
		super(EXPR_VAR);
		this.line = line;
		this.var = v;
	}

	public Type returnType() {
		return var.type;
	}

	public int lineNumber() {
		return line;
	}

	public Object accept(ExprVisitor v, Object args) {
		return v.visitVar(this, args);
	}
}
