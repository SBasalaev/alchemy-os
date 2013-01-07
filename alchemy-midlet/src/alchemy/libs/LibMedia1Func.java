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

import alchemy.core.Context;
import alchemy.nlib.NativeFunction;
import java.io.InputStream;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;

/**
 * Libmedia implementation.
 * @author Sergey Basalaev
 */
class LibMedia1Func extends NativeFunction {
	
	public LibMedia1Func(String name, int index) {
		super(name, index);
	}

	protected Object execNative(Context c, Object[] args) throws Exception {
		switch (index) {
			case 0: // play_tone(note: Int, duration: Int, volume: Int)
				Manager.playTone(ival(args[0]), ival(args[1]), ival(args[2]));
				return null;
			case 1: // new_player(in: IStream, ctype: String): Player
				return Manager.createPlayer((InputStream)args[0], (String)args[1]);
			case 2: // Player.get_ctype(): String
				return ((Player)args[0]).getContentType();
			case 3: // Player.set_loops(count: Int)
				((Player)args[0]).setLoopCount(ival(args[1]));
				return null;
			case 4: // Player.get_duration(): Long
				return Lval(((Player)args[0]).getDuration());
			case 5: // Player.get_time(): Long
				return Lval(((Player)args[0]).getMediaTime());
			case 6: // Player.set_time(time: Long)
				((Player)args[0]).setMediaTime(lval(args[1]));
				return null;
			case 7: // Player.start()
				((Player)args[0]).start();
				return null;
			case 8: // Player.stop()
				((Player)args[0]).stop();
				return null;
			case 9: // Player.close()
				((Player)args[0]).close();
				return null;
			case 10: // get_supported_ctypes(): [String]
				return Manager.getSupportedContentTypes(null);
			default:
				return null;
		}
	}

	protected String soname() {
		return "libmedia.1.so";
	}
}
