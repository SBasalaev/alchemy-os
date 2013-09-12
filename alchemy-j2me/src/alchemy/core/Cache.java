/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011-2013, Sergey Basalaev <sbasalaev@gmail.com>
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
 * Cache of reusable objects loaded from files.
 * This cache holds immutable objects such as libraries
 * and images that were loaded from the file system and
 * can be shared.
 *
 * @author Sergey Basalaev
 */
public class Cache {

	/** Maps file names to CacheEntries. */
	private static final Hashtable cache = new Hashtable();

	private Cache() { }

	/**
	 * Returns object for given file and timestamp.
	 * If object for given file name is not in cache or
	 * have older timestamp then <code>null</code> is returned.
	 */
	public static synchronized Object get(String file, long tstamp) {
		CacheEntry entry = (CacheEntry)cache.get(file);
		if (entry == null || entry.tstamp < tstamp) return null;
		return entry.ref.get();
		/* We don't remove entry if ref contains null.
		 * This is because in typical situation the next thing
		 * to happen is loading and caching new version of object.
		 */
	}

	/** Puts object in the cache. */
	public static synchronized void put(String file, long tstamp, Object obj) {
		cache.put(file, new CacheEntry(obj, tstamp));
	}

	/** Holds cached object and a timestamp. */
	private static class CacheEntry {
		public final WeakReference ref;
		public final long tstamp;

		public CacheEntry(Object obj, long tstamp) {
			this.ref = new WeakReference(obj);
			this.tstamp = tstamp;
		}
	}
}
