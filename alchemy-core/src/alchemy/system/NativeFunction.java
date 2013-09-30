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

/**
 * Function loaded by native library.
 *
 * @author Sergey Basalaev
 */
final class NativeFunction extends Function {
	
	private final NativeLibrary lib;
	
	/** Index of this function in invokeNative. */
	private final int index;
	
	public NativeFunction(NativeLibrary lib, String name, int index) {
		super(name);
		this.index = index;
		this.lib = lib;
	}
	
	public final Object invoke(Process p, Object[] args) throws AlchemyException {
		try {
			return lib.invokeNative(this.index, p, args);
		} catch (Exception e) {
			AlchemyException ae = new AlchemyException(e);
			ae.addTraceElement(name, "native");
			throw ae;
		}
	}
}
