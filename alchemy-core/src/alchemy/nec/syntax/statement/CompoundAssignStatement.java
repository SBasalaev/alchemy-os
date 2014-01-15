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

package alchemy.nec.syntax.statement;

import alchemy.nec.syntax.Function;
import alchemy.nec.syntax.expr.Expr;

/**
 * Compound assignment.
 * <pre><i>lvalue</i> += <i>assignExpr</i></pre>
 *
 * If <i>opmethod</i> is not null it is used
 * as override for operator.
 *
 * @author Sergey Basalaev
 */
public final class CompoundAssignStatement extends Statement {

	public Expr lvalue;
	public final int assignOperator;
	public Expr assignExpr;
	public final Function opmethod;

	public CompoundAssignStatement(Expr lvalue, int assignOperator, Expr assignExpr, Function opmethod) {
		super(STAT_COMPOUND_ASSIGN);
		this.lvalue = lvalue;
		this.assignOperator = assignOperator;
		this.assignExpr = assignExpr;
		this.opmethod = opmethod;
	}

	public int lineNumber() {
		return assignExpr.lineNumber();
	}

	public Object accept(StatementVisitor v, Object args) {
		return v.visitCompoundAssignStatement(this, args);
	}
}
