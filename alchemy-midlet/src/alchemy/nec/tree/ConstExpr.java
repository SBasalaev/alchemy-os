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

/**
 * Constant expression.
 * Includes all literal expressions and global constants.
 * 
 * @author Sergey Basalaev
 */
public class ConstExpr extends Expr {

	public final Object value;

	public ConstExpr(Object obj) {
		value = obj;
	}

	public Type rettype() {
		Class clz = value == null ? null : value.getClass();
		if (clz == null) {
			return BuiltinType.typeAny;
		} else if (clz == Integer.class) {
			return BuiltinType.typeInt;
		} else if (clz == Long.class) {
			return BuiltinType.typeLong;
		} else if (clz == Float.class) {
			return BuiltinType.typeFloat;
		} else if (clz == Double.class) {
			return BuiltinType.typeDouble;
		} else if (clz == String.class) {
			return BuiltinType.typeString;
		} else if (clz == Boolean.class) {
			return BuiltinType.typeBool;
		} else if (clz == Func.class) {
			return ((Func)value).type;
		} else {
			return null;
		}
	}

	public Object accept(ExprVisitor v, Object data) {
		return v.visitConst(this, data);
	}
}
