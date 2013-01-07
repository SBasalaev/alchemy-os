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

package alchemy.midlet;

import alchemy.fs.FSManager;
import alchemy.fs.NavigatorHelper;
import java.io.IOException;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

/**
 * JSR-75 file system navigator for the installer.
 * 
 * @author Sergey Basalaev
 */
final class FSNavigator extends List implements CommandListener {
	
	private final Image iconDir;
	private final Image iconFile;
	private final Image iconDisk;

	private final Command cmdOpen = new Command("Open", Command.OK, 1);
	private final Command cmdChoose = new Command("Choose", Command.ITEM, 2);
	private final Command cmdNewDir = new Command("New dir", Command.ITEM, 3);
	private final Command cmdOk = new Command("Ok", Command.OK, 1);
	private final Command cmdCancel = new Command("Cancel", Command.CANCEL, 5);
	
	private final Display display;
	private final TextBox dirbox;
	
	private String currentDir;
	private NavigatorHelper helper;
	
	FSNavigator(Display d, String fstype) throws Exception {
		super("Choose path", Choice.IMPLICIT);

		display = d;

		dirbox = new TextBox("New dir", null, 64, TextField.ANY);
		dirbox.setCommandListener(this);
		dirbox.addCommand(cmdOk);
		dirbox.addCommand(cmdCancel);
		
		helper = (NavigatorHelper) Class.forName("alchemy.fs."+fstype+".Helper").newInstance();

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
		
		setSelectCommand(cmdOpen);
		addCommand(cmdCancel);
		setCurrentDir("");
		setCommandListener(this);
	}
	
	public void setCurrentDir(String dir) throws IOException {
		deleteAll();
		String path = FSManager.normalize(dir);
		if (path.length() == 0) {
			String[] roots = helper.listRoots();
			for (int i=0; i<roots.length; i++) {
				append(roots[i], iconDisk);
			}
		} else {
			append("../", null);
			String[] dirs = helper.list(path);
			for (int i=0; i<dirs.length; i++) {
				append(dirs[i], dirs[i].endsWith("/") ? iconDir : iconFile);
			}
		}
		currentDir = path;
	}
	
	public String getCurrentDir()  {
		return currentDir;
	}
	
	public void createDir(String dir) throws IOException {
		helper.mkdir(dir);
	}

	public void commandAction(Command c, Displayable d) {
		if (c == cmdOpen) {
			String path = getString(getSelectedIndex());
			if (path.endsWith("/")) try {
				setCurrentDir(getCurrentDir()+'/'+path);
				if (getCurrentDir().length() == 0) {
					removeCommand(cmdChoose);
					removeCommand(cmdNewDir);
				} else {
					addCommand(cmdChoose);
					addCommand(cmdNewDir);
				}
			} catch (IOException ioe) {
				Alert alert = new Alert("I/O error", ioe.toString(), null, AlertType.ERROR);
				display.setCurrent(alert, this);
			}
		} else if (c == cmdNewDir) {
			dirbox.setString("");
			display.setCurrent(dirbox);
		} else if (c == cmdChoose) {
			// awaken installer
			synchronized (this) { notify(); }
		} else if (c == cmdOk) {
			try {
				helper.mkdir(currentDir+'/'+dirbox.getString());
				setCurrentDir(currentDir);
				display.setCurrent(this);
			} catch (IOException ioe) {
				Alert alert = new Alert("I/O error", ioe.toString(), null, AlertType.ERROR);
				display.setCurrent(alert, this);
			}
		} else if (c == cmdCancel) {
			if (d != this) {
				display.setCurrent(this);
			} else {
				currentDir = null;
				// awaken installer
				synchronized (this) { notify(); }
			}
		}
	}
}
