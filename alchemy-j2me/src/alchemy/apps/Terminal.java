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

import alchemy.libs.ui.UIServer;
import alchemy.io.IO;
import alchemy.system.NativeApp;
import alchemy.system.Process;
import javax.microedition.lcdui.Command;

/**
 * Native terminal.
 *
 * @author Sergey Basalaev
 */
public class Terminal extends NativeApp {

	private static final String HELP = "Usage: terminal [-k] <command> <args>...";
	private static final String VERSION = "Native terminal v1.2";
	private static final Command cmdClose = new Command("Close", Command.OK, 4);

	public Terminal() { }

	public int main(Process p, String[] args) {
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
		final Command cmdInput = new Command("Input", Command.OK, 1);
		final TerminalForm form = new TerminalForm(cmdInput);
		UIServer.setScreen(p, form);
		Process child = new Process(p);
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
			child.start(childCmd, childArgs);
			while (child.getState() != Process.ENDED) {
				Object[] event = (Object[])UIServer.readEvent(p, false);
				if (event != null && event[2] == cmdInput) synchronized (form) {
					form.notify();
				}
				Thread.sleep(100);
			}
			if (keep) {
				form.addCommand(cmdClose);
				Object[] event;
				do {
					event = (Object[])UIServer.readEvent(p, true);
				} while (event[2] != cmdClose);
			}
			return child.getExitCode();
		} catch (Exception e) {
			form.print(e.toString());
			return 1;
		} finally {
			UIServer.removeScreen(p);
		}
	}
}
