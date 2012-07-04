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
 * Type that is uniquely identified by its name.
 * <p/>
 * This very class is used for abstract types. Subclasses are
 * type implementations (e.g. structure type). When type is
 * implemented in E code, Type instance in type cache is replaced
 * by implementation with the same name (so they are considered
 * equal).
 * 
 * @author Sergey Basalaev
 */
public class NamedType extends Type {

	private final String name;
	private final Type superType;

	public NamedType(String name, Type superType) {
		this.name = name;
		this.superType = superType;
	}

	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (!(obj instanceof NamedType)) return false;
		NamedType other = (NamedType)obj;
		return this.name.equals(other.name);
	}

	public String toString() {
		return name;
	}

	public Type superType() {
		return superType;
	}
}