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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * File system management for Alchemy OS.
 * 
 * @author Sergey Basalaev
 */
public final class Filesystem {
	
	private Filesystem() { }
	
	/** Maps directory names to mounts. */
	private static final Hashtable mounts = new Hashtable();
	
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
	 * Returns name part of this path.
	 * File name is a part after the last slash.
	 */
	public static String fileName(String path) {
		if (path.length() == 0) return path;
		int lastslash = path.lastIndexOf('/');
		if (lastslash >= 0) return path.substring(lastslash + 1);
		return path;
	}

	/**
	 * Returns directory part of this path.
	 * Directory name is a part before the last slash.
	 * If the name does not contain a slash this method
	 * returns <code>null</code>.
	 */
	public static String fileParent(String file) {
		int lastslash = file.lastIndexOf('/');
		if (lastslash < 0) return null;
		return file.substring(0, lastslash);
	}
		
	/**
	 * Returns file system on which corresponding file is mounted.
	 * File must be normalized.
	 */
	static synchronized Mount findMount(String file) {
		for ( ; file != null; file = fileParent(file)) {
			Object mount = mounts.get(file);
			if (mount != null) return (Mount)mount;
		}
		return null;
	}

	/**
	 * Mounts specified file system to the given directory.
	 * File system class is determined as
	 * <pre>alchemy.fs.${type}.Driver</pre>
	 * This method makes no checks on <code>dir</code> argument,
	 * because it is also used to mount root directory.
	 *
	 * @throws IOException
	 *   if the file system fails to initialize
	 */
	public static synchronized void mount(String dir, String type, String options) throws IOException {
		String path = normalize(dir);
		try {
			Class fsclass = Class.forName("alchemy.fs."+type+".Driver");
			FSDriver fs = (FSDriver)fsclass.newInstance();
			fs.init(options);
			Mount oldmount = (Mount) mounts.put(path, new Mount(path, fs));
			if (oldmount != null) oldmount.driver.close();
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
	public static synchronized boolean unmount(String dir) {
		String path = normalize(dir);
		Mount oldmount = (Mount) mounts.remove(path);
		if (oldmount != null) {
			oldmount.driver.close();
			return true;
		} else {
			return false;
		}
	}
	
	/** Unmounts and finalizes all file systems. */
	public static synchronized void unmountAll() {
		for (Enumeration e = mounts.elements(); e.hasMoreElements(); ) {
			Mount mount = (Mount) e.nextElement();
			mount.driver.close();
		}
		mounts.clear();
	}

	/**
	 * Returns a stream to read from the file.
	 *
	 * @param file a file to read from
	 * @return <code>InputStream</code> instance
	 * @throws IOException
	 *   if file does not exist, is a directory or an I/O error occurs
	 * @throws SecurityException
	 *   if system denies read access to the file
	 */
	public static InputStream read(String file) throws IOException {
		file = normalize(file);
		Mount mount = findMount(file);
		return mount.driver.read(file.substring(mount.pathlen));
	}
	
	/**
	 * Returns a stream to write to the file.
	 * Contents of the file becomes overwritten.
	 * If file does not exist it is created.
	 *
	 * @param file a file to write to
	 * @return <code>OutputStream</code> instance
	 * @throws IOException
	 *   if file cannot be created, is a directory or an I/O error occurs
	 * @throws SecurityException
	 *   if system denies write access to the file
	 * @see #append(String)
	 */
	public static OutputStream write(String file) throws IOException {
		file = normalize(file);
		Mount mount = findMount(file);
		return mount.driver.write(file.substring(mount.pathlen));
	}

	/**
	 * Returns a stream to write to the file.
	 * New contents is added to the end of the file.
	 * If file does not exist it is created.
	 *
	 * @param file a file to write to
	 * @return <code>OutputStream</code> instance
	 * @throws IOException
	 *   if file cannot be created, is a directory or an I/O error occurs
	 * @throws SecurityException
	 *   if system denies write access to the file
	 * @see #write(String)
	 */
	public static OutputStream append(String file) throws IOException {
		file = normalize(file);
		Mount mount = findMount(file);
		return mount.driver.append(file.substring(mount.pathlen));
	}

	/**
	 * Lists file names that the specified directory contains.
	 * The pathnames ".." and "." are not included in the
	 * output. All directory names end with '/' character.
	 * If the directory is empty then an empty array is returned.
	 * 
	 * @param file a directory to list
	 * @return
	 *   array of file names the specified directory contains
	 * @throws IOException
	 *   if file does not exist, is not a directory or an I/O error occurs
	 * @throws SecurityException
	 *   if system denies read access to the file
	 */
	public static String[] list(String file) throws IOException {
		file = normalize(file);
		Mount mount = findMount(file);
		return mount.driver.list(file.substring(mount.pathlen));
	}
	
	/**
	 * Tests whether the specified file exists.
	 * If an I/O error occurs this method
	 * returns <code>false</code>.
	 *
	 * @param file a file to test
	 * @return <code>true</code> if file exists,
	 *         <code>false</code> otherwise
	 * @throws SecurityException
	 *   if system denies read access to the directory containing this file
	 */
	public static boolean exists(String file) {
		file = normalize(file);
		Mount mount = findMount(file);
		return mount.driver.exists(file.substring(mount.pathlen));
	}
	
	/**
	 * Tests whether the specified file exists and a directory.
	 * If an I/O error occurs this method returns <code>false</code>.
	 *
	 * @param file a file to test
	 * @return <code>true</code> if file exists and a directory,
	 *         <code>false</code> otherwise
	 * @throws SecurityException
	 *   if system denies read access to the directory containing this file
	 * @see #exists(String)
	 */
	public static boolean isDirectory(String file) {
		file = normalize(file);
		Mount mount = findMount(file);
		return mount.driver.isDirectory(file.substring(mount.pathlen));
	}

	/**
	 * Creates new file in a filesystem.
	 *
	 * @param file a file to create
	 * @throws SecurityException
	 *   if system denies permission to create file
	 * @throws IOException
	 *   if file exists or an I/O error occurs
	 */
	public static void create(String file) throws IOException {
		file = normalize(file);
		Mount mount = findMount(file);
		mount.driver.create(file.substring(mount.pathlen));
	}

	/**
	 * Creates new directory in a filesystem.
	 *
	 * @param file a directory to create
	 * @throws SecurityException
	 *   if system denies permission to create directory
	 * @throws IOException
	 *   if file exists or an I/O error occurs
	 */
	public static void mkdir(String file) throws IOException {
		file = normalize(file);
		Mount mount = findMount(file);
		mount.driver.mkdir(file.substring(mount.pathlen));
	}

	/**
	 * Creates new directory and all its parents in the filesystem.
	 * This method does not fail if directory already exists.
	 *
	 * @param file a directory to create
	 * @throws SecurityException
	 *   if system denies permission to create directory
	 * @throws IOException
	 *   if an I/O error occurs
	 */
	public static void mkdirTree(String file) throws IOException {
		file = normalize(file);
		if (file.equals("")) return;
		if (!exists(fileParent(file))) mkdirTree(fileParent(file));
		mkdir(file);
	}

	/**
	 * Removes file from the file system.
	 *
	 * @param file a file to remove
	 * @throws SecurityException
	 *   if system denies permission to remove the file
	 * @throws IOException
	 *   if file is non-empty directory, mounted directory or an I/O error occurs
	 */
	public static void remove(String file) throws IOException {
		file = normalize(file);
		Mount mount = findMount(file);
		if (file.length() == mount.pathlen)
			throw new IOException("Cannot remove mounted directory");
		mount.driver.remove(file.substring(mount.pathlen));
	}
	
	/**
	 * Removes file from the file system.
	 * If file is a directory, also removes all its
	 * contents recursively.
	 *
	 * @param file a file to remove
	 * @throws SecurityException
	 *   if system denies permission to remove the file
	 * @throws IOException
	 *   if an I/O error occurs
	 */
	public static void removeTree(String file) throws IOException {
		if (isDirectory(file)) {
			String[] list = list(file);
			for (int i=list.length-1; i>=0; i--) {
				removeTree(file + '/' + list[i]);
			}
		}
		remove(file);
	}

	/**
	 * Copies contents of one file to another.
	 *
	 * @param source  the origin of copying
	 * @param dest    the destination of copying
	 * @throws SecurityException
	 *   if access permissions are insufficient to make a copy of file
	 * @throws IOException
	 *   if an I/O error occurs during copying
	 */
	public static void copy(String source, String dest) throws IOException {
		source = normalize(source);
		dest = normalize(dest);
		Mount srcMount = findMount(source);
		Mount destMount = findMount(dest);
		source = source.substring(srcMount.pathlen);
		dest = dest.substring(destMount.pathlen);
		if (srcMount == destMount) {
			srcMount.driver.copy(source, dest);
		} else {
			copyFile(srcMount.driver, source, destMount.driver, dest);
		}
	}

	/**
	 * Moves file to a new location.
	 *
	 * @param source  the original file
	 * @param dest    new name of the file
	 * @throws SecurityException
	 *   if access permissions are insufficient to move file
	 * @throws IOException
	 *   if <code>dest</code> exists or if an I/O error occurs during moving
	 */
	public static void move(String source, String dest) throws IOException {
		source = normalize(source);
		dest = normalize(dest);
		Mount srcMount = findMount(source);
		Mount destMount = findMount(dest);
		source = source.substring(srcMount.pathlen);
		dest = dest.substring(destMount.pathlen);
		if (srcMount == destMount) {
			srcMount.driver.move(source, dest);
		} else {
			if (srcMount.driver.exists(dest))
				throw new IOException("Cannot move "+source+" to "+dest+", destination already exists");
			copyFile(srcMount.driver, source, destMount.driver, dest);
			srcMount.driver.remove(source);
		}
	}
	
	/**
	 * Returns the time of the last modification of the file.
	 *
	 * @param file the file
	 * @return time in format used by <code>System.currentTimeMillis()</code>
	 * @throws SecurityException
	 *   if system denies reading of file attribute
	 * @throws IOException
	 *   if file does not exist or an I/O error occurs
	 * @see System#currentTimeMillis()
	 */
	public static long lastModified(String file) throws IOException {
		file = normalize(file);
		Mount mount = findMount(file);
		return mount.driver.lastModified(file.substring(mount.pathlen));
	}
	
	/**
	 * Tests whether this file exists and can be read.
	 * If an I/O error occurs this method returns <code>false</code>.
	 *
	 * @param file the file
	 * @return <code>true</code> if this file can be read,
	 *         <code>false</code> otherwise
	 * @throws SecurityException
	 *   if system denies reading of file attribute
	 */
	public static boolean canRead(String file) {
		file = normalize(file);
		Mount mount = findMount(file);
		return mount.driver.canRead(file.substring(mount.pathlen));
	}

	/**
	 * Tests whether this file can be written.
	 * If an I/O error occurs this method returns <code>false</code>.
	 *
	 * @param file the file
	 * @return <code>true</code> if this file can be written,
	 *         <code>false</code> otherwise
	 * @throws SecurityException
	 *   if system denies reading of file attribute
	 */
	public static boolean canWrite(String file) {
		file = normalize(file);
		Mount mount = findMount(file);
		return mount.driver.canWrite(file.substring(mount.pathlen));
	}

	/**
	 * Tests whether this file can be executed.
	 * If an I/O error occurs this method returns <code>false</code>.
	 *
	 * @param file the file
	 * @return <code>true</code> if this file can be executed,
	 *         <code>false</code> otherwise
	 * @throws SecurityException
	 *   if system denies reading of file attribute
	 */
	public static boolean canExec(String file) {
		file = normalize(file);
		Mount mount = findMount(file);
		return mount.driver.canExec(file.substring(mount.pathlen));
	}

	/**
	 * Changes ability of file to be read.
	 *
	 * @param file  the file
	 * @param on    if <code>true</code>, file can be read;
	 *              if <code>false</code>, it cannot
	 * @throws SecurityException
	 *   if system denies changing of file attribute
	 * @throws IOException
	 *   if file does not exist or an I/O error occurs
	 */
	public static void setRead(String file, boolean on) throws IOException {
		file = normalize(file);
		Mount mount = findMount(file);
		mount.driver.setRead(file.substring(mount.pathlen), on);
	}

	/**
	 * Changes ability of file to be written.
	 * @param file  the file
	 * @param on    if <code>true</code>, file can be written;
	 *              if <code>false</code>, it cannot
	 * 
	 * @throws SecurityException
	 *   if system denies changing of file attribute
	 * @throws IOException
	 *   if file does not exist or an I/O error occurs
	 */
	public static void setWrite(String file, boolean on) throws IOException {
		file = normalize(file);
		Mount mount = findMount(file);
		mount.driver.setWrite(file.substring(mount.pathlen), on);
	}

	/**
	 * Changes ability of file to be executed.
	 *
	 * @param file  the file
	 * @param on    if <code>true</code>, file can be executed;
	 *              if <code>false</code>, it cannot
	 * @throws SecurityException
	 *   if system denies changing of file attribute
	 * @throws IOException
	 *   if file does not exist or an I/O error occurs
	 */
	public static void setExec(String file, boolean on) throws IOException {
		file = normalize(file);
		Mount mount = findMount(file);
		mount.driver.setExec(file.substring(mount.pathlen), on);
	}

	/**
	 * Returns the size of the file.
	 * If file is accessible but the size cannot be computed
	 * this method returns <code>0L</code>.
	 * 
	 * @param file the file
	 * @return file size, in bytes
	 * 
	 * @throws SecurityException
	 *   if system denies reading of file attribute
	 * @throws IOException
	 *   if file does not exist, cannot be accessed
	 *   or I/O error occurs
	 */
	public static long size(String file) throws IOException {
		file = normalize(file);
		Mount mount = findMount(file);
		return mount.driver.size(file.substring(mount.pathlen));
	}

	/**
	 * Determines the total size of the filesystem.
	 *
	 * @return
	 *   the total size of the file system in bytes, or
	 *   <code>0L</code> if this value cannot be estimated
	 * @throws SecurityException
	 *   if system denies reading this attribute
	 */
	public static long spaceTotal(String dir) {
		dir = normalize(dir);
		Mount mount = findMount(dir);
		return mount.driver.spaceTotal();
	}

	/**
	 * Determines the free memory that is available on the
	 * file system.
	 *
	 * @return
	 *  the available size in bytes on a file system, or
	 *   <code>0L</code> if this value cannot be estimated
	 * @throws SecurityException
	 *   if system denies reading this attribute
	 */
	public static long spaceFree(String dir) {
		dir = normalize(dir);
		Mount mount = findMount(dir);
		return mount.driver.spaceFree();
	}

	/**
	 * Determines the used memory of the file system.
	 *
	 * @return
	 *  the used size in bytes on a file system, or
	 *   <code>0L</code> if this value cannot be estimated
	 * @throws SecurityException
	 *   if system denies reading this attribute
	 */
	public static long spaceUsed(String dir) {
		dir = normalize(dir);
		Mount mount = findMount(dir);
		return mount.driver.spaceUsed();
	}
	
	/**
	 * Returns native URL to the given file.
	 * If there is no URL representation for the file,
	 * this method returns <code>null</code>.
	 */
	public static String getNativeURL(String file) {
		file = normalize(file);
		Mount mount = findMount(file);
		return mount.driver.getNativeURL(file);
	}
	
	static void copyFile(FSDriver from, String source, FSDriver to, String dest) throws IOException {
		InputStream in = from.read(source);
		OutputStream out = to.write(dest);
		try {
			byte[] buf = new byte[1024];
			int count = in.read(buf);
			while (count >= 0) {
				out.write(buf, 0, count);
				count = in.read(buf);
			}
			out.flush();
		} finally {
			try {
				in.close();
			} catch (IOException ioe) { }
			try {
				out.close();
			} catch (IOException ioe) { }
		}
	}
}
