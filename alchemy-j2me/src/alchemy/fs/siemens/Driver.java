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

package alchemy.fs.siemens;

import alchemy.fs.Filesystem;
import alchemy.fs.FSDriver;
import alchemy.util.ArrayList;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import javax.microedition.io.Connector;
import com.siemens.mp.io.file.FileConnection;

/**
 * FS driver for Siemens filesystem.
 *
 * @author Sergey Basalaev
 */
public class Driver extends FSDriver {

	/** Root prefix. */
	private String root;
	
	/** If true then root directory is also device root.
	 * If false, then it is a regular directory.
	 */
	private boolean isSysRoot;

	/**
	 * Constructor to load through the reflection.
	 * <code>init()</code> should be called before
	 * filesystem can be used.
	 */
	public Driver() { }

	/**
	 * Creates new <code>JSR75Filesystem</code> using
	 * given string as root directory.
	 * @param root root directory
	 */
	public void init(String root) {
		String rootpath = Filesystem.normalize(root);
		if (rootpath.length() == 0) throw new RuntimeException("Root is not specified");
		isSysRoot = rootpath.indexOf('/', 1) < 0;
		this.root = "file://"+rootpath;
	}
	
	/**
	 * Converts file path to native path.
	 */
	public String getNativeURL(String file) {
		if (isSysRoot && file.length() == 0) return root+'/';
		else return root+file;
	}

	public OutputStream append(String file) throws IOException {
		FileConnection fc = (FileConnection)Connector.open(getNativeURL(file), Connector.READ_WRITE);
		try {
			if (!fc.exists()) fc.create();
			return fc.openOutputStream(fc.fileSize());
		} finally {
			fc.close();
		}
	}

	public OutputStream write(String file) throws IOException {
		FileConnection fc = (FileConnection)Connector.open(getNativeURL(file), Connector.READ_WRITE);
		try {
			if (!fc.exists()) fc.create();
			fc.truncate(0);
			return fc.openOutputStream();
		} finally {
			fc.close();
		}
	}

	public InputStream read(String file) throws IOException {
		FileConnection fc = (FileConnection)Connector.open(getNativeURL(file), Connector.READ);
		try {
			return fc.openInputStream();
		} finally {
			fc.close();
		}
	}

	public boolean canExec(String file) {
		return true;
	}

	public boolean canRead(String file) {
		if (file.length() == 0) return true;
		try {
			FileConnection fc = (FileConnection)Connector.open(getNativeURL(file), Connector.READ);
			try {
				return fc.canRead();
			} finally {
				fc.close();
			}
		} catch (IOException e) {
			return false;
		}
	}

	public boolean canWrite(String file) {
		if (file.length() == 0) return true;
		try {
			FileConnection fc = (FileConnection)Connector.open(getNativeURL(file), Connector.READ);
			try {
				return fc.canWrite();
			} finally {
				fc.close();
			}
		} catch (IOException e) {
			return false;
		}
	}

	public void create(String file) throws IOException {
		FileConnection fc = (FileConnection)Connector.open(getNativeURL(file), Connector.WRITE);
		try {
			fc.create();
		} finally {
			fc.close();
		}
	}

	public void mkdir(String file) throws IOException {
		FileConnection fc = (FileConnection)Connector.open(getNativeURL(file)+'/', Connector.READ_WRITE);
		try {
			fc.mkdir();
		} finally {
			fc.close();
		}
	}

	public void remove(String file) throws IOException {
		if (file.length() == 0) throw new IOException("Cannot remove mounted directory.");
		FileConnection fc = (FileConnection)Connector.open(getNativeURL(file), Connector.READ_WRITE);
		try {
			if (fc.exists()) fc.delete();
		} finally {
			fc.close();
		}
	}

	public boolean exists(String file) {
		if (file.length() == 0) return true;
		try {
			FileConnection fc = (FileConnection)Connector.open(getNativeURL(file), Connector.READ);
			try {
				return fc.exists();
			} finally {
				fc.close();
			}
		} catch (IOException e) {
			return false;
		}
	}

	public boolean isDirectory(String file) {
		if (file.length() == 0) return true;
		try {
			FileConnection fc = (FileConnection)Connector.open(getNativeURL(file), Connector.READ);
			try {
				return fc.isDirectory();
			} finally {
				fc.close();
			}
		} catch (IOException e) {
			return false;
		}
	}

	public long size(String file) throws IOException {
		FileConnection fc = (FileConnection)Connector.open(getNativeURL(file), Connector.READ);
		try {
			if (fc.isDirectory()) return 0l;
			else return fc.fileSize();
		} finally {
			fc.close();
		}
	}

	public long lastModified(String file) throws IOException {
		FileConnection fc = (FileConnection)Connector.open(getNativeURL(file), Connector.READ);
		try {
			return fc.lastModified();
		} finally {
			fc.close();
		}
	}

	public String[] list(String file) throws IOException {
		FileConnection fc = (FileConnection)Connector.open(getNativeURL(file)+'/', Connector.READ);
		try {
			Enumeration e = fc.list("*", true);
			ArrayList list = new ArrayList();
			while (e.hasMoreElements()) list.add(e.nextElement());
			int size = list.size();
			String[] files = new String[size];
			for (int i=0; i<size; i++) {
				files[i] = (String)list.get(i);
			}
			return files;
		} finally {
			fc.close();
		}
	}

	public void setExec(String file, boolean on) throws IOException {
		return;
	}

	public void setRead(String file, boolean on) throws IOException {
		FileConnection fc = (FileConnection)Connector.open(getNativeURL(file), Connector.READ_WRITE);
		try {
			fc.setReadable(on);
		} finally {
			fc.close();
		}
	}

	public void setWrite(String file, boolean on) throws IOException {
		FileConnection fc = (FileConnection)Connector.open(getNativeURL(file), Connector.READ_WRITE);
		try {
			fc.setWritable(on);
		} finally {
			fc.close();
		}
	}

	public void move(String source, String dest) throws IOException {
		if (source.length() == 0) throw new IOException("Cannot move mounted directory");
		if (exists(dest)) throw new IOException("File already exists: "+dest);
		String sparent = Filesystem.fileParent(source);
		String dparent = Filesystem.fileParent(dest);
		if (sparent.equals(dparent)) {
			FileConnection fc = (FileConnection)Connector.open(getNativeURL(source), Connector.READ_WRITE);
			try {
				fc.rename(Filesystem.fileName(dest));
			} finally {
				fc.close();
			}
		} else {
			super.move(source, dest);
		}
	}

	public long spaceFree() {
		try {
			FileConnection fc = (FileConnection)Connector.open(getNativeURL("")+'/', Connector.READ);
			try {
				return fc.availableSize();
			} finally {
				fc.close();
			}
		} catch (IOException e) {
			return 0l;
		}
	}

	public long spaceTotal() {
		try {
			FileConnection fc = (FileConnection)Connector.open(getNativeURL("")+'/', Connector.READ);
			try {
				return fc.totalSize();
			} finally {
				fc.close();
			}
		} catch (IOException e) {
			return 0l;
		}
	}

	public long spaceUsed() {
		try {
			FileConnection fc = (FileConnection)Connector.open(getNativeURL("")+'/', Connector.READ);
			try {
				return fc.usedSize();
			} finally {
				fc.close();
			}
		} catch (IOException e) {
			return 0l;
		}
	}
}
