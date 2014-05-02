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
import alchemy.platform.Installer;
import alchemy.platform.Platform;
import alchemy.system.Process;
import alchemy.system.ProcessListener;
import alchemy.system.UIServer;
import alchemy.util.HashMap;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * Alchemy MIDlet.
 * @author Sergey Basalaev
 */
public class AlchemyMIDlet extends MIDlet implements CommandListener, ProcessListener {

	private final Command cmdQuit = new Command("Quit", Command.EXIT, 1);
	private Process root;

	public static AlchemyMIDlet instance;

	public AlchemyMIDlet() {
		instance = this;
		try {
			Installer installer = new Installer();
			if (!installer.isInstalled()) {
				kernelPanic("Alchemy OS is not installed. Please, run Installer first.");
				return;
			}
			if (installer.isUpdateNeeded()) {
				installer.update();
			}
			// setting up filesystem
			HashMap cfg = installer.getInstalledConfig();
			Filesystem.mount("", (String)cfg.get(Installer.FS_DRIVER), (String)cfg.get(Installer.FS_OPTIONS));
			Filesystem.mount("/dev", "devfs", "");
			// setting up environment
			root = new Process(null, "sh", new String[] {"/cfg/init"});
			root.setEnv("PATH", "/bin");
			root.setEnv("LIBPATH", "/lib");
			root.setEnv("INCPATH", "/inc");
			root.setCurrentDirectory("/home");
			root.addProcessListener(this);
			runApp();
		} catch (Throwable t) {
			kernelPanic(t.toString());
			t.printStackTrace();
		}
	}

	private void runApp() {
		Display.getDisplay(this).callSerially(
			new Runnable() {
				public void run() {
					try {
						root.start();
					} catch (Throwable t) {
						kernelPanic(t.toString());
						t.printStackTrace();
					}
				}
			}
		);
	}

	protected void startApp() throws MIDletStateChangeException {
		Object screen = Platform.getPlatform().getUI().getCurrentScreen();
		UIServer.pushEvent(UIServer.EVENT_SHOW, screen, null);
		UIServer.displayCurrent();
	}

	protected void pauseApp() {
		Object screen = Platform.getPlatform().getUI().getCurrentScreen();
		UIServer.pushEvent(UIServer.EVENT_HIDE, screen, null);
	}

	protected void destroyApp(boolean unconditional) {
		try {
			Filesystem.unmountAll();
		} catch (Exception e) { }
		notifyDestroyed();
	}

	public void commandAction(Command c, Displayable d) {
		if (c == cmdQuit) {
			destroyApp(true);
		}
	}

	public void processEnded(Process c) {
		destroyApp(true);
	}

	private void kernelPanic(String message) {
		Alert alert = new Alert("Kernel panic");
		alert.setCommandListener(this);
		alert.addCommand(cmdQuit);
		alert.setString(message);
		alert.setTimeout(Alert.FOREVER);
		alert.setType(AlertType.ERROR);
		Display.getDisplay(this).setCurrent(alert);
	}
}
