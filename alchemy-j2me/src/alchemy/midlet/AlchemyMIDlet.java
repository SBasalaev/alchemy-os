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

import alchemy.fs.Filesystem;
import alchemy.libs.ui.UIServer;
import alchemy.system.Process;
import alchemy.system.ProcessListener;
import alchemy.util.Properties;
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

	public AlchemyMIDlet() {
		UIServer.setDisplay(Display.getDisplay(this));
		try {
			if (!InstallInfo.exists()) {
				kernelPanic("Alchemy is not installed.");
				return;
			}
			Properties prop = InstallInfo.read();
			Filesystem.mount("", prop.get(InstallInfo.FS_TYPE), prop.get(InstallInfo.FS_INIT));
			//setting up environment
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
		UIServer.pushEvent(UIServer.currentScreen(), UIServer.EVENT_SHOW, null);
		UIServer.displayCurrent();
	}

	protected void pauseApp() {
		UIServer.pushEvent(UIServer.currentScreen(), UIServer.EVENT_HIDE, null);
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
