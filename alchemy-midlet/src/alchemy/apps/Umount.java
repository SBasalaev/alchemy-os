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

/**
 * Native program 'umount'.
 * 
 * @author Sergey Basalaev
 */
public class Umount extends NativeApp {

	private static final String HELP = "Unmounts file system at given path.\n\nUsage: umount <dir>";
	private static final String VERSION = "umount 1.0";
	
	public int main(Context c, String[] args) {
		if (args.length == 0 || args[0].equals("-h")) {
			IO.println(c.stdout, HELP);
		} else if (args[0].equals("-v")) {
			IO.println(c.stdout, VERSION);
		} else {
			String dir = c.toFile(args[0]);
			if (dir.length() == 0) {
			IO.println(c.stderr, "umount: Cannot unmount root directory!");
			return 1;
			} else if (!FSManager.umount(args[0])) {
				IO.println(c.stderr, "umount: "+args[0]+" is not mounted");
				return 1;
			}
		}
		return 0;
	}
}
