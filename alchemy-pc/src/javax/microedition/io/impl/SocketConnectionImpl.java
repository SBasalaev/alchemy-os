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
import java.net.Socket;
import javax.microedition.io.SocketConnection;

/**
 * Socket connection implementation.
 * @author Sergey Basalaev
 */
public class SocketConnectionImpl implements SocketConnection {

	final Socket socket;

	public SocketConnectionImpl(Socket socket) throws IOException {
		this.socket = socket;
	}

	@Override
	public String getAddress() throws IOException {
		if (socket.isClosed())
			throw new IOException("Connection is closed");
		return socket.getInetAddress().toString();
	}

	@Override
	public int getPort() throws IOException {
		if (socket.isClosed())
			throw new IOException("Connection is closed");
		return socket.getPort();
	}

	@Override
	public String getLocalAddress() throws IOException {
		if (socket.isClosed())
			throw new IOException("Connection is closed");
		return socket.getLocalAddress().toString();
	}

	@Override
	public int getLocalPort() throws IOException {
		if (socket.isClosed())
			throw new IOException("Connection is closed");
		return socket.getLocalPort();
	}

	@Override
	public int getSocketOption(byte option) throws IOException {
		if (socket.isClosed())
			throw new IOException("Connection is closed");
		switch (option) {
			case DELAY:
				return socket.getTcpNoDelay() ? 0 : 1;
			case KEEPALIVE:
				return socket.getKeepAlive() ? 1 : 0;
			case LINGER:
				return socket.getSoLinger();
			case SNDBUF:
				return socket.getSendBufferSize();
			case RCVBUF:
				return socket.getReceiveBufferSize();
			default:
				throw new IllegalArgumentException();
		}
	}

	@Override
	public void setSocketOption(byte option, int value) throws IOException {
		if (socket.isClosed())
			throw new IOException("Connection is closed");
		switch (option) {
			case DELAY:
				socket.setTcpNoDelay(value == 0);
				return;
			case KEEPALIVE:
				socket.setKeepAlive(value != 0);
				return;
			case LINGER:
				socket.setSoLinger(value != 0, value);
				return;
			case SNDBUF:
				socket.setSendBufferSize(value);
				return;
			case RCVBUF:
				socket.setReceiveBufferSize(value);
				return;
			default:
				throw new IllegalArgumentException();
		}
	}

	@Override
	public void close() throws IOException {
		socket.close();
	}

	@Override
	public InputStream openInputStream() throws IOException {
		return socket.getInputStream();
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		return socket.getOutputStream();
	}
}
