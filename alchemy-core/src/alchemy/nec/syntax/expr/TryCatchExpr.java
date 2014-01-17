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
 * Try-catch expression.
 * <pre><b>try</b> tryExpr <b>catch</b> catchExpr</pre>
 *
 * @author Sergey Basalaev
 */
public final class TryCatchExpr extends Expr {

	public Expr tryExpr;
	public Expr catchExpr;

	public TryCatchExpr(Expr tryExpr, Expr catchExpr) {
		super(EXPR_TRYCATCH);
		this.tryExpr = tryExpr;
		this.catchExpr = catchExpr;
	}

	public int lineNumber() {
		return tryExpr.lineNumber();
	}

	public Type returnType() {
		return tryExpr.returnType();
	}

	public Object accept(ExprVisitor v, Object args) {
		return v.visitTryCatch(this, args);
	}
}
