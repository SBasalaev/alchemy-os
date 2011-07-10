/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011  Sergey Basalaev <sbasalaev@gmail.com>
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

import alchemy.core.Context;
import alchemy.core.LibBuilder;
import alchemy.core.Library;
import alchemy.util.UTFReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * Builder for native libraries.
 * Native library starts with magic '#@' which
 * follows class name.
 *
 * @author Sergey Basalaev
 */
public class NativeLibBuilder implements LibBuilder {

	public NativeLibBuilder() { }

	public Library build(Context c, InputStream in) throws IOException, InstantiationException {
		UTFReader r = new UTFReader(in);
		String classname = r.readLine();
		try {
			return (Library)Class.forName(classname).newInstance();
		} catch (ClassNotFoundException cnfe) {
			throw new InstantiationException("Class not found: "+classname);
		} catch (IllegalAccessException iae) {
			throw new InstantiationException("Class not accessible: "+classname);
		} catch (ClassCastException cce) {
			throw new InstantiationException("Not a library: "+classname);
		}
	}
}
