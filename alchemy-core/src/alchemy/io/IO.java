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

import alchemy.util.Strings;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
	
	/**
	 * Reads data from input stream.
	 * Reading from native streams should be done through
	 * this function because some phones do not follow the contract of
	 * {@link InputStream#read(byte[], int, int) InputStream.read}
	 * accurately.
	 * 
	 * Known bugs:
	 * <ul>
	 *   <li>Nokia phones fail to read zero bytes</li>
	 * </ul>
	 * 
	 * @throws IOException if an I/O error occurs.
	 */
	public static int readArray(InputStream in, byte[] buf, int ofs, int len) throws IOException {
		if (buf == null)
			throw new NullPointerException();
		if (ofs < 0 || len < 0 || ofs+len > buf.length)
			throw new IndexOutOfBoundsException();
		if (len == 0)
			return 0;
		return in.read(buf, ofs, len);
	}
	
	/**
	 * This is a shortcut for readArray(in, buf, 0, buf.length).
	 */
	public static int readArray(InputStream in, byte[] buf) throws IOException {
		return readArray(in, buf, 0, buf.length);
	}
	
	/**
	 * Writes data into output stream.
	 * Writing to native streams should be done through
	 * this function because some phones do not follow the contract of
	 * {@link OutputStream#write(byte[], int, int) OutputStream.write}
	 * accurately.
	 * 
	 * Known bugs:
	 * <ul>
	 *   <li>Nokia phones fail to write zero bytes</li>
	 * </ul>
	 * 
	 * @throws IOException if an I/O error occurs.
	 */
	public static void writeArray(OutputStream out, byte[] buf, int ofs, int len) throws IOException {
		if (buf == null)
			throw new NullPointerException();
		if (ofs < 0 || len < 0 || ofs+len > buf.length)
			throw new IndexOutOfBoundsException();
		if (len == 0)
			return;
		out.write(buf, ofs, len);
	}
	
	/** This is a shortcut for writeArray(out, buf, 0, buf.length). */
	public static void writeArray(OutputStream out, byte[] buf) throws IOException {
		writeArray(out, buf, 0, buf.length);
	}
	
	/**
	 * Skips over and discards data from input stream.
	 * Skipping in native streams should be done through
	 * this function because some phones do not follow the contract of
	 * {@link InputStream#skip(long) InputStream.skip}
	 * accurately.
	 * 
	 * Known bugs:
	 * <ul>
	 *   <li>Siemens phones implement skip as seek</li>
	 * </ul>
	 * 
	 * @throws IOException 
	 */
	public static long skip(InputStream in, long n) throws IOException {
		if (n <= 0L) return 0L;
		
		long remains = n;
		byte[] buf = new byte[Math.min((int)remains, 1024)];
		while (remains > 0L) {
			int skipped = readArray(in, buf, 0, Math.min((int)remains, buf.length));
			if (skipped <= 0) break;
			remains -= skipped;
		}
		return n - remains;
	}
	
	/** Converts object to a string and writes it as UTF-8. */
	public static void print(OutputStream s, Object obj) {
		try {
			writeArray(s, Strings.utfEncode(Strings.toString(obj)));
		} catch (IOException ioe) { }
	}
	
	/** Converts object to a string, writes it as UTF-8 and terminates a line. */
	public static void println(OutputStream s, Object obj) {
		try {
			writeArray(s, Strings.utfEncode(Strings.toString(obj)));
			s.write('\n');
			s.flush();
		} catch (IOException ioe) { }
	}
	
	/** Channels all data from the input to the output. */
	public static void writeAll(InputStream from, OutputStream to) throws IOException {
		byte[] buf = new byte[1024];
		int len = readArray(from, buf, 0, buf.length);
		while (len > 0) {
			writeArray(to, buf, 0, len);
			len = readArray(from, buf, 0, buf.length);
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
}
