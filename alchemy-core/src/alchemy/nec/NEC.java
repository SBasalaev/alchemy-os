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

package alchemy.nec;

import alchemy.fs.Filesystem;
import alchemy.io.IO;
import alchemy.nec.opt.ConstOptimizer;
import alchemy.nec.syntax.Unit;
import alchemy.system.NativeApp;
import alchemy.system.Process;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Native Ether compiler.
 * @author Sergey Basalaev
 */
public class NEC extends NativeApp {

	static private final String VERSION =
			"Native Ether Compiler version 2.2";

	static private final String HELP =
			"Usage: ec [options] <input> \n" +
			"Options:\n" +
			"-o <output>\n write to this file\n" +
			"-O<level>\n choose optimization level\n" +
			"-I<path>\n add path to includes\n" +
			"-W<cat> -Wno-<cat>\n Turns on/off category of warnings\n" +
			"-g\n turn on debugging info\n" +
			"-f<opt> -fno-<opt>\n Turns on/off option\n" +
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
		int warnmask = -1; // all warnings
		int optmask = 0;
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
			} else if (arg.startsWith("-O")) {
				try {
					optlevel = Integer.parseInt(arg.substring(2));
				} catch (Exception e) {
					optlevel = 1;
				}
			} else if (arg.equals("-g")) {
				dbginfo = true;
			} else if (arg.startsWith("-fno-")) {
				String nooption = arg.substring(5);
				for (int j=0; j < CompilerEnv.OPTION_STRINGS.length; j++) {
					if (nooption.equals(CompilerEnv.OPTION_STRINGS[j]))
						optmask &= ~(1 << j);
				}
			} else if (arg.startsWith("-f")) {
				String option = arg.substring(2);
				for (int j=0; j < CompilerEnv.OPTION_STRINGS.length; j++) {
					if (option.equals(CompilerEnv.OPTION_STRINGS[j]))
						optmask |= (1 << j);
				}
			} else if (arg.startsWith("-Wno-")) {
				String nowarn = arg.substring(5);
				if (nowarn.equals("all")) warnmask = 0;
				else for (int j=0; j < CompilerEnv.WARNING_STRINGS.length; j++) {
					if (nowarn.equals(CompilerEnv.WARNING_STRINGS[j]))
						warnmask &= ~(1 << j);
				}
			} else if (arg.startsWith("-W")) {
				String warn = arg.substring(2);
				if (warn.equals("all")) warnmask = 0xffffffff;
				else for (int j=0; j < CompilerEnv.WARNING_STRINGS.length; j++) {
					if (warn.equals(CompilerEnv.WARNING_STRINGS[j]))
						warnmask |= (1 << j);
				}
			} else if (arg.startsWith("-I") && arg.length() > 2) {
				p.setEnv("INCPATH", arg.substring(2) + ':' + p.getEnv("INCPATH"));
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
		if (fname == null) {
			IO.println(p.stderr, "No input files.");
			return 1;
		}
		if (outname == null) {
			outname = fname + ".o";
		}
		// parsing source
		CompilerEnv env = new CompilerEnv(p, optmask, warnmask, dbginfo);
		Parser parser = new Parser(env);
		Unit unit = null;
		unit = parser.parseUnit(p.toFile(fname));
		if (env.getErrorCount() > 0) return 1;
		// optimizing
		if (optlevel > 0) {
			new ConstOptimizer(env).visitUnit(unit);
		}
		if (env.getErrorCount() > 0) return 1;
		// writing binary code
		try {
			EAsmWriter wr = new EAsmWriter(env);
			OutputStream out = Filesystem.write(p.toFile(outname));
			wr.writeTo(unit, out);
			out.flush();
			out.close();
		} catch (IOException ioe) {
			IO.println(p.stderr, "I/O error while writing " + outname + '\n' + ioe.getMessage());
			return 1;
		}
		return 0;
	}
}
