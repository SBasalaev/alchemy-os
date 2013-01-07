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

import alchemy.libs.LibCore30;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UTFDataFormatException;
import java.util.Vector;

/**
 * Miscellaneous I/O utilities used in Alchemy.
 * @author Sergey Basalaev
 */
public final class IO {

	private static final String ERR_END = "Incomplete character at the end";
	private static final String ERR_WRONG = "Wrong code at byte ";
	private static final String ERR_LONG = "Encoded string is too long";

	private IO() { }

	/**
	 * Decodes bytes to String using modified UTF format.
	 * @param b  byte array
	 * @return decoded string
	 * @throws UTFDataFormatException  if given byte sequence is not valid UTF
	 */
	public static String utfDecode(byte[] b) throws UTFDataFormatException {
        int len = b.length;
		char[] chars = new char[len];
		int count = 0;                 //count of chars read so far
		int ofs  = 0;                  //offset in byte array
		int b1, b2, b3;                //bytes to compound symbols from

		while (ofs < len) {
			b1 = b[ofs++] & 0xff;
			if (b1 < 0x80) {  // 0xxx xxxx
				chars[count++]=(char)b1;
				continue;
			}
			switch (b1 & 0xf0) {
				case 0xc0:    // 1100 xxxx   10xx xxxx
				case 0xd0:    // 1101 xxxx   10xx xxxx
					if (ofs + 1 > len) throw new UTFDataFormatException(ERR_END);
					b2 = b[ofs++];
					if ((b2 & 0xc0) != 0x80) throw new UTFDataFormatException(ERR_WRONG+(ofs-1));
					chars[count++] = (char)( ((b1 & 0x1f) << 6) | (b2 & 0x3f) );
					break;
				case 0xe0:    // 1110 xxxx   10xx xxxx   10xx xxxx
					if (ofs + 2 > len) throw new UTFDataFormatException(ERR_END);
					b2 = b[ofs++];
					b3 = b[ofs++];
					if (((b2 & 0xc0) != 0x80)) throw new UTFDataFormatException(ERR_WRONG+(ofs-2));
					if (((b3 & 0xc0) != 0x80)) throw new UTFDataFormatException(ERR_WRONG+(ofs-1));
					chars[count++] = (char)( ((b1 & 0x0f) << 12) | ((b2 & 0x3f) << 6) | (b3 & 0x3f) );
					break;
				default:
					throw new UTFDataFormatException(ERR_WRONG+(ofs-1));
			}
		}

        return new String(chars, 0, count);
	}

	/**
	 * Encodes string in byte array in modified UTF format.
	 * @param str  string to encode
	 * @return  byte array containing encoded string
	 * @throws UTFDataFormatException if string is too long to encode
	 */
	public static byte[] utfEncode(String str) throws UTFDataFormatException {
		int len = str.length();  //string length, C.O.
		int count = 0;           //a count of bytes
		char ch;                 //current character
		byte[] bytes;            //result of encoding
		int ofs = 0;             //offset in byte array

		//calculating byte count
		for (int i=0; i<len; i++) {
			ch = str.charAt(i);
			if (ch == 0) count += 2;
			else if (ch < 0x80) count++;
			else if (ch < 0x0800) count += 2;
			else count += 3;
		}

		if (count >= 0xffff)
			throw new UTFDataFormatException(ERR_LONG);

		bytes = new byte[count];

		//string encoding
		for (int i=0; i<len; i++) {
			ch = str.charAt(i);
			if (ch == 0) {
				bytes[ofs++] = (byte)0xc0;
				bytes[ofs++] = (byte)0x80;
			} else if (ch < 0x80) {
				bytes[ofs++] = (byte)(ch);
			} else if (ch < 0x800) {
				bytes[ofs++] = (byte)((ch >> 6) | 0xc0);
				bytes[ofs++] = (byte)(ch&0x3f | 0x80);
			} else {
				bytes[ofs++] = (byte)((ch >> 12) | 0xe0);
				bytes[ofs++] = (byte)((ch >> 6)&0x3f | 0x80);
				bytes[ofs++] = (byte)(ch&0x3f | 0x80);
			}
		}

		return bytes;
	}

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
	
	public static void print(OutputStream s, Object obj) {
		try {
			s.write(utfEncode(LibCore30.stringValue(obj)));
		} catch (IOException ioe) { }
	}
	
	public static void println(OutputStream s, Object obj) {
		try {
			s.write(utfEncode(LibCore30.stringValue(obj)));
			s.write('\n');
			s.flush();
		} catch (IOException ioe) { }
	}
	
	/**
	 * Splits specified string around given characters.
	 * @param str  string to split
	 * @param ch   delimiter characters
	 * @return the array of strings computed by splitting the string
	 *   around given character
	 */
	public static String[] split(String str, char ch) {
		Vector strings = new Vector();
		int index = str.indexOf(ch);
		while (index >= 0) {
			strings.addElement(str.substring(0, index));
			str = str.substring(index+1);
			index = str.indexOf(ch);
		}
		strings.addElement(str);
		String[] ret = new String[strings.size()];
		for (int i=strings.size()-1; i>=0; i--) {
			ret[i] = strings.elementAt(i).toString();
		}
		return ret;
	}
	
	/**
	 * Returns formatted string using specified format string and arguments.
	 * Format specifiers are substrings of form <code>%n</code> where
	 * <code>n</code> is from 0 to 9. Each format specifier is substituted
	 * with corresponding value from array <code>args</code>. Specifier
	 * <code>%%</code> is substituted with percent character.
	 * 
	 * @param fmt   format string
	 * @param args  arguments referenced by the format specifiers
	 * @return  a formatted string
	 */
	public static String printf(String fmt, Object[] args) {
		StringBuffer buf = new StringBuffer();
		while (true) {
			int index = fmt.indexOf('%');
			if (index < 0 || index == fmt.length()-1) {
				buf.append(fmt);
				break;
			} else {
				buf.append(fmt.substring(0, index));
				char param = fmt.charAt(index+1);
				if (param >= '0' && param <= '9') {
					buf.append(LibCore30.stringValue(args[param-'0']));
				} else {
					buf.append(param);
				}
				fmt = fmt.substring(index+2);
			}
		}
		return buf.toString();
	}
	
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

}
