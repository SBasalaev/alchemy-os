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

package alchemy.core;

import java.util.Hashtable;

/**
 * Library implementation backed with a hashtable.
 * <p>
 * This library manages the pool of functions in a hashtable.
 *
 * @author Sergey Basalaev
 */
public class HashLibrary extends Library {

	/**
	 * Creates new empty <code>HashLibrary</code>.
	 */
	public HashLibrary() {
	}

	/** Library functions. */
	private final Hashtable functions = new Hashtable();
	/** If <code>true</code> then library cannot be modified. */
	private boolean locked = false;

	/** {@inheritDoc} */
	public Function getFunction(String sig) {
		return (Function)functions.get(sig);
	}

	/**
	 * Puts function in this library.
	 * @param f new function
	 * @return previous function for this signature
	 * @throws SecurityException if library is locked
	 */
	public Function putFunc(Function f) {
		if (locked) throw new SecurityException();
		return (Function)functions.put(f.signature,f);
	}

	/**
	 * Removes function from this library.
	 * @param sig function signature
	 * @return function for this signature
	 * @throws SecurityException if library is locked
	 */
	public Function removeFunc(String sig) {
		if (locked) throw new SecurityException();
		return (Function)functions.remove(sig);
	}

	/**
	 * Locks this library so its contents cannot be changed.
	 */
	public void lock() {
		locked = true;
	}
}
