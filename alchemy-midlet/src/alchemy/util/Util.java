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

package alchemy.util;

import alchemy.l10n.I18N;
import java.io.IOException;
import java.io.InputStream;
import java.io.UTFDataFormatException;

/**
 * Misc. utilities used in Alchemy.
 * @author Sergey Basalaev
 */
public final class Util {

	private static final String ERR_END = I18N._("Incomplete character at the end");
	private static final String ERR_WRONG = I18N._("Wrong code at byte ");
	private static final String ERR_LONG = I18N._("Encoded string is too long");

	private Util() { }

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
}
