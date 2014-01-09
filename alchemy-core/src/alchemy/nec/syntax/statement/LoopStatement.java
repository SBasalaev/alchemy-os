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

package alchemy.nec.syntax.statement;

import alchemy.nec.syntax.expr.Expr;

/**
 * Unified loop statement.
 *
 * <pre>
 * <b>while</b> (<i>condition</i>) <i>postBody</i>
 *
 * <b>while</b> (<i>preBody</i>, <i>condition</i>) <i>postBody</i>
 *
 * <b>do</b> <i>preBody</i> <b>while</b> (<i>condition</i>)
 * </pre>
 * @author Sergey Basalaev
 */
public final class LoopStatement extends Statement {

	public Statement preBody;
	public Expr condition;
	public Statement postBody;

	public LoopStatement(Statement preBody, Expr condition, Statement postBody) {
		super(STAT_LOOP);
		this.preBody = preBody;
		this.condition = condition;
		this.postBody = postBody;
	}

	public int lineNumber() {
		return preBody.lineNumber();
	}

	public Object accept(StatementVisitor v, Object args) {
		return v.visitLoopStatement(this, args);
	}
}
