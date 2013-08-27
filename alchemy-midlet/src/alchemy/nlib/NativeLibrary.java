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

package alchemy.nlib;

import alchemy.core.Function;
import alchemy.core.Library;
import alchemy.util.UTFReader;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Native library.
 * Native function signatures and indices are determined
 * from the resource file which contains function names
 * in implementation order.
 * 
 * @author Sergey Basalaev
 */
public abstract class NativeLibrary extends Library {

	/** Maps function name to function object. */
	private Hashtable functions = new Hashtable();
	
	protected NativeLibrary() { }
	
	/** Loads functions using specified symbols file. */
	public void load(String symbols) throws IOException {
		UTFReader r = new UTFReader(getClass().getResourceAsStream(symbols));
		int index = functions.size();
		String name;
		while ((name = r.readLine()) != null) {
			functions.put(name, loadFunction(name, index));
			index++;
		}
		r.close();
	}
	
	/** Returns native function of appropriate class. */
	public abstract NativeFunction loadFunction(String name, int index);

	public Function getFunction(String sig) {
		return (Function)functions.get(sig);
	}
}
