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
 * Builtin types.
 * @author Sergey Basalaev
 */
public class BuiltinType extends NamedType {

	private final int numindex;
	
	private BuiltinType(String name, Type superType, int numindex) {
		super(name, superType);
		this.numindex = numindex;
	}

	/** Returns numeric index of this type.
	 * @return 1 for Int, 2 for Long, 3 for Float, 4 for Double, -1 for other types
	 */
	public int numIndex() {
		return numindex;
	}

	static public final BuiltinType ANY = new BuiltinType("Any", null, -1);
	static public final BuiltinType INT = new BuiltinType("Int", ANY, 1);
	static public final BuiltinType LONG = new BuiltinType("Long", ANY, 2);
	static public final BuiltinType FLOAT = new BuiltinType("Float", ANY, 3);
	static public final BuiltinType DOUBLE = new BuiltinType("Double", ANY, 4);
	static public final BuiltinType STRING = new BuiltinType("String", ANY, -1);
	static public final BuiltinType BOOL = new BuiltinType("Bool", ANY, -1);
	static public final BuiltinType ARRAY = new BuiltinType("Array", ANY, -1);
	static public final BuiltinType BARRAY = new BuiltinType("BArray", ANY, -1);
	static public final BuiltinType CARRAY = new BuiltinType("CArray", ANY, -1);
	static public final BuiltinType FUNCTION = new BuiltinType("Function", ANY, -1);
	static public final BuiltinType STRUCTURE = new BuiltinType("Structure", ANY, -1);
	/** Special type that used when there is no return value. */
	static public final BuiltinType NONE = new BuiltinType("*none*", null, -1);
}
