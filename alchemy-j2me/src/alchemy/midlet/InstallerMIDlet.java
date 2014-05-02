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

package alchemy.midlet;

import alchemy.fs.Filesystem;
import alchemy.fs.FSDriver;
import alchemy.fs.rms.Driver;
import alchemy.io.IO;
import alchemy.platform.Installer;
import alchemy.util.ArrayList;
import alchemy.util.HashMap;
import alchemy.util.Strings;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

/**
 * MIDlet to install Alchemy.
 * @author Sergey Basalaev
 */
public class InstallerMIDlet extends MIDlet implements CommandListener {

	private static final String ABOUT_TEXT =
		"Alchemy OS %0" +
		"\n\nCopyright (c) 2011-2014, Sergey Basalaev\n" +
		"http://alchemy-os.org\n" +
		"\n" +
		"This program is free software and is licensed under GNU GPL version 3\n" +
		"A copy of the GNU GPL may be found at http://www.gnu.org/licenses/\n";

	private Displayable current;

	private final Display display;
	private final Form messages;
	private final List menu;

	/** Dialog commands. */
	private final Command cmdMenu = new Command("Menu", Command.SCREEN, 1);

	private final Command cmdYes = new Command("Yes", Command.OK, 1);
	private final Command cmdNo = new Command("No", Command.CANCEL, 2);

	private Command selected;

	private static final int ACTION_INSTALL = 0;
	private static final int ACTION_ABOUT = 1;
	private static final int ACTION_QUIT = 2;
	
	private Installer installer;

	public InstallerMIDlet() {
		// prepare screens
		display = Display.getDisplay(this);

		messages = new Form("Installer");
		messages.addCommand(cmdMenu);
		messages.setCommandListener(this);

		menu = new List("Menu", List.IMPLICIT);
		menu.setCommandListener(this);

		current = messages;
		display.setCurrent(current);

		// prepare installer and menu
		try {
			installer = new Installer();
		} catch (Exception e) {
			messages.append("Fatal error: "+"cannot read setup.cfg"+'\n'+e+'\n');
			return;
		}
		menu.append(installer.isInstalled() ? "Uninstall" : "Install", null);
		menu.append("About", null);
		menu.append("Quit", null);

//		display.callSerially(new InstallerThread(0));
	}

	protected void startApp() throws MIDletStateChangeException {
		display.setCurrent(current);
	}

	protected void pauseApp() {
		current = display.getCurrent();
	}

	protected void destroyApp(boolean unconditional) {
		Filesystem.unmountAll();
		notifyDestroyed();
	}

	public void commandAction(Command c, Displayable d) {
		if (c == List.SELECT_COMMAND) {
			new InstallerThread(menu.getSelectedIndex()).start();
		} else if (c == cmdMenu) {
			current = menu;
			display.setCurrent(menu);
		} else {
			selected = c;
			synchronized (this) {
				this.notify();
			}
		}
	}

	private void install() throws Exception {
		messages.deleteAll();
		HashMap instCfg = installer.getInstalledConfig();
		HashMap setupCfg = installer.getSetupConfig();
		instCfg.set("rms.name", "rsfiles");
		//choosing filesystem
		ArrayList filesystems = new ArrayList();
		String[] fstypes = Strings.split((String)setupCfg.get("install.fs"), ' ', true);
		for (int i=0; i<fstypes.length; i++) {
			try {
				Class.forName((String)setupCfg.get("install.fs." + fstypes[i] + ".test"));
				filesystems.add(fstypes[i]);
			} catch (ClassNotFoundException cnfe) {
				// skip this file system
			}
		}
		final List fschoice = new List("Choose filesystem", Choice.IMPLICIT);
		for (int i=0; i<filesystems.size(); i++) {
			fschoice.append((String)setupCfg.get("install.fs." + filesystems.get(i) + ".name"), null);
		}
		fschoice.setCommandListener(this);
		display.setCurrent(fschoice);
		synchronized (fschoice) {
			fschoice.wait();
		}
		String selectedfs = filesystems.get(fschoice.getSelectedIndex()).toString();
		messages.append("Selected filesystem: "+fschoice.getString(fschoice.getSelectedIndex())+'\n');
		//choosing root path if needed
		String fsinit = (String) setupCfg.get("install.fs." + selectedfs + ".init");
		if (fsinit == null) fsinit = "";
		String neednav = (String) setupCfg.get("install.fs." + selectedfs + ".nav");
		if ("true".equals(neednav)) {
			final FSNavigator navigator = new FSNavigator(display, selectedfs);
			display.setCurrent(navigator);
			synchronized (navigator) {
				navigator.wait();
			}
			String path = navigator.getCurrentDir();
			if (path == null) throw new Exception("Installation aborted");
			fsinit = path;
			messages.append("Selected path: "+path+'\n');
		}
		display.setCurrent(messages);
		//installing base files
		installer.install(selectedfs, fsinit);
		//writing install config
		messages.append("Saving configuration..."+'\n');
		installer.saveInstalledConfig();
		messages.append("Launch Alchemy OS to finish installation"+'\n');
	}

	private void uninstall() throws Exception {
		messages.deleteAll();
		messages.append("Uninstalling..."+'\n');
		HashMap instCfg = installer.getInstalledConfig();
		//purging filesystem
		if (question("Do you want to erase internal file system?")) {
			if (instCfg.get(Installer.FS_DRIVER).equals("rms")) {
				try {
					RecordStore.deleteRecordStore((String)instCfg.get("rms.name"));
					messages.append("File system erased"+'\n');
				} catch (RecordStoreException rse) { }
			}
		}
		//removing config
		installer.removeInstalledConfig();
		messages.append("Configuration removed"+'\n');
	}
	
	/** Should be only available when RMS file system is in use. */
	private void rebuildFileSystem() throws Exception {
		messages.deleteAll();
		messages.append("Optimizing RMS filesystem...\n");
		// opening old FS
		HashMap cfg = installer.getInstalledConfig();
		String oldname = (String)cfg.get("rms.name");
		Driver oldfs = new Driver();
		long oldlen = oldfs.spaceUsed();
		// creating new FS
		String newname = (oldname.equals("rsfiles")) ? "rsfiles2" : "rsfiles";
		cfg.set("rms.name", newname);
		try {
			RecordStore.deleteRecordStore(newname);
		} catch (RecordStoreException rse) { }
		Driver newfs = new Driver();
		// copying all files from the old FS to the new
		String[] list = oldfs.list("");
		for (int i=0; i<list.length; i++) {
			copyTree(oldfs, newfs, "/"+list[i]);
		}
		oldfs.close();
		// computing new len
		newfs.close();
		newfs = new Driver();
		long newlen = newfs.spaceUsed();
		newfs.close();
		// writing configuration
		messages.append("Saving configuration...\n");
		cfg.set("rms.name", newname);
		installer.saveInstalledConfig();
		// removing old FS
		RecordStore.deleteRecordStore(oldname);
		messages.append("Optimization complete.\n");
		messages.append("" + (oldlen - newlen) + " bytes saved.\n");
	}
	
	private void copyTree(FSDriver from, FSDriver to, String file) throws IOException {
		file = Filesystem.normalize(file);
		boolean fRead = from.canRead(file);
		boolean fWrite = from.canWrite(file);
		if (!fRead) from.setRead(file, true);
		if (!fWrite) from.setWrite(file, true);
		if (from.isDirectory(file)) {
			to.mkdir(file);
			String[] list = from.list(file);
			for (int i=0; i<list.length; i++) {
				String subfile = file+'/'+list[i];
				copyTree(from, to, subfile);
			}
		} else {
			to.create(file);
			InputStream in = from.read(file);
			OutputStream out = to.write(file);
			IO.writeAll(in, out);
			in.close();
			out.flush();
			out.close();
		}
		to.setExec(file, from.canExec(file));
		to.setWrite(file, fWrite);
		to.setRead(file, fRead);
	}

	private boolean question(String msg) {
		final Alert alert = new Alert("Installer", msg, null, AlertType.WARNING);
		alert.setTimeout(Alert.FOREVER);
		alert.addCommand(cmdYes);
		alert.addCommand(cmdNo);
		alert.setCommandListener(this);
		display.setCurrent(alert);
		try {
			synchronized (this) {
				this.wait();
			}
			return selected == cmdYes;
		} catch (InterruptedException ie) {
			return false;
		}
	}

	private class InstallerThread extends Thread {

		private final int action;

		public InstallerThread(int action) {
			this.action = action;
		}

		public void run() {
			display.setCurrent(messages);
			try {
				switch(action) {
					case ACTION_INSTALL:
						if (installer.isInstalled()) 
							uninstall();
						else
							install();
						break;
					case ACTION_ABOUT:
						messages.append(Strings.format(
								ABOUT_TEXT,
								new Object[] { installer.getSetupConfig().get(Installer.VERSION) }));
						break;
					case ACTION_QUIT:
						destroyApp(true);
				}
			} catch (Throwable e) {
				e.printStackTrace();
				messages.append("Fatal error: "+e.toString()+'\n');
			}
		}
	}
}
