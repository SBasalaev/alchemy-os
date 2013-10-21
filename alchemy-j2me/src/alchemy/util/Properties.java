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

import alchemy.io.UTFReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Reads properties from the text stream.
 *
 * @author Sergey Basalaev
 * @deprecated 
 */
public class Properties {

	private Hashtable props = new Hashtable();

	public Properties() { }

	/**
	 * Reads properties from given text stream.
	 * Stream is not closed after reading.
	 * @param r  text stream
	 * @throws IOException if an I/O error occurs
	 */
	public static Properties readFrom(UTFReader r) throws IOException {
		String line;
		Properties p = new Properties();
		while ((line = r.readLine()) != null) {
			line = line.trim();
			if (line.length() == 0) continue;
			if (line.charAt(0) == '#') continue;
			int eqindex = line.indexOf('=');
			if (eqindex < 0) throw new IOException("Cannot parse:"+' '+line);
			p.props.put(
					line.substring(0,eqindex).trim(),
					line.substring(eqindex+1).trim());
		}
		return p;
	}

	public String get(String key) {
		Object o = props.get(key);
		return o != null ? o.toString() : null;
	}

	public void put(String key, String value) {
		if (key.indexOf('=') >= 0)
			throw new IllegalArgumentException("Key must not contain '='");
		props.put(key, value);
	}

	public synchronized String toString() {
		StringBuffer sb = new StringBuffer();
		for (Enumeration e = props.keys(); e.hasMoreElements(); ) {
			Object key = e.nextElement();
			sb.append(key).append('=').append(props.get(key)).append('\n');
		}
		return sb.toString();
	}
}
