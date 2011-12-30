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
		if (value == null) {
			return BuiltinType.typeAny;
		} else if (value instanceof Integer) {
			return BuiltinType.typeInt;
		} else if (value instanceof Long) {
			return BuiltinType.typeLong;
		} else if (value instanceof Float) {
			return BuiltinType.typeFloat;
		} else if (value instanceof Double) {
			return BuiltinType.typeDouble;
		} else if (value instanceof String) {
			return BuiltinType.typeString;
		} else if (value instanceof Boolean) {
			return BuiltinType.typeBool;
		} else if (value instanceof Func) {
			return ((Func)value).asVar.type;
		} else {
			return null;
		}
	}

	public Object accept(ExprVisitor v, Object data) {
		return v.visitConst(this, data);
	}
}
