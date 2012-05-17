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

import alchemy.core.HashLibrary;
import alchemy.util.UTFReader;
import java.io.IOException;

/**
 * Native UI library for Alchemy.
 * @author Sergey Basalaev
 * @version 0.1.0
 */
public class LibUI01 extends HashLibrary {
	
	public LibUI01() throws IOException {
		UTFReader r = new UTFReader(getClass().getResourceAsStream("/libui01.symbols"));
		String name;
		int index = 0;
		while ((name = r.readLine()) != null) {
			putFunc(new LibUI01Func(name, index));
			index++;
		}
		r.close();
		lock();
	}
}
