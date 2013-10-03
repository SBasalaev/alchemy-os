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
import java.net.HttpURLConnection;
import java.net.URLConnection;
import javax.microedition.io.HttpConnection;

/**
 * Implementation for HttpConnection.
 * @author Sergey Basalaev
 */
public class HttpConnectionImpl extends StreamConnectionImpl implements HttpConnection {

	public HttpConnectionImpl(URLConnection conn) {
		super(conn);
	}

	@Override
	public void close() throws IOException {
		((HttpURLConnection)conn).disconnect();
	}

	@Override
	public String getURL() {
		return conn.getURL().toExternalForm();
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
		return conn.getURL().getPort();
	}

	@Override
	public String getEncoding() {
		return conn.getContentEncoding();
	}

	@Override
	public String getType() {
		return conn.getContentType();
	}

	@Override
	public long getLength() {
		return conn.getContentLengthLong();
	}

	@Override
	public String getRequestMethod() {
		return ((HttpURLConnection)conn).getRequestMethod();
	}

	@Override
	public void setRequestMethod(String method) throws IOException {
		((HttpURLConnection)conn).setRequestMethod(method);
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
		return ((HttpURLConnection)conn).getResponseCode();
	}

	@Override
	public String getResponseMessage() throws IOException {
		return ((HttpURLConnection)conn).getResponseMessage();
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
