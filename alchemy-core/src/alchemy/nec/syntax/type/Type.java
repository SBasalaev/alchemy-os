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
 * The type of the expression.
 *
 * @author Sergey Basalaev
 */
public abstract class Type {
	public static final int TYPE_NONE = 0;
	public static final int TYPE_BOOL = 1;
	public static final int TYPE_BYTE = 2;
	public static final int TYPE_SHORT = 3;
	public static final int TYPE_CHAR = 4;
	public static final int TYPE_INT = 5;
	public static final int TYPE_LONG = 6;
	public static final int TYPE_FLOAT = 7;
	public static final int TYPE_DOUBLE = 8;
	public static final int TYPE_OBJECT = 9;
	public static final int TYPE_FUNCTION = 10;
	public static final int TYPE_ARRAY = 11;
	public static final int TYPE_INTRANGE = 12;
	public static final int TYPE_LONGRANGE = 13;

	/** Kind of this type, one of TYPE_* constants. */
	public final int kind;
	public final String name;

	protected Type(String name, int kind) {
		this.kind = kind;
		this.name = name;
	}

	public abstract boolean equals(Type other);

	public String toString() {
		return name;
	}

	public final boolean equals(Object other) {
		if (other == null || !(other instanceof Type)) return false;
		return equals((Type)other);
	}

	public abstract Type superType();

	/**
	 * Returns true if value of this type can be safely used as value of given type.
	 */
	public final boolean safeToCastTo(Type other) {
		if (kind == TYPE_ARRAY) {
			if (other == BuiltinType.ARRAY || other == BuiltinType.ANY) return true;
		} else if (kind == TYPE_FUNCTION) {
			if (other == BuiltinType.FUNCTION || other == BuiltinType.ANY) return true;
			if (other.kind != TYPE_FUNCTION) return false;
			FunctionType self = (FunctionType)this;
			FunctionType func = (FunctionType)other;
			if (self.argtypes.length != func.argtypes.length) return false;
			for (int i=self.argtypes.length-1; i>=0; i--) {
				if (!func.argtypes[i].safeToCastTo(self.argtypes[i])) return false;
			}
			return self.returnType.safeToCastTo(func.returnType);
		} else if (kind == TYPE_OBJECT && other.kind == TYPE_OBJECT) {
			Type self = this;
			if (self == BuiltinType.NULL) return true;
			while (self != null) {
				if (self.equals(other)) return true;
				self = self.superType();
			}
			return false;
		}
		return equals(other);
	}

	public static Type commonSuperType(Type t1, Type t2) {
		if (t1.kind == Type.TYPE_NONE || t2.kind == Type.TYPE_NONE)
			return BuiltinType.NONE;
		while (t2 != null && !t1.safeToCastTo(t2)) {
			t2 = t2.superType();
		}
		return t2;
	}

	public final boolean isNumeric() {
		int k = kind;
		return k == Type.TYPE_BYTE || k == Type.TYPE_CHAR
		    || k == Type.TYPE_SHORT || k == Type.TYPE_INT
		    || k == Type.TYPE_LONG || k == Type.TYPE_FLOAT
		    || k == Type.TYPE_DOUBLE;
	}
}
