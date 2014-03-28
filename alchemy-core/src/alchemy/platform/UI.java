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
	 * Sets given object as currently displayed screen.
	 */
	void setCurrentScreen(Object screen);

	/**
	 * Vibrates device for specified amount of time.
	 * Zero as argument stops vibrating.
	 * Returns false if feature is not supported.
	 */
	boolean vibrate(long millis);

	/**
	 * Flashes device backlight for specified amount of time.
	 * Zero as argument stops flashing.
	 * Returns false if feature is not supported.
	 */
	boolean flash(long millis);
}
