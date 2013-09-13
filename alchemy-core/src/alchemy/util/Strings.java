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

import java.io.UTFDataFormatException;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Various utility functions to operate on strings.
 *
 * @author Sergey Basalaev
 */
public final class Strings {

	private Strings() { }
	
	private static final String ERR_END = "Incomplete character at the end";
	private static final String ERR_WRONG = "Wrong code at byte ";
	private static final String ERR_LONG = "Encoded string is too long";

	/**
	 * Decodes bytes to String using modified UTF-8 format.
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
			b1 = b[ofs] & 0xff;
			ofs++;
			if (b1 < 0x80) {  // 0xxx xxxx
				chars[count]=(char)b1;
				count++;
				continue;
			}
			switch (b1 & 0xf0) {
				case 0xc0:    // 1100 xxxx   10xx xxxx
				case 0xd0:    // 1101 xxxx   10xx xxxx
					if (ofs + 1 > len) throw new UTFDataFormatException(ERR_END);
					b2 = b[ofs];
					ofs++;
					if ((b2 & 0xc0) != 0x80) throw new UTFDataFormatException(ERR_WRONG+(ofs-1));
					chars[count] = (char)( ((b1 & 0x1f) << 6) | (b2 & 0x3f) );
					count++;
					break;
				case 0xe0:    // 1110 xxxx   10xx xxxx   10xx xxxx
					if (ofs + 2 > len) throw new UTFDataFormatException(ERR_END);
					b2 = b[ofs];
					ofs++;
					b3 = b[ofs];
					ofs++;
					if (((b2 & 0xc0) != 0x80)) throw new UTFDataFormatException(ERR_WRONG+(ofs-2));
					if (((b3 & 0xc0) != 0x80)) throw new UTFDataFormatException(ERR_WRONG+(ofs-1));
					chars[count] = (char)( ((b1 & 0x0f) << 12) | ((b2 & 0x3f) << 6) | (b3 & 0x3f) );
					count++;
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
				bytes[ofs] = (byte)0xc0;
				ofs++;
				bytes[ofs] = (byte)0x80;
				ofs++;
			} else if (ch < 0x80) {
				bytes[ofs] = (byte)(ch);
				ofs++;
			} else if (ch < 0x800) {
				bytes[ofs] = (byte)((ch >> 6) | 0xc0);
				ofs++;
				bytes[ofs] = (byte)(ch&0x3f | 0x80);
				ofs++;
			} else {
				bytes[ofs] = (byte)((ch >> 12) | 0xe0);
				ofs++;
				bytes[ofs] = (byte)((ch >> 6)&0x3f | 0x80);
				ofs++;
				bytes[ofs] = (byte)(ch&0x3f | 0x80);
				ofs++;
			}
		}

		return bytes;
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
	public static String format(String fmt, Object[] args) {
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
					buf.append(stringValue(args[param-'0']));
				} else {
					buf.append(param);
				}
				fmt = fmt.substring(index+2);
			}
		}
		return buf.toString();
	}

	/**
	 * Splits specified string around given characters.
	 * @param str  string to split
	 * @param ch   delimiter characters
	 * @return the array of strings computed by splitting the string
	 *   around given character
	 */
	public static String[] split(String str, char ch) {
		ArrayList strings = new ArrayList();
		int index = str.indexOf(ch);
		while (index >= 0) {
			strings.add(str.substring(0, index));
			str = str.substring(index+1);
			index = str.indexOf(ch);
		}
		strings.add(str);
		String[] ret = new String[strings.size()];
		for (int i=strings.size()-1; i>=0; i--) {
			ret[i] = strings.get(i).toString();
		}
		return ret;
	}

	public static String stringValue(Object a) {
		if (a instanceof Object[]) {
			StringBuffer sb = new StringBuffer().append('[');
			final Object[] ar = (Object[]) a;
			for (int i=0; i < ar.length; i++) {
				if (i != 0) sb.append(", ");
				sb.append(stringValue(ar[i]));
			}
			return sb.append(']').toString();
		} else if (a instanceof char[]) {
			StringBuffer sb = new StringBuffer().append('[');
			final char[] ar = (char[]) a;
			for (int i=0; i < ar.length; i++) {
				if (i != 0) sb.append(", ");
				sb.append((int)ar[i]);
			}
			return sb.append(']').toString();
		} else if (a instanceof byte[]) {
			StringBuffer sb = new StringBuffer().append('[');
			final byte[] ar = (byte[]) a;
			for (int i=0; i < ar.length; i++) {
				if (i != 0) sb.append(", ");
				sb.append(ar[i]);
			}
			return sb.append(']').toString();
		} else if (a instanceof short[]) {
			StringBuffer sb = new StringBuffer().append('[');
			final short[] ar = (short[]) a;
			for (int i=0; i < ar.length; i++) {
				if (i != 0) sb.append(", ");
				sb.append(ar[i]);
			}
			return sb.append(']').toString();
		} else if (a instanceof boolean[]) {
			StringBuffer sb = new StringBuffer().append('[');
			final boolean[] ar = (boolean[]) a;
			for (int i=0; i < ar.length; i++) {
				if (i != 0) sb.append(", ");
				sb.append(ar[i] ? 1 : 0);
			}
			return sb.append(']').toString();
		} else if (a instanceof int[]) {
			StringBuffer sb = new StringBuffer().append('[');
			final int[] ar = (int[]) a;
			for (int i=0; i < ar.length; i++) {
				if (i != 0) sb.append(", ");
				sb.append(ar[i]);
			}
			return sb.append(']').toString();
		} else if (a instanceof long[]) {
			StringBuffer sb = new StringBuffer().append('[');
			final long[] ar = (long[]) a;
			for (int i=0; i < ar.length; i++) {
				if (i != 0) sb.append(", ");
				sb.append(ar[i]);
			}
			return sb.append(']').toString();
		} else if (a instanceof float[]) {
			StringBuffer sb = new StringBuffer().append('[');
			final float[] ar = (float[]) a;
			for (int i=0; i < ar.length; i++) {
				if (i != 0) sb.append(", ");
				sb.append(ar[i]);
			}
			return sb.append(']').toString();
		} else if (a instanceof double[]) {
			StringBuffer sb = new StringBuffer().append('[');
			final double[] ar = (double[]) a;
			for (int i=0; i < ar.length; i++) {
				if (i != 0) sb.append(", ");
				sb.append(ar[i]);
			}
			return sb.append(']').toString();
		} else if (a instanceof Hashtable) {
			StringBuffer sb = new StringBuffer().append('[');
			final Hashtable h = (Hashtable)a;
			boolean first = true;
			for (Enumeration e = h.keys(); e.hasMoreElements(); ) {
				Object key = e.nextElement();
				if (first) first = false;
				else sb.append(", ");
				sb.append(stringValue(key)).append('=').append(stringValue(h.get(key)));
			}
			return sb.append(']').toString();
		} else {
			return String.valueOf(a);
		}
	}
}
