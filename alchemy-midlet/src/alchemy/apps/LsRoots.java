/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011-2012, Sergey Basalaev <sbasalaev@gmail.com>
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
import alchemy.fs.NavigatorHelper;
import alchemy.nlib.NativeApp;
import alchemy.util.IO;

/**
 * Prints lists of roots in all supported file systems.
 * @author Sergey Basalaev
 */
public class LsRoots extends NativeApp {
	
	public LsRoots() { }

	public int main(Context c, String[] args) {
		IO.println(c.stdout, "rms driver\n /");
		IO.println(c.stdout, "jsr75 driver\n");
		try {
			NavigatorHelper helper = (NavigatorHelper) Class.forName("alchemy.fs.jsr75.Helper").newInstance();
			String[] roots = helper.listRoots();
			for (int i=0; i < roots.length; i++) {
				c.stdout.write(' ');
				IO.println(c.stdout, roots[i]);
			}
		} catch (Throwable t) {
			IO.println(c.stdout, "* not supported");
		}
		IO.println(c.stdout, "siemens driver\n");
		try {
			NavigatorHelper helper = (NavigatorHelper) Class.forName("alchemy.fs.siemens.Helper").newInstance();
			String[] roots = helper.listRoots();
			for (int i=0; i < roots.length; i++) {
				c.stdout.write(' ');
				IO.println(c.stdout, roots[i]);
			}
		} catch (Throwable t) {
			IO.println(c.stdout, "* not supported");
		}
		return 0;
	}
}
