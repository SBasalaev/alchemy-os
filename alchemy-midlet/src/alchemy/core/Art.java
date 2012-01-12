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

import alchemy.fs.Filesystem;

/**
 * Alchemy runtime.
 * @author Sergey Basalaev
 */
public final class Art {

	/** Filesystem to use. */
	Filesystem fs;

	private Context root;
	LibCache cache = new LibCache();
	BuilderMap builders = new BuilderMap();

	/**
	 * Creates new Alchemy runtime.
	 * @param fs  filesystem to use
	 */
	public Art(Filesystem fs) {
		this.fs = fs;
		root = new Context(this);
	}

	/**
	 * Returns root context for this runtime.
	 */
	public Context rootContext() {
		return root;
	}

	/**
	 * Associates given builder with given magic.
	 */
	public void setLibBuilder(short magic, LibBuilder builder) {
		builders.put(magic, builder);
	}
}
