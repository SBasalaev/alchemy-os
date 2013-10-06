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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import javax.microedition.io.HttpConnection;

/**
 * Implementation for HttpConnection.
 * @author Sergey Basalaev
 */
public class HttpConnectionImpl implements HttpConnection {

	protected final HttpURLConnection conn;

	public HttpConnectionImpl(HttpURLConnection conn) {
		this.conn = conn;
		this.conn.setInstanceFollowRedirects(false);
	}

	@Override
	public void close() throws IOException {
		conn.disconnect();
	}

	@Override
	public InputStream openInputStream() throws IOException {
		return conn.getInputStream();
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		return conn.getOutputStream();
	}

	@Override
	public String getURL() {
		return conn.getURL().toString();
	}

	@Override
	public String getProtocol() {
		return conn.getURL().getProtocol();
	}

	@Override
	public String getHost() {
		return conn.getURL().getHost();
	}

	@Override
	public String getFile() {
		return conn.getURL().getFile();
	}

	@Override
	public String getRef() {
		return conn.getURL().getRef();
	}

	@Override
	public String getQuery() {
		return conn.getURL().getQuery();
	}

	@Override
	public int getPort() {
		int port = conn.getURL().getPort();
		return (port != -1) ? port : 80;
	}

	@Override
	public String getEncoding() {
		try {
			return getHeaderField("content-encoding");
		} catch (IOException ioe) {
			return null;
		}
	}

	@Override
	public String getType() {
		try {
			return getHeaderField("content-type");
		} catch (IOException ioe) {
			return null;
		}
	}

	@Override
	public long getLength() {
		try {
			return getHeaderFieldDate("content-length", -1);
		} catch (IOException ioe) {
			return -1;
		}
	}

	@Override
	public String getRequestMethod() {
		return conn.getRequestMethod();
	}

	@Override
	public void setRequestMethod(String method) throws IOException {
		conn.setRequestMethod(method);
	}

	@Override
	public String getRequestProperty(String key) {
		return conn.getRequestProperty(key);
	}

	@Override
	public void setRequestProperty(String key, String value) throws IOException {
		try {
			conn.setRequestProperty(key, value);
		} catch (IllegalStateException ise) {
			throw new IOException(ise);
		}
	}

	@Override
	public int getResponseCode() throws IOException {
		return conn.getResponseCode();
	}

	@Override
	public String getResponseMessage() throws IOException {
		return conn.getResponseMessage();
	}

	@Override
	public long getExpiration() throws IOException {
		return conn.getExpiration();
	}

	@Override
	public long getDate() throws IOException {
		return conn.getDate();
	}

	@Override
	public long getLastModified() throws IOException {
		return conn.getLastModified();
	}

	@Override
	public String getHeaderField(String name) throws IOException {
		return conn.getHeaderField(name);
	}

	@Override
	public int getHeaderFieldInt(String name, int def) throws IOException {
		return conn.getHeaderFieldInt(name, def);
	}

	@Override
	public long getHeaderFieldDate(String name, long def) throws IOException {
		return conn.getHeaderFieldDate(name, def);
	}

	@Override
	public String getHeaderField(int n) throws IOException {
		return conn.getHeaderField(n);
	}

	@Override
	public String getHeaderFieldKey(int n) throws IOException {
		return conn.getHeaderFieldKey(n);
	}
}
