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

package alchemy.fs.siemens;

import alchemy.fs.NavigatorHelper;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
import com.siemens.mp.io.file.FileSystemRegistry;
import com.siemens.mp.io.file.FileConnection;

/**
 * FSNavigator helper for Siemens.
 * @author Sergey Basalaev
 */
public class Helper implements NavigatorHelper {

	public Helper() { }
	
	public String[] listRoots() {
		Enumeration e = FileSystemRegistry.listRoots();
		Vector rootv = new Vector();
		while (e.hasMoreElements()) rootv.addElement(e.nextElement());
		String[] roots = new String[rootv.size()];
		for (int i=0; i<rootv.size(); i++) {
			String str = String.valueOf(rootv.elementAt(i));
			if (!str.endsWith("/")) roots[i] = str+'/';
			else roots[i] = str;
		}
		return roots;
	}
	
	public void mkdir(String dir) throws IOException {
		FileConnection fc = (FileConnection)Connector.open("file://"+dir+'/', Connector.READ_WRITE);
		try {
			fc.mkdir();
		} finally {
			fc.close();
		}
	}

	public String[] list(String dir) throws IOException {
		FileConnection fc = (FileConnection)Connector.open("file://"+dir+'/', Connector.READ);
		try {
			Enumeration e = fc.list();
			Vector v = new Vector();
			while (e.hasMoreElements()) v.addElement(e.nextElement());
			int size = v.size();
			String[] files = new String[size];
			for (int i=0; i<size; i++) {
				files[i] = (String)v.elementAt(i);
			}
			return files;
		} finally {
			fc.close();
		}
	}
}
