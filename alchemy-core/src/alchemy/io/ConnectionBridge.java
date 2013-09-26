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

package alchemy.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.Connection;

/**
 * Wrapper around input and output streams.
 * Method close() closes encapsulated stream.
 *
 * @author Sergey Basalaev
 */
public final class ConnectionBridge implements Connection {

	private final InputStream in;
	private final OutputStream out;

	public ConnectionBridge(InputStream stream) {
		this.in = stream;
		this.out = null;
	}

	public ConnectionBridge(OutputStream out) {
		this.in = null;
		this.out = out;
	}

	public void close() throws IOException {
		if (in != null) {
			in.close();
		} else {
			out.close();
		}
	}
}
