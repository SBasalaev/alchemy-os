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

package alchemy.nlib;

import alchemy.core.Function;

/**
 * Skeleton for functions loaded by native libraries.
 * For speed and compactness all functions of native
 * library are implemented in single class. exec()
 * function is a switch where requested function is
 * chosen by integer index.
 * 
 * @author Sergey Basalaev
 */
public abstract class NativeFunction extends Function {
	
	/** Index of this function. */
	protected final int index;
	
	public NativeFunction(String name, int index) {
		super(name);
		this.index = index;
	}
}
