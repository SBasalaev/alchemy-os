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
 * Array type.
 * <pre>
 * [<i>elementType</i>]
 * </pre>
 * 
 * @author Sergey Basalaev.
 */
public final class ArrayType extends Type {

	public final Type elementType;

	public ArrayType(Type elementType) {
		super("Array", TYPE_ARRAY);
		this.elementType = elementType;
	}

	public boolean equals(Type type) {
		if (type.kind != TYPE_ARRAY) return false;
		final ArrayType other = (ArrayType)type;
		return this.elementType.equals(other.elementType);
	}

	public String toString() {
		return "["+elementType+"]";
	}

	public Type superType() {
		return BuiltinType.ARRAY;
	}
}
