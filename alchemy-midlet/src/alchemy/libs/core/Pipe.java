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

package alchemy.libs.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import javax.microedition.io.StreamConnection;

/**
 *
 * @author Sergey Basalaev
 */
public final class Pipe implements StreamConnection {
	
	private static final int BUFFER_SIZE = 1024;
	
	/** Circular pipe buffer. */
	private byte[] buf = new byte[BUFFER_SIZE];
	/** Index of next byte to be written. */
	private int next = 0;
	/** Count of bytes in buffer. */
	private int count = 0;
	
	private DataInputStream in;
	private DataOutputStream out;

	public void close() { }

	public InputStream openInputStream() throws IOException {
		return openDataInputStream();
	}

	public DataInputStream openDataInputStream() throws IOException {
		if (in == null) return in = new DataInputStream(new PipeInputStream());
		else throw new IOException("Stream already opened.");
	}

	public OutputStream openOutputStream() throws IOException {
		return openDataOutputStream();
	}

	public DataOutputStream openDataOutputStream() throws IOException {
		if (out == null) return out = new DataOutputStream(new PipeOutputStream());
		else throw new IOException("Stream already opened.");
	}
	
	private class PipeOutputStream extends OutputStream {
		
		public PipeOutputStream() { }

		public void write(int b) throws IOException {
			synchronized (Pipe.this) {
				if (buf == null) throw new IOException("Pipe is closed");
				while (count == BUFFER_SIZE) {
					Pipe.this.notify();
					try {
						Pipe.this.wait(1000);
					} catch (InterruptedException ie) {
						throw new InterruptedIOException();
					}
				}
				buf[next] = (byte)b;
				next = (next+1) % BUFFER_SIZE;
				count++;
			}
		}

		public void write(byte[] b, int off, int len) throws IOException {
			synchronized (Pipe.this) {
				if (buf == null) throw new IOException("Pipe is closed");
				while (len > 0) {
					// calculate available continuous empty region
					int copyoff = next;
					int copylen;
					if (next >= count) {
						copylen = BUFFER_SIZE - next;
					} else {
						copylen = BUFFER_SIZE - count;
					}
					if (copylen > len) copylen = len;
					// if cannot copy bytes, wait this round
					if (copylen == 0) {
						Pipe.this.notify();
						try {
							Pipe.this.wait(1000);
						} catch (InterruptedException ie) {
							throw new InterruptedIOException();
						}
					} else {
						System.arraycopy(b, off, buf, copyoff, copylen);
						next = (next + copylen) % BUFFER_SIZE;
						count += copylen;
						off += copylen;
						len -= copylen;
					}
				}
			}
		}

		public void flush() throws IOException {
			synchronized (Pipe.this) {
				Pipe.this.notify();
			}
		}
		
		public void close() {
			synchronized (Pipe.this) {
				buf = null;
			}
		}
	}
	
	private class PipeInputStream extends InputStream {
		
		public PipeInputStream() { }
		
		public int read() throws IOException {
			synchronized (Pipe.this) {
				if (buf == null) return -1;
				while (count == 0) {
					Pipe.this.notify();
					try {
						Pipe.this.wait(1000);
					} catch (InterruptedException ie) {
						throw new InterruptedIOException();
					}
				}
				int index = next - count;
				if (index < 0) index += BUFFER_SIZE;
				count--;
				return buf[index];
			}
		}

		public int read(byte[] b, int off, int len) throws IOException {
			synchronized (Pipe.this) {
				if (buf == null) return -1;
				int readtotal = 0;
				while (buf != null && len > 0) {
					// calculate available continuous filled region
					int copyoff;
					int copylen;
					if (next >= count) {
						copyoff = next - count;
						copylen = count;
					} else {
						copyoff = next - count + BUFFER_SIZE;
						copylen = BUFFER_SIZE - copyoff;
					}
					if (copylen > len) copylen = len;
					// if cannot copy bytes, wait this round
					if (copylen == 0) {
						Pipe.this.notify();
						try {
							Pipe.this.wait(1000);
						} catch (InterruptedException ie) {
							throw new InterruptedIOException();
						}
					} else {
						System.arraycopy(buf, copyoff, b, off, copylen);
						count -= copylen;
						off += copylen;
						len -= copylen;
						readtotal += len;
					}
				}
				return readtotal;
			}
		}

		public void close() {
			synchronized (Pipe.this) {
				buf = null;
			}
		}

		public int available() throws IOException {
			return count;
		}
	}
}
