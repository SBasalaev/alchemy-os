/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011-2012, Sergey Basalaev <sbasalaev@gmail.com>
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
import alchemy.util.Closeable;
import alchemy.util.Initable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;

/**
 * Filesystem implementation based on a record storage system.
 * This filesystem is suitable for all MIDP devices, but is
 * rather slow.
 * @author Sergey Basalaev
 */
public final class FS extends Filesystem implements Initable, Closeable {

	// file attributes
	static private int A_DIR = 16;
	static private int A_READ = 4;
	static private int A_WRITE = 2;
	static private int A_EXEC = 1;

	/** <code>RecordStore</code> to use as filesystem. */
	private RecordStore store;

	/** File descriptor for the root directory. */
	private final FD rootFD;
	
	/**
	 * Cache of file descriptors.
	 * Maps file name to descriptor.
	 */
	private Hashtable fdCache = new Hashtable();
	
	/**
	 * Constructor to load through the reflection.
	 * <code>init()</code> should be called before
	 * filesystem can be used.
	 */
	public FS() {
		FD fd = new FD();
		fd.name = "/";
		fd.record = 1;
		fd.attrs = A_READ | A_WRITE | A_DIR;
		rootFD = fd;
	}

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
				rs.addRecord(new byte[12], 0, 12);
			}
		} catch (RecordStoreException rse) {
			throw new RuntimeException(rse.toString());
		}
		this.store = rs;
	}

	public void close() {
		try { store.closeRecordStore(); }
		catch (RecordStoreException rse) { }
	}
	
	public String[] listRoots() {
		return new String[] {"/"};
	}

	public synchronized void create(File file) throws IOException {
		File parent = file.parent();
		if (parent == null) throw new IOException("File already exists: /");
		FD parentfd = getFD(parent);
		if ((parentfd.attrs & A_DIR) == 0) throw new IOException("Not a directory: "+parent);
		Directory dir = new Directory(parentfd);
		int index = dir.getIndex(file.name());
		if (index >= 0) throw new IOException("File already exists: "+file);
		FD fd = new FD();
		fd.name = file.name();
		fd.attrs = A_READ | A_WRITE;
		fd.record = createFileNode(false);
		dir.nodes[dir.count] = fd;
		dir.count++;
		dir.flush();
		fdCache.put(file, fd);
	}

	public synchronized void mkdir(File file) throws IOException {
		File parent = file.parent();
		if (parent == null) throw new IOException("File already exists: /");
		FD parentfd = getFD(parent);
		if ((parentfd.attrs & A_DIR) == 0) throw new IOException("Not a directory: "+parent);
		Directory dir = new Directory(parentfd);
		int index = dir.getIndex(file.name());
		if (index >= 0) throw new IOException("File already exists: "+file);
		FD fd = new FD();
		fd.name = file.name();
		fd.attrs = A_READ | A_WRITE | A_DIR;
		fd.record = createFileNode(true);
		dir.nodes[dir.count] = fd;
		dir.count++;
		dir.flush();
		fdCache.put(file, fd);
	}

	public synchronized InputStream read(File file) throws IOException {
		FD fd = getFD(file);
		if ((fd.attrs & A_DIR) != 0) throw new IOException("Is a directory: "+file);
		return new FileInputStream(fd);
	}

	public synchronized OutputStream write(File file) throws IOException {
		if (!exists(file)) create(file);
		FD fd = getFD(file);
		if ((fd.attrs & A_DIR) != 0) throw new IOException("Is a directory: "+file);
		return new FileOutputStream(fd, false);
	}

	public synchronized OutputStream append(File file) throws IOException {
		if (!exists(file)) create(file);
		FD fd = getFD(file);
		if ((fd.attrs & A_DIR) != 0) throw new IOException("Is a directory: "+file);
		return new FileOutputStream(fd, true);
	}

	public synchronized void remove(File file) throws IOException {
		File parent = file.parent();
		if (parent == null) throw new IOException("Cannot remove /");
		FD parentfd = getFD(parent);
		if ((parentfd.attrs & A_DIR) == 0) throw new IOException("Not a directory: "+parent);
		Directory dir = new Directory(parentfd);
		int index = dir.getIndex(file.name());
		if (index < 0) return;
		if ((dir.nodes[index].attrs & A_DIR) != 0) {
			Directory toRemove = new Directory(dir.nodes[index]);
			if (toRemove.count > 0) throw new IOException("Cannot remove non-empty directory: "+file);
		}
		try {
			store.deleteRecord(dir.nodes[index].record);
		} catch (RecordStoreException rse) {
			throw new IOException(rse.toString());
		}
		dir.count--;
		dir.nodes[index] = dir.nodes[dir.count];
		dir.flush();
		fdCache.remove(file);
	}

	public synchronized String[] list(File file) throws IOException {
		FD fd = getFD(file);
		if ((fd.attrs & A_DIR) == 0) throw new IOException("Not a directory: "+file);
		Directory dir = new Directory(fd);
		String[] list = new String[dir.count];
		for (int i=0; i<dir.count; i++) {
			FD node = dir.nodes[i];
			list[i] = node.name;
			if ((node.attrs & A_DIR) != 0) list[i] = list[i].concat("/");
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
			FD fd = getFD(file);
			return (fd.attrs & A_DIR) != 0;
		} catch (IOException e) {
			return false;
		}
	}

	public synchronized boolean canRead(File file) {
		try {
			FD fd = getFD(file);
			return (fd.attrs & A_READ) != 0;
		} catch (IOException e) {
			return false;
		}
	}

	public synchronized boolean canWrite(File file) {
		try {
			FD fd = getFD(file);
			return (fd.attrs & A_WRITE) != 0;
		} catch (IOException e) {
			return false;
		}
	}

	public synchronized boolean canExec(File file) {
		try {
			FD fd = getFD(file);
			return (fd.attrs & A_EXEC) != 0;
		} catch (IOException e) {
			return false;
		}
	}

	public synchronized void setRead(File file, boolean on) throws IOException {
		File parent = file.parent();
		if (parent == null) throw new IOException("Cannot change attrubutes of /");
		FD parentfd = getFD(parent);
		if ((parentfd.attrs & A_DIR) == 0) throw new IOException("Not a directory: "+parent);
		Directory dir = new Directory(parentfd);
		int index = dir.getIndex(file.name());
		if (index < 0) throw new IOException("File not found: "+file);
		FD fd = dir.nodes[index];
		if (on) {
			if ((fd.attrs & A_READ) == 0) {
				fd.attrs |= A_READ;
				dir.flush();
				fdCache.put(file, fd);
			}
		} else {
			if ((fd.attrs & A_READ) != 0) {
				fd.attrs &= ~A_READ;
				dir.flush();
				fdCache.put(file, fd);
			}
		}
	}

	public synchronized void setWrite(File file, boolean on) throws IOException {
		File parent = file.parent();
		if (parent == null) throw new IOException("Cannot change attrubutes of /");
		FD parentfd = getFD(parent);
		if ((parentfd.attrs & A_DIR) == 0) throw new IOException("Not a directory: "+parent);
		Directory dir = new Directory(parentfd);
		int index = dir.getIndex(file.name());
		if (index < 0) throw new IOException("File not found: "+file);
		FD fd = dir.nodes[index];
		if (on) {
			if ((fd.attrs & A_WRITE) == 0) {
				fd.attrs |= A_WRITE;
				dir.flush();
				fdCache.put(file, fd);
			}
		} else {
			if ((fd.attrs & A_WRITE) != 0) {
				fd.attrs &= ~A_WRITE;
				dir.flush();
				fdCache.put(file, fd);
			}
		}
	}

	public synchronized void setExec(File file, boolean on) throws IOException {
		File parent = file.parent();
		if (parent == null) throw new IOException("Cannot change attrubutes of /");
		FD parentfd = getFD(parent);
		if ((parentfd.attrs & A_DIR) == 0) throw new IOException("Not a directory: "+parent);
		Directory dir = new Directory(parentfd);
		int index = dir.getIndex(file.name());
		if (index < 0) throw new IOException("File not found: "+file);
		FD fd = dir.nodes[index];
		if (on) {
			if ((fd.attrs & A_EXEC) == 0) {
				fd.attrs |= A_EXEC;
				dir.flush();
				fdCache.put(file, fd);
			}
		} else {
			if ((fd.attrs & A_EXEC) != 0) {
				fd.attrs &= ~A_EXEC;
				dir.flush();
				fdCache.put(file, fd);
			}
		}
	}

	public synchronized void move(File source, File dest) throws IOException {
		File srcparent = source.parent();
		if (srcparent == null) throw new IOException("Cannot move /");
		if (exists(dest)) throw new IOException("Cannot move "+source+" to "+dest+", destination exists.");
		if (srcparent.equals(dest.parent())) {
			// rename within one directory
			FD parentfd = getFD(srcparent);
			Directory parentdir = new Directory(parentfd);
			int index = parentdir.getIndex(source.name());
			if (index < 0) throw new IOException("File not found: "+source);
			FD destfd = parentdir.nodes[index];
			destfd.name = dest.name();
			parentdir.flush();
			// apply changes to cache
			fdCache.remove(source);
			fdCache.put(dest, destfd);
		} else {
			// read both dirs
			FD destdirfd = getFD(dest.parent());
			Directory destdir = new Directory(destdirfd);
			FD srcdirfd = getFD(source.parent());
			Directory srcdir = new Directory(srcdirfd);
			int index = srcdir.getIndex(source.name());
			if (index < 0) throw new IOException("File not found: "+source);
			// move / rename
			FD destfd = srcdir.nodes[index];
			destfd.name = dest.name();
			destdir.nodes[destdir.count] = destfd;
			destdir.count++;
			srcdir.count--;
			srcdir.nodes[index] = srcdir.nodes[srcdir.count];
			destdir.flush();
			srcdir.flush();
			// apply changes to cache
			fdCache.remove(source);
			fdCache.put(dest, destfd);
		}
	}
	
	public int size(File file) throws IOException {
		FD fd = getFD(file);
		try {
			return store.getRecordSize(fd.record)-8;
		} catch (RecordStoreException rse) {
			throw new IOException(rse.toString());
		}
	}

	public long lastModified(File file) throws IOException {
		FD fd = getFD(file);
		FileInputStream stream = new FileInputStream(fd);
		long stamp = stream.timeStamp();
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
		long free = spaceFree();
		long used = spaceUsed();
		if (free < 0 || used < 0) return -1;
		else return free + used;
	}

	public long spaceUsed() {
		try {
			return store.getSize();
		} catch (RecordStoreException rse) {
			return -1;
		}
	}

	private FD getFD(File file) throws IOException {
		File parent = file.parent();
		if (parent == null) return rootFD;
		FD parentfd = getFD(parent);
		if ((parentfd.attrs & A_DIR) == 0) throw new IOException("Not a directory: "+parent);
		if ((parentfd.attrs & A_READ) == 0) throw new IOException("Access denied to "+parent);
		FD fd = (FD)fdCache.get(file);
		if (fd != null) {
			return fd;
		} else {
			Directory dir = new Directory(parentfd);
			int index = dir.getIndex(file.name());
			if (index < 0) throw new IOException("File not found: "+file);
			fd = dir.nodes[index];
			fdCache.put(file, fd);
			return fd;
		}
	}

	private int createFileNode(boolean isDir) throws IOException {
		byte[] buf = new byte[12];
		long time = System.currentTimeMillis();
		for (int i=7; i>=0; i--) {
			buf[i] = (byte)time;
			time >>>= 8;
		}
		try {
			return store.addRecord(buf, 0, isDir ? 12 : 8);
		} catch (RecordStoreFullException rsfe) {
			throw new IOException("Cannot create file, no more free space.");
		} catch (RecordStoreException rse) {
			throw new IOException(rse.toString());
		}
	}

	/** File descriptor */
	private static class FD {
		int record;
		int attrs;
		String name;
	}

	private class Directory {

		private FD fd;
		private int count;
		private FD[] nodes;

		public Directory(FD fd) throws IOException {
			this.fd = fd;
			DataInputStream stream = new DataInputStream(new FileInputStream(fd));
			count = stream.readUnsignedShort();
			nodes = new FD[count+1];
			for (int i=0; i<count; i++) {
				nodes[i] = new FD();
				nodes[i].record = stream.readInt();
				nodes[i].attrs = stream.readUnsignedByte();
				nodes[i].name = stream.readUTF();
				//TODO: optimize here
			}
			stream.close();
		}

		public int getIndex(String name) {
			for (int i=count-1; i>=0; i--) {
				if (nodes[i].name.equals(name)) return i;
			}
			return -1;
		}

		public void flush() throws IOException {
			DataOutputStream stream = new DataOutputStream(new FileOutputStream(fd, false));
			stream.writeShort(count);
			for (int i=0; i<count; i++) {
				stream.writeInt(nodes[i].record);
				stream.writeByte(nodes[i].attrs);
				stream.writeUTF(nodes[i].name);
				// TODO: optimize here
			}
			stream.close();
		}
	}

	private class FileInputStream extends InputStream {

		private FD fd;
		private byte[] buf;
		private int mark = 8;
		private int pos = 8;

		public FileInputStream(FD fd) throws IOException {
			if ((fd.attrs & A_READ) == 0) throw new IOException("Access denied to "+fd.name);
			try {
				buf = store.getRecord(fd.record);
			} catch (RecordStoreException rse) {
				throw new IOException(rse.toString());
			}
		}

		public synchronized int read() throws IOException {
			if (buf == null) throw new IOException("Stream is closed");
			if (pos == buf.length) return -1;
			return buf[pos++] & 0xff;
		}

		public synchronized int read(byte[] b, int off, int len) throws IOException {
			if (buf == null) throw new IOException("Stream is closed");
			if (pos == buf.length) return -1;
			if (off < 0 || len < 0 || off+len > b.length) throw new ArrayIndexOutOfBoundsException();
			if (len == 0) return 0;
			int reallen = buf.length-pos;
			if (reallen == 0) return -1;
			if (reallen > len) reallen = len;
			System.arraycopy(buf, pos, b, off, reallen);
			pos += reallen;
			return reallen;
		}

		public int available() throws IOException {
			if (buf == null) throw new IOException("Stream is closed");
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

		public synchronized void reset() {
			pos = mark;
		}

		public synchronized long skip(long n) throws IOException {
			if (buf == null) throw new IOException("Stream is closed");
			if (n <= 0) return 0;
			long realskip = buf.length-pos;
			if (realskip > n) realskip = n;
			pos += realskip;
			return realskip;
		}

		public long timeStamp() {
			long time = 0;
			for (int i=0; i<8; i++) {
				time = (time << 8) | (buf[i] & 0xff);
			}
			return time;
		}
	}

	private class FileOutputStream extends OutputStream {

		private final FD fd;
		private byte[] buf;
		private int count;
		private boolean modified;

		public FileOutputStream(FD fd, boolean append) throws IOException {
			if ((fd.attrs & A_WRITE) == 0) throw new IOException("Access denied to "+fd.name);
			this.fd = fd;
			if (append) {
				try {
					count = store.getRecordSize(fd.record);
					buf = new byte[count+32];
					store.getRecord(fd.record, buf, 0);
					modified = false;
				} catch (RecordStoreException rse) {
					throw new IOException(rse.toString());
				}
			} else {
				buf = new byte[40];
				count = 8;
				modified = true;
			}
		}

		public synchronized void write(int b) throws IOException {
			if (buf == null) throw new IOException("Stream is closed");
			if (buf.length == count) grow(count << 1);
			buf[count] = (byte)b;
			count++;
			modified = true;
		}

		public synchronized void write(byte[] b, int off, int len) throws IOException {
			if (buf == null) throw new IOException("Stream is closed");
			if (off < 0 || len < 0 || off+len > b.length) throw new ArrayIndexOutOfBoundsException();
			if (buf.length < count+len) grow(buf.length+len);
			System.arraycopy(b, off, buf, count, len);
			count += len;
			modified = true;
		}

		public synchronized void flush() throws IOException {
			if (buf == null) throw new IOException("Stream is closed");
			if ((fd.attrs & A_WRITE) == 0) throw new IOException("Access denied to "+fd.name);
			if (modified) {
				long stamp = System.currentTimeMillis();
				for (int i=7; i >= 0; i--) {
					buf[i] = (byte)(stamp);
					stamp >>>= 8;
				}
				try {
					synchronized (FS.this) {
						store.setRecord(fd.record, buf, 0, count);
					}
				} catch (RecordStoreException rse) {
					throw new IOException(rse.toString());
				}
				modified = false;
			}
		}

		public synchronized void close() {
			if (buf == null) return;
			try {
				flush();
			} catch (IOException ioe) { }
			buf = null;
		}

		private void grow(int len) {
			byte[] newbuf = new byte[len];
			System.arraycopy(buf, 8, newbuf, 8, count-8);
			buf = newbuf;
		}
	}
}
