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

import alchemy.types.Float32;
import alchemy.types.Float64;
import alchemy.types.Int32;
import alchemy.types.Int64;
import java.io.UTFDataFormatException;

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
					buf.append(toString(args[param-'0']));
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

	/** Returns hexadecimal character that represents the number. */
	private static char hexchar(int i) {
		return (char) (i <= 9 ? '0'+i : 'A'-10 + i);
	}

	/**
	 * Writes character to the buffer.
	 * If character is non-ASCII then it is escaped.
	 */
	private static void writeChar(char ch, StringBuffer buf) {
		switch (ch) {
			case '\n': buf.append("\\n"); break;
			case '\r': buf.append("\\r"); break;
			case '\t': buf.append("\\t"); break;
			case '\0': buf.append("\\0"); break;
			default:
				if (ch >= ' ' && ch < 127) {
					buf.append(ch);
				} else {
					buf.append("\\u")
					   .append(hexchar((ch >> 12) & 0xF))
					   .append(hexchar((ch >> 8) & 0xF))
					   .append(hexchar((ch >> 4) & 0xF))
					   .append(hexchar(ch & 0xF));
				}
		}
	}

	/** Converts array to a string and writes it to the buffer. */
	static void arrayToString(Object a, ArrayList dejaVu, StringBuffer buf) {
		buf.append('[');
		if (a instanceof Object[]) {
			if (dejaVu.indexOf(a) >= 0) {
				buf.append("[...]");
			} else {
				dejaVu.add(a);
				Object[] aarray = (Object[])a;
				int len = aarray.length;
				for (int i=0; i<len; i++) {
					if (i != 0) buf.append(", ");
					buildString(aarray[i], dejaVu, buf);
				}
				dejaVu.remove(a);
			}
		} else if (a instanceof boolean[]) {
			boolean[] zarray = (boolean[])a;
			int len = zarray.length;
			for (int i=0; i<len; i++) {
				if (i != 0) buf.append(", ");
				buf.append(zarray[i] ? "true" : "false");
			}
		} else if (a instanceof byte[]) {
			byte[] barray = (byte[])a;
			int len = barray.length;
			for (int i=0; i<len; i++) {
				if (i != 0) buf.append(", ");
				buf.append(barray[i]);
			}
		} else if (a instanceof char[]) {
			char[] carray = (char[])a;
			int len = carray.length;
			for (int i=0; i<len; i++) {
				if (i != 0) buf.append(", ");
				buf.append('\'');
				writeChar(carray[i], buf);
				buf.append('\'');
			}
		} else if (a instanceof short[]) {
			short[] sarray = (short[])a;
			int len = sarray.length;
			for (int i=0; i<len; i++) {
				if (i != 0) buf.append(", ");
				buf.append(sarray[i]);
			}
		} else if (a instanceof int[]) {
			int[] iarray = (int[])a;
			int len = iarray.length;
			for (int i=0; i<len; i++) {
				if (i != 0) buf.append(", ");
				buf.append(iarray[i]);
			}
		} else if (a instanceof long[]) {
			long[] larray = (long[])a;
			int len = larray.length;
			for (int i=0; i<len; i++) {
				if (i != 0) buf.append(", ");
				buf.append(larray[i]).append('l');
			}
		} else if (a instanceof float[]) {
			float[] farray = (float[])a;
			int len = farray.length;
			for (int i=0; i<len; i++) {
				if (i != 0) buf.append(", ");
				buf.append(farray[i]).append('f');
			}
		} else if (a instanceof double[]) {
			double[] darray = (double[])a;
			int len = darray.length;
			for (int i=0; i<len; i++) {
				if (i != 0) buf.append(", ");
				buf.append(darray[i]).append('d');
			}
		}
		buf.append(']');
	}

	static void buildString(Object a, ArrayList dejaVu, StringBuffer buf) {
		if (a == null) buf.append("null");
		Class clz = a.getClass();
		if (clz == ArrayList.class) {
			((ArrayList)a).buildString(dejaVu, buf);
		} else if (clz == String.class) {
			buf.append('"');
			String str = (String)a;
			int len = str.length();
			for (int i=0; i<len; i++) {
				writeChar(str.charAt(i), buf);
			}
			buf.append('"');
		} else if (clz.isArray()) {
			arrayToString(a, dejaVu, buf);
		} else if (clz == Int32.class) {
			buf.append(a);
		} else if (clz == Int64.class) {
			buf.append(a).append('l');
		} else if (clz == Float32.class) {
			buf.append(a).append('f');
		} else if (clz == Float64.class) {
			buf.append(a).append('d');
		} else {
			buf.append(clz.getName());
		}
	}

	/**
	 * Converts Alchemy object to a string.
	 */
	public static String toString(Object a) {
		if (a == null) return "null";
		Class clz = a.getClass();
		if (clz == String.class || clz == Int32.class || clz == Int64.class
		 || clz == Float32.class || clz == Float64.class) {
			return a.toString();
		}
		StringBuffer buf = new StringBuffer();
		buildString(a, new ArrayList(), buf);
		return buf.toString();
	}
}
