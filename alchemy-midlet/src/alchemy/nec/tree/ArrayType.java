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
 * Array type.
 * <pre>
 * [<i>elementType</i>]
 * </pre>
 * 
 * @author Sergey Basalaev.
 */
public class ArrayType extends Type {
	
	private final Type elementType;
	
	public ArrayType(Type elementType) {
		this.elementType = elementType;
	}

	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (!(obj instanceof ArrayType)) return false;
		ArrayType other = (ArrayType)obj;
		return this.elementType.equals(other.elementType);
	}

	public String toString() {
		return "["+elementType+"]";
	}

	public Type superType() {
		return BuiltinType.ARRAY;
	}
	
	public Type elementType() {
		return elementType;
	}
}
