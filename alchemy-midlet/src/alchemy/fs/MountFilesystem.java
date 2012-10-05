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

package alchemy.fs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * File system returned by FSManager.
 * All methods in this class normalize file names, find
 * the appropriate file system in the mount table and
 * delegate method to the underlying file system.
 * 
 * @author Sergey Basalaev
 */
class MountFilesystem extends Filesystem {

	public MountFilesystem() { }
	
	/** Returns file name in the underlying file system. */
	private String npath(Mount mount, String file) {
		return file.substring(mount.path.length());
	}

	public InputStream read(String file) throws IOException {
		String nfile = FSManager.normalize(file);
		Mount mount = FSManager.findMount(nfile);
		return mount.fs.read(npath(mount, nfile));
	}

	public OutputStream write(String file) throws IOException {
		String nfile = FSManager.normalize(file);
		Mount mount = FSManager.findMount(nfile);
		return mount.fs.write(npath(mount, nfile));
	}
	
	public OutputStream append(String file) throws IOException {
		String nfile = FSManager.normalize(file);
		Mount mount = FSManager.findMount(nfile);
		return mount.fs.append(npath(mount, nfile));
	}

	public boolean canRead(String file) {
		String nfile = FSManager.normalize(file);
		Mount mount = FSManager.findMount(nfile);
		return mount.fs.canRead(npath(mount, nfile));
	}

	public boolean canWrite(String file) {
		String nfile = FSManager.normalize(file);
		Mount mount = FSManager.findMount(nfile);
		return mount.fs.canWrite(npath(mount, nfile));
	}

	public boolean canExec(String file) {
		String nfile = FSManager.normalize(file);
		Mount mount = FSManager.findMount(nfile);
		return mount.fs.canExec(npath(mount, nfile));
	}

	public void setRead(String file, boolean on) throws IOException {
		String nfile = FSManager.normalize(file);
		Mount mount = FSManager.findMount(nfile);
		mount.fs.setRead(npath(mount, nfile), on);
	}

	public void setWrite(String file, boolean on) throws IOException {
		String nfile = FSManager.normalize(file);
		Mount mount = FSManager.findMount(nfile);
		mount.fs.setWrite(npath(mount, nfile), on);
	}

	public void setExec(String file, boolean on) throws IOException {
		String nfile = FSManager.normalize(file);
		Mount mount = FSManager.findMount(nfile);
		mount.fs.setExec(npath(mount, nfile), on);
	}

	public void create(String file) throws IOException {
		String nfile = FSManager.normalize(file);
		Mount mount = FSManager.findMount(nfile);
		mount.fs.create(npath(mount, nfile));
	}

	public void mkdir(String file) throws IOException {
		String nfile = FSManager.normalize(file);
		Mount mount = FSManager.findMount(nfile);
		mount.fs.mkdir(npath(mount, nfile));
	}

	public boolean exists(String file) {
		String nfile = FSManager.normalize(file);
		Mount mount = FSManager.findMount(nfile);
		return mount.fs.exists(npath(mount, nfile));
	}

	public boolean isDirectory(String file) {
		String nfile = FSManager.normalize(file);
		Mount mount = FSManager.findMount(nfile);
		return mount.fs.isDirectory(npath(mount, nfile));
	}

	public String[] list(String file) throws IOException {
		String nfile = FSManager.normalize(file);
		Mount mount = FSManager.findMount(nfile);
		return mount.fs.list(npath(mount, nfile));
	}

	public long lastModified(String file) throws IOException {
		String nfile = FSManager.normalize(file);
		Mount mount = FSManager.findMount(nfile);
		return mount.fs.lastModified(npath(mount, nfile));
	}

	public void remove(String file) throws IOException {
		String nfile = FSManager.normalize(file);
		Mount mount = FSManager.findMount(nfile);
		nfile = npath(mount, nfile);
		if (nfile.length() == 0) throw new IOException("Cannot remove mounted directory");
		mount.fs.remove(npath(mount, nfile));		
	}

	public long size(String file) throws IOException {
		String nfile = FSManager.normalize(file);
		Mount mount = FSManager.findMount(nfile);
		return mount.fs.size(npath(mount, nfile));
	}

	public long spaceTotal(String root) {
		String nfile = FSManager.normalize(root);
		Mount mount = FSManager.findMount(nfile);
		return mount.fs.spaceTotal(npath(mount, nfile));
	}

	public long spaceFree(String root) {
		String nfile = FSManager.normalize(root);
		Mount mount = FSManager.findMount(nfile);
		return mount.fs.spaceFree(npath(mount, nfile));
	}

	public long spaceUsed(String root) {
		String nfile = FSManager.normalize(root);
		Mount mount = FSManager.findMount(nfile);
		return mount.fs.spaceUsed(npath(mount, nfile));
	}
}
