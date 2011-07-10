/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011  Sergey Basalaev <sbasalaev@gmail.com>
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
import alchemy.midlet.AlchemyMIDlet;
import alchemy.nlib.NativeApp;
import javax.microedition.lcdui.Command;

/**
 * Native console.
 *
 * @author Sergey Basalaev
 */
public class Console extends NativeApp {

	private static final String HELP = "Usage: con [-k] <command> <args>...\n";
	private static final Command cmdClose = new Command("Close", Command.EXIT, 4);

	public Console() { }

	public int main(Context c, String[] args) {
		if (args.length == 0) {
			c.stderr.print("con: no command given\n");
			c.stderr.print(HELP);
			return 1;
		}
		if (args[0].equals("-v")) {
			c.stdout.print("Native console v1.0\n");
			return 0;
		}
		if (args[0].equals("-h")) {
			c.stdout.print("con - run command in a console\n");
			c.stdout.print(HELP);
			return 0;
		}
		boolean keep = false;
		if (args[0].equals("-k")) {
			keep = true;
			if (args.length == 1) {
				c.stderr.print("con: no command given\n");
				c.stderr.print(HELP);
				return 1;
			}
		}
		ConsoleForm form = new ConsoleForm();
		final NotifyListener l = new NotifyListener();
		form.setCommandListener(l);
		AlchemyMIDlet.getInstance().pushScreen(form);
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
			child.stdin = form.in;
			child.stdout = form.out;
			child.stderr = form.out;
			int result = child.startAndWait(childCmd, childArgs);
			if (keep) {
				form.addCommand(cmdClose);
				synchronized (l) { l.wait(); }
			}
			return result;
		} catch (Exception e) {
			form.print(e.toString());
			form.print(child.dumpCallStack());
			e.printStackTrace();
			return 1;
		} finally {
			AlchemyMIDlet.getInstance().popScreen();
		}
	}
}
