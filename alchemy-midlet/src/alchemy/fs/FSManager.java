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

import alchemy.util.Closeable;
import alchemy.util.Initable;
import java.io.IOException;
import java.util.Vector;

/**
 * Manages pool of file systems.
 * <p>
 * File systems are mounted and unmounted using respective methods.
 * 
 * @author Sergey Basalaev
 */
public class FSManager {
	
	private FSManager() { }
	
	private static final Vector mounts = new Vector();
	
	private static final Filesystem filesys = new MountFilesystem();
	
	/** Normalizes given path.
	 * Normalized path is a string of form
	 * <code>/path/to/the/file</code>, i.e. sequence
	 * of names prepended by slashes. Root directory
	 * is denoted by empty string.
	 * <ul>
	 * <li>All sequences of slashes are replaced by single slash.</li>
	 * <li>All entries of form <code>/./</code> and <code>/dir/../</code> are removed.</li>
	 * </ul>
	 */
	public static String normalize(String path) {
		if (path.length() == 0) return path;
		StringBuffer sb = new StringBuffer(path.length()+1);
		int beg = 0;
		while (beg < path.length()) {
			if (path.charAt(beg) == '/') {
				beg++;
			} else {
				int end = path.indexOf('/', beg);
				if (end < 0) end = path.length();
				String name = path.substring(beg, end);
				if (name.equals(".")) {
					// skip this name
				} else if (name.equals("..")) {
					int len = sb.length()-1;
					while (len > 0 && sb.charAt(len) != '/') len--;
					sb.setLength(len);
				} else {
					sb.append('/').append(name);
				}
				beg = end+1;
			}
		}
		return sb.toString();
	}
	
	/**
	 * Mounts specified file system to given directory.
	 * File system class is determined as
	 * <pre>alchemy.fs.${type}.FS</pre>
	 */
	public static void mount(String dir, String type, String options) throws IOException {
		String path = normalize(dir);
		try {
			Class fsclass = Class.forName("alchemy.fs."+type+".FS");
			Filesystem fs = (Filesystem)fsclass.newInstance();
			if (fs instanceof Initable) ((Initable)fs).init(options);
			synchronized (mounts) {
				int index = mounts.size()-1;
				while (index > 0) {
					Mount mount = (Mount) mounts.elementAt(index);
					if (mount.path.compareTo(path) <= 0) break;
					index--;
				}
				mounts.insertElementAt(new Mount(path, fs), index+1);
			}
		} catch (ClassNotFoundException cnfe) {
			throw new IOException("FS driver not found: "+type);
		} catch (Exception e) {
			throw new IOException("Failed to load FS driver: "+type+"\nCause: "+e);
		}
	}
	
	/**
	 * Unmounts specified file system.
	 * 
	 * @return
	 *   <code>true</code> if file system was successfully
	 *   unmounted, <code>false</code> if it was not attached
	 */
	public static boolean umount(String dir) {
		String path = normalize(dir);
		synchronized (mounts) {
			for (int i=mounts.size(); i >= 0; i--) {
				Mount m = (Mount) mounts.elementAt(i);
				if (m.path.equals(path)) {
					mounts.removeElementAt(i);
					if (m.fs instanceof Closeable) {
						((Closeable) m.fs).close();
					}
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns file system to work with.
	 */
	public static Filesystem fs() {
		return filesys;
	}
	
	/** Closes all file systems. */
	public static void umountAll() {
		synchronized (mounts) {
			for (int i=mounts.size()-1; i >= 0; i--) {
				Mount mount = (Mount) mounts.elementAt(i);
				Filesystem fs = mount.fs;
				if (fs instanceof Closeable) ((Closeable)fs).close();
			}
			mounts.setSize(0);
		}
	}
	
	/**
	 * Used by MountFilesystem.
	 * Returns filesystem on which corresponding file is mounted.
	 */
	static Mount findMount(String file) {
		synchronized (mounts) {
			for (int i=mounts.size()-1; i >= 0; i--) {
				Mount mount = (Mount) mounts.elementAt(i);
				if (file.startsWith(mount.path)) return mount;
			}
		}
		// if root is mounted, we should never reach this string
		return null;
	}
}
