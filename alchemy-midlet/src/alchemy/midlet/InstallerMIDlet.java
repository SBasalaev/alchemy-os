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
import alchemy.util.I18N;
import alchemy.util.IO;
import alchemy.util.Properties;
import alchemy.util.UTFReader;
import java.io.DataInputStream;
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
	private final Form messages = new Form(I18N._("Installer"));

	private final Command cmdQuit = new Command(I18N._("Quit"), Command.EXIT, 10);
	private final Command cmdAbout = new Command(I18N._("About"), Command.OK, 8);
	private final Command cmdInstall = new Command(I18N._("Install"), Command.OK, 1);
	private final Command cmdUpdate = new Command(I18N._("Update"), Command.OK, 2);
	private final Command cmdUninstall = new Command(I18N._("Uninstall"), Command.OK, 5);
	private final Command cmdOk = new Command(I18N._("Ok"), Command.OK, 1);

	/** Used by interactive dialogs. */
	private final Object waitForAction = new Object();

	private Properties setupCfg;

	public InstallerMIDlet() {
		display = Display.getDisplay(this);
		current = messages;
		messages.setCommandListener(this);
		try {
			setupCfg = Properties.readFrom(new UTFReader(getClass().getResourceAsStream("/setup.cfg")));
		} catch (Exception e) {
			messages.append(I18N._("Fatal error: {0}", I18N._("cannot read setup.cfg")+'\n'+e+'\n'));
			return;
		}
		ABOUT_TEXT = "Alchemy OS v"+setupCfg.get("alchemy.version")+
			// "Development branch\n" +
			"\n\nCopyright (c) 2011-2012, Sergey Basalaev\n" +
			"http://alchemy-os.googlecode.com\n" +
			"\n" +
			I18N._("This MIDlet is free software and is licensed under GNU GPL version 3")+'\n' +
			I18N._("A copy of the GNU GPL may be found at http://www.gnu.org/licenses/")+'\n';

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
		} else {
			synchronized (waitForAction) {
				waitForAction.notify();
			}
		}
	}

	private void check() {
		if (InstallInfo.exists()) {
			Properties instCfg = InstallInfo.read();
			messages.append(I18N._("Installed version:")+' '+instCfg.get("alchemy.version")+'\n');
			messages.addCommand(cmdUninstall);
			String[] curVersion = IO.split(instCfg.get("alchemy.version"), '.');
			String[] newVersion = IO.split(setupCfg.get("alchemy.version"), '.');
			int vcmp = newVersion[0].compareTo(curVersion[0]);
			if (vcmp == 0) vcmp = newVersion[1].compareTo(curVersion[1]);
			if (vcmp > 0) {
				messages.append(I18N._("Can be updated to {0}", setupCfg.get("alchemy.version"))+'\n');
				messages.addCommand(cmdUpdate);
			}
		} else {
			messages.append(I18N._("Not installed")+'\n');
			messages.addCommand(cmdInstall);
		}
	}

	private void install() throws Exception {
		messages.deleteAll();
		Properties instCfg = InstallInfo.read();
		//choosing filesystem
		String[] filesystems = IO.split(setupCfg.get("install.fs"), ' ');
		List fschoice = new List(I18N._("Choose filesystem"), Choice.EXCLUSIVE);
		for (int i=0; i<filesystems.length; i++) {
			fschoice.append(setupCfg.get("install.fs."+filesystems[i]+".name"), null);
		}
		fschoice.addCommand(cmdOk);
		fschoice.setSelectCommand(cmdOk);
		fschoice.setCommandListener(this);
		display.setCurrent(fschoice);
		synchronized (waitForAction) {
			waitForAction.wait();
		}
		String selectedfs = filesystems[fschoice.getSelectedIndex()];
		//reading fs init settings
		String fsask = setupCfg.get("install.fs."+selectedfs+".ask");
		String fsinit = setupCfg.get("install.fs."+selectedfs+".default");
		if (fsask != null) {
			TextBox bxInitFs = new TextBox(I18N._(fsask), fsinit, 512, TextField.ANY);
			bxInitFs.addCommand(cmdOk);
			bxInitFs.setCommandListener(this);
			display.setCurrent(bxInitFs);
			synchronized (waitForAction) {
				waitForAction.wait();
			}
			fsinit = bxInitFs.getString();
		}
		display.setCurrent(messages);
		//initializing fs
		instCfg.put(InstallInfo.FS_TYPE, selectedfs);
		instCfg.put(InstallInfo.FS_INIT, fsinit);
		//installing files
		installArchives();
		installCfg();
		//writing configuration data
		instCfg.put("alchemy.initcmd", setupCfg.get("alchemy.initcmd"));
		instCfg.put("alchemy.version", setupCfg.get("alchemy.version"));
		//writing install config
		messages.append(I18N._("Saving configuration...")+'\n');
		InstallInfo.save();
		messages.append(I18N._("Installed successfully")+'\n');
		messages.addCommand(cmdUninstall);
	}

	private void uninstall() throws Exception {
		messages.deleteAll();
		messages.append(I18N._("Uninstalling...")+'\n');
		//purging filesystem
		try {
			RecordStore.deleteRecordStore("rsfiles");
		} catch (RecordStoreException rse) { }
		//removing config
		InstallInfo.remove();
		messages.append(I18N._("Configuration removed")+'\n');
		messages.addCommand(cmdInstall);
	}

	private void update() throws Exception {
		messages.deleteAll();
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
		InstallInfo.save();
		messages.append(I18N._("Updated successfully")+'\n');
		messages.addCommand(cmdUninstall);
	}

	private void installArchives() throws Exception {
		Filesystem fs = InstallInfo.getFilesystem();
		String[] archives = IO.split(setupCfg.get("install.archives"), ' ');
		for (int i=0; i<archives.length; i++) {
			String arh = archives[i];
			DataInputStream datastream = new DataInputStream(getClass().getResourceAsStream("/"+arh));
			messages.append(I18N._("Unpacking {0}", arh+'\n'));
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
				messages.append(I18N._("Fatal error: {0}", e.toString()+'\n'));
			}
			messages.addCommand(cmdQuit);
			messages.addCommand(cmdAbout);
		}
	}
}
