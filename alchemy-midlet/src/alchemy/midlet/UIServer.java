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

import java.util.Stack;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

/**
 * UI server.
 * @author Sergey Basalaev
 */
public final class UIServer {
	
	static Display display;
	
	private static Stack screens = new Stack();

	private UIServer() { }
	
	/**
	 * Pushes screen on the top of screen stack.
	 */
	public static void pushScreen(Displayable d) {
		screens.push(d);
		display.setCurrent(d);
	}

	/**
	 * Removes top screen from the stack and shows
	 * screen next to it.
	 */
	public static void popScreen() {
		screens.pop();
		display.setCurrent(currentScreen());
	}
	
	public static void alert(String title, String text, AlertType type) {
		Alert a = new Alert(title, text, null, type);
		display.setCurrent(a);
	}
	
	public static Displayable currentScreen() {
		return (!screens.empty()) ? (Displayable)screens.peek() : null;
	}
	
	public static void displayCurrent() {
		display.setCurrent(currentScreen());
	}
}
