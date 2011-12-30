/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011  Sergey Basalaev <sbasalaev@gmail.com>
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

package alchemy.nec.tree;

/**
 * Conditional expression.
 * <pre>
 * <b>if</b> (<i>condition</i>)
 *   <i>expr;</i>
 * <b>else</b>
 *   <i>expr;</i>
 * </pre>
 * 
 * @author Sergey Basalaev
 */
public class IfExpr extends Expr {
	
	// for these types condition returns Bool
	public static final int FALSE = 0;
	public static final int TRUE = 1;
	// for these types condition returns Int
	public static final int ZERO = 2;
	public static final int NOTZERO = 3;
	public static final int POS = 4;
	public static final int NEG = 5;
	public static final int NOTPOS = 6;
	public static final int NOTNEG = 7;
	// for these types condition returns Any
	public static final int NULL = 8;
	public static final int NOTNULL = 9;
	
	public int type;
	public Expr condition;
	public Expr ifexpr;
	public Expr elseexpr;

	public IfExpr(Expr condition, int type, Expr ifexpr, Expr elseexpr) {
		this.type = type;
		this.condition = condition;
		this.ifexpr = ifexpr;
		this.elseexpr = elseexpr;
	}

	public Type rettype() {
		return ifexpr.rettype();
	}

	public Object accept(ExprVisitor v, Object data) {
		return v.visitIf(this, data);
	}
}
