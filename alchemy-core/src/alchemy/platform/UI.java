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

package alchemy.platform;

/**
 * Interface for UI server.
 *
 * @author Sergey Basalaev
 */
public interface UI {

	/**
	 * Returns currently displayed screen. 
	 */
	Object getCurrentScreen();

	/**
	 * Sets given object as the currently displayed screen.
	 */
	void setCurrentScreen(Object screen);

	/**
	 * Sets given object as the icon of the currently displayed screen.
	 */
	void setIcon(Object icon);

	/**
	 * Informs platform UI that screen title has changed.
	 * If the screen is the current one, application title
	 * should change accordingly.
	 */
	void screenTitleChanged(Object screen, String title);

	/**
	 * Informs platform UI that screen menu is attached.
	 */
	void screenMenuAdded(Object screen, Object menu);

	/**
	 * Informs platform UI that screen menu is detached.
	 */
	void screenMenuRemoved(Object screen, Object menu);

	/**
	 * Vibrates device for specified amount of time.
	 * Zero as argument stops vibrating.
	 * Returns false if feature is not supported.
	 */
	boolean vibrate(int millis);

	/**
	 * Flashes device backlight for specified amount of time.
	 * Zero as argument stops flashing.
	 * Returns false if feature is not supported.
	 */
	boolean flash(int millis);
}
