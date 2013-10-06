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
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.microedition.io.HttpsConnection;
import javax.microedition.io.SecurityInfo;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

/**
 * Implementation for https connection.
 * @author Sergey Basalaev
 */
public class HttpsConnectionImpl extends HttpConnectionImpl implements HttpsConnection {

	public HttpsConnectionImpl(HttpsURLConnection conn) {
		super(conn);
	}

	@Override
	public SecurityInfo getSecurityInfo() throws IOException {
		HttpsURLConnection https = (HttpsURLConnection) conn;
		if (https.getServerCertificates().length == 0) {
			throw new IOException("No certificates");
		}
		X509Certificate cert = (X509Certificate) https.getServerCertificates()[0];
		try {
			return new SecurityInfoImpl(SSLContext.getInstance("TLS").getProtocol(), https.getCipherSuite(), cert);
		} catch (NoSuchAlgorithmException nsae) {
			throw new IOException(nsae);
		}
	}
}
