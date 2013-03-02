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

package alchemy.fs;

import alchemy.util.Closeable;
import alchemy.util.Initable;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Manages pool of file systems.
 * <p>
 * File systems are mounted and unmounted using respective methods.
 * 
 * @author Sergey Basalaev
 */
public class FSManager {
	
	private FSManager() { }
	
	/** Maps dirs to mounts. */
	private static final Hashtable mounted = new Hashtable();
	
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
	 * Mounts specified file system to the given directory.
	 * File system class is determined as
	 * <pre>alchemy.fs.${type}.FS</pre>
	 */
	public static void mount(String dir, String type, String options) throws IOException {
		String path = normalize(dir);
		try {
			Class fsclass = Class.forName("alchemy.fs."+type+".FS");
			Filesystem fs = (Filesystem)fsclass.newInstance();
			if (fs instanceof Initable) ((Initable)fs).init(options);
			Mount oldmount = (Mount) mounted.put(path, new Mount (path, fs));
			if (oldmount != null && oldmount.fs instanceof Closeable)
				((Closeable)oldmount.fs).close();
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
		Mount oldmount = (Mount) mounted.remove(path);
		if (oldmount != null && oldmount.fs instanceof Closeable)
			((Closeable)oldmount.fs).close();
		return oldmount != null;
	}
	
	/**
	 * Returns file system to work with.
	 */
	public static Filesystem fs() {
		return filesys;
	}
	
	/** Closes all file systems. */
	public static synchronized void umountAll() {
		for (Enumeration e = mounted.elements(); e.hasMoreElements(); ) {
			Mount mount = (Mount) e.nextElement();
			if (mount.fs instanceof Closeable) ((Closeable)mount.fs).close();
		}
		mounted.clear();
	}
	
	/**
	 * Used by MountFilesystem.
	 * Returns filesystem on which corresponding file is mounted.
	 * File must be normalized.
	 */
	static synchronized Mount findMount(String file) {
		Object ret = null;
		while ((ret = mounted.get(file)) == null && file != null) {
			file = Filesystem.fparent(file);
		}
		return (Mount) ret;
	}
}
