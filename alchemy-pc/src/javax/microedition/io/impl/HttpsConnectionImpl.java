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
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import javax.microedition.io.HttpsConnection;
import javax.microedition.io.SecurityInfo;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

/**
 * Implementation for https connection.
 * @author Sergey Basalaev
 */
public class HttpsConnectionImpl extends HttpConnectionImpl implements HttpsConnection {

	public HttpsConnectionImpl(URLConnection conn) {
		super(conn);
	}

	@Override
	public SecurityInfo getSecurityInfo() throws IOException {
		try {
			return new SecurityInfoImpl((HttpsURLConnection)conn, SSLContext.getInstance("TLS").getProtocol());
		} catch (NoSuchAlgorithmException nsae) {
			throw new IOException(nsae);
		}
	}
}
