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

	private BuiltinType(String name, int numindex) {
		super(name);
		this.numindex = numindex;
	}

	/** Returns numeric index of this type.
	 * @return 1 for Int, 2 for Long, 3 for Float, 4 for Double, -1 for other types
	 */
	public int numIndex() {
		return numindex;
	}

	static public final BuiltinType typeInt = new BuiltinType("Int", 1);
	static public final BuiltinType typeLong = new BuiltinType("Long", 2);
	static public final BuiltinType typeFloat = new BuiltinType("Float", 3);
	static public final BuiltinType typeDouble = new BuiltinType("Double", 4);
	static public final BuiltinType typeString = new BuiltinType("String", -1);
	static public final BuiltinType typeBool = new BuiltinType("Bool", -1);
	static public final BuiltinType typeArray = new BuiltinType("Array", -1);
	static public final BuiltinType typeBArray = new BuiltinType("BArray", -1);
	static public final BuiltinType typeCArray = new BuiltinType("CArray", -1);
	static public final BuiltinType typeAny = new BuiltinType("Any", -1);
	static public final BuiltinType typeNone = new BuiltinType("[none]", -1);
}
