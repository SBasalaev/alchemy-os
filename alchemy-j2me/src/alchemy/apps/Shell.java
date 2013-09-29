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
import alchemy.io.UTFReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import alchemy.apps.TerminalForm.TerminalInputStream;
import alchemy.fs.Filesystem;
import alchemy.io.ConnectionBridge;
import alchemy.system.NativeApp;
import alchemy.system.Process;
import alchemy.util.ArrayList;
import alchemy.util.Strings;

/**
 * Native shell.
 * @author Sergey Basalaev
 */
public class Shell extends NativeApp {

	static private final String C_USAGE = "Usage: sh -c <cmd> <args>...";

	public Shell() { }

	public int main(Process p, String[] args) {
		try {
			int exitcode = 0;
			InputStream scriptinput;
			if (args.length == 0) {
				scriptinput = p.stdin;
			} else if (args[0].equals("-c")) {
				if (args.length < 2) {
					IO.println(p.stderr, C_USAGE);
					return -1;
				}
				StringBuffer cmdline = new StringBuffer(args[1]);
				for (int i=2; i<args.length; i++) {
					cmdline.append(' ').append(args[i]);
				}
				scriptinput = new ByteArrayInputStream(Strings.utfEncode(cmdline.toString()));
				p.addConnection(new ConnectionBridge(scriptinput));
			} else {
				String file = p.toFile(args[0]);
				byte[] buf = new byte[(int)Filesystem.size(file)];
				InputStream in = Filesystem.read(p.toFile(args[0]));
				in.read(buf);
				in.close();
				scriptinput = new ByteArrayInputStream(buf);
				p.addConnection(new ConnectionBridge(scriptinput));
			}
			if (p.stdin instanceof TerminalInputStream) {
				((TerminalInputStream)p.stdin).setPrompt(p.getCurrentDirectory()+'>');
			}
			UTFReader r = new UTFReader(scriptinput);
			while (true) try {
				String line = r.readLine();
				if (line == null) break;
				line = line.trim();
				if (line.length() == 0) continue;
				if (line.charAt(0) == '#') continue;
				ShellCommand cc = null;
				try { cc = split(line); }
				catch (IllegalArgumentException e) {
					IO.println(p.stderr, r.lineNumber()+':'+e.getMessage());
					if (scriptinput instanceof TerminalInputStream) continue;
					else return 1;
				}
				if (cc.cmd.equals("exit")) {
					if (cc.args.length > 0) try {
						exitcode = Integer.parseInt(cc.args[0]);
					} catch (NumberFormatException e) {
						IO.println(p.stderr, "exit: Not a number: "+cc.args[0]);
						exitcode = 1;
					}
					return exitcode;
				} else if (cc.cmd.equals("cd")) {
					if (cc.args.length > 0) {
						String newdir = p.toFile(cc.args[0]);
						p.setCurrentDirectory(newdir);
						if (p.stdin instanceof TerminalInputStream) {
							((TerminalInputStream)p.stdin).setPrompt(p.getCurrentDirectory()+'>');
						}
						exitcode = 0;
					} else {
						IO.println(p.stderr, "cd: no directory specified");
						exitcode = 1;
					}
				} else if (cc.cmd.equals("cls")) {
					if (p.stdin instanceof TerminalInputStream) {
						((TerminalInputStream)p.stdin).clearScreen();
					}
					exitcode = 0;
				} else {
					Process child = new Process(p);
					if (cc.in != null) {
						child.stdin = Filesystem.read(p.toFile(cc.in));
					}
					if (cc.out != null) {
						String outfile = p.toFile(cc.out);
						if (cc.appendout) {
							child.stdout = Filesystem.append(outfile);
						} else {
							child.stdout = Filesystem.write(outfile);
						}
					}
					if (cc.err != null) {
						String errfile = p.toFile(cc.err);
						if (cc.appenderr) {
							child.stderr = Filesystem.append(errfile);
						} else {
							child.stderr = Filesystem.write(errfile);
						}
					}
					if (p.stdin instanceof TerminalInputStream) {
						((TerminalInputStream)p.stdin).setPrompt("");
					}
					exitcode = child.startAndWait(cc.cmd, cc.args);
					if (cc.in != null) child.stdin.close();
					if (cc.out != null) child.stdout.close();
					if (cc.err != null) child.stderr.close();
					if (p.stdin instanceof TerminalInputStream) {
						((TerminalInputStream)p.stdin).setPrompt(p.getCurrentDirectory()+'>');
					}
				}
			} catch (Throwable t) {
				IO.println(p.stderr, t);
			}
			return exitcode;
		} catch (Exception e) {
			//e.printStackTrace();
			IO.println(p.stderr, e);
			return 1;
		}
	}

	private static final int MODE_CMD = 0;
	private static final int MODE_ARG = 1;
	private static final int MODE_QARG = 2;
	private static final int MODE_IN = 3;
	private static final int MODE_OUT_W = 4;
	private static final int MODE_OUT_A = 5;
	private static final int MODE_ERR_W = 6;
	private static final int MODE_ERR_A = 7;

	private ShellCommand split(String line) throws IllegalArgumentException {
		ShellCommand cc = new ShellCommand();
		ArrayList argv = new ArrayList();
		int mode = MODE_CMD;
		while (line.length() > 0) {
			int end;
			String token;
			if (line.charAt(0) == '\'') {
				end = line.indexOf('\'', 1);
				if (end < 0) throw new IllegalArgumentException("Unclosed '");
				token = line.substring(1,end);
				line = line.substring(end+1).trim();
				if (mode == MODE_ARG) mode = MODE_QARG;
			} else if (line.charAt(0) == '"') {
				end = line.indexOf('"', 1);
				if (end < 0) throw new IllegalArgumentException("Unclosed \"");
				token = line.substring(1, end);
				line = line.substring(end+1).trim();
				if (mode == MODE_ARG) mode = MODE_QARG;
			} else if (line.charAt(0) == '#') {
				break;
			} else {
				end = line.indexOf(' ');
				if (end < 0) end = line.length();
				token = line.substring(0,end);
				line = line.substring(end).trim();
			}
			if (token.length() == 0) continue;
			switch (mode) {
				case MODE_CMD:
					cc.cmd = token;
					mode = MODE_ARG;
					break;
				case MODE_IN:
					cc.in = token;
					mode = MODE_ARG;
					break;
				case MODE_OUT_W:
					cc.out = token;
					cc.appendout = false;
					mode = MODE_ARG;
					break;
				case MODE_OUT_A:
					cc.out = token;
					cc.appendout = true;
					mode = MODE_ARG;
					break;
				case MODE_ERR_W:
					cc.err = token;
					cc.appenderr = false;
					mode = MODE_ARG;
					break;
				case MODE_ERR_A:
					cc.err = token;
					cc.appenderr = true;
					mode = MODE_ARG;
					break;
				case MODE_ARG:
					if (token.equals(">") || token.equals("1>")) mode = MODE_OUT_W;
					else if (token.equals(">>") || token.equals("1>>")) mode = MODE_OUT_A;
					else if (token.equals("2>")) mode = MODE_ERR_W;
					else if (token.equals("2>>")) mode = MODE_ERR_A;
					else if (token.equals("<")) mode = MODE_IN;
					else argv.add(token);
					break;
				case MODE_QARG:
					argv.add(token);
					mode = MODE_ARG;
			}
		}
		String[] args = new String[argv.size()];
		for (int i=0; i<argv.size(); i++) {
			args[i] = argv.get(i).toString();
		}
		cc.args = args;
		return cc;
	}

	private static class ShellCommand {
		public String cmd;
		public String[] args;
		public String in;
		public String out;
		public boolean appendout;
		public String err;
		public boolean appenderr;
	}
}
