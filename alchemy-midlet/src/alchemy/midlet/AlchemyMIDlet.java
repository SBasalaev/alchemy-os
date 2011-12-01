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
import alchemy.evm.ELibBuilder;
import alchemy.fs.File;
import alchemy.l10n.I18N;
import alchemy.nlib.NativeLibBuilder;
import java.util.Stack;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * Alchemy MIDlet.
 * @author Sergey Basalaev
 */
public class AlchemyMIDlet extends MIDlet implements CommandListener {

	private static AlchemyMIDlet instance;

	/** Returns MIDlet instance. */
	public static AlchemyMIDlet getInstance() {
		return instance;
	}

	private Display display;

	private final Command cmdQuit = new Command(I18N._("Quit"), Command.EXIT, 1);
	private final Command cmdStart = new Command(I18N._("Start"), Command.OK, 1);
	private Art runtime;

	private Displayable current;

	private Stack screens = new Stack();

	public AlchemyMIDlet() {
		instance = this;
		display = Display.getDisplay(this);
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
			//preloading core library
			root.loadLibrary("/lib/libcore.2.0.so");
			runApp();
		} catch (Throwable t) {
			kernelPanic(t.toString());
		}
	}

	private void runApp() {
		Alert alert = new Alert("Alchemy");
		alert.setTimeout(Alert.FOREVER);
		alert.setString(I18N._("Start application?"));
		alert.addCommand(cmdQuit);
		alert.addCommand(cmdStart);
		alert.setCommandListener(this);
		alert.setType(AlertType.CONFIRMATION);
		current = alert;
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
		} else if (c == cmdStart) {
			new FinalizerThread().start();
		}
	}

	/**
	 * Pushes screen on the top of screen stack.
	 */
	public void pushScreen(Displayable d) {
		screens.push(d);
		display.setCurrent(d);
	}

	/**
	 * Removes top screen from the stack and shows
	 * screen next to it.
	 */
	public void popScreen() {
		screens.pop();
		if (!screens.empty()) {
			display.setCurrent((Displayable)screens.peek());
		}
	}

	public void alert(String title, String text, AlertType type) {
		Alert a = new Alert(title, text, null, type);
		display.setCurrent(a);
	}

	private void kernelPanic(String message) {
		Alert alert = new Alert("Kernel panic");
		alert.setCommandListener(this);
		alert.addCommand(cmdQuit);
		alert.setString(message);
		alert.setTimeout(Alert.FOREVER);
		alert.setType(AlertType.ERROR);
		display.setCurrent(alert);
	}

	/** A thread that closes MIDlet after root context has finished. */
	private class FinalizerThread extends Thread {

		public FinalizerThread() {
			super("FinalizerThread");
		}

		public void run() {
			try {
				runtime.rootContext().startAndWait("terminal", new String[] {"sh"});
				destroyApp(true);
			} catch (Throwable t) {
				kernelPanic(t.toString());
			}
		}
	}
}
