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

import alchemy.fs.File;
import alchemy.fs.Filesystem;
import alchemy.fs.rms.FS;
import alchemy.util.Closeable;
import alchemy.util.IO;
import alchemy.util.Properties;
import alchemy.util.UTFReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
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

	private static String ABOUT_TEXT;
	private Displayable current;

	private final Display display;
	private final Form messages = new Form("Installer");

	/** Commands for main screen. */
	private final Command cmdQuit = new Command("Quit", Command.EXIT, 10);
	private final Command cmdAbout = new Command("About", Command.SCREEN, 9);
	private final Command cmdInstall = new Command("Install", Command.SCREEN, 1);
	private final Command cmdUpdate = new Command("Update", Command.SCREEN, 2);
	private final Command cmdUninstall = new Command("Uninstall", Command.SCREEN, 5);
	private final Command cmdRebuild = new Command("Rebuild FS", Command.SCREEN, 3);
	
	/** Command for dialogs. */
	private final Command cmdChoose = new Command("Choose", Command.OK, 2);
	private final Command cmdOpenDir = new Command("Open", Command.ITEM, 1);
	
	//#ifdef DEBUGLOG
	/** Commands for debug log. */
	private final Command cmdShowLog = new Command("Show log", Command.SCREEN, 7);
	private final Command cmdClearLog = new Command("Clear log", Command.SCREEN, 8);
	//#endif
	
	private Properties setupCfg;

	public InstallerMIDlet() {
		display = Display.getDisplay(this);
		current = messages;
		messages.setCommandListener(this);
		try {
			setupCfg = Properties.readFrom(new UTFReader(getClass().getResourceAsStream("/setup.cfg")));
		} catch (Exception e) {
			messages.append("Fatal error: "+"cannot read setup.cfg"+'\n'+e+'\n');
			return;
		}
		ABOUT_TEXT = "Alchemy OS v"+setupCfg.get("alchemy.version")+
			"\n\nCopyright (c) 2011-2012, Sergey Basalaev\n" +
			"http://alchemy-os.googlecode.com\n" +
			"\n" +
			"This MIDlet is free software and is licensed under GNU GPL version 3\n" +
			"A copy of the GNU GPL may be found at http://www.gnu.org/licenses/\n";

		new InstallerThread(0).start();
		//#ifdef DEBUGLOG
		Logger.log("Start: Installer");
		//#endif
	}

	protected void startApp() throws MIDletStateChangeException {
		display.setCurrent(current);
	}

	protected void pauseApp() {
		current = display.getCurrent();
	}

	protected void destroyApp(boolean unconditional) {
		notifyDestroyed();
	}

	public void commandAction(Command c, Displayable d) {
		if (c == cmdQuit) {
			destroyApp(true);
		} else if (c == cmdAbout) {
			messages.deleteAll();
			messages.append(ABOUT_TEXT);
		//#ifdef DEBUGLOG
		} else if (c == cmdShowLog) {
			messages.deleteAll();
			messages.append(Logger.getLog());
		} else if (c == cmdClearLog) {
			Logger.clearLog();
		//#endif
		} else if (c == cmdInstall) {
			new InstallerThread(1).start();
		} else if (c == cmdUninstall) {
			new InstallerThread(2).start();
		} else if (c == cmdUpdate) {
			new InstallerThread(3).start();
		} else if (c == cmdRebuild) {
			new InstallerThread(4).start();
		} else if (c == cmdChoose) {
			synchronized (d) {
				d.notify();
			}
		} else if (c == cmdOpenDir) {
			FSNavigator nav = (FSNavigator)d;
			String path = nav.getString(nav.getSelectedIndex());
			if (path.endsWith("/")) try {
				nav.setCurrentDir(new File(nav.getCurrentDir(), path));
				if (nav.getCurrentDir().path().length() == 0) {
					nav.removeCommand(cmdChoose);
				} else {
					nav.addCommand(cmdChoose);
				}
			} catch (IOException ioe) {
				Alert alert = new Alert("I/O error", ioe.toString(), null, AlertType.ERROR);
				display.setCurrent(alert, d);
			}
		}
	}

	private void check() {
		if (InstallInfo.exists()) {
			// fetching version
			Properties instCfg = InstallInfo.read();
			messages.append("Installed version: "+instCfg.get("alchemy.version")+'\n');
			messages.addCommand(cmdUninstall);
			// testing whether update is needed
			int vcmp = compareVersions(instCfg.get("alchemy.version"), setupCfg.get("alchemy.version"));
			if (vcmp < 0) {
				messages.append("Can be updated to "+setupCfg.get("alchemy.version")+'\n');
				messages.addCommand(cmdUpdate);
			}
			// testing whether FS can be rebuilt
			if (instCfg.get("fs.type").equals("rms")) {
				messages.addCommand(cmdRebuild);
			}
		} else {
			messages.append("Not installed"+'\n');
			messages.addCommand(cmdInstall);
		}
	}
	
	private int compareVersions(String v1, String v2) {
		String[] v1parts = IO.split(v1, '.');
		String[] v2parts = IO.split(v2, '.');
		int index = 0;
		while (true) {
			if (index < v1parts.length) {
				int i1 = Integer.parseInt(v1parts[index]);
				int i2 = (index < v2parts.length) ? Integer.parseInt(v2parts[index]) : 0;
				if (i1 != i2) return i1-i2;
			} else if (index < v2parts.length) {
				int i2 = Integer.parseInt(v2parts[index]);
				if (i2 != 0) return -i2;
			} else {
				return 0; // the two versions are equal
			}
			index++;
		}
	}

	private void install() throws Exception {
		messages.deleteAll();
		Properties instCfg = InstallInfo.read();
		//choosing filesystem
		Vector filesystems = new Vector();
		String[] fstypes = IO.split(setupCfg.get("install.fs"), ' ');
		for (int i=0; i<fstypes.length; i++) {
			try {
				Class.forName(setupCfg.get("install.fs."+fstypes[i]+".test"));
				filesystems.addElement(fstypes[i]);
			} catch (ClassNotFoundException cnfe) {
				// skip this file system
			}
		}
		final List fschoice = new List("Choose filesystem", Choice.IMPLICIT);
		for (int i=0; i<filesystems.size(); i++) {
			fschoice.append(setupCfg.get("install.fs."+filesystems.elementAt(i)+".name"), null);
		}
		fschoice.addCommand(cmdChoose);
		fschoice.setSelectCommand(cmdChoose);
		fschoice.setCommandListener(this);
		display.setCurrent(fschoice);
		synchronized (fschoice) {
			fschoice.wait();
		}
		String selectedfs = filesystems.elementAt(fschoice.getSelectedIndex()).toString();
		messages.append("Selected filesystem: "+fschoice.getString(fschoice.getSelectedIndex())+'\n');
		//choosing root path if needed
		String fsinit = setupCfg.get("install.fs."+selectedfs+".init");
		if (fsinit == null) fsinit = "";
		String neednav = setupCfg.get("install.fs."+selectedfs+".nav");
		instCfg.put("fs.type", selectedfs);
		instCfg.put("fs.init", fsinit);
		if ("true".equals(neednav)) {
			final FSNavigator navigator = new FSNavigator(InstallInfo.getFilesystem());
			navigator.setSelectCommand(cmdOpenDir);
			navigator.addCommand(cmdChoose);
			navigator.setCommandListener(this);
			navigator.setCurrentDir(new File(""));
			display.setCurrent(navigator);
			synchronized (navigator) {
				navigator.wait();
			}
			String path = navigator.getCurrentDir().path();
			instCfg.put("fs.init", path);
			messages.append("Selected path: "+path+'\n');
		}
		display.setCurrent(messages);
		//installing core files
		installArchives("install.archives");
		//writing configuration data
		instCfg.put("alchemy.version", setupCfg.get("alchemy.version"));
		//writing install config
		messages.append("Saving configuration..."+'\n');
		InstallInfo.save();
		messages.append("Launch Alchemy OS to finish installation"+'\n');
		messages.addCommand(cmdUninstall);
	}

	private void uninstall() throws Exception {
		messages.deleteAll();
		messages.append("Uninstalling..."+'\n');
		//purging filesystem
		Properties instCfg = InstallInfo.read();
		if (instCfg.get("fs.type").equals("rms")) {
			try {
				RecordStore.deleteRecordStore(instCfg.get("fs.init"));
				messages.append("Filesystem erased"+'\n');
			} catch (RecordStoreException rse) { }
		}
		//removing config
		InstallInfo.remove();
		messages.append("Configuration removed"+'\n');
		messages.addCommand(cmdInstall);
	}

	private void update() throws Exception {
		messages.deleteAll();
		//installing new files
		installArchives("update.archives");
		//writing configuration data
		Properties instCfg = InstallInfo.read();
		instCfg.put("alchemy.version", setupCfg.get("alchemy.version"));
		messages.append("Saving configuration..."+'\n');
		InstallInfo.save();
		messages.append("Launch Alchemy OS to finish update"+'\n');
		messages.addCommand(cmdUninstall);
	}

	private void installArchives(String property) throws Exception {
		Filesystem fs = InstallInfo.getFilesystem();
		String[] archives = IO.split(setupCfg.get(property), ' ');
		for (int i=0; i<archives.length; i++) {
			String arh = archives[i];
			DataInputStream datastream = new DataInputStream(getClass().getResourceAsStream("/"+arh));
			messages.append("Unpacking "+arh+'\n');
			while (datastream.available() > 0) {
				String fname = datastream.readUTF();
				File f = new File('/'+fname);
				datastream.skip(8); //timestamp
				int attrs = datastream.readUnsignedByte();
				if ((attrs & 16) != 0) { //directory
					if (!fs.exists(f)) fs.mkdir(f);
				} else {
					if (!fs.exists(f)) fs.create(f);
					byte[] data = new byte[datastream.readInt()];
					datastream.readFully(data);
					OutputStream out = fs.write(f);
					out.write(data);
					out.flush();
					out.close();
				}
				fs.setRead(f, (attrs & 4) != 0);
				fs.setWrite(f, (attrs & 2) != 0);
				fs.setExec(f, (attrs & 1) != 0);
			}
		}
		fs.remove(new File("/PACKAGE"));
		if (fs instanceof Closeable) {
			((Closeable)fs).close();
		}
	}
	
	/** Should be only available when RMS file system is in use. */
	private void rebuildFileSystem() throws Exception {
		messages.deleteAll();
		// opening old FS and creating new
		messages.append("Creating new file system...\n");
		Properties instCfg = InstallInfo.read();
		String oldfsname = instCfg.get("fs.init");
		String newfsname = (oldfsname.equals("rsfiles")) ? "rsfiles2" : "rsfiles";
		alchemy.fs.rms.FS oldfs = new FS();
		oldfs.init(oldfsname);
		try {
			RecordStore.deleteRecordStore(newfsname);
		} catch (RecordStoreException rse) { }
		alchemy.fs.rms.FS newfs = new FS();
		newfs.init(newfsname);
		// copying all files from the old FS to the new
		messages.append("Copying data from the old file system...\n");
		File root = new File("");
		String[] list = oldfs.list(root);
		for (int i=0; i<list.length; i++) {
			copyTree(oldfs, newfs, new File(root, list[i]));
		}
		// writing configuration
		oldfs.close();
		newfs.close();
		messages.append("Saving configuration...\n");
		instCfg.put("fs.init", newfsname);
		InstallInfo.save();
		// removing old FS
		messages.append("Removing old file system...\n");
		RecordStore.deleteRecordStore(oldfsname);
		messages.append("Rebuilding FS complete.\n");
	}
	
	private void copyTree(Filesystem from, Filesystem to, File file) throws IOException {
		boolean fRead = from.canRead(file);
		boolean fWrite = from.canWrite(file);
		if (!fRead) from.setRead(file, true);
		if (!fWrite) from.setWrite(file, true);
		if (from.isDirectory(file)) {
			to.mkdir(file);
			String[] list = from.list(file);
			for (int i=0; i<list.length; i++) {
				File subfile = new File(file, list[i]);
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

	private class InstallerThread extends Thread {

		/**
		 * Actions:
		 *   0 - check
		 *   1 - install
		 *   2 - uninstall
		 *   3 - update
		 *   4 - rebuild FS
		 */
		private final int action;

		public InstallerThread(int action) {
			this.action = action;
		}

		public void run() {
			messages.removeCommand(cmdQuit);
			messages.removeCommand(cmdAbout);
			messages.removeCommand(cmdInstall);
			messages.removeCommand(cmdUninstall);
			messages.removeCommand(cmdUpdate);
			messages.removeCommand(cmdRebuild);
			//#ifdef DEBUGLOG
			messages.removeCommand(cmdShowLog);
			messages.removeCommand(cmdClearLog);
			//#endif
			try {
				switch(action) {
					case 0: check(); break;
					case 1: install(); break;
					case 2: uninstall(); break;
					case 3: update(); break;
					case 4: rebuildFileSystem();
				}
			} catch (Throwable e) {
				// e.printStackTrace();
				messages.append("Fatal error: "+e.toString()+'\n');
			}
			messages.addCommand(cmdQuit);
			messages.addCommand(cmdAbout);
			//#ifdef DEBUGLOG
			messages.addCommand(cmdShowLog);
			messages.addCommand(cmdClearLog);
			//#endif
		}
	}
}
