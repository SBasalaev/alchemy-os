/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2013-2014, Sergey Basalaev <sbasalaev@gmail.com>
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
 * Switch expression.
 * <pre>
 * <b>switch</b> (keyExpr) {
 *   keySets[0]: exprs[0]
 *   ...
 *   keySets[N]: exprs[N]
 *   <b>else</b>: elseExpr
 * }
 * </pre>
 *
 * @author Sergey Basalaev
 */
public final class SwitchExpr extends Expr {

	public Expr keyExpr;

	public int[][] keySets;
	public Expr[] exprs;

	public Expr elseExpr;

	public SwitchExpr(Expr keyExpr, int[][] keySets, Expr[] exprs, Expr elseExpr) {
		super(EXPR_SWITCH);
		this.keyExpr = keyExpr;
		this.keySets = keySets;
		this.exprs = exprs;
		this.elseExpr = elseExpr;
	}

	public int lineNumber() {
		return keyExpr.lineNumber();
	}

	public Type returnType() {
		return elseExpr.returnType();
	}

	public Object accept(ExprVisitor v, Object args) {
		return v.visitSwitch(this, args);
	}
}
