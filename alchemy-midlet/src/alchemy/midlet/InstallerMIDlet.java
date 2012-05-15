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

package alchemy.midlet;

import alchemy.fs.File;
import alchemy.fs.Filesystem;
import alchemy.util.IO;
import alchemy.util.Properties;
import alchemy.util.UTFReader;
import java.io.DataInputStream;
import java.io.IOException;
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
	private final Command cmdAbout = new Command("About", Command.OK, 8);
	private final Command cmdInstall = new Command("Install", Command.OK, 1);
	private final Command cmdUpdate = new Command("Update", Command.OK, 2);
	private final Command cmdUninstall = new Command("Uninstall", Command.OK, 5);
	
	/** Command for dialogs. */
	private final Command cmdChoose = new Command("Choose", Command.OK, 2);
	private final Command cmdOpenDir = new Command("Open", Command.ITEM, 1);

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
			"\nDevelopment branch\n" +
			"\n\nCopyright (c) 2011-2012, Sergey Basalaev\n" +
			"http://alchemy-os.googlecode.com\n" +
			"\n" +
			"This MIDlet is free software and is licensed under GNU GPL version 3"+'\n' +
			"A copy of the GNU GPL may be found at http://www.gnu.org/licenses/"+'\n';

		new InstallerThread(0).start();
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
		} else if (c == cmdInstall) {
			new InstallerThread(1).start();
		} else if (c == cmdUninstall) {
			new InstallerThread(2).start();
		} else if (c == cmdUpdate) {
			new InstallerThread(3).start();
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
			Properties instCfg = InstallInfo.read();
			messages.append("Installed version: "+instCfg.get("alchemy.version")+'\n');
			messages.addCommand(cmdUninstall);
			String[] curVersion = IO.split(instCfg.get("alchemy.version"), '.');
			String[] newVersion = IO.split(setupCfg.get("alchemy.version"), '.');
			int vcmp = newVersion[0].compareTo(curVersion[0]);
			if (vcmp == 0) vcmp = newVersion[1].compareTo(curVersion[1]);
			if (vcmp > 0) {
				messages.append("Can be updated to "+setupCfg.get("alchemy.version")+'\n');
			//	messages.addCommand(cmdUpdate);
			}
			//always allow update in development branch
			// FIXME: remove in release
			messages.addCommand(cmdUpdate);
		} else {
			messages.append("Not installed"+'\n');
			messages.addCommand(cmdInstall);
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
			messages.append("Selected path: "+path);
		}
		display.setCurrent(messages);
		//installing files
		installArchives();
		installCfg();
		//writing configuration data
		instCfg.put("alchemy.initcmd", setupCfg.get("alchemy.initcmd"));
		instCfg.put("alchemy.version", setupCfg.get("alchemy.version"));
		//writing install config
		messages.append("Saving configuration..."+'\n');
		InstallInfo.save();
		messages.append("Installed successfully"+'\n');
		messages.addCommand(cmdUninstall);
	}

	private void uninstall() throws Exception {
		messages.deleteAll();
		messages.append("Uninstalling..."+'\n');
		//purging filesystem
		try {
			RecordStore.deleteRecordStore("rsfiles");
			messages.append("Filesystem erased"+'\n');
		} catch (RecordStoreException rse) { }
		//removing config
		InstallInfo.remove();
		messages.append("Configuration removed"+'\n');
		messages.addCommand(cmdInstall);
	}

	private void update() throws Exception {
		messages.deleteAll();
		messages.append("Removing deprecated components"+'\n');
		Filesystem fs = InstallInfo.getFilesystem();
		//changes since 1.0
		fs.remove(new File("/lib/libcore"));
		fs.remove(new File("/lib/libcore.0"));
		fs.remove(new File("/lib/libcore.0.0"));
		fs.remove(new File("/inc/array.eh"));
		//changes since 1.1
		fs.remove(new File("/bin/con"));
		//changes since 1.2 / 1.2.1
		fs.remove(new File("/lib/libcore.1.1.so"));
		fs.remove(new File("/lib/libcore.1.0.so"));
		fs.remove(new File("/lib/libcore.1.so"));
		//installing new files
		installArchives();
		installCfg();
		Properties instCfg = InstallInfo.read();
		instCfg.put("alchemy.initcmd", setupCfg.get("alchemy.initcmd"));
		instCfg.put("alchemy.version", setupCfg.get("alchemy.version"));
		messages.append("Saving configuration..."+'\n');
		InstallInfo.save();
		messages.append("Updated successfully"+'\n');
		messages.addCommand(cmdUninstall);
	}

	private void installArchives() throws Exception {
		Filesystem fs = InstallInfo.getFilesystem();
		String[] archives = IO.split(setupCfg.get("install.archives"), ' ');
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
	}

	private void installCfg() throws Exception {
		//writing current locale
		Filesystem fs = InstallInfo.getFilesystem();
		File localeFile = new File("/cfg/locale");
		if (!fs.exists(localeFile)) {
			String locale = System.getProperty("microedition.locale");
			if (locale == null) locale = "en_US";
			else locale = locale.replace('-', '_');
			OutputStream out = fs.write(localeFile);
			out.write(locale.getBytes());
			out.flush();
			out.close();
		}
	}

	private class InstallerThread extends Thread {

		/**
		 * Actions:
		 *   0 - check
		 *   1 - install
		 *   2 - uninstall
		 *   3 - update
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
			try {
				switch(action) {
					case 0: check(); break;
					case 1: install(); break;
					case 2: uninstall(); break;
					case 3: update(); break;
				}
			} catch (Throwable e) {
				// e.printStackTrace();
				messages.append("Fatal error: "+e.toString()+'\n');
			}
			messages.addCommand(cmdQuit);
			messages.addCommand(cmdAbout);
		}
	}
}
