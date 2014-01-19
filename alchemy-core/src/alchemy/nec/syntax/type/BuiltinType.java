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

package alchemy.nec.syntax.type;

/**
 * Primitive types and builtin object types.
 * @author Sergey Basalaev
 */
public final class BuiltinType extends Type {
	/* Primitive types. */
	public static final BuiltinType NONE = new BuiltinType("<none>", TYPE_NONE);
	public static final BuiltinType BOOL = new BuiltinType("Bool", TYPE_BOOL);
	public static final BuiltinType BYTE = new BuiltinType("Byte", TYPE_BYTE);
	public static final BuiltinType CHAR = new BuiltinType("Char", TYPE_CHAR);
	public static final BuiltinType SHORT = new BuiltinType("Short", TYPE_SHORT);
	public static final BuiltinType INT = new BuiltinType("Int", TYPE_INT);
	public static final BuiltinType LONG = new BuiltinType("Long", TYPE_LONG);
	public static final BuiltinType FLOAT = new BuiltinType("Float", TYPE_FLOAT);
	public static final BuiltinType DOUBLE = new BuiltinType("Double", TYPE_DOUBLE);
	public static final BuiltinType INTRANGE = new BuiltinType("IntRange", TYPE_INTRANGE);
	public static final BuiltinType LONGRANGE = new BuiltinType("LongRange", TYPE_LONGRANGE);

	/* Object types. */
	public static final BuiltinType NULL = new BuiltinType("Null", TYPE_OBJECT);
	public static final BuiltinType ANY = new BuiltinType("Any", TYPE_OBJECT);
	public static final BuiltinType FUNCTION = new BuiltinType("Function", TYPE_OBJECT);
	public static final BuiltinType ARRAY = new BuiltinType("Array", TYPE_OBJECT);
	public static final BuiltinType ERROR = new BuiltinType("Error", TYPE_OBJECT);
	public static final BuiltinType STRING = new BuiltinType("String", TYPE_OBJECT);

	private BuiltinType(String name, int kind) {
		super(name, kind);
	}

	public boolean equals(Type other) {
		return this == other;
	}

	public Type superType() {
		return (this == ANY || this == NONE) ? null : ANY;
	}
}
