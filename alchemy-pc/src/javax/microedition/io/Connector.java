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
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import javax.microedition.io.impl.HttpConnectionImpl;
import javax.microedition.io.impl.HttpsConnectionImpl;

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

	public static Connection open(String name) throws IOException {
		URL url = new URL(name);
		if (url.getProtocol().equals("http")) {
			return new HttpConnectionImpl(url.openConnection());
		} else if (url.getProtocol().equals("https")) {
			return new HttpsConnectionImpl(url.openConnection());
		} else {
			throw new ConnectionNotFoundException("Unsupported protocol: " + url.getProtocol());
		}
	}

	public static DataInputStream openDataInputStream(String name) throws IOException {
		StreamConnection conn = null;
		try {
			conn = (StreamConnection) open(name);
			return conn.openDataInputStream();
		} finally {
			try {
				if (conn != null) conn.close();
			} catch (IOException ioe) { }
		}
	}

	public static InputStream openInputStream(String name) throws IOException {
		StreamConnection conn = null;
		try {
			conn = (StreamConnection) open(name);
			return conn.openInputStream();
		} finally {
			try {
				if (conn != null) conn.close();
			} catch (IOException ioe) { }
		}
	}

	public static DataOutputStream openDataOutputStream(String name) throws IOException {
		StreamConnection conn = null;
		try {
			conn = (StreamConnection) open(name);
			return conn.openDataOutputStream();
		} finally {
			try {
				if (conn != null) conn.close();
			} catch (IOException ioe) { }
		}
	}

	public static OutputStream openOutputStream(String name) throws IOException {
		StreamConnection conn = null;
		try {
			conn = (StreamConnection) open(name);
			return conn.openOutputStream();
		} finally {
			try {
				if (conn != null) conn.close();
			} catch (IOException ioe) { }
		}
	}
}
