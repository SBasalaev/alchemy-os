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

/**
 * Loop break statement.
 * <pre><b>break</b></pre>
 * @author Sergey Basalaev
 */
public final class BreakStatement extends Statement {

	private final int line;

	public BreakStatement(int line) {
		super(STAT_BREAK);
		this.line = line;
	}

	public int lineNumber() {
		return line;
	}

	public Object accept(StatementVisitor v, Object args) {
		return v.visitBreakStatement(this, args);
	}
}
