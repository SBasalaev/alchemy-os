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

import alchemy.core.Art;
import alchemy.core.Context;
import alchemy.core.ContextListener;
import alchemy.evm.ELibBuilder;
import alchemy.fs.FSManager;
import alchemy.nlib.NativeLibBuilder;
import alchemy.util.IO;
import alchemy.util.Properties;
import alchemy.util.UTFReader;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * Alchemy MIDlet.
 * @author Sergey Basalaev
 */
public class AlchemyMIDlet extends MIDlet implements CommandListener, ContextListener {

	private final Command cmdQuit = new Command("Quit", Command.EXIT, 1);
	private Art runtime;

	public AlchemyMIDlet() {
		UIServer.display = Display.getDisplay(this);
		try {
			if (!InstallInfo.exists()) {
				kernelPanic("Alchemy is not installed.");
				return;
			}
			Properties prop = InstallInfo.read();
			FSManager.mount("", prop.get("fs.type"), prop.get("fs.init"));
			//setting up environment
			runtime = new Art();
			runtime.setLibBuilder((short)0xC0DE, new ELibBuilder());
			runtime.setLibBuilder((short)(('#'<<8)|'@'), new NativeLibBuilder());
			Context root = runtime.rootContext();
			root.setEnv("PATH", "/bin");
			root.setEnv("LIBPATH", "/lib");
			root.setEnv("INCPATH", "/inc");
			root.setCurDir("/home");
			root.addContextListener(this);
			runApp();
		} catch (Throwable t) {
			kernelPanic(t.toString());
		}
	}

	private void runApp() {
		Display.getDisplay(this).callSerially(
			new Runnable() {
				public void run() {
					try {
						Properties props = InstallInfo.read();
						FSManager.mount("", props.get("fs.type"), props.get("fs.init"));
						UTFReader r = new UTFReader(FSManager.fs().read("/cfg/init"));
						String[] cmd = IO.split(r.readLine(), ' ');
						r.close();
						String[] cmdargs = new String[cmd.length-1];
						System.arraycopy(cmd, 1, cmdargs, 0, cmdargs.length);
						runtime.rootContext().start(cmd[0], cmdargs);
					} catch (Throwable t) {
						kernelPanic(t.toString());
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
			FSManager.umountAll();
		} catch (Exception e) { }
		notifyDestroyed();
	}

	public void commandAction(Command c, Displayable d) {
		if (c == cmdQuit) {
			destroyApp(true);
		}
	}

	public void contextEnded(Context c) {
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
