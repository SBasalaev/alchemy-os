/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011-2013, Sergey Basalaev <sbasalaev@gmail.com>
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

import alchemy.system.NativeLibrary;
import alchemy.system.Process;
import java.io.IOException;
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
 * Network library for Alchemy OS.
 * @author Sergey Basalaev
 * @version 2.0
 */
public class LibNet2 extends NativeLibrary {

	public LibNet2() throws IOException {
		load("/symbols/net2");
		name = "libnet.2.so";
	}

	protected Object invokeNative(int index, Process p, Object[] args) throws Exception {
		switch (index) {
			case 0: { // Socket.new(host: String, port: Int): Socket
				String host = ((String)args[0]).trim();
				int port = ival(args[1]);
				if (host.length() == 0) throw new IllegalArgumentException("No host");
				Connection conn = Connector.open("socket://"+host+':'+port);
				p.addConnection(conn);
				return conn;
			}
			case 1: // Socket.getHost(): String
				return ((SocketConnection)args[0]).getAddress();
			case 2: // Socket.getPort(): Int
				return Ival(((SocketConnection)args[0]).getPort());
			case 3: // Socket.getLocalHost(): String
				return ((SocketConnection)args[0]).getLocalAddress();
			case 4: // Socket.getLocalPort(): Int
				return Ival(((SocketConnection)args[0]).getLocalPort());
			case 5: // Socket.getDelay(): Bool
				return Ival(((SocketConnection)args[0]).getSocketOption(SocketConnection.DELAY));
			case 6: // Socket.setDelay(on: Bool)
				((SocketConnection)args[0]).setSocketOption(SocketConnection.DELAY, ival(args[1]));
				return null;
			case 7: // Socket.getKeepAlive(): Bool
				return Ival(((SocketConnection)args[0]).getSocketOption(SocketConnection.KEEPALIVE));
			case 8: // Socket.setKeepAlive(on: Bool)
				((SocketConnection)args[0]).setSocketOption(SocketConnection.KEEPALIVE, ival(args[1]));
				return null;
			case 9: // Socket.getLinger(): Int
				return Ival(((SocketConnection)args[0]).getSocketOption(SocketConnection.LINGER));
			case 10: // Socket.setLinger(linger: Int)
				((SocketConnection)args[0]).setSocketOption(SocketConnection.LINGER, ival(args[1]));
				return null;
			case 11: // Socket.getSndBuf(): Int
				return Ival(((SocketConnection)args[0]).getSocketOption(SocketConnection.SNDBUF));
			case 12: // Socket.setSndBuf(size: Int)
				((SocketConnection)args[0]).setSocketOption(SocketConnection.SNDBUF, ival(args[1]));
				return null;
			case 13: // Socket.getRcvBuf(): Int
				return Ival(((SocketConnection)args[0]).getSocketOption(SocketConnection.RCVBUF));
			case 14: // Socket.setRcvBuf(size: Int)
				((SocketConnection)args[0]).setSocketOption(SocketConnection.RCVBUF, ival(args[1]));
				return null;
			case 15: { // Securesocket.new(host: String, port: Int): SecureSocket
				String host = ((String)args[0]).trim();
				int port = ival(args[1]);
				if (host.length() == 0) throw new IllegalArgumentException("No host");
				Connection conn = Connector.open("ssl://"+host+':'+port);
				p.addConnection(conn);
				return conn;
			}
			case 16: // SecureSocket.getSecInfo(): SecInfo
				return ((SecureConnection)args[0]).getSecurityInfo();
			case 17: { // ServerSocket.new(port: Int): ServerSocket
				int port = ival(args[0]);
				String url = "socket://" + ((port >= 0) ? ":"+port : "");
				Connection conn = Connector.open(url);
				p.addConnection(conn);
				return conn;
			}
			case 18: // ServerSocket.getLocalHost(): String
				return ((ServerSocketConnection)args[0]).getLocalAddress();
			case 19: // ServerSocket.getLocalPort(): Int
				return Ival(((ServerSocketConnection)args[0]).getLocalPort());
			case 20: // ServerSocket.accept(): StreamConnection
				return ((ServerSocketConnection)args[0]).acceptAndOpen();
			case 21: // SecInfo.certificate(): Certificate
				return ((SecurityInfo)args[0]).getServerCertificate();
			case 22: // SecInfo.protocolName(): String
				return ((SecurityInfo)args[0]).getProtocolName();
			case 23: // SecInfo.protocolVersion(): String
				return ((SecurityInfo)args[0]).getProtocolVersion();
			case 24: // SecInfo.cipherSuite(): String
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
			case 32: { // Http.new(host: String): Http
				Connection conn = Connector.open("http://"+args[0]);
				p.addConnection(conn);
				return conn;
			}
			case 33: // Http.getReqMethod(): String
				return ((HttpConnection)args[0]).getRequestMethod();
			case 34: // Http.setReqMethod(method: String)
				((HttpConnection)args[0]).setRequestMethod((String)args[1]);
				return null;
			case 35: // Http.getReqProperty(key: String): String
				return ((HttpConnection)args[0]).getRequestProperty((String)args[1]);
			case 36: // Http.setReqProperty(key: String, value: String)
				((HttpConnection)args[0]).setRequestProperty((String)args[1], (String)args[2]);
				return null;
			case 37: // Http.getType(): String
				return ((HttpConnection)args[0]).getType();
			case 38: // Http.getEncoding(): String
				return ((HttpConnection)args[0]).getEncoding();
			case 39: // Http.getLength(): Long
				return Lval(((HttpConnection)args[0]).getLength());
			case 40: // Http.getRespCode(): Int
				return Ival(((HttpConnection)args[0]).getResponseCode());
			case 41: // Http.getRespMsg(): String
				return ((HttpConnection)args[0]).getResponseMessage();
			case 42: // Http.getExpires(): Long
				return Lval(((HttpConnection)args[0]).getExpiration());
			case 43: // Http.getDate(): Long
				return Lval(((HttpConnection)args[0]).getDate());
			case 44: // Http.getModified(): Long
				return Lval(((HttpConnection)args[0]).getLastModified());
			case 45: { // Https.new(host: String): Https
				Connection conn = Connector.open("https://"+args[0]);
				p.addConnection(conn);
				return conn;
			}
			case 46: // Https.getPort(): Int
				return Ival(((HttpsConnection)args[0]).getPort());
			case 47: // Https.getSecInfo(): SecInfo
				return ((HttpsConnection)args[0]).getSecurityInfo();
			default:
				return null;
		}
	}
}
