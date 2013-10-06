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
import javax.microedition.io.Connection;
import javax.microedition.io.InputConnection;

/**
 * Input stream connected to the external resource.
 * When this input stream is closed, the underlying resource
 * (stream or connection) is also closed. This class also serves
 * as workaround for several known bugs on mobile platforms:
 * <ul>
 *   <li>Nokia phones fail to read zero bytes</li>
 *   <li>Siemens phones implement skip as seek</li>
 * </ul>
 *
 * @author Sergey Basalaev
 */
public final class ConnectionInputStream extends InputStream implements Connection {

	private final InputStream input;
	private final InputConnection conn;

	/**
	 * Wraps given input stream.
	 */
	public ConnectionInputStream(InputStream in) {
		this.conn = null;
		this.input = in;
	}

	/**
	 * Wraps input stream from the given connection.
	 */
	public ConnectionInputStream(InputConnection in) throws IOException {
		this.conn = in;
		this.input = in.openInputStream();
	}

	public void close() throws IOException {
		if (conn != null) conn.close();
		input.close();
	}

	public int read() throws IOException {
		return input.read();
	}

	public int read(byte[] buf, int ofs, int len) throws IOException {
		if (buf == null)
			throw new NullPointerException();
		if (ofs < 0 || len < 0 || ofs+len > buf.length)
			throw new IndexOutOfBoundsException();
		if (len == 0)
			return 0;
		return input.read(buf, ofs, len);
	}

	public long skip(long n) throws IOException {
		if (n <= 0L) return 0L;
		
		long remains = n;
		byte[] buf = new byte[Math.min((int)remains, 1024)];
		while (remains > 0L) {
			int skipped = read(buf, 0, Math.min((int)remains, buf.length));
			if (skipped <= 0) break;
			remains -= skipped;
		}
		return n - remains;
	}

	public void reset() throws IOException {
		input.reset();
	}
}
