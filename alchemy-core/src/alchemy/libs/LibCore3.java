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

package alchemy.libs;

import alchemy.fs.Filesystem;
import alchemy.io.IO;
import alchemy.system.Library;
import alchemy.system.NativeLibrary;
import alchemy.system.Process;
import alchemy.util.ArrayList;
import alchemy.util.Strings;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Alchemy core runtime library.
 *
 * @deprecated Will be removed after new API is finished
 * @author Sergey Basalaev
 * @version 3.0
 */
public class LibCore3 extends NativeLibrary {

	/**
	 * Constructor without parameters to load
	 * library through the native interface.
	 * @throws IOException if I/O error occured while reading
	 *         function definitions file
	 */
	public LibCore3() throws IOException {
		load("/symbols/core3");
		name = "libcore.3.so";
	}

	protected Object invokeNative(int index, Process p, Object[] args) throws Exception {
		switch (index) {
			case 36: // getenv(key: String): String
				return p.getEnv(((String)args[0]));
			case 37: // setenv(key: String, value: String)
				p.setEnv(((String)args[0]), (String)args[1]);
				return null;
			case 56: // systime(): Long
				return Lval(System.currentTimeMillis());
			case 57: // get_cwd(): String
				return p.getCurrentDirectory();
			case 70: { // Process.start_wait(cmd: String, args: [String]): Int
				Process cc = (Process)args[0];
				String prog = (String)args[1];
				Object[] oargs = (Object[])args[2];
				String[] sargs = new String[oargs.length];
				for (int i=oargs.length-1; i>=0; i--) {
					sargs[i] = String.valueOf(oargs[i]);
				}
				return Ival(cc.startAndWait(prog, sargs));
			}
			case 71: { // Process.start(cmd: String, args: [String])
				Process cc = (Process)args[0];
				String prog = (String)args[1];
				Object[] oargs = (Object[])args[2];
				String[] sargs = new String[oargs.length];
				for (int i=oargs.length-1; i>=0; i--) {
					sargs[i] = String.valueOf(oargs[i]);
				}
				cc.start(prog, sargs);
				return null;
			}
			case 100: // new_dict(): Dict
				return new Hashtable();
			case 101: // Dict.set(key: Any, value: Any)
				((Hashtable)args[0]).put(args[1], args[2]);
				return null;
			case 102: // Dict.get(key: Any): Any
				return ((Hashtable)args[0]).get(args[1]);
			case 103: // Dict.remove(key: Any)
				((Hashtable)args[0]).remove(args[1]);
				return null;
			case 123: { // Dict.keys(): [Any]
				ArrayList keyv = new ArrayList();
				for (Enumeration e = ((Hashtable)args[0]).keys(); e.hasMoreElements(); ) {
					keyv.add(e.nextElement());
				}
				Object[] keys = new Object[keyv.size()];
				for (int i=keys.length-1; i>=0; i--) {
					keys[i] = keyv.get(i);
				}
				return keys;
			}
			case 124: // sleep(millis: Int)
				Thread.sleep(ival(args[0]));
				return null;
			case 128: // Dict.size(): Int
				return Ival(((Hashtable)args[0]).size());
			case 129: // Dict.clear()
				((Hashtable)args[0]).clear();
				return null;
			case 143: // new_process(): Process
				return new Process(p);
			case 144: // Process.get_state(): Int
				return Ival(((Process)args[0]).getState());
			case 145: // Process.getenv(key: String): String
				return ((Process)args[0]).getEnv((String)args[1]);
			case 146: // Process.setenv(key: String, value: String)
				((Process)args[0]).setEnv((String)args[1], (String)args[2]);
				return null;
			case 147: // Process.get_in(): IStream
				return ((Process)args[0]).stdin;
			case 148: // Process.get_out(): OStream
				return ((Process)args[0]).stdout;
			case 149: // Process.get_err(): OStream
				return ((Process)args[0]).stderr;
			case 150: // Process.set_in(in: IStream)
				((Process)args[0]).stdin = (InputStream)args[1];
				return null;
			case 151: // Process.set_out(out: OStream)
				((Process)args[0]).stdout = (OutputStream)args[1];
				return null;
			case 152: // Process.set_err(err: OStream)
				((Process)args[0]).stderr = (OutputStream)args[1];
				return null;
			case 153: // Process.get_cwd(): String
				return ((Process)args[0]).getCurrentDirectory();
			case 154: // Process.set_cwd(dir: String)
				((Process)args[0]).setCurrentDirectory((String)args[1]);
				return null;
			case 155: // Process.get_priority(): Int
				return Ival(((Process)args[0]).getPriority());
			case 156: // Process.set_priority(value: Int)
				((Process)args[0]).setPriority(ival(args[1]));
				return null;
			case 157: // Process.get_name(): String
				return ((Process)args[0]).getName();
			case 158: // Process.interrupt()
				((Process)args[0]).interrupt();
				return null;
			case 159: // Process.get_exitcode(): Int
				return Ival(((Process)args[0]).getExitCode());
			case 167: // this_process(): Process;
				return p;
			case 175: // sys_property(key: String): String
				return System.getProperty((String)args[0]);
			default:
				return null;
		}
	}
}
