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
	
	private BuiltinType(String name, Type superType) {
		super(name, superType);
	}

	/* Root of type hierarchy. */
	static public final BuiltinType ANY = new BuiltinType("Any", null);
	/* Numeric types. */
	static public final BuiltinType NUMBER = new BuiltinType("<number>", ANY);
	static public final BuiltinType BYTE = new BuiltinType("Byte", NUMBER);
	static public final BuiltinType SHORT = new BuiltinType("Short", NUMBER);
	static public final BuiltinType CHAR = new BuiltinType("Char", NUMBER);
	static public final BuiltinType INT = new BuiltinType("Int", NUMBER);
	static public final BuiltinType LONG = new BuiltinType("Long", NUMBER);
	static public final BuiltinType FLOAT = new BuiltinType("Float", NUMBER);
	static public final BuiltinType DOUBLE = new BuiltinType("Double", NUMBER);
	/* Array types. */
	static public final BuiltinType ARRAY = new BuiltinType("Array", ANY);
	static public final BuiltinType BARRAY = new BuiltinType("[Byte]", ARRAY);
	static public final BuiltinType CARRAY = new BuiltinType("[Char]", ARRAY);
	static public final BuiltinType SARRAY = new BuiltinType("[Short]", ARRAY);
	static public final BuiltinType ZARRAY = new BuiltinType("[Bool]", ARRAY);
	static public final BuiltinType IARRAY = new BuiltinType("[Int]", ARRAY);
	static public final BuiltinType LARRAY = new BuiltinType("[Long]", ARRAY);
	static public final BuiltinType FARRAY = new BuiltinType("[Float]", ARRAY);
	static public final BuiltinType DARRAY = new BuiltinType("[Double]", ARRAY);
	/* Others. */
	static public final BuiltinType STRING = new BuiltinType("String", ANY);
	static public final BuiltinType BOOL = new BuiltinType("Bool", ANY);
	static public final BuiltinType FUNCTION = new BuiltinType("Function", ANY);
	static public final BuiltinType STRUCTURE = new BuiltinType("Structure", ANY);
	static public final BuiltinType ERROR = new BuiltinType("Error", ANY);
	/* A special type which is subtype of all types. */
	static public final BuiltinType NULL = new BuiltinType("<null>", ANY);
	/* A special type which is used when expression does not return a value. */
	static public final BuiltinType NONE = new BuiltinType("<none>", null);
}
