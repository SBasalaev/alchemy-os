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

import alchemy.io.IO;
import alchemy.system.NativeApp;

/**
 * Terminal emulation for PC version.
 *
 * @author Sergey Basalaev
 */
public class Terminal extends NativeApp {

	private static final String HELP = "Usage: terminal [-k] <command> <args>...\n";
	private static final String VERSION = "PC terminal v1.2\n";

	public Terminal() { }

	@Override
	public int main(alchemy.system.Process p, String[] args) {
		if (args.length == 0) {
			args = new String[] {"sh"};
		}
		if (args[0].equals("-v")) {
			IO.println(p.stdout, VERSION);
			return 0;
		}
		if (args[0].equals("-h")) {
			IO.println(p.stdout, "terminal - run command in the new terminal");
			IO.println(p.stdout, HELP);
			return 0;
		}
		boolean keep = false;
		if (args[0].equals("-k")) {
			keep = true;
			if (args.length == 1) {
				IO.println(p.stderr, "terminal: no command given");
				IO.println(p.stderr, HELP);
				return 1;
			}
		}
		try {
			String[] childArgs = null;
			String childCmd;
			if (keep) {
				childCmd = args[1];
				if (args.length > 2) {
					childArgs = new String[args.length-2];
					System.arraycopy(args, 2, childArgs, 0, args.length-2);
				}
			} else  {
				childCmd = args[0];
				childArgs = new String[args.length-1];
				if (args.length > 1) {
					System.arraycopy(args, 1, childArgs, 0, args.length-1);
				}
			}
			alchemy.system.Process child = new alchemy.system.Process(p, childCmd, childArgs);
			child.stdin = new TerminalInputStream();
			child.stdout = System.out;
			child.stderr = System.err;
			return child.start().waitFor();
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
	}
}
