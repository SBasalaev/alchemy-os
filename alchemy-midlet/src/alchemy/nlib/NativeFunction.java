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

import alchemy.core.AlchemyException;
import alchemy.core.Context;
import alchemy.core.Function;

/**
 * Skeleton for functions loaded by native libraries.
 * For speed and compactness all functions of native
 * library are implemented in single class.
 * 
 * The invokeNative method should be implemented using switch on index:
 * <pre>
 * protected Object execNative(Context c, Object[] args) throws Exception {
 *   switch (index) {
 *     case 0: {
 *       // implement function with index 0
 *     }
 *     case 1: {
 *       // implement function with index 1
 *     }
 *     ...
 *     default:
 *       return null
 *   }
 * }
 * </pre>
 * 
 * @author Sergey Basalaev
 */
public final class NativeFunction extends Function {
	
	private final NativeLibrary lib;
	
	/** Index of this function in invokeNative. */
	private final int index;
	
	public NativeFunction(NativeLibrary lib, String name, int index) {
		super(name);
		this.index = index;
		this.lib = lib;
	}
	
	public String toString() {
		return lib.soname()+':'+signature;
	}
	
	public final Object invoke(Context c, Object[] args) throws AlchemyException {
		try {
			return lib.invokeNative(this.index, c, args);
		} catch (Exception e) {
			AlchemyException ae = new AlchemyException(e);
			ae.addTraceElement(this, "native");
			throw ae;
		}
	}
}
