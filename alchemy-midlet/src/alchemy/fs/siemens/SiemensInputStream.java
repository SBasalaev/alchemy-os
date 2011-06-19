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
 *
 */

package alchemy.fs.siemens;

import java.io.IOException;
import java.io.InputStream;

/**
 * Coverage of input stream from Siemens file system
 * to fix incorrectly working <code>skip</code> method.
 * @author Sergey Basalaev
 */
final class SiemensInputStream extends InputStream {

	private final InputStream in;

	public SiemensInputStream(InputStream in) {
		this.in = in;
	}

	public int read() throws IOException {
		return in.read();
	}

	public int read(byte[] b, int off, int len) throws IOException {
		return in.read(b, off, len);
	}

	public boolean markSupported() {
		return in.markSupported();
	}

	public void mark(int readlimit) {
		in.mark(readlimit);
	}

	public void reset() throws IOException {
		in.reset();
	}

	public int available() throws IOException {
		return in.available();
	}

	public void close() throws IOException {
		in.close();
	}

	public long skip(long n) throws IOException {
		//not from in
		return super.skip(n);
	}
}
