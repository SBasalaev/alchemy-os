/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011-2012, Sergey Basalaev <sbasalaev@gmail.com>
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

package alchemy.libs;

import alchemy.core.Context;
import alchemy.nlib.NativeFunction;
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.HttpsConnection;
import javax.microedition.io.SecureConnection;
import javax.microedition.io.SecurityInfo;
import javax.microedition.io.ServerSocketConnection;
import javax.microedition.io.SocketConnection;
import javax.microedition.pki.Certificate;

/**
 * Functions for libnet.1.so
 * @author Sergey Basalaev
 */
class LibNet1Func extends NativeFunction {

	public LibNet1Func(String name, int index) {
		super(name, index);
	}

	protected Object exec(Context c, Object[] args) throws Exception {
		switch (index) {
			case 0: { // new_socket(host: String, port: Int): Socket
				String host = ((String)args[0]).trim();
				int port = ival(args[1]);
				if (host.length() == 0) throw new IllegalArgumentException("No host");
				Connection conn = Connector.open("socket://"+host+':'+port);
				c.addStream(conn);
				return conn;
			}
			case 1: // Socket.get_host(): String
				return ((SocketConnection)args[0]).getAddress();
			case 2: // Socket.get_port(): Int
				return Ival(((SocketConnection)args[0]).getPort());
			case 3: // Socket.get_localhost(): String
				return ((SocketConnection)args[0]).getLocalAddress();
			case 4: // Socket.get_localport(): Int
				return Ival(((SocketConnection)args[0]).getLocalPort());
			case 5: // Socket.get_delay(): Bool
				return Ival(((SocketConnection)args[0]).getSocketOption(SocketConnection.DELAY));
			case 6: // Socket.set_delay(on: Bool)
				((SocketConnection)args[0]).setSocketOption(SocketConnection.DELAY, ival(args[1]));
				return null;
			case 7: // Socket.get_keepalive(): Bool
				return Ival(((SocketConnection)args[0]).getSocketOption(SocketConnection.KEEPALIVE));
			case 8: // Socket.set_keepalive(on: Bool)
				((SocketConnection)args[0]).setSocketOption(SocketConnection.KEEPALIVE, ival(args[1]));
				return null;
			case 9: // Socket.get_linger(): Int
				return Ival(((SocketConnection)args[0]).getSocketOption(SocketConnection.LINGER));
			case 10: // Socket.set_linger(linger: Int)
				((SocketConnection)args[0]).setSocketOption(SocketConnection.LINGER, ival(args[1]));
				return null;
			case 11: // Socket.get_sndbuf(): Int
				return Ival(((SocketConnection)args[0]).getSocketOption(SocketConnection.SNDBUF));
			case 12: // Socket.set_sndbuf(size: Int)
				((SocketConnection)args[0]).setSocketOption(SocketConnection.SNDBUF, ival(args[1]));
				return null;
			case 13: // Socket.get_rcvbuf(): Int
				return Ival(((SocketConnection)args[0]).getSocketOption(SocketConnection.RCVBUF));
			case 14: // Socket.set_rcvbuf(size: Int)
				((SocketConnection)args[0]).setSocketOption(SocketConnection.RCVBUF, ival(args[1]));
				return null;
			case 15: { // new_securesocket(host: String, port: Int): SecureSocket
				String host = ((String)args[0]).trim();
				int port = ival(args[1]);
				if (host.length() == 0) throw new IllegalArgumentException("No host");
				Connection conn = Connector.open("ssl://"+host+':'+port);
				c.addStream(conn);
				return conn;
			}
			case 16: // SecureSocket.get_secinfo(): SecInfo
				return ((SecureConnection)args[0]).getSecurityInfo();
			case 17: { // new_serversocket(port: Int): ServerSocket
				int port = ival(args[0]);
				String url = "socket://" + ((port >= 0) ? ":"+port : "");
				Connection conn = Connector.open(url);
				c.addStream(conn);
				return conn;
			}
			case 18: // ServerSocket.get_localhost(): String
				return ((ServerSocketConnection)args[0]).getLocalAddress();
			case 19: // ServerSocket.get_localport(): Int
				return Ival(((ServerSocketConnection)args[0]).getLocalPort());
			case 20: // ServerSocket.accept(): StreamConnection
				return ((ServerSocketConnection)args[0]).acceptAndOpen();
			case 21: // SecInfo.certificate(): Certificate
				return ((SecurityInfo)args[0]).getServerCertificate();
			case 22: // SecInfo.protocol_name(): String
				return ((SecurityInfo)args[0]).getProtocolName();
			case 23: // SecInfo.protocol_version(): String
				return ((SecurityInfo)args[0]).getProtocolVersion();
			case 24: // SecInfo.cipher_suite(): String
				return ((SecurityInfo)args[0]).getCipherSuite();
			case 25: // Certificate.subject(): String
				return ((Certificate)args[0]).getSubject();
			case 26: // Certificate.issuer(): String
				return ((Certificate)args[0]).getIssuer();
			case 27: // Certificate.certtype(): String
				return ((Certificate)args[0]).getType();
			case 28: // Certificate.version(): String
				return ((Certificate)args[0]).getVersion();
			case 29: // Certificate.signalg(): String
				return ((Certificate)args[0]).getSigAlgName();
			case 30: // Certificate.notbefore(): Long
				return Lval(((Certificate)args[0]).getNotBefore());
			case 31: // Certificate.notafter(): Long
				return Lval(((Certificate)args[0]).getNotAfter());
			case 32: { // new_http(host: String): Http
				Connection conn = Connector.open("http://"+args[0]);
				c.addStream(conn);
				return conn;
			}
			case 33: // Http.get_req_method(): String
				return ((HttpConnection)args[0]).getRequestMethod();
			case 34: // Http.set_req_method(method: String)
				((HttpConnection)args[0]).setRequestMethod((String)args[1]);
				return null;
			case 35: // Http.get_req_property(key: String): String
				return ((HttpConnection)args[0]).getRequestProperty((String)args[1]);
			case 36: // Http.set_req_property(key: String, value: String)
				((HttpConnection)args[0]).setRequestProperty((String)args[1], (String)args[2]);
				return null;
			case 37: // Http.get_type(): String
				return ((HttpConnection)args[0]).getType();
			case 38: // Http.get_encoding(): String
				return ((HttpConnection)args[0]).getEncoding();
			case 39: // Http.get_length(): Long
				return Lval(((HttpConnection)args[0]).getLength());
			case 40: // Http.get_resp_code(): Int
				return Ival(((HttpConnection)args[0]).getResponseCode());
			case 41: // Http.get_resp_msg(): String
				return ((HttpConnection)args[0]).getResponseMessage();
			case 42: // Http.get_expires(): Long
				return Lval(((HttpConnection)args[0]).getExpiration());
			case 43: // Http.get_date(): Long
				return Lval(((HttpConnection)args[0]).getDate());
			case 44: // Http.get_modified(): Long
				return Lval(((HttpConnection)args[0]).getLastModified());
			case 45: { // new_https(host: String): Https
				Connection conn = Connector.open("https://"+args[0]);
				c.addStream(conn);
				return conn;
			}
			case 46: // Https.get_port(): Int
				return Ival(((HttpsConnection)args[0]).getPort());
			case 47: // Https.get_secinfo(): SecInfo
				return ((HttpsConnection)args[0]).getSecurityInfo();
			default:
				return null;
		}
	}

	protected String soname() {
		return "libnet.1.so";
	}
}
