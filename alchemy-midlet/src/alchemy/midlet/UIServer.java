/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011-2012  Sergey Basalaev <sbasalaev@gmail.com>
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

import alchemy.core.Context;
import alchemy.core.ContextListener;
import java.util.Vector;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

/**
 * UI server that manages stack of screens.
 * @author Sergey Basalaev
 */
public final class UIServer {
	
	static Display display;

	private static final Vector contexts = new Vector();
	private static final Vector screens = new Vector();
	
	private static UIListener l = new UIListener();
	
	private UIServer() { }
	
	/**
	 * Maps specified context to given screen.
	 * If context is already mapped then its screen is replaced.
	 * If context is not mapped then new mapping is added and
	 * corresponding screen is placed on top of the screen stack.
	 */
	public static void mapContext(Context c, Displayable d) {
		synchronized (contexts) {
			int index = contexts.indexOf(c);
			if (index < 0) {
				c.addContextListener(l);
				contexts.addElement(c);
				screens.addElement(d);
			} else {
				screens.setElementAt(d, index);
			}
			displayCurrent();
		}
	}
	
	/**
	 * Unmaps context mapping and removes corresponding screen
	 * from the screen stack.
	 */
	public static void unmapContext(Context c) {
		synchronized (contexts) {
			int index = contexts.indexOf(c);
			if (index >= 0) {
				c.removeContextListener(l);
				contexts.removeElementAt(index);
				screens.removeElementAt(index);
				displayCurrent();
			}
		}
	}
	
	public static void alert(String title, String text, AlertType type) {
		Alert a = new Alert(title, text, null, type);
		display.setCurrent(a);
	}
	
	public static Displayable currentScreen() {
		synchronized (contexts) {
			int top = contexts.size()-1;
			return (top >= 0) ? (Displayable)screens.elementAt(top) : null;
		}
	}
	
	public static void displayCurrent() {
		display.setCurrent(currentScreen());
	}
	
	/** Removes screen mapping when context ends. */
	private static class UIListener implements ContextListener {

		public void contextEnded(Context c) {
			unmapContext(c);
		}
	}
}
