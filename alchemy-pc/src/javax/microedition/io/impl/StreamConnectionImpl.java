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

package javax.microedition.io.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import javax.microedition.io.StreamConnection;

/**
 * Implementation of stream connection.
 * @author Sergey Basalaev
 */
public abstract class StreamConnectionImpl implements StreamConnection {

	protected final URLConnection conn;

	public StreamConnectionImpl(URLConnection conn) {
		this.conn = conn;
	}

	@Override
	public InputStream openInputStream() throws IOException {
		return conn.getInputStream();
	}

	@Override
	public DataInputStream openDataInputStream() throws IOException {
		return new DataInputStream(openInputStream());
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		return conn.getOutputStream();
	}

	@Override
	public DataOutputStream openDataOutputStream() throws IOException {
		return new DataOutputStream(openOutputStream());
	}
}
