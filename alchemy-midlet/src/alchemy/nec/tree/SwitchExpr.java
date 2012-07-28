/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011-2012, Sergey Basalaev <sbasalaev@gmail.com>
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

import java.util.Vector;

/**
 * Switch expression.
 * <pre>
 * <b>switch</b> (<i>expr</i>) {
 *   1: <i>expr1</i>
 *   2,3: <i>expr2</i>
 *   <b>else</b>: <i>expr</i>
 * }
 * </pre>
 * 
 * @author Sergey Basalaev
 */
public class SwitchExpr extends Expr {
	
	public Expr indexexpr;
	
	/** May be null! */
	public Expr elseexpr;
	
	/** int[] arrays. */
	public Vector keys = new Vector();
	
	public Vector exprs = new Vector();
	
	public SwitchExpr() { }

	public Type rettype() {
		return ((Expr)exprs.firstElement()).rettype();
	}

	public Object accept(ExprVisitor v, Object data) {
		return v.visitSwitch(this, data);
	}
}
