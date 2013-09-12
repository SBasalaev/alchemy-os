/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011-2013, Sergey Basalaev <sbasalaev@gmail.com>
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
 * Increment integer variable.
 * Special kind of AST for expressions like
 * <pre>i += 1</pre>
 * 
 * @author Sergey Basalaev
 */
public class IincExpr extends Expr {
	
	public final int line;
	public final Var var;
	public final int incr;

	public IincExpr(int line, Var var, int incr) {
		this.line = line;
		this.var = var;
		this.incr = incr;
	}

	public Type rettype() {
		return BuiltinType.NONE;
	}

	public int lineNumber() {
		return line;
	}
	
	public Object accept(ExprVisitor v, Object data) {
		return v.visitIinc(this, data);
	}
}
