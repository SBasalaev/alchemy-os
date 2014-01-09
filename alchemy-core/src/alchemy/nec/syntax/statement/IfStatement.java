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

import alchemy.nec.syntax.expr.Expr;

/**
 * Conditional statement.
 * <pre>
 * <b>if</b> (<i>condition</i>) <i>ifstat</i> <b>else</b> <i>elsestat</i>
 *
 * <b>if</b> (<i>condition</i>) <i>ifstat</i>
 * </pre>
 * @author Sergey Basalaev
 */
public final class IfStatement extends Statement {

	public Expr condition;
	public Statement ifstat;
	public Statement elsestat;

	public IfStatement() {
		super(STAT_IF);
	}

	public int lineNumber() {
		return condition.lineNumber();
	}

	public Object accept(StatementVisitor v, Object args) {
		return v.visitIfStatement(this, args);
	}
}
