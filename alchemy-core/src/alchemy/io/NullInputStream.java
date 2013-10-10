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

package alchemy.io;

import java.io.InputStream;
import javax.microedition.io.Connection;

/**
 * The input stream that always returns the same number.
 * Used to implement /dev/zero and /dev/null
 *
 * @author Sergey Basalaev
 */
public final class NullInputStream extends InputStream implements Connection {
	
	private final int b;
	
	public NullInputStream(int b) {
		this.b = b;
	}

	public int read() {
		return b;
	}

	public void reset() { }
	public boolean markSupported() { return true; }
}
