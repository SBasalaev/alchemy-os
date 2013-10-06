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
import java.security.cert.X509Certificate;
import javax.microedition.io.SecurityInfo;
import javax.microedition.pki.Certificate;

/**
 * Implementation of security info.
 * @author Sergey Basalaev
 */
class SecurityInfoImpl implements SecurityInfo {

	private final String protocol;
	private final String cipherSuite;
	private final CertificateImpl cert;

	SecurityInfoImpl(String protocol, String cipherSuite, X509Certificate cert) throws IOException {
		this.protocol = protocol;
		this.cipherSuite = cipherSuite;
		this.cert = new CertificateImpl(cert);
	}

	@Override
	public String getCipherSuite() {
		return cipherSuite;
	}

	@Override
	public Certificate getServerCertificate() {
		return cert;
	}

	@Override
	public String getProtocolName() {
		if (protocol.startsWith("SSL")) return "SSL";
		else if (protocol.startsWith("TLS")) return "TLS";
		else throw new RuntimeException(protocol);
	}

	@Override
	public String getProtocolVersion() {
		if (protocol.startsWith("SSL")) return "3.0";
		else if (protocol.startsWith("TLS")) return "3.1";
		else throw new RuntimeException(protocol);
	}
}
