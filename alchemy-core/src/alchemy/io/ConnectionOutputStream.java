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
import java.io.OutputStream;
import javax.microedition.io.Connection;
import javax.microedition.io.OutputConnection;

/**
 * Output stream connected to the external resource.
 * When this output stream is closed, the underlying resource
 * (stream or connection) is also closed. This class also serves
 * as workaround for several known bugs on mobile platforms:
 * <ul>
 *   <li>Nokia phones fail to write zero bytes</li>
 * </ul>
 *
 * @author Sergey Basalaev
 */
public final class ConnectionOutputStream extends OutputStream implements Connection {

	private final OutputStream output;
	private final OutputConnection conn;

	/**
	 * Wraps given output stream.
	 */
	public ConnectionOutputStream(OutputStream out) {
		this.conn = null;
		this.output = out;
	}

	/**
	 * Wraps output stream from the given connection.
	 */
	public ConnectionOutputStream(OutputConnection out) throws IOException {
		this.conn = out;
		this.output = out.openOutputStream();
	}

	public void close() throws IOException {
		if (conn != null) conn.close();
		output.close();
	}

	public void write(int b) throws IOException {
		output.write(b);
	}

	public void write(byte[] buf, int ofs, int len) throws IOException {
		if (buf == null)
			throw new NullPointerException();
		if (ofs < 0 || len < 0 || ofs+len > buf.length)
			throw new IndexOutOfBoundsException();
		if (len == 0)
			return;
		output.write(buf, ofs, len);
	}
}
