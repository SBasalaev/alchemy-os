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
 * Compatibility layer with libcore 2.
 * 
 * @author Sergey Basalaev
 * @deprecated This will be dropped in 1.6
 */
public class LibCore20 extends NativeLibrary {

	public LibCore20() throws IOException {
		super("/libcore20.symbols");
	}

	public NativeFunction loadFunction(String name, int index) {
		return new LibCore20Func(name, index);
	}
}
