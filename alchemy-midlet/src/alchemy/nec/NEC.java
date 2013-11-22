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

package alchemy.nec;

import alchemy.core.Process;
import alchemy.fs.FSManager;
import alchemy.nec.tree.Unit;
import alchemy.nlib.NativeApp;
import alchemy.util.IO;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Native Ether compiler.
 * @author Sergey Basalaev
 */
public class NEC extends NativeApp {
	
	static private final String VERSION =
			"Native Ether Compiler version 2.0";

	static private final String HELP =
			"Usage: ec [options] <input> \n" +
			"Options:\n" +
			"-o <output>\n write to this file\n" +
			"-t<target>\n compile for given target\n" +
			"-O<level>\n choose optimization level\n" +
			"-I<path>\n add path to includes\n" +
			"-W<cat> -Wno-<cat>\n Switches category of warnings\n" +
			"-g\n turn on debugging info\n" +
			"-Xcommand\n use experimental feature\n" +
			"-h\n print this help and exit\n" +
			"-v\n print version and exit";

	/**
	 * Constructor without arguments.
	 * Needed to be loaded through the native interface.
	 */
	public NEC() { }

	public int main(Process p, String[] args) {
		//parsing arguments
		String outname = null;
		String fname = null;
		boolean wait_outname = false;
		int optlevel = 1;
		boolean dbginfo = false;
		int Wmask = -1; // all warnings
		int Xmask = 0;
		int target = 0x0201;
		for (int i=0; i<args.length; i++) {
			String arg = args[i];
			if (arg.equals("-h")) {
				IO.println(p.stdout, HELP);
				return 0;
			} else if (arg.equals("-v")) {
				IO.println(p.stdout, VERSION);
				return 0;
			} else if (arg.equals("-o")) {
				wait_outname = true;
			} else if (arg.startsWith("-t")) {
				// does anyone really need cross-compilation?
				if (arg.equals("-t2.1")) {
					target = 0x0201;
				} else {
					IO.println(p.stderr, "Unsupported target: "+arg.substring(2));
					return 1;
				}
			} else if (arg.startsWith("-O")) {
				try {
					optlevel = Integer.parseInt(arg.substring(2));
				} catch (Exception e) {
					optlevel = 1;
				}
			} else if (arg.startsWith("-X")) {
				String Xfeature = arg.substring(2);
				for (int j=0; j < Parser.X_STRINGS.length; j++) {
					if (Xfeature.equals(Parser.X_STRINGS[j]))
						Xmask |= (1 << j);
				}
			} else if (arg.equals("-g")) {
				dbginfo = true;
			} else if (arg.startsWith("-Wno-")) {
				String nowarn = arg.substring(5);
				if (nowarn.equals("all")) Wmask = 0;
				else for (int j=0; j < Parser.WARN_STRINGS.length; j++) {
					if (nowarn.equals(Parser.WARN_STRINGS[j]))
						Wmask &= ~(1 << j);
				}
			} else if (arg.startsWith("-W")) {
				String warn = arg.substring(2);
				if (warn.equals("all")) Wmask = 0xffffffff;
				else for (int j=0; j < Parser.WARN_STRINGS.length; j++) {
					if (warn.equals(Parser.WARN_STRINGS[j]))
						Wmask |= (1 << j);
				}
			} else if (arg.startsWith("-I") && arg.length() > 2) {
				p.setEnv("INCPATH", p.getEnv("INCPATH")+':'+arg.substring(2));
			} else if (arg.charAt(0) == '-') {
				IO.println(p.stderr, "Unknown argument: "+arg);
				IO.println(p.stderr, HELP);
				return 1;
			} else if (wait_outname) {
				outname = arg;
				wait_outname = false;
			} else {
				if (fname != null) {
					IO.println(p.stderr, "Excess parameter: "+fname);
					IO.println(p.stderr, HELP);
					return 1;
				}
				fname = arg;
			}
		}
		if (outname == null) {
			outname = fname+".o";
		}
		//parsing source
		Parser parser = new Parser(p, target, optlevel, Wmask, Xmask);
		Unit unit = null;
		try {
			unit = parser.parse(p.toFile(fname));
		} catch (Exception e) {
			IO.println(p.stderr, "There is a bug in compiler. Please report it with your source code and the following error message: "+e);
		}
		if (unit == null) return -1;
		//optimizing
		if (optlevel > 0) new Optimizer().visitUnit(unit);
		//writing object code
		new VarIndexer().visitUnit(unit);
		EAsmWriter wr = new EAsmWriter(dbginfo);
		try {
			OutputStream out = FSManager.fs().write(p.toFile(outname));
			wr.writeTo(unit, out);
			out.close();
		} catch (IOException ioe) {
			IO.println(p.stderr, "I/O error: " + ioe);
		} catch (Exception e) {
			IO.println(p.stderr, "There is a bug in compiler. Please report it with your source code and the following error message: "+e);
			e.printStackTrace();
			return -1;
		}
		return 0;
	}
}
