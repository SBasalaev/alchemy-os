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

package alchemy.system;

import alchemy.util.HashMap;

/**
 * A library is a container for function objects.
 *
 * @author Sergey Basalaev
 */
public abstract class Library {

	/** Maps names to functions. */
	protected final HashMap functions;

	/** Constructor for subclasses. */
	protected Library() {
		functions = new HashMap();
	}

	/**
	 * Returns function for given name.
	 * @param name function name
	 * @return
	 *   <code>Function</code> instance or <code>null</code>
	 *   if library does not contain function with given name
	 */
	public Function getFunction(String name) {
		return (Function) functions.get(name);
	}
	
	protected void putFunction(Function f) {
		functions.set(f.name, f);
	}
}
