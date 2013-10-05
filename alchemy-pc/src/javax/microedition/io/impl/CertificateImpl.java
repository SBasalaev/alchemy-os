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

import java.security.cert.X509Certificate;
import javax.microedition.pki.Certificate;

/**
 * Implementation of server side certificate for Alchemy OS.
 * @author Sergey Basalaev
 */
class CertificateImpl implements Certificate {

	private X509Certificate cert;

	CertificateImpl(X509Certificate cert) {
		this.cert = cert;
	}

	@Override
	public String getSubject() {
		return cert.getSubjectX500Principal().getName();
	}

	@Override
	public String getIssuer() {
		return cert.getIssuerX500Principal().getName();
	}

	@Override
	public String getSigAlgName() {
		return cert.getSigAlgName();
	}

	@Override
	public long getNotBefore() {
		return cert.getNotBefore().getTime();
	}

	@Override
	public long getNotAfter() {
		return cert.getNotAfter().getTime();
	}

	@Override
	public String getType() {
		return cert.getType();
	}

	@Override
	public String getVersion() {
		return String.valueOf(cert.getVersion());
	}

	private char hexDigit(int number) {
		if (number >= 0 && number <= 9) return (char)(number + '0');
		else return (char)(number - 10 + 'A');
	}

	@Override
	public String getSerialNumber() {
		byte[] bytes = cert.getSerialNumber().toByteArray();
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<bytes.length; i++) {
			if (i != 0) sb.append(':');
			sb.append(hexDigit((bytes[i] >> 8) & 0xf));
			sb.append(hexDigit(bytes[i] & 0xf));
		}
		return sb.toString();
	}
}
