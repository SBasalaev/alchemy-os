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
import javax.microedition.io.StreamConnection;
import javax.microedition.pki.Certificate;

/**
 * Functions for libnet.1.so
 * @author Sergey Basalaev
 */
public class LibNet1Func extends NativeFunction {

	public LibNet1Func(String name, int index) {
		super(name, index);
	}

	protected Object exec(Context c, Object[] args) throws Exception {
		switch (index) {
			case 0: // Connection.close()
				((Connection)args[0]).close();
				return null;
			case 1: // StreamConnection.open_input(): IStream
				return ((StreamConnection)args[0]).openInputStream();
			case 2: // StreamConnection.open_output(): OStream
				return ((StreamConnection)args[0]).openOutputStream();
			case 3: { // new_socket(host: String, port: Int): Socket
				String host = ((String)args[0]).trim();
				int port = ival(args[1]);
				if (host.length() == 0) throw new IllegalArgumentException("No host");
				return Connector.open("socket://"+host+':'+port);
			}
			case 4: // Socket.get_host(): String
				return ((SocketConnection)args[0]).getAddress();
			case 5: // Socket.get_port(): Int
				return Ival(((SocketConnection)args[0]).getPort());
			case 6: // Socket.get_localhost(): String
				return ((SocketConnection)args[0]).getLocalAddress();
			case 7: // Socket.get_localport(): Int
				return Ival(((SocketConnection)args[0]).getLocalPort());
			case 8: // Socket.get_delay(): Bool
				return Ival(((SocketConnection)args[0]).getSocketOption(SocketConnection.DELAY));
			case 9: // Socket.set_delay(on: Bool)
				((SocketConnection)args[0]).setSocketOption(SocketConnection.DELAY, ival(args[1]));
				return null;
			case 10: // Socket.get_keepalive(): Bool
				return Ival(((SocketConnection)args[0]).getSocketOption(SocketConnection.KEEPALIVE));
			case 11: // Socket.set_keepalive(on: Bool)
				((SocketConnection)args[0]).setSocketOption(SocketConnection.KEEPALIVE, ival(args[1]));
				return null;
			case 12: // Socket.get_linger(): Int
				return Ival(((SocketConnection)args[0]).getSocketOption(SocketConnection.LINGER));
			case 13: // Socket.set_linger(linger: Int)
				((SocketConnection)args[0]).setSocketOption(SocketConnection.LINGER, ival(args[1]));
				return null;
			case 14: // Socket.get_sndbuf(): Int
				return Ival(((SocketConnection)args[0]).getSocketOption(SocketConnection.SNDBUF));
			case 15: // Socket.set_sndbuf(size: Int)
				((SocketConnection)args[0]).setSocketOption(SocketConnection.SNDBUF, ival(args[1]));
				return null;
			case 16: // Socket.get_rcvbuf(): Int
				return Ival(((SocketConnection)args[0]).getSocketOption(SocketConnection.RCVBUF));
			case 17: // Socket.set_rcvbuf(size: Int)
				((SocketConnection)args[0]).setSocketOption(SocketConnection.RCVBUF, ival(args[1]));
				return null;
			case 18: { // new_securesocket(host: String, port: Int): SecureSocket
				String host = ((String)args[0]).trim();
				int port = ival(args[1]);
				if (host.length() == 0) throw new IllegalArgumentException("No host");
				return Connector.open("ssl://"+host+':'+port);
			}
			case 19: // SecureSocket.get_secinfo(): SecInfo
				return ((SecureConnection)args[0]).getSecurityInfo();
			case 20: { // new_serversocket(port: Int): ServerSocket
				int port = ival(args[0]);
				String url = "socket://" + ((port >= 0) ? ":"+port : "");
				return Connector.open(url);
			}
			case 21: // ServerSocket.get_localhost(): String
				return ((ServerSocketConnection)args[0]).getLocalAddress();
			case 22: // ServerSocket.get_localport(): Int
				return Ival(((ServerSocketConnection)args[0]).getLocalPort());
			case 23: // ServerSocket.accept(): StreamConnection
				return ((ServerSocketConnection)args[0]).acceptAndOpen();
			case 24: // SecInfo.certificate(): Certificate
				return ((SecurityInfo)args[0]).getServerCertificate();
			case 25: // SecInfo.protocol_name(): String
				return ((SecurityInfo)args[0]).getProtocolName();
			case 26: // SecInfo.protocol_version(): String
				return ((SecurityInfo)args[0]).getProtocolVersion();
			case 27: // SecInfo.cipher_suite(): String
				return ((SecurityInfo)args[0]).getCipherSuite();
			case 28: // Certificate.subject(): String
				return ((Certificate)args[0]).getSubject();
			case 29: // Certificate.issuer(): String
				return ((Certificate)args[0]).getIssuer();
			case 30: // Certificate.certtype(): String
				return ((Certificate)args[0]).getType();
			case 31: // Certificate.version(): String
				return ((Certificate)args[0]).getVersion();
			case 32: // Certificate.signalg(): String
				return ((Certificate)args[0]).getSigAlgName();
			case 33: // Certificate.notbefore(): Long
				return Lval(((Certificate)args[0]).getNotBefore());
			case 34: // Certificate.notafter(): Long
				return Lval(((Certificate)args[0]).getNotAfter());
			case 35: // new_http(host: String): Http
				return Connector.open("http://"+args[0]);
			case 36: // Http.get_req_method(): String
				return ((HttpConnection)args[0]).getRequestMethod();
			case 37: // Http.set_req_method(method: String)
				((HttpConnection)args[0]).setRequestMethod((String)args[1]);
				return null;
			case 38: // Http.get_req_property(key: String): String
				return ((HttpConnection)args[0]).getRequestProperty((String)args[1]);
			case 39: // Http.set_req_property(key: String, value: String)
				((HttpConnection)args[0]).setRequestProperty((String)args[1], (String)args[2]);
				return null;
			case 40: // Http.get_type(): String
				return ((HttpConnection)args[0]).getType();
			case 41: // def Http.get_encoding(): String
				return ((HttpConnection)args[0]).getEncoding();
			case 42: // def Http.get_length(): Long
				return Lval(((HttpConnection)args[0]).getLength());
			case 43: // def Http.get_resp_code(): Int
				return Ival(((HttpConnection)args[0]).getResponseCode());
			case 44: // def Http.get_resp_msg(): String
				return ((HttpConnection)args[0]).getResponseMessage();
			case 45: // def Http.get_expires(): Long
				return Lval(((HttpConnection)args[0]).getExpiration());
			case 46: // def Http.get_date(): Long
				return Lval(((HttpConnection)args[0]).getDate());
			case 47: // def Http.get_modified(): Long
				return Lval(((HttpConnection)args[0]).getLastModified());
			case 48: // new_https(host: String): Https
				return Connector.open("https://"+args[0]);
			case 49: // Https.get_port(): Int
				return Ival(((HttpsConnection)args[0]).getPort());
			case 50: // Https.get_secinfo(): SecInfo
				return ((HttpsConnection)args[0]).getSecurityInfo();
			default:
				return null;
		}
	}

	public String toString() {
		return "libnet.1.so:"+signature;
	}
}
