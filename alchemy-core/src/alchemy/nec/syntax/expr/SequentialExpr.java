/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2014, Sergey Basalaev <sbasalaev@gmail.com>
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
 * Helper expression for some constructs.
 * Results from expressions in sequence are
 * stored in local variables. Result of the last
 * expression is returned.
 *
 * @author Sergey Basalaev
 */
public final class SequentialExpr extends Expr {

	public Var[] seqVars;
	public Expr[] seqExprs;
	public Expr lastExpr;

	public SequentialExpr(Var[] seqVars, Expr[] seqExprs, Expr lastExpr) {
		super(EXPR_SEQUENTIAL);
		this.seqVars = seqVars;
		this.seqExprs = seqExprs;
		this.lastExpr = lastExpr;
	}

	public int lineNumber() {
		return seqExprs[0].lineNumber();
	}

	public Type returnType() {
		return lastExpr.returnType();
	}

	public Object accept(ExprVisitor v, Object args) {
		return v.visitSequential(this, args);
	}
}
