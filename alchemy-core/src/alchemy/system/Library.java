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
 * Library is a container for function objects.
 * The name of a library is a debugging information
 * that will appear in stack traces. If library itself
 * does not supply a name, it will be assigned by the system.
 *
 * @author Sergey Basalaev
 */
public class Library {

	/** Maps names to functions. */
	private final HashMap functions;
	/** Name assigned to this library. */
	protected String name;

	/** Constructor for subclasses. */
	public Library() {
		this(null);
	}

	/** Constructor for subclasses. */
	public Library(String name) {
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

	/** Puts public function in this library. */
	public void putFunction(Function f) {
		if (f.library != null) throw new IllegalArgumentException();
		f.library = this;
		functions.set(f.name, f);
	}

	/** Returns number of functions in this library. */
	public int size() {
		return functions.size();
	}

	public String toString() {
		return name;
	}
}
