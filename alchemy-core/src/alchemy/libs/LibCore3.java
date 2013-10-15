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

package alchemy.libs;

import alchemy.system.NativeLibrary;
import alchemy.system.Process;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Alchemy core runtime library.
 *
 * @deprecated Will be removed after new API is finished
 * @author Sergey Basalaev
 * @version 3.0
 */
public class LibCore3 extends NativeLibrary {

	/**
	 * Constructor without parameters to load
	 * library through the native interface.
	 * @throws IOException if I/O error occured while reading
	 *         function definitions file
	 */
	public LibCore3() throws IOException {
		load("/symbols/core3");
		name = "libcore.3.so";
	}

	protected Object invokeNative(int index, Process p, Object[] args) throws Exception {
		switch (index) {
			case 36: // getenv(key: String): String
				return p.getEnv(((String)args[0]));
			case 37: // setenv(key: String, value: String)
				p.setEnv(((String)args[0]), (String)args[1]);
				return null;
			case 56: // systime(): Long
				return Lval(System.currentTimeMillis());
			case 124: // sleep(millis: Int)
				Thread.sleep(ival(args[0]));
				return null;
			case 175: // sys_property(key: String): String
				return System.getProperty((String)args[0]);
			default:
				return null;
		}
	}
}
