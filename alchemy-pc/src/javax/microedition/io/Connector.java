/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2013, Sergey Basalaev <sbasalaev@gmail.com>
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

package javax.microedition.io;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;

/**
 * This is implementation of the generic connection
 * framework for PC version of Alchemy OS.
 * @author Sergey Basalaev
 */
public class Connector {

	private Connector() { }

	public static final int READ = 1;
	public static final int WRITE = 2;
	public static final int READ_WRITE = READ | WRITE;

	public static DataInputStream openDataInputStream(String name) throws IOException {
		return new DataInputStream(openInputStream(name));
	}

	public static InputStream openInputStream(String name) throws IOException {
		return new URL(name).openStream();
	}
}