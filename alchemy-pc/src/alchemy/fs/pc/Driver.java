/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2013, Sergey Basalaev <sbasalaev@gmail.com>
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

package alchemy.fs.pc;

import alchemy.fs.FSDriver;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Filesystem driver for PC version of Alchemy OS.
 *
 * @author Sergey Basalaev
 */
public final class Driver extends FSDriver {
	
	private File root;
	
	public Driver() { }

	@Override
	public void init(String cfg) throws IOException {
		root = new File(cfg);
		if (!root.isDirectory())
			throw new IOException("Not a directory: " + root);
	}

	@Override
	public InputStream read(String file) throws IOException {
		return new BufferedInputStream(new FileInputStream(new File(root, file)));
	}

	@Override
	public OutputStream write(String file) throws IOException {
		return new BufferedOutputStream(new FileOutputStream(new File(root, file)));
	}

	@Override
	public OutputStream append(String file) throws IOException {
		return new BufferedOutputStream(new FileOutputStream(new File(root, file), true));
	}

	@Override
	public boolean canRead(String file) {
		return new File(root, file).canRead();
	}

	@Override
	public boolean canWrite(String file) {
		return new File(root, file).canWrite();
	}

	@Override
	public boolean canExec(String file) {
		return new File(root, file).canExecute();
	}

	@Override
	public void setRead(String file, boolean on) throws IOException {
		new File(root, file).setReadable(on);
	}

	@Override
	public void setWrite(String file, boolean on) throws IOException {
		new File(root, file).setWritable(on);
	}

	@Override
	public void setExec(String file, boolean on) throws IOException {
		new File(root, file).setExecutable(on);
	}

	@Override
	public long size(String file) throws IOException {
		return new File(root, file).length();
	}

	@Override
	public long lastModified(String file) throws IOException {
		return new File(root, file).lastModified();
	}

	@Override
	public void create(String file) throws IOException {
		if (!new File(root, file).createNewFile())
			throw new IOException("File already exists: " + file);
	}

	@Override
	public void mkdir(String file) throws IOException {
		if (!new File(root, file).mkdir())
			throw new IOException("Failed to create directory: " + file);
	}

	@Override
	public String[] list(String file) throws IOException {
		File dir = new File(root, file);
		String[] list = dir.list();
		for (int i=0; i<list.length; i++) {
			if (new File(dir, list[i]).isDirectory() && !list[i].endsWith("/")) {
				list[i] += "/";
			}
		}
		return list;
	}

	@Override
	public void remove(String file) throws IOException {
		if (!new File(root, file).delete())
			throw new IOException("Failed to remove file: " + file);
	}

	@Override
	public String getNativeURL(String path) {
		return new File(root, path).toURI().toString();
	}

	@Override
	public boolean exists(String file) {
		return new File(root, file).exists();
	}

	@Override
	public boolean isDirectory(String file) {
		return new File(root, file).isDirectory();
	}

	@Override
	public void move(String source, String dest) throws IOException {
		if (!new File(root, source).renameTo(new File(root, dest)))
			throw new IOException("Failed to move " + source + " to " + dest);
	}

	@Override
	public long spaceFree() {
		return root.getFreeSpace();
	}

	@Override
	public long spaceTotal() {
		return root.getTotalSpace();
	}

	@Override
	public long spaceUsed() {
		return spaceTotal() - spaceFree();
	}
}
