/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011-2013, Sergey Basalaev <sbasalaev@gmail.com>
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

import alchemy.nec.syntax.Var;

/**
 * Object type.
 * <pre>
 * <b>type</b> <i>Name</i> &lt; <i>Supertype</i> {
 *   field1: type1,
 *   field2: type2 = defaultValue,
 *   ...
 *   fieldN: typeN
 * }
 * </pre>
 * 
 * @author Sergey Basalaev
 */
public final class ObjectType extends Type {

	public final Var[] fields;
	public final ObjectType parent;

	public ObjectType(String name, ObjectType parent, Var[] fields) {
		super(name, TYPE_OBJECT);
		this.parent = parent;
		this.fields = fields;
	}

	public boolean equals(Type other) {
		return this.name.equals(other.name);
	}

	public Type superType() {
		return (this.parent != null) ? (Type)this.parent : (Type)BuiltinType.ANY;
	}
}
