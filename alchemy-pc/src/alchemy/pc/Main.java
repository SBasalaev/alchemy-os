/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2013, Sergey Basalaev <sbasalaev@gmail.com>
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

package alchemy.pc;

import alchemy.fs.Filesystem;

/**
 * Main entry point for PC.
 * @author Sergey Basalaev
 */
public class Main {

	private Main() { }

	public static void main(String[] args) {
		try {
			Filesystem.mount("/", "pc", ".");
			alchemy.system.Process ps = new alchemy.system.Process("terminal", new String[0]);
			ps.setEnv("PATH", "/bin");
			ps.setEnv("LIBPATH", "/lib");
			ps.setEnv("INCPATH", "/inc");
			ps.start().waitFor();
			if (ps.getError() != null) {
				System.err.println(ps.getError());
			}
			System.exit(ps.getExitCode());
		} catch (Exception e) {
			System.err.println("!!! Alchemy OS crashed !!!");
			e.printStackTrace();
		}
	}
}
