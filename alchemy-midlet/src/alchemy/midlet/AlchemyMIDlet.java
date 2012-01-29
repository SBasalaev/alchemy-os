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

import alchemy.core.Art;
import alchemy.core.Context;
import alchemy.core.ContextListener;
import alchemy.evm.ELibBuilder;
import alchemy.fs.File;
import alchemy.util.I18N;
import alchemy.nlib.NativeLibBuilder;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * Alchemy MIDlet.
 * @author Sergey Basalaev
 */
public class AlchemyMIDlet extends MIDlet implements CommandListener, ContextListener {

	private final Command cmdQuit = new Command(I18N._("Quit"), Command.EXIT, 1);
	private Art runtime;

	public AlchemyMIDlet() {
		UIServer.display = Display.getDisplay(this);
		try {
			if (!InstallInfo.exists()) {
				kernelPanic(I18N._("Alchemy is not installed."));
				return;
			}
			//setting up environment
			runtime = new Art(InstallInfo.getFilesystem());
			runtime.setLibBuilder((short)0xC0DE, new ELibBuilder());
			runtime.setLibBuilder((short)(('#'<<8)|'@'), new NativeLibBuilder());
			Context root = runtime.rootContext();
			root.setEnv("PATH", "/bin");
			root.setEnv("LIBPATH", "/lib");
			root.setEnv("INCPATH", "/inc");
			root.setCurDir(new File("/home"));
			root.addContextListener(this);
			//preloading core library
			root.loadLibrary("/lib/libcore.2.0.so");
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
						runtime.rootContext().start("terminal", new String[] {"sh"});
					} catch (Throwable t) {
						kernelPanic(t.toString());
					}					
				}
			}
		);
	}

	protected void startApp() throws MIDletStateChangeException {
		UIServer.displayCurrent();
	}

	protected void pauseApp() { }

	protected void destroyApp(boolean unconditional) {
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
