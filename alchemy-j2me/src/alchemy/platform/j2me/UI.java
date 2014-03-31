/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2014, Sergey Basalaev <sbasalaev@gmail.com>
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

package alchemy.platform.j2me;

import alchemy.midlet.AlchemyMIDlet;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

/**
 * UI implementation for j2me.
 * @author Sergey Basalaev
 */
public final class UI implements alchemy.platform.UI {

	public Object getCurrentScreen() {
		return Display.getDisplay(AlchemyMIDlet.instance).getCurrent();
	}

	public void setCurrentScreen(Object screen) {
		Display.getDisplay(AlchemyMIDlet.instance).setCurrent((Displayable)screen);
	}

	public void setIcon(Object icon) {
		// do nothing, j2me screens do not have icon
	}

	public void screenTitleChanged(Object screen, String title) {
		// do nothing, j2me screens are responsible for their titles themselves
	}

	public void screenMenuAdded(Object screen, Object menu) {
		// do nothing, j2me screens are responsible for their menus themselves
	}

	public void screenMenuRemoved(Object screen, Object menu) {
		// do nothing, j2me screens are responsible for their menus themselves
	}

	public boolean vibrate(int millis) {
		return Display.getDisplay(AlchemyMIDlet.instance).vibrate(millis);
	}

	public boolean flash(int millis) {
		return Display.getDisplay(AlchemyMIDlet.instance).flashBacklight(millis);
	}
}
