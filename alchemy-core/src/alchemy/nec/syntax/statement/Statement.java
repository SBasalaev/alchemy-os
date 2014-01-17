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
 * Single statement in Ether code.
 * @author Sergey Basalaev
 */
public abstract class Statement {

	public static final int STAT_ASSIGN = 0;
	public static final int STAT_BLOCK = 1;
	public static final int STAT_BREAK = 2;
	public static final int STAT_COMPOUND_ASSIGN = 3;
	public static final int STAT_CONTINUE = 4;
	public static final int STAT_EMPTY = 5;
	public static final int STAT_EXPR = 6;
	public static final int STAT_IF = 7;
	public static final int STAT_LOOP = 8;
	public static final int STAT_RETURN = 9;
	public static final int STAT_THROW = 10;
	public static final int STAT_ARRAYSET = 11;

	/** Kind of this statement, one of STAT_* constants. */
	public final int kind;

	protected Statement(int kind) {
		this.kind = kind;
	}

	/** Number of the starting line of this statement. */
	public abstract int lineNumber();

	public abstract Object accept(StatementVisitor v, Object args);
}
