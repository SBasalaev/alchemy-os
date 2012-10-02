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

package alchemy.midlet;

import alchemy.fs.FSManager;
import java.io.IOException;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;

/**
 * File system navigator for the installer.
 * Uses file system provided by FSManager.
 * @author Sergey Basalaev
 */
class FSNavigator extends List {
	
	private static final Image iconDir;
	private static final Image iconFile;
	private static final Image iconDisk;

	static {
		Image img;
		try { img = Image.createImage("/nav-dir.png"); }
		catch (IOException ioe) { img = null; }
		iconDir = img;
		try { img = Image.createImage("/nav-disk.png"); }
		catch (IOException ioe) { img = null; }
		iconDisk = img;
		try { img = Image.createImage("/nav-file.png"); }
		catch (IOException ioe) { img = null; }
		iconFile = img;
	}
	
	private String currentDir;	
	
	FSNavigator() {
		super("Choose path", Choice.IMPLICIT);
	}
	
	public void setCurrentDir(String dir) throws IOException {
		deleteAll();
		if (dir.length() == 0) {
			String[] roots = FSManager.fs().listRoots();
			for (int i=0; i<roots.length; i++) {
				append(roots[i], iconDisk);
			}
		} else {
			append("../", null);
			String[] dirs = FSManager.fs().list(dir);
			for (int i=0; i<dirs.length; i++) {
				append(dirs[i], dirs[i].endsWith("/") ? iconDir : iconFile);
			}
		}
		currentDir = dir;
	}
	
	public String getCurrentDir()  {
		return currentDir;
	}
}
