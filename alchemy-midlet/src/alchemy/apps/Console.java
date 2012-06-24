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
import alchemy.midlet.UIServer;
import alchemy.nlib.NativeApp;
import alchemy.util.IO;
import javax.microedition.lcdui.Command;

/**
 * Native console.
 *
 * @author Sergey Basalaev
 */
public class Console extends NativeApp {

	private static final String HELP = "Usage: terminal [-k] <command> <args>...";
	private static final Command cmdClose = new Command("Close", Command.EXIT, 4);

	public Console() { }

	public int main(Context c, String[] args) {
		if (args.length == 0) {
			IO.println(c.stderr, "terminal: no command given");
			IO.println(c.stderr, HELP);
			return 1;
		}
		if (args[0].equals("-v")) {
			IO.println(c.stdout, "Native terminal v1.1");
			return 0;
		}
		if (args[0].equals("-h")) {
			IO.println(c.stdout, "terminal - run command in the new terminal");
			IO.println(c.stdout, HELP);
			return 0;
		}
		boolean keep = false;
		if (args[0].equals("-k")) {
			keep = true;
			if (args.length == 1) {
				IO.println(c.stderr, "terminal: no command given");
				IO.println(c.stderr, HELP);
				return 1;
			}
		}
		ConsoleForm form = new ConsoleForm();
		UIServer.setScreen(c, form);
		Context child = new Context(c);
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
				if (args.length > 1) {
					childArgs = new String[args.length-1];
					System.arraycopy(args, 1, childArgs, 0, args.length-1);
				}
			}
			form.setTitle(childCmd+" - Terminal");
			child.stdin = form.in;
			child.stdout = form.out;
			child.stderr = form.out;
			int result = child.startAndWait(childCmd, childArgs);
			if (keep) {
				form.addCommand(cmdClose);
				Object[] event;
				do {
					event = (Object[])UIServer.readEvent(c, true);
				} while (event[2] != cmdClose);
			}
			return result;
		} catch (Exception e) {
			form.print(e.toString());
			form.print(child.dumpCallStack());
			return 1;
		} finally {
			UIServer.removeScreen(c);
		}
	}
}
