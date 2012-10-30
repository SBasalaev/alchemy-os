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

package alchemy.libs;

import alchemy.nlib.NativeFunction;
import alchemy.nlib.NativeLibrary;
import java.io.IOException;

/**
 * Basic media library for Alchemy OS.
 * 
 * @author Sergey Basalaev
 * @version 1.0
 */
public class LibMedia1 extends NativeLibrary {

	public LibMedia1() throws IOException {
		super("/libmedia1.symbols");
	}

	public NativeFunction loadFunction(String name, int index) {
		return new LibMedia1Func(name, index);
	}

}