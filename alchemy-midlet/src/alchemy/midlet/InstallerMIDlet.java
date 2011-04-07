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
 *
 */

package alchemy.midlet;

import alchemy.fs.File;
import alchemy.fs.Filesystem;
import alchemy.util.Properties;
import alchemy.util.UTFReader;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.util.Vector;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * MIDlet to install Alchemy.
 * @author Sergey Basalaev
 */
public class InstallerMIDlet extends MIDlet implements CommandListener {

	private Displayable current;

	private final Display display;
	private final Form messages = new Form("Installer");

	private final Command cmdQuit = new Command("Quit", Command.EXIT, 10);
	private final Command cmdInstall = new Command("Install", Command.OK, 1);
	private final Command cmdUninstall = new Command("Uninstall", Command.OK, 5);
	private final Command cmdOk = new Command("Ok", Command.OK, 1);

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
			messages.append("Fatal error: cannot read /setup.cfg\n"+e+'\n');
			return;
		}
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
		} else if (c == cmdInstall) {
			new InstallerThread(1).start();
		} else if (c == cmdUninstall) {
			new InstallerThread(2).start();
		} else {
			synchronized (waitForAction) {
				waitForAction.notify();
			}
		}
	}

	private void check() {
		if (InstallInfo.exists()) {
			messages.append("Install config found\n");
			messages.addCommand(cmdUninstall);
		} else {
			messages.append("Not installed\n");
			messages.addCommand(cmdInstall);
		}
	}

	private void install() throws Exception {
		Properties instCfg = InstallInfo.read();
		//choosing filesystem
		String[] filesystems = split(setupCfg.get("install.fs"));
		List fschoice = new List("Choose filesystem", Choice.EXCLUSIVE);
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
			TextBox bxInitFs = new TextBox(fsask, fsinit, 512, TextField.ANY);
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
		Properties installCfg = InstallInfo.read();
		installCfg.put(InstallInfo.FS_TYPE, selectedfs);
		installCfg.put(InstallInfo.FS_INIT, fsinit);
		Filesystem fs = InstallInfo.getFilesystem();
		//installing base.arh
		DataInputStream datastream = new DataInputStream(getClass().getResourceAsStream("/base.arh"));
		messages.append("Installing base.arh\n");
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
				out.close();
			}
			fs.setRead(f, (attrs & 4) != 0);
			fs.setWrite(f, (attrs & 2) != 0);
			fs.setExec(f, (attrs & 1) != 0);
		}
		//writing install config
		messages.append("Saving configuration...\n");
		InstallInfo.save();
		messages.append("Installed successfully\n");
		messages.addCommand(cmdUninstall);
	}

	private void uninstall() throws Exception {
		messages.deleteAll();
		messages.append("Uninstalling...\n");
		//removing config
		InstallInfo.remove();
		messages.append("Uninstalled successfully.\n");
		messages.addCommand(cmdInstall);
	}
	
	private String[] split(String str) {
		Vector v = new Vector();
		str = str.trim();
		while (true) {
			int sp = str.indexOf(' ');
			if (sp < 0) {
				v.addElement(str);
				break;
			} else {
				v.addElement(str.substring(0, sp));
				str = str.substring(sp+1).trim();
			}
		}
		String[] ret = new String[v.size()];
		for (int i=0; i<v.size(); i++) {
			ret[i] = v.elementAt(i).toString();
		}
		return ret;
	}

	private class InstallerThread extends Thread {

		/**
		 * Actions:
		 *   0 - check
		 *   1 - install
		 *   2 - uninstall
		 */
		private final int action;

		public InstallerThread(int action) {
			this.action = action;
		}

		public void run() {
			messages.removeCommand(cmdQuit);
			messages.removeCommand(cmdInstall);
			messages.removeCommand(cmdUninstall);
			try {
				switch(action) {
					case 0: check(); break;
					case 1: install(); break;
					case 2: uninstall(); break;
				}
			} catch (Throwable e) {
				e.printStackTrace();
				messages.append("Fatal error: "+e+'\n');
			}
			messages.addCommand(cmdQuit);
		}
	}
}
