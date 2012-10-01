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

package alchemy.core;

import java.lang.ref.WeakReference;
import java.util.Hashtable;

/**
 * Cache of libraries.
 * Libraries are cached using filename and
 * file timestamp as keys.
 *
 * @author Sergey Basalaev
 */
class LibCache {

	/** Maps Files to LibCacheEntries. */
	private final Hashtable cache = new Hashtable();

	public LibCache() {	}

	/**
	 * Returns library for given file and timestamp.
	 * If library with given parameters is not cached
	 * then <code>null</code> is returned.
	 */
	public synchronized Library getLibrary(String file, long tstamp) {
		LibCacheEntry entry = (LibCacheEntry)cache.get(file);
		if (entry == null) return null;
		if (entry.tstamp < tstamp) return null;
		return (Library)entry.lib.get();
		/*
		 * No need to remove reference from cache if lib.get()
		 * returns null because the next thing to happen will
		 * be loading and caching new library.
		 */
	}

	/**
	 * Puts library in cache.
	 */
	public synchronized void putLibrary(String file, long tstamp, Library lib) {
		cache.put(file, new LibCacheEntry(new WeakReference(lib), tstamp));
	}


	/**
	 * Library, cached in <code>LibCache</code>.
	 */
	private static class LibCacheEntry {
		public final WeakReference lib;
		public final long tstamp;

		public LibCacheEntry(WeakReference lib, long tstamp) {
			this.lib = lib;
			this.tstamp = tstamp;
		}
	}
}
