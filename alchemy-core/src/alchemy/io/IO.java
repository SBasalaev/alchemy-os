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

package alchemy.io;

import alchemy.fs.Filesystem;
import alchemy.util.Strings;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.Connector;

/**
 * Miscellaneous I/O utilities used in Alchemy OS.
 * @author Sergey Basalaev
 */
public final class IO {

	private IO() { }

	/**
	 * Reads until the end of the stream and returns result as a byte array.
	 * @param s input stream
	 * @return byte array containing all bytes read
	 * @throws IOException if an I/O error occurs
	 */
	public static byte[] readFully(InputStream s) throws IOException {
		int count = 0;
		byte[] buf = new byte[32];
		while (true) {
			if (count == buf.length) {
				byte[] newbuf = new byte[count << 1];
				System.arraycopy(buf, 0, newbuf, 0, count);
				buf = newbuf;
			}
			int len = s.read(buf, count, buf.length-count);
			if (len <= 0) break;
			count += len;
		}
		byte[] ret = new byte[count];
		System.arraycopy(buf, 0, ret, 0, count);
		return ret;
	}
	
	/** Converts object to a string and writes it as UTF-8. */
	public static void print(OutputStream s, Object obj) {
		try {
			s.write(Strings.utfEncode(Strings.toString(obj)));
		} catch (IOException ioe) { }
	}

	/** Converts object to a string, writes it as UTF-8 and terminates a line. */
	public static void println(OutputStream s, Object obj) {
		try {
			s.write(Strings.utfEncode(Strings.toString(obj)));
			s.write('\n');
			s.flush();
		} catch (IOException ioe) { }
	}

	/** Channels all data from the input to the output. */
	public static void writeAll(InputStream from, OutputStream to) throws IOException {
		byte[] buf = new byte[1024];
		int len = from.read(buf);
		while (len > 0) {
			to.write(buf, 0, len);
			len = from.read(buf);
		}
	}

	/**
	 * Checks if given filename matches glob pattern.
	 * Supported wildcards are <code>*</code> and <code>?</code>.
	 * These characters can be escaped using backslash.
	 */
    public static boolean matchesPattern(String name, String pattern) {
        int nameofs = 0;
        int pofs = 0;
        int namelen = name.length();
        int plen = pattern.length();
        while (pofs < plen) {
            char ch = pattern.charAt(pofs);
            pofs++;
            switch (ch) {
                case '*': { // any sequence of characters
                    /* skip subsequent stars */
                    while (pofs < plen && pattern.charAt(pofs) == '*')
                        pofs++;
                    /* ending star matches everything */
                    if (pofs == plen) return true;
                    /* match tails */
                    String tailpattern = pattern.substring(pofs, plen);
                    ch = tailpattern.charAt(0);
                    if (ch == '?') { //unoptimized cycle
                        for (int i = nameofs; i < namelen; i++) {
                            if (matchesPattern(name.substring(i,namelen), tailpattern))
                                return true;
                        }
                    } else {
                        if (ch == '\\' && tailpattern.length() > 1) ch = tailpattern.charAt(1);
                        for (int i = name.indexOf(ch, nameofs); i >= 0; i = name.indexOf(ch, i+1)) {
                            if (matchesPattern(name.substring(i,namelen), tailpattern))
                                return true;
                        }
                    }
                    return false;
                }
                case '\\': { // escaped character, exact match
                    if (pofs < plen) ch = pattern.charAt(pofs);
                    if (nameofs == namelen) return false;
                    if (name.charAt(nameofs) != ch) return false;
                    nameofs++;
                    pofs++;
                    break;
                }
                case '?': { // any character
                    if (nameofs == namelen) return false;
                    nameofs++;
                    break;
                }
                default: { // exact character match
                    if (nameofs == namelen) return false;
                    if (name.charAt(nameofs) != ch) return false;
                    nameofs++;
                }
            }
        }
        /* empty pattern matches empty string */
        return nameofs == namelen;
    }

	/**
	 * Returns input stream to read from the specified URL.
	 * Supported protocols are "file", "res", "http" and "https".
	 */
	public static ConnectionInputStream readUrl(String url) throws IOException {
		int cl = url.indexOf(':');
		if (cl <= 0)
			throw new IOException("No protocol in URL");
		ConnectionInputStream input;
		String protocol = url.substring(0,cl);
		String path = url.substring(cl+1);
		InputStream in;
		if ("file".equals(protocol)) {
			in = Filesystem.read(path);
		} else if ("res".equals(protocol)) {
			in = String.class.getResourceAsStream(path);
			if (in == null)
				throw new IOException("Resource not found: " + path);
		} else if ("http".equals(protocol) || "https".equals(protocol)) {
			in = Connector.openInputStream(url);
		} else {
			throw new IOException("Unknown protocol: " + protocol);
		}
		if (in instanceof ConnectionInputStream) {
			return (ConnectionInputStream)in;
		} else {
			return new ConnectionInputStream(in);
		}
	}
}
