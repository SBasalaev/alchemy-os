/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011-2014, Sergey Basalaev <sbasalaev@gmail.com>
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

import alchemy.fs.Filesystem;
import alchemy.io.ConnectionInputStream;
import alchemy.system.NativeLibrary;
import alchemy.system.Process;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.io.Connection;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;

/**
 * Basic media library for Alchemy OS.
 * 
 * @author Sergey Basalaev
 * @version 2.0
 */
public class LibMedia2 extends NativeLibrary {

	public LibMedia2() throws IOException {
		load("/symbols/media2");
		name = "libmedia.2.so";
	}

	protected Object invokeNative(int index, Process p, Object[] args) throws Exception {
		switch (index) {
			case 0: // playTone(note: Int, duration: Int, volume: Int)
				Manager.playTone(ival(args[0]), ival(args[1]), ival(args[2]));
				return null;
			case 1: // getSupportedCtypes(): [String]
				return Manager.getSupportedContentTypes(null);
			case 2: { // streamPlayer(in: IStream, ctype: String): Player
				Player player = Manager.createPlayer((InputStream) args[0], (String) args[1]);
				p.addConnection(new PlayerConnection(player));
				return player;
			}
			case 3: { // filePlayer(file: String, ctype: String): Player
				String fileName = (String) args[0];
				String url = Filesystem.getNativeURL(p.toFile(fileName));
				Player player;
				if (url != null) {
					player = Manager.createPlayer(url);
				} else {
					InputStream input = new ConnectionInputStream(Filesystem.read(p.toFile(fileName)));
					p.addConnection(new ConnectionInputStream(input));
					player = Manager.createPlayer(input, (String)args[1]);
				}
				p.addConnection(new PlayerConnection(player));
				return player;
			}
			case 4: // Player.getCtype(): String
				return ((Player)args[0]).getContentType();
			case 5: // Player.setLoops(count: Int)
				((Player)args[0]).setLoopCount(ival(args[1]));
				return null;
			case 6: // Player.getDuration(): Long
				return Lval(((Player)args[0]).getDuration());
			case 7: // Player.getTime(): Long
				return Lval(((Player)args[0]).getMediaTime());
			case 8: // Player.setTime(time: Long)
				((Player)args[0]).setMediaTime(lval(args[1]));
				return null;
			case 9: // Player.start()
				((Player)args[0]).start();
				return null;
			case 10: // Player.stop()
				((Player)args[0]).stop();
				return null;
			case 11: // Player.close()
				((Player)args[0]).close();
				return null;
			default:
				return null;
		}
	}

	private static final class PlayerConnection implements Connection {

		private final Player player;

		public PlayerConnection(Player player) {
			this.player = player;
		}

		public void close() {
			player.close();
		}
	}
}
