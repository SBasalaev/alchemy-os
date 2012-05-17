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
 * Cast from one primitive type to another.
 * @author Sergey Basalaev
 */
public class CastPrimitiveExpr extends Expr {

	public static final int I2L = 0x37;
	public static final int I2F = 0x38;
	public static final int I2D = 0x39;
	public static final int L2I = 0x3C;
	public static final int L2F = 0x3A;
	public static final int L2D = 0x3B;
	public static final int F2I = 0x48;
	public static final int F2L = 0x49;
	public static final int F2D = 0x47;
	public static final int D2I = 0x4A;
	public static final int D2L = 0x4B;
	public static final int D2F = 0x4C;

	public final int casttype;
	public Expr expr;

	public CastPrimitiveExpr(Expr expr, int casttype) {
		this.casttype = casttype;
		this.expr = expr;
	}

	public Type rettype() {
		switch (casttype) {
			case L2I:
			case F2I:
			case D2I:
				return BuiltinType.typeInt;
			case I2L:
			case F2L:
			case D2L:
				return BuiltinType.typeLong;
			case I2F:
			case L2F:
			case D2F:
				return BuiltinType.typeFloat;
			case I2D:
			case L2D:
			case F2D:
				return BuiltinType.typeDouble;
			default:
				return null;
		}
	}

	public Object accept(ExprVisitor v, Object data) {
		return v.visitCastPrimitive(this, data);
	}
}
