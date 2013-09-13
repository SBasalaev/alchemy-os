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

package alchemy.util;

/**
 * Maps keys to values.
 * <p>
 * Unlike Hashtable this class is not synchronized.
 *
 * @author Sergey Basalaev
 */
public class HashMap {
	
	private HashMapEntry[] entries;
	private int size;

	public HashMap() {
		entries = new HashMapEntry[11];
	}
	
	public Object get(Object key) {
		HashMapEntry[] table = entries;
		int hash = key.hashCode();
		int index = (hash & 0x7FFFFFFF) % table.length;
		for (HashMapEntry e = table[index]; e != null; e = e.next) {
			if (e.hash == hash && e.key.equals(key)) {
				return e.value;
			}
		}
		return null;
	}

	public void set(Object key, Object value) {
		if (value == null)
			throw new NullPointerException();
		
		// search existing entry
		HashMapEntry[] table = entries;
		int hash = key.hashCode();
		int index = (hash & 0x7FFFFFFF) % table.length;
		for (HashMapEntry e = table[index]; e != null; e = e.next) {
			if (e.hash == hash && e.key.equals(key)) {
				e.value = value;
				return;
			}
		}
		
		// rehash if too many objects
		if (size * 3 / 4 > table.length)
			rehash();
		
		// create new entry
		HashMapEntry e = new HashMapEntry();
		e.hash = hash;
		e.key = key;
		e.value = value;
		e.next = table[index];
		table[index] = e;
		size++;
	}
	
	public void remove(Object key) {
		HashMapEntry[] table = entries;
		int hash = key.hashCode();
		int index = (hash & 0x7FFFFFFF) % table.length;
		for (HashMapEntry e = table[index], prev = null; e != null; prev = e, e = e.next) {
			if (e.hash == hash && e.key.equals(key)) {
				if (prev != null) prev.next = e.next;
				else table[index] = e.next;
				size--;
			}
		}
	}
	
	public int size() {
		return size;
	}
	
	public void clear() {
		HashMapEntry[] table = entries;
		for (int i=table.length-1; i >= 0; i--) {
			table[i] = null;
		}
		size = 0;
	}
	
	public Object[] keys() {
		Object[] ret = new Object[size];
		int pos = 0;
		HashMapEntry[] table = entries;
		for (int i=table.length-1; i>=0; i--) {
			for (HashMapEntry e = table[i]; e != null; e = e.next) {
				ret[pos] = e.key;
				pos++;
			}
		}
		return ret;
	}
	
	private void rehash() {
		HashMapEntry[] oldTable = entries;
		HashMapEntry[] newTable = new HashMapEntry[oldTable.length * 2 + 1];
		for (int index=oldTable.length-1; index >= 0; index--) {
			HashMapEntry list = oldTable[index];
			while (list != null) {
				HashMapEntry e = list;
				list = list.next;
				int newIndex = (e.hash & 0x7FFFFFFF) % newTable.length;
				e.next = newTable[newIndex];
				newTable[newIndex] = e;
			}
		}
		entries = newTable;
	}
	
	void buildString(ArrayList dejaVu, StringBuffer buf) {
		if (dejaVu.indexOf(this) >= 0) {
			buf.append("{...}");
		} else {
			dejaVu.add(this);
			buf.append('{');
			HashMapEntry[] table = entries;
			for (int index = table.length-1; index >= 0; index--) {
				for (HashMapEntry e = table[index]; e != null; e = e.next) {
					Strings.buildString(e.key, dejaVu, buf);
					buf.append('=');
					Strings.buildString(e.value, dejaVu, buf);
				}
			}
			buf.append('}');
			dejaVu.remove(this);
		}
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buildString(new ArrayList(), buf);
		return buf.toString();
	}

	private static class HashMapEntry {
		int hash;
		Object key;
		Object value;
		HashMapEntry next;
	}
}
