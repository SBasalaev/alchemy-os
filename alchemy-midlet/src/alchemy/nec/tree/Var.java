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

package alchemy.nec.tree;

/**
 * Variable.
 * @author Sergey Basalaev
 */
public class Var {
	/** Name of the variable. */
	public String name;

	/** Type of the variable. */
	public Type type;
	
	/** Indicates whether this variable is a constant. */
	public boolean isConst = false;
	
	/**
	 * If not null, contains constant or default value of variable.
	 */
	public Object constValue = null;
	
	/** Index assigned to a variable by compiler. */
	public int index = -1;

	public Var(String name, Type type) {
		this.name = name;
		this.type = type;
	}
}
