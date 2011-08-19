/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011  Sergey Basalaev <sbasalaev@gmail.com>
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
 * Type of composite structure.
 * @author Sergey Basalaev
 */
public class StructureType extends Type {

	private final String name;
	public Var[] fields;

	public StructureType(String name) {
		this.name = name;
	}

	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj.getClass() != StructureType.class) return false;
		return ((StructureType)obj).name.equals(name);
	}

	public String toString() {
		return name;
	}
}
