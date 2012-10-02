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

package alchemy.apps;

import alchemy.core.Context;
import alchemy.fs.FSManager;
import alchemy.nlib.NativeApp;
import alchemy.util.IO;
import java.io.IOException;

/**
 * Native program 'mount'.
 * 
 * @author Sergey Basalaev
 */
public class Mount extends NativeApp {

	private static final String VERSION = "mount 1.0";
	private static final String HELP = "Mounts file system to the given directory.\n\nUsage: mount dir type [options]";
	
	public int main(Context c, String[] args) {
		if (args.length == 0 || args[0].equals("-h")) {
			IO.println(c.stdout, HELP);
			return 0;
		}
		if (args[0].equals("-v")) {
			IO.println(c.stdout, VERSION);
			return 0;
		}
		if (args.length < 2) {
			IO.println(c.stderr, "mount: Insufficient arguments");
			return 1;
		}
		String dir = c.toFile(args[0]);
		String options = (args.length < 3) ? "" : args[2];
		if (!FSManager.fs().isDirectory(dir)) {
			IO.println(c.stderr, "Directory does not exist: "+dir);
			return 1;
		}
		try {
			FSManager.mount(dir, args[1], options);
		} catch (IOException ioe) {
			IO.println(c.stderr, "I/O error: "+ioe.getMessage());
			return 1;
		}
		return 0;
	}
}
