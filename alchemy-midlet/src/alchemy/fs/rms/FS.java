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

package alchemy.fs.rms;

import alchemy.fs.File;
import alchemy.fs.Filesystem;
import alchemy.util.Initable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;

/**
 * Filesystem implementation based on a record storage system.
 * This filesystem is suitable for all MIDP devices, but is
 * rather slow.
 * @author Sergey Basalaev
 */
public final class FS extends Filesystem implements Initable {

	static private final String ERR_DENIED = "Access denied to ";
	static private final String ERR_CLOSED = "File is closed: ";
	static private final String ERR_NOT_DIR = "Not a directory: ";
	static private final String ERR_IS_DIR = "Is a directory: ";
	static private final String ERR_EXISTS = "File already exists: ";
	static private final String ERR_NOT_FOUND = "File not found: ";
	static private final String ERR_NOT_EMPTY = "Directory not empty: ";

	static private int F_DIR = 16;
	static private int F_READ = 4;
	static private int F_WRITE = 2;
	static private int F_EXEC = 1;

	/** <code>RecordStore</code> to use as filesystem. */
	private RecordStore store;

	/**
	 * Constructor to load through the reflection.
	 * <code>init()</code> should be called before
	 * filesystem can be used.
	 */
	public FS() { }

	/**
	 * Creates new <code>RSFilesystem</code> that uses
	 * record store with given name.
	 * @param name    name of the record store to use
	 * @throws RuntimeException
	 *   if an error occurs within record storage system
	 */
	public void init(Object name) {
		RecordStore rs = null;
		String storename = String.valueOf(name);
		try {
			try {
				rs = RecordStore.openRecordStore(storename, false);
			} catch (RecordStoreNotFoundException rsnfe) {
				rs = RecordStore.openRecordStore(storename, true);
				rs.addRecord(new byte[] {0,0,0,0,0,0,0,0,0,0,0,0}, 0, 12);
			}
		} catch (RecordStoreException rse) {
			throw new RuntimeException(rse.toString());
		}
		this.store = rs;
	}

	public synchronized void create(File file) throws IOException {
		RSFD parentfd = getFD(file.parent());
		if ((parentfd.attrs & F_DIR) == 0)
			throw new IOException(ERR_NOT_DIR+parentfd.file.path());
		RSDirectory dir = new RSDirectory(parentfd);
		int index = dir.getIndex(file.name());
		if (index >= 0) throw new IOException(ERR_EXISTS+file.path());
		RSFD fd = new RSFD();
		fd.file = file;
		fd.attrs = F_READ | F_WRITE;
		fd.record = createFileNode(false);
		dir.nodes[dir.count] = fd;
		dir.count++;
		dir.flush();
	}

	public synchronized void mkdir(File file) throws IOException {
		RSFD parentfd = getFD(file.parent());
		if ((parentfd.attrs & F_DIR) == 0)
			throw new IOException(ERR_NOT_DIR+parentfd.file.path());
		RSDirectory dir = new RSDirectory(parentfd);
		int index = dir.getIndex(file.name());
		if (index >= 0) throw new IOException(ERR_EXISTS+file.path());
		RSFD fd = new RSFD();
		fd.file = file;
		fd.attrs = F_READ | F_WRITE | F_DIR;
		fd.record = createFileNode(true);
		dir.nodes[dir.count] = fd;
		dir.count++;
		dir.flush();
	}

	public synchronized InputStream read(File file) throws IOException {
		RSFD fd = getFD(file);
		if ((fd.attrs & F_DIR) != 0) throw new IOException(ERR_IS_DIR+file.path());
		return new RSInputStream(fd);
	}

	public synchronized OutputStream write(File file) throws IOException {
		if (!exists(file)) create(file);
		RSFD fd = getFD(file);
		if ((fd.attrs & F_DIR) != 0) throw new IOException(ERR_IS_DIR+file.path());
		return new RSOutputStream(fd, false);
	}

	public synchronized OutputStream append(File file) throws IOException {
		if (!exists(file)) create(file);
		RSFD fd = getFD(file);
		if ((fd.attrs & F_DIR) != 0) throw new IOException(ERR_IS_DIR+file.path());
		return new RSOutputStream(fd, true);
	}

	public synchronized void remove(File file) throws IOException {
		RSFD parentfd = getFD(file.parent());
		if ((parentfd.attrs & F_DIR) == 0)
			throw new IOException(ERR_NOT_DIR+parentfd.file.path());
		RSDirectory dir = new RSDirectory(parentfd);
		int index = dir.getIndex(file.name());
		if (index < 0) return;
		if ((dir.nodes[index].attrs & F_DIR) != 0) {
			RSDirectory toRemove = new RSDirectory(dir.nodes[index]);
			if (toRemove.count > 0)
				throw new IOException(ERR_NOT_EMPTY+dir.nodes[index].file);
		}
		try {
			store.deleteRecord(dir.nodes[index].record);
		} catch (RecordStoreException rse) {
			throw new IOException(rse.toString());
		}
		dir.count--;
		dir.nodes[index] = dir.nodes[dir.count];
		dir.flush();
	}

	public synchronized String[] list(File file) throws IOException {
		RSFD fd = getFD(file);
		if ((fd.attrs & F_DIR) == 0) throw new IOException(ERR_NOT_DIR+file.path());
		RSDirectory dir = new RSDirectory(fd);
		String[] list = new String[dir.count];
		for (int i=0; i<dir.count; i++) {
			RSFD node = dir.nodes[i];
			list[i] = node.file.name();
			if ((node.attrs & F_DIR) != 0) list[i] = list[i].concat("/");
		}
		return list;
	}

	public synchronized boolean exists(File file) {
		try {
			getFD(file);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public synchronized boolean isDirectory(File file) {
		try {
			RSFD fd = getFD(file);
			return (fd.attrs & F_DIR) != 0;
		} catch (IOException e) {
			return false;
		}
	}

	public synchronized boolean canRead(File file) {
		try {
			RSFD fd = getFD(file);
			return (fd.attrs & F_READ) != 0;
		} catch (IOException e) {
			return false;
		}
	}

	public synchronized boolean canWrite(File file) {
		try {
			RSFD fd = getFD(file);
			return (fd.attrs & F_WRITE) != 0;
		} catch (IOException e) {
			return false;
		}
	}

	public synchronized boolean canExec(File file) {
		try {
			RSFD fd = getFD(file);
			return (fd.attrs & F_EXEC) != 0;
		} catch (IOException e) {
			return false;
		}
	}

	public synchronized void setRead(File file, boolean on) throws IOException {
		RSFD parentfd = getFD(file.parent());
		if ((parentfd.attrs & F_DIR) == 0)
			throw new IOException(ERR_NOT_DIR+parentfd.file.path());
		RSDirectory dir = new RSDirectory(parentfd);
		int index = dir.getIndex(file.name());
		if (index < 0) throw new IOException(ERR_NOT_FOUND+file.path());
		RSFD fd = dir.nodes[index];
		if (on) {
			if ((fd.attrs & F_READ) == 0) {
				fd.attrs |= F_READ;
				dir.flush();
			}
		} else {
			if ((fd.attrs & F_READ) != 0) {
				fd.attrs &= ~F_READ;
				dir.flush();
			}
		}
	}

	public synchronized void setWrite(File file, boolean on) throws IOException {
		RSFD parentfd = getFD(file.parent());
		if ((parentfd.attrs & F_DIR) == 0)
			throw new IOException(ERR_NOT_DIR+parentfd.file.path());
		RSDirectory dir = new RSDirectory(parentfd);
		int index = dir.getIndex(file.name());
		if (index < 0) throw new IOException(ERR_NOT_FOUND+file.path());
		RSFD fd = dir.nodes[index];
		if (on) {
			if ((fd.attrs & F_WRITE) == 0) {
				fd.attrs |= F_WRITE;
				dir.flush();
			}
		} else {
			if ((fd.attrs & F_WRITE) != 0) {
				fd.attrs &= ~F_WRITE;
				dir.flush();
			}
		}
	}

	public synchronized void setExec(File file, boolean on) throws IOException {
		RSFD parentfd = getFD(file.parent());
		if ((parentfd.attrs & F_DIR) == 0)
			throw new IOException(ERR_NOT_DIR+parentfd.file.path());
		RSDirectory dir = new RSDirectory(parentfd);
		int index = dir.getIndex(file.name());
		if (index < 0) throw new IOException(ERR_NOT_FOUND+file.path());
		RSFD fd = dir.nodes[index];
		if (on) {
			if ((fd.attrs & F_EXEC) == 0) {
				fd.attrs |= F_EXEC;
				dir.flush();
			}
		} else {
			if ((fd.attrs & F_EXEC) != 0) {
				fd.attrs &= ~F_EXEC;
				dir.flush();
			}
		}
	}

	public int size(File file) throws IOException {
		RSFD fd = getFD(file);
		try {
			return store.getRecordSize(fd.record)-8;
		} catch (RecordStoreException rse) {
			throw new IOException(rse.toString());
		}
	}

	public long lastModified(File file) throws IOException {
		RSFD fd = getFD(file);
		RSInputStream stream = new RSInputStream(fd);
		long stamp = stream.tstamp();
		stream.close();
		return stamp;
	}

	public long spaceFree() {
		try {
			return store.getSizeAvailable();
		} catch (RecordStoreException rse) {
			return -1;
		}
	}

	public long spaceTotal() {
		return spaceFree() + spaceUsed();
	}

	public long spaceUsed() {
		try {
			return store.getSize();
		} catch (RecordStoreException rse) {
			return -1;
		}
	}

	private RSFD root() {
		RSFD fd = new RSFD();
		fd.record = 1;
		fd.attrs = F_DIR | F_READ | F_WRITE;
		fd.file = new File("");
		return fd;
	}

	private RSFD getFD(File file) throws IOException {
		if (file.path().length() == 0) return root();
		RSFD parent = getFD(file.parent());
		if ((parent.attrs & F_DIR) == 0)
			throw new IOException(ERR_NOT_DIR+parent.file.path());
		RSDirectory dir = new RSDirectory(parent);
		int index = dir.getIndex(file.name());
		if (index < 0) throw new IOException(file.path());
		return dir.nodes[index];
	}

	private int createFileNode(boolean isdir) throws IOException {
		byte[] buf = new byte[12];
		long time = System.currentTimeMillis();
		buf[0] = (byte)(time >> 56);
		buf[1] = (byte)(time >> 48);
		buf[2] = (byte)(time >> 40);
		buf[3] = (byte)(time >> 32);
		buf[4] = (byte)(time >> 24);
		buf[5] = (byte)(time >> 16);
		buf[6] = (byte)(time >> 8);
		buf[7] = (byte)(time);
		try {
			return store.addRecord(buf, 0, isdir ? 12 : 8);
		} catch (RecordStoreException rse) {
			throw new IOException(rse.toString());
		}
	}

	private static class RSFD {
		int record;
		int attrs;
		File file;
	}

	private class RSDirectory {

		private RSFD fd;
		private int count;
		private RSFD[] nodes;

		public RSDirectory(RSFD fd) throws IOException {
			this.fd = fd;
			DataInputStream stream = new DataInputStream(new RSInputStream(fd));
			count = stream.readUnsignedShort();
			nodes = new RSFD[count+1];
			for (int i=0; i<count; i++) {
				nodes[i] = new RSFD();
				nodes[i].record = stream.readInt();
				nodes[i].attrs = stream.readUnsignedByte();
				nodes[i].file = new File(fd.file, stream.readUTF());
			}
			stream.close();
		}

		public int getIndex(String name) {
			int i=count-1;
			while (i >= 0) {
				if (nodes[i].file.name().equals(name)) break;
				i--;
			}
			return i;
		}

		public void flush() throws IOException {
			DataOutputStream stream = new DataOutputStream(new RSOutputStream(fd, false));
			stream.writeShort(count);
			for (int i=0; i<count; i++) {
				stream.writeInt(nodes[i].record);
				stream.writeByte(nodes[i].attrs);
				stream.writeUTF(nodes[i].file.name());
			}
			stream.close();
		}
	}

	private class RSInputStream extends InputStream {

		private RSFD fd;
		private byte[] buf;
		private int mark = 8;
		private int pos = 8;

		public RSInputStream(RSFD fd) throws IOException {
			if ((fd.attrs & F_READ) == 0) throw new IOException(ERR_DENIED+fd.file);
			try {
				buf = store.getRecord(fd.record);
			} catch (RecordStoreException rse) {
				throw new IOException(rse.toString());
			}
		}

		public synchronized int read() throws IOException {
			if (buf == null) throw new IOException(ERR_CLOSED+fd.file);
			if (pos == buf.length) return -1;
			return buf[pos++] & 0xff;
		}

		public synchronized int read(byte[] b, int off, int len) throws IOException {
			if (buf == null) throw new IOException(ERR_CLOSED+fd.file);
			if (pos == buf.length) return -1;
			int reallen = Math.min(len, buf.length-pos);
			System.arraycopy(buf, pos, b, off, reallen);
			pos += reallen;
			return reallen;
		}

		public int available() throws IOException {
			if (buf == null) throw new IOException(ERR_CLOSED+fd.file);
			return buf.length - pos;
		}

		public synchronized void close() {
			buf = null;
		}

		public synchronized void mark(int readlimit) {
			mark = pos;
		}

		public boolean markSupported() {
			return true;
		}

		public synchronized void reset() throws IOException {
			pos = mark;
		}

		public synchronized long skip(long n) throws IOException {
			if (buf == null) throw new IOException(ERR_CLOSED+fd.file);
			if (n <= 0) return 0;
			long realskip = Math.min(n, buf.length-pos);
			pos += realskip;
			return realskip;
		}

		public long tstamp() {
			return (long)buf[0]          << 56 |
			       (long)(buf[1] & 0xff) << 48 |
			       (long)(buf[2] & 0xff) << 40 |
			       (long)(buf[3] & 0xff) << 32 |
			       (long)(buf[4] & 0xff) << 24 |
			       (long)(buf[5] & 0xff) << 16 |
			       (long)(buf[6] & 0xff) <<  8 |
			       (long)(buf[7] & 0xff);
		}
	}

	private class RSOutputStream extends OutputStream {

		private RSFD fd;
		private byte[] buf;
		private int count;

		public RSOutputStream(RSFD fd, boolean append) throws IOException {
			if ((fd.attrs & F_WRITE) == 0) throw new IOException(ERR_DENIED+fd.file);
			this.fd = fd;
			if (append) {
				try {
					count = store.getRecordSize(fd.record);
					buf = new byte[count+128];
					store.getRecord(fd.record, buf, 0);
				} catch (RecordStoreException rse) {
					throw new IOException(rse.toString());
				}
			} else {
				buf = new byte[128];
				count = 8;
			}
		}

		public synchronized void write(int b) throws IOException {
			if (buf == null) throw new IOException(ERR_CLOSED+fd.file);
			if (buf.length == count) grow(0);
			buf[count] = (byte)b;
			count++;
		}

		public synchronized void write(byte[] b, int off, int len) throws IOException {
			if (buf == null) throw new IOException(ERR_CLOSED+fd.file);
			if (count + len >= buf.length) grow(len);
			System.arraycopy(b, off, buf, count, len);
			count += len;
		}

		public synchronized void flush() throws IOException {
			if (buf == null) throw new IOException(ERR_CLOSED+fd.file);
			if ((fd.attrs & F_WRITE) == 0) throw new IOException(ERR_DENIED+fd.file);
			long stamp = System.currentTimeMillis();
			buf[0] = (byte)(stamp >> 56);
			buf[1] = (byte)(stamp >> 48);
			buf[2] = (byte)(stamp >> 40);
			buf[3] = (byte)(stamp >> 32);
			buf[4] = (byte)(stamp >> 24);
			buf[5] = (byte)(stamp >> 16);
			buf[6] = (byte)(stamp >> 8);
			buf[7] = (byte)(stamp);
			try {
				store.setRecord(fd.record, buf, 0, count);
			} catch (RecordStoreException rse) {
				throw new IOException(rse.toString());
			}
		}

		public synchronized void close() {
			if (buf == null) return;
			try {
				flush();
			} catch (IOException ioe) { }
			buf = null;
		}

		private void grow(int needed) {
			byte[] newbuf = new byte[(buf.length << 1) + needed];
			System.arraycopy(buf, 8, newbuf, 8, count-8);
			buf = newbuf;
		}
	}
}
