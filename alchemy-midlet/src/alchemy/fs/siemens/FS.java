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

package alchemy.fs.siemens;

import alchemy.fs.File;
import alchemy.fs.Filesystem;
import alchemy.util.Initable;
import com.siemens.mp.io.file.FileConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;

/**
 * Siemens file system.
 * @author Sergey Basalaev
 */
public final class FS extends Filesystem implements Initable {

	private String root;

	public FS() { }

	public void init(Object root) {
		this.root = "file:///"+String.valueOf(root);
	}


	public OutputStream append(File file) throws IOException {
		FileConnection fc = (FileConnection)Connector.open(root+file.path(), Connector.READ_WRITE);
		try {
			if (!fc.exists()) fc.create();
			return fc.openOutputStream(fc.fileSize());
		} finally {
			fc.close();
		}
	}

	public OutputStream write(File file) throws IOException {
		FileConnection fc = (FileConnection)Connector.open(root+file.path(), Connector.READ_WRITE);
		try {
			if (!fc.exists()) fc.create();
			fc.truncate(0);
			return fc.openOutputStream();
		} finally {
			fc.close();
		}
	}

	public InputStream read(File file) throws IOException {
		FileConnection fc = (FileConnection)Connector.open(root+file.path(), Connector.READ);
		try {
			return new SiemensInputStream(fc.openInputStream());
		} finally {
			fc.close();
		}
	}

	public boolean canExec(File file) {
		return true;
	}

	public boolean canRead(File file) {
		try {
			FileConnection fc = (FileConnection)Connector.open(root+file.path(), Connector.READ);
			try {
				return fc.canRead();
			} finally {
				fc.close();
			}
		} catch (IOException e) {
			return false;
		}
	}

	public boolean canWrite(File file) {
		try {
			FileConnection fc = (FileConnection)Connector.open(root+file.path(), Connector.READ);
			try {
				return fc.canWrite();
			} finally {
				fc.close();
			}
		} catch (IOException e) {
			return false;
		}
	}

	public void create(File file) throws IOException {
		FileConnection fc = (FileConnection)Connector.open(root+file.path(), Connector.WRITE);
		try {
			fc.create();
		} finally {
			fc.close();
		}
	}

	public void mkdir(File file) throws IOException {
		FileConnection fc = (FileConnection)Connector.open(root+file.path()+'/', Connector.READ_WRITE);
		try {
			fc.mkdir();
		} finally {
			fc.close();
		}
	}

	public void remove(File file) throws IOException {
		FileConnection fc = (FileConnection)Connector.open(root+file.path(), Connector.READ_WRITE);
		try {
			if (file.path().length() == 0) throw new SecurityException("Cannot delete root directory.");
			if (fc.exists()) fc.delete();
		} finally {
			fc.close();
		}
	}

	public boolean exists(File file) {
		try {
			FileConnection fc = (FileConnection)Connector.open(root+file.path(), Connector.READ);
			try {
				return fc.exists();
			} finally {
				fc.close();
			}
		} catch (IOException e) {
			return false;
		}
	}

	public boolean isDirectory(File file) {
		try {
			FileConnection fc = (FileConnection)Connector.open(root+file.path(), Connector.READ);
			try {
				return fc.isDirectory();
			} finally {
				fc.close();
			}
		} catch (IOException e) {
			return false;
		}
	}

	public int size(File file) throws IOException {
		FileConnection fc = (FileConnection)Connector.open(root+file.path(), Connector.READ);
		try {
			if (fc.isDirectory()) return 0;
			else {
				long size = fc.fileSize();
				if (size > Integer.MAX_VALUE) return Integer.MAX_VALUE;
				else return (int)size;
			}
		} finally {
			fc.close();
		}
	}

	public long lastModified(File file) throws IOException {
		FileConnection fc = (FileConnection)Connector.open(root+file.path(), Connector.READ);
		try {
			return fc.lastModified();
		} finally {
			fc.close();
		}
	}

	public String[] list(File file) throws IOException {
		FileConnection fc = (FileConnection)Connector.open(root+file.path(), Connector.READ);
		try {
			Enumeration e = fc.list("*", true);
			Vector v = new Vector();
			while (e.hasMoreElements()) v.addElement(e.nextElement());
			int size = v.size();
			String[] files = new String[size];
			for (int i=0; i<size; i++) {
				files[i] = (String)v.elementAt(i);
			}
			return files;
		} finally {
			fc.close();
		}
	}

	public void setExec(File file, boolean on) throws IOException {
		return;
	}

	public void setRead(File file, boolean on) throws IOException {
		FileConnection fc = (FileConnection)Connector.open(root+file.path(), Connector.READ_WRITE);
		try {
			fc.setReadable(on);
		} finally {
			fc.close();
		}
	}

	public void setWrite(File file, boolean on) throws IOException {
		FileConnection fc = (FileConnection)Connector.open(root+file.path(), Connector.READ_WRITE);
		try {
			fc.setWritable(on);
		} finally {
			fc.close();
		}
	}

	public long spaceFree() {
		try {
			FileConnection fc = (FileConnection)Connector.open(root, Connector.READ);
			try {
				return fc.availableSize();
			} finally {
				fc.close();
			}
		} catch (IOException e) {
			return -1l;
		}
	}

	public long spaceTotal() {
		try {
			FileConnection fc = (FileConnection)Connector.open(root, Connector.READ);
			try {
				return fc.totalSize();
			} finally {
				fc.close();
			}
		} catch (IOException e) {
			return -1l;
		}
	}

	public long spaceUsed() {
		try {
			FileConnection fc = (FileConnection)Connector.open(root, Connector.READ);
			try {
				return fc.usedSize();
			} finally {
				fc.close();
			}
		} catch (IOException e) {
			return -1l;
		}
	}
}
