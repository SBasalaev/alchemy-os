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

import alchemy.io.ConnectionInputStream;
import alchemy.io.ConnectionOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import javax.microedition.io.impl.HttpConnectionImpl;
import javax.microedition.io.impl.HttpsConnectionImpl;
import javax.microedition.io.impl.SecureConnectionImpl;
import javax.microedition.io.impl.ServerSocketConnectionImpl;
import javax.microedition.io.impl.SocketConnectionImpl;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

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
		int colon = name.indexOf(':');
		if (colon <= 0) {
			throw new IllegalArgumentException(name);
		}
		String protocol = name.substring(0, colon);
		if (protocol.equals("http")) {
			URLConnection conn = new URL(name).openConnection();
			if (conn instanceof HttpURLConnection) {
				return new HttpConnectionImpl((HttpURLConnection)conn);
			}
		} else if (protocol.equals("https")) {
			URLConnection conn = new URL(name).openConnection();
			if (conn instanceof HttpURLConnection) {
				return new HttpsConnectionImpl((HttpsURLConnection)conn);
			}
		} else if (protocol.equals("socket")) {
			if (!name.startsWith("socket://"))
				throw new IllegalArgumentException(name);
			name = name.substring("socket://".length());
			colon = name.lastIndexOf(':');
			String host;
			int port;
			if (colon < 0) {
				host = name;
				port = -1;
			} else {
				host = name.substring(0, colon);
				port = Integer.parseInt(name.substring(colon+1));
			}
			if (host.length() == 0) {
				return new ServerSocketConnectionImpl(port);
			} else if (port == -1) {
				throw new IllegalArgumentException("Port missing");
			} else {
				Socket socket = new Socket(host, port);
				return new SocketConnectionImpl(socket);
			}
		} else if (protocol.equals("ssl")) {
			if (!name.startsWith("ssl://"))
				throw new IllegalArgumentException(name);
			name = name.substring("ssl://".length());
			colon = name.lastIndexOf(':');
			if (colon < 0)
				throw new IllegalArgumentException("Port missing");
			String host = name.substring(0, colon);
			int port = Integer.parseInt(name.substring(colon+1));
			SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(host, port);
			return new SecureConnectionImpl(socket);
		}
		throw new ConnectionNotFoundException("Connection not found: " + protocol);
	}

	public static InputStream openInputStream(String name) throws IOException {
		return new ConnectionInputStream((InputConnection)open(name));
	}

	public static OutputStream openOutputStream(String name) throws IOException {
		return new ConnectionOutputStream((OutputConnection)open(name));
	}
}
