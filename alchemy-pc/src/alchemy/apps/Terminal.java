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
import alchemy.system.NativeApp;
import alchemy.system.Process;
import alchemy.system.ProcessKilledException;
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
	private static final String VERSION = "PC terminal v2.0";

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
		// show terminal and start subprocess
		TerminalFrame frame = new TerminalFrame(childCmd + " - Terminal");
		try {
			Process child = new Process(p, childCmd, childArgs);
			child.stdin = frame.in;
			child.stdout = frame.out;
			child.stderr = frame.err;
			frame.setVisible(true);
			child.start();
			while (child.getState() != Process.ENDED) {
				Thread.sleep(100);
				if (p.killed || !frame.isVisible())
					throw new ProcessKilledException();
			}
			if (keep) {
				frame.end(p.getName());
				while (frame.isVisible()) {
					Thread.sleep(100);
				}
			}
			return child.getExitCode();
		} finally {
			frame.dispose();
		}
	}
}
