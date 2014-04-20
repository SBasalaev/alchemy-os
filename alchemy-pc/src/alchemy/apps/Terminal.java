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

package alchemy.apps;

import alchemy.io.IO;
import alchemy.libs.ui.UiMenu;
import alchemy.system.NativeApp;
import alchemy.system.Process;
import alchemy.system.ProcessKilledException;
import alchemy.system.UIServer;
import java.util.ArrayList;

/**
 * Terminal emulation for PC version.
 *
 * @author Sergey Basalaev
 */
public class Terminal extends NativeApp {

	private static final String HELP =
			"terminal - run command in the new terminal\n" +
			"Usage: terminal [-k] <command> <args>...";
	private static final String VERSION = "PC terminal v2.1";

	public Terminal() { }

	@Override
	public int main(Process p, String[] args) throws Exception {
		// parse arguments
		boolean keep = false;
		String childCmd = null;
		ArrayList<String> cmdArgs = new ArrayList<String>();
		boolean readArgs = false;
		for (String arg : args) {
			if (readArgs) {
				cmdArgs.add(arg);
			} else if (arg.equals("-v")) {
				IO.println(p.stdout, VERSION);
				return 0;
			} else if (arg.equals("-h")) {
				IO.println(p.stdout, HELP);
				return 0;
			} else if (arg.equals("-k")) {
				keep = true;
			} else if (arg.startsWith("-")) {
				IO.println(p.stderr, "Unknown option " + arg);
				return 1;
			} else {
				childCmd = arg;
				readArgs = true;
			}
		}
		if (childCmd == null) childCmd = "sh";
		String[] childArgs = cmdArgs.toArray(new String[0]);

		// create terminal screen
		TerminalScreen screen = new TerminalScreen(childCmd + " - Terminal");
		UiMenu eofMenu = new UiMenu("Send EOF", 1, UiMenu.MT_SCREEN);
		UiMenu killMenu = new UiMenu("Kill", 2, UiMenu.MT_SCREEN);
		screen.addMenu(eofMenu);
		screen.addMenu(killMenu);

		// start subprocess
		Process child = new Process(p, childCmd, childArgs);
		child.stdin = screen.in;
		child.stdout = screen.out;
		child.stderr = screen.err;
		UIServer.setScreen(p, screen);
		child.start();

		// wait for child to die and process menus
		while (child.getState() != Process.ENDED) {
			Thread.sleep(100);
			Object[] ev = UIServer.readEvent(p, false);
			if (ev != null && ev[0] == UIServer.EVENT_MENU) {
				if (ev[2] == killMenu) {
					child.kill();
				} else if (ev[1] == eofMenu) {
					screen.sendEOF();
				}
			}
			if (p.killed) throw new ProcessKilledException();
		}
		if (keep) {
			screen.end(p.getName());
		}
		return child.getExitCode();
	}
}
