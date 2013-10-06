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
import java.net.ServerSocket;
import javax.microedition.io.ServerSocketConnection;
import javax.microedition.io.StreamConnection;

/**
 * Implementation of server socket connection.
 * @author Sergey Basalaev
 */
public class ServerSocketConnectionImpl implements ServerSocketConnection {

	private final ServerSocket socket;

	public ServerSocketConnectionImpl(int port) throws IOException {
		if (port == -1) {
			socket = new ServerSocket();
			socket.bind(null);
		} else {
			socket = new ServerSocket(port);
		}
	}

	@Override
	public String getLocalAddress() throws IOException {
		if (socket.isClosed())
			throw new IOException("Connection is closed");
		return socket.getInetAddress().toString();
	}

	@Override
	public int getLocalPort() throws IOException {
		if (socket.isClosed())
			throw new IOException("Connection is closed");
		return socket.getLocalPort();
	}

	@Override
	public StreamConnection acceptAndOpen() throws IOException {
		return new SocketConnectionImpl(socket.accept());
	}

	@Override
	public void close() throws IOException {
		socket.close();
	}
}
