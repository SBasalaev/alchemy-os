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

package alchemy.core;

/**
 * Maps magic numbers to builders.
 *
 * @author Sergey Basalaev
 */
class BuilderMap {

	private short[] magics;
	private LibBuilder[] builders;

	private int len;

	public BuilderMap() {
		len = 0;
		magics = new short[4];
		builders = new LibBuilder[4];
	}

	/**
	 * Returns LibBuilder for given magic.
	 * If there is no such builder then null is returned.
	 */
	public synchronized LibBuilder get(short magic) {
		for (int i=len-1; i>=0; i--) {
			if (magics[i]==magic) return builders[i];
		}
		return null;
	}

	/**
	 * Assotiates builder with magic.
	 */
	public synchronized void put(short magic, LibBuilder builder) {
		int index = 0;
		while (index < len && magics[index] != magic) {
			index++;
		}
		if (index == magics.length) grow();
		magics[index] = magic;
		builders[index] = builder;
	}

	/**
	 * Called when len is at max.
	 */
	private void grow() {
		int capacity = magics.length;
		short[] newmagics = new short[capacity << 1];
		System.arraycopy(magics, 0, newmagics, 0, capacity);
		magics = newmagics;
		LibBuilder[] newbuilders = new LibBuilder[capacity << 1];
		System.arraycopy(builders, 0, newbuilders, 0, capacity);
		builders = newbuilders;
	}
}
