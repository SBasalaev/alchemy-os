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

package alchemy.nec;

import alchemy.core.Context;
import alchemy.nec.tree.Unit;
import alchemy.nlib.NativeApp;
import java.io.OutputStream;

/**
 * Native E compiler.
 * @author Sergey Basalaev
 */
public class NEC extends NativeApp {

	static private final String VERSION =
			"Native E Compiler\n" +
			"version 0.1";

	static private final String HELP =
			"Usage: ec <input> [-o <output>]";

	/**
	 * Constructor without arguments.
	 * Needed to be loaded through the native interface.
	 */
	public NEC() { }

	public int main(Context c, String[] args) {
		//parsing arguments
		String outname = null;
		String fname = null;
		boolean wait_outname = false;
		for (int i=0; i<args.length; i++) {
			String arg = args[i];
			if (arg.equals("-h")) {
				c.stdout.println(HELP);
				return 0;
			} else if (arg.equals("-v")) {
				c.stdout.println(VERSION);
				return 0;
			} else if (arg.equals("-o")) {
				wait_outname = true;
			} else if (arg.charAt(0) == '-') {
				c.stderr.println("Unknown argument: "+arg);
				c.stderr.println(HELP);
				return 1;
			} else if (wait_outname) {
				outname = arg;
				wait_outname = false;
			} else {
				if (fname != null) {
					c.stderr.println("Excess parameter: "+fname);
					c.stderr.println(HELP);
					return 1;
				}
				fname = arg;
			}
		}
		//guessing outname
		if (outname == null) {
			if (fname.endsWith(".e") && fname.length() > 2)
				outname = fname.substring(0, fname.length()-1)+'o';
			else
				outname = fname+".o";
		}
		//parsing source
		Parser parser = new Parser(c);
		Unit unit = parser.parse(c.toFile(fname));
		if (unit == null) return -1;
		//TODO: optimizing
		//writing object code
		CodeWriter wr = new CodeWriter(c, unit);
		try {
			OutputStream out = c.fs().write(c.toFile(outname));
			wr.write(out);
			out.close();
		} catch (Exception e) {
			c.stderr.println("Error: "+e.toString());
			return -1;
		}
		return 0;
	}
}
