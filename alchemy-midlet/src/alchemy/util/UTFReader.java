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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UTFDataFormatException;

/**
 * Reader of UTF-encoded data.
 * Also this reader allows one to read a single line of text
 * and to get a number of the current line.
 *
 * @author Sergey Basalaev
 */
public class UTFReader extends Reader {

	private InputStream input;
	private int linenum;

	public UTFReader(InputStream s) {
		input = s;
	}

	public synchronized int read() throws IOException {
		if (input == null) throw new IOException("Stream is closed");
		int b1 = input.read();
		if (b1 < 0) return -1;
		if (b1 <= 0x7f) {
			if (b1 == '\n') linenum++;
			return b1;
		}
		if (b1 < 0xC0) throw new UTFDataFormatException("Malformed UTF");
		int b2 = input.read();
		if (b2 < 0) throw new UTFDataFormatException("Unfinished UTF char at EOF");
		if ((b2 & 0xC0) != 0x80) throw new UTFDataFormatException("Malformed UTF");
		if (b1 < 0xE0) {
			return ((b1 & 0x1f) << 6) | (b2 & 0x3f);
		}
		int b3 = input.read();
		if (b3 < 0) throw new UTFDataFormatException("Unfinished UTF char at EOF");
		if ((b3 & 0xC0) != 0x80) throw new UTFDataFormatException("Malformed UTF");
		return ((b1 & 0x0f) << 12) | ((b2 & 0x3f) << 6) | (b3 & 0x3f);
	}

	public synchronized int read(char[] cbuf, int off, int len) throws IOException {
		if (off+len > cbuf.length || off < 0)
			throw new IndexOutOfBoundsException();
		if (len == 0) return 0;
		int reallen = 0;
		while (reallen < len) {
			int ch = read();
			if (ch < 0) break;
			cbuf[off+reallen] = (char)ch;
			reallen++;
		}
		if (reallen == 0) return -1;
		return reallen;
	}

	public void close() throws IOException {
		if (input == null) return;
		input.close();
		input = null;
	}

	/**
	 * Reads a single line of text.
	 * @return
	 *   characters until first <code>'\n'</code> or EOF
	 *   as a string; <code>null</code> if stream is at end
	 * @throws IOException if an I/O error occurs
	 */
	public synchronized String readLine() throws IOException {
		int ch = read();
		if (ch < 0) return null;
		StringBuffer sb = new StringBuffer();
		while (ch > 0 && ch != '\n') {
			sb.append((char)ch);
			ch = read();
		}
		return sb.toString();
	}

	/**
	 * Returns number of the current line.
	 * @return current line number
	 */
	public int lineNumber() {
		return linenum;
	}
}
