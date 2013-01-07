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

import alchemy.core.types.Char;
import alchemy.core.types.Int;

/**
 * Constant expression.
 * Includes all literal expressions and global constants.
 * 
 * @author Sergey Basalaev
 */
public class ConstExpr extends Expr {

	public final Object value;
	public final int line;

	public ConstExpr(int lnum, Object obj) {
		line = lnum;
		value = obj;
	}

	public Type rettype() {
		if (value instanceof Char) {
			return BuiltinType.CHAR;
		} else if (value instanceof Int) {
			return BuiltinType.INT;
		} else if (value instanceof Long) {
			return BuiltinType.LONG;
		} else if (value instanceof Float) {
			return BuiltinType.FLOAT;
		} else if (value instanceof Double) {
			return BuiltinType.DOUBLE;
		} else if (value instanceof String) {
			return BuiltinType.STRING;
		} else if (value instanceof Boolean) {
			return BuiltinType.BOOL;
		} else if (value instanceof Func) {
			return ((Func)value).type;
		} else {
			return BuiltinType.NULL;
		}
	}

	public Object accept(ExprVisitor v, Object data) {
		return v.visitConst(this, data);
	}
}
