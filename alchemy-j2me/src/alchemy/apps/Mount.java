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

package alchemy.apps;

import alchemy.fs.Filesystem;
import alchemy.io.IO;
import alchemy.system.NativeApp;
import alchemy.system.Process;
import java.io.IOException;

/**
 * Native program 'mount'.
 * 
 * @author Sergey Basalaev
 */
public class Mount extends NativeApp {

	private static final String VERSION = "mount 1.0";
	private static final String HELP = "Mounts file system to the given directory.\n\nUsage: mount dir type [options]";
	
	public int main(Process p, String[] args) throws IOException {
		if (args.length == 0 || args[0].equals("-h")) {
			IO.println(p.stdout, HELP);
			return 0;
		}
		if (args[0].equals("-v")) {
			IO.println(p.stdout, VERSION);
			return 0;
		}
		if (args.length < 2) {
			IO.println(p.stderr, "mount: Insufficient arguments");
			return 1;
		}
		String type = args[1];
		String dir = p.toFile(args[0]);
		String options = (args.length < 3) ? "" : args[2];
		if (!Filesystem.isDirectory(dir)) {
			IO.println(p.stderr, "Directory does not exist: "+dir);
			return 1;
		}
		if (Filesystem.list(dir).length > 0) {
			IO.println(p.stderr, "Warning: directory not empty");
		}
		Filesystem.mount(dir, type, options);
		return 0;
	}
}
