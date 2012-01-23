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

package alchemy.fs;

import alchemy.util.I18N;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Filesystem abstraction.
 *
 * @see File
 *
 * @author Sergey Basalaev
 */
public abstract class Filesystem {

	/** Constructor for subclasses. */
	protected Filesystem() { }

	/**
	 * Returns a stream to read from the file.
	 *
	 * @param file a file to read from
	 * @return <code>InputStream</code> instance
	 * @throws IOException
	 *   if file does not exist, is not accessible,
	 *   is a directory or I/O error occurs
	 * @throws SecurityException
	 *   if application is not granted access to the file
	 */
	public abstract InputStream read(File file) throws IOException;

	/**
	 * Returns a stream to write to the file.
	 * Contents of the file becomes overwritten.
	 * If file does not exist it is created.
	 *
	 * @param file a file to write to
	 * @return <code>OutputStream</code> instance
	 * @throws IOException
	 *   if file cannot be created, is not accessible,
	 *   is a directory or I/O error occurs
	 * @throws SecurityException
	 *   if application is not granted access to the file
	 * @see #append(File)
	 */
	public abstract OutputStream write(File file) throws IOException;

	/**
	 * Returns a stream to write to the file.
	 * New contents is added to the end of file.
	 * If file does not exist it is created.
	 *
	 * @param file a file to write to
	 * @return <code>OutputStream</code> instance
	 * @throws IOException
	 *   if file cannot be created, is not accessible,
	 *   is a directory or I/O error occurs
	 * @throws SecurityException
	 *   if application is not granted access to the file
	 * @see #write(File)
	 */
	public abstract OutputStream append(File file) throws IOException;

	/**
	 * Lists file names that the specified directory contains.
	 * The pathnames ".." and "." are not included in the
	 * output. If the directory is empty then an empty
	 * array is returned.
	 * 
	 * @param file a directory to list
	 * @return
	 *   array of file names the specified director contains
	 * @throws IOException
	 *   if file does not exist, cannot be accessed,
	 *   is not a directory or I/O error occurs
	 * @throws SecurityException
	 *   if application is not granted access to the file
	 */
	public abstract String[] list(File file) throws IOException;

	/**
	 * Tests whether the specified file exists.
	 * If an I/O error occurs then method
	 * returns <code>false</code>.
	 *
	 * @param file a file to test
	 * @return <code>true</code> if file exists,
	 *         <code>false</code> otherwise
	 * @throws SecurityException
	 *   if application is not granted access to the file
	 */
	public abstract boolean exists(File file);

	/**
	 * Tests whether the specified file exists and a directory.
	 *
	 * @param file a file to test
	 * @return <code>true</code> if file exists and a directory,
	 *         <code>false</code> otherwise
	 * @throws SecurityException
	 *   if application is not granted access to the file
	 * @see #exists(File)
	 */
	public abstract boolean isDirectory(File file);

	/**
	 * Creates new file in a filesystem.
	 *
	 * @param file a file to create
	 * @throws SecurityException
	 *   if application is not granted access to the file
	 * @throws IOException
	 *   if file exists, cannot be accessed or I/O error occurs
	 */
	public abstract void create(File file) throws IOException;

	/**
	 * Creates new directory in a filesystem.
	 *
	 * @param file a directory to create
	 * @throws SecurityException
	 *   if application is not granted access to the file
	 * @throws IOException
	 *   if file exists, cannot be accessed or I/O error occurs
	 */
	public abstract void mkdir(File file) throws IOException;

	/**
	 * Removes file from the system.
	 *
	 * @param file a file to remove
	 * @throws SecurityException
	 *   if application is not granted access to the file
	 * @throws IOException
	 *   if file cannot be accessed, is non-empty
	 *   directory or I/O error occurs
	 */
	public abstract void remove(File file) throws IOException;

	/**
	 * Copies contents of one file to another.
	 * <p/>
	 * Default implementation opens <code>source</code>
	 * for reading, <code>dest</code> for writing and
	 * writes contents of the first file to the second.
	 * Subclasses are encouraged to override this method
	 * if file system supports more efficient method of
	 * copying files.
	 *
	 * @param source  the origin of copying
	 * @param dest    the destination of copying
	 * @throws SecurityException
	 *   if application is not granted access to the file
	 * @throws IOException
	 *   if I/O error occurs during copying
	 */
	public void copy(File source, File dest) throws IOException {
		InputStream in = read(source);
		OutputStream out = write(dest);
		try {
			byte[] buf = new byte[128];
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

	/**
	 * Moves file to a new location.
	 * <p/>
	 * The default implementation copies <code>source</code>
	 * to <code>dest</code> and then removes <code>source</code>
	 * as if methods
	 * <pre>
	 * copy(source, dest);
	 * remove(source);</pre>
	 * were called.
	 * Subclasses are encouraged to override this method
	 * if file system supports more efficient method of
	 * moving files.
	 *
	 * @param source  the original file
	 * @param dest    new name of the file
	 * @throws SecurityException
	 *   if application is not granted access to the file
	 * @throws IOException
	 *   if <code>dest</code> exists or if an I/O error occurs during moving
	 */
	public void move(File source, File dest) throws IOException {
		if (exists(dest)) throw new IOException(I18N._("Cannot move {0} to {1}, file already exists", source, dest));
		copy(source, dest);
		remove(source);
	}

	/**
	 * Returns the time of the last modification of the file.
	 *
	 * @param file the file
	 * @return time in format used by <code>System.currentTimeMillis()</code>
	 * @throws SecurityException
	 *   if application is not granted access to the file
	 * @throws IOException
	 *   if file does not exist, cannot be accessed
	 *   or I/O error occurs
	 * @see System#currentTimeMillis()
	 */
	public abstract long lastModified(File file) throws IOException;

	/**
	 * Tests whether this file exists and can be read.
	 * If an I/O error occurs this method returns <code>false</code>.
	 *
	 * @param file the file
	 * @return <code>true</code> if this file can be read,
	 *         <code>false</code> otherwise
	 * @throws SecurityException
	 *   if application is not granted access to the file
	 */
	public abstract boolean canRead(File file);

	/**
	 * Tests whether this file can be written.
	 * If an I/O error occurs this method returns <code>false</code>.
	 *
	 * @param file the file
	 * @return <code>true</code> if this file can be written,
	 *         <code>false</code> otherwise
	 * @throws SecurityException
	 *   if application is not granted access to the file
	 */
	public abstract boolean canWrite(File file);

	/**
	 * Tests whether this file can be executed.
	 * If an I/O error occurs this method returns <code>false</code>.
	 *
	 * @param file the file
	 * @return <code>true</code> if this file can be executed,
	 *         <code>false</code> otherwise
	 * @throws SecurityException
	 *   if application is not granted access to the file
	 */
	public abstract boolean canExec(File file);

	/**
	 * Changes ability of file to be read.
	 *
	 * @param file  the file
	 * @param on    if <code>true</code>, file can be read;
	 *              if <code>false</code>, it cannot
	 * @throws SecurityException
	 *   if application is not granted access to the file
	 * @throws IOException
	 *   if file does not exist, cannot be accessed
	 *   or I/O error occurs
	 */
	public abstract void setRead(File file, boolean on) throws IOException;

	/**
	 * Changes ability of file to be written.
	 * @param file  the file
	 * @param on    if <code>true</code>, file can be written;
	 *              if <code>false</code>, it cannot
	 * 
	 * @throws SecurityException
	 *   if application is not granted access to the file
	 * @throws IOException
	 *   if file does not exist, cannot be accessed
	 *   or I/O error occurs
	 */
	public abstract void setWrite(File file, boolean on) throws IOException;

	/**
	 * Changes ability of file to be executed.
	 *
	 * @param file  the file
	 * @param on    if <code>true</code>, file can be executed;
	 *              if <code>false</code>, it cannot
	 * @throws SecurityException
	 *   if application is not granted access to the file
	 * @throws IOException
	 *   if file does not exist, cannot be accessed
	 *   or I/O error occurs
	 */
	public abstract void setExec(File file, boolean on) throws IOException;

	/**
	 * Returns the size of the file.
	 * If file is accessible but the size cannot be computed
	 * this method returns <code>-1</code>.
	 * 
	 * @param file the file
	 * @return file size, in bytes

	 * @throws SecurityException
	 *   if application is not granted access to the file
	 * @throws IOException
	 *   if file does not exist, cannot be accessed
	 *   or I/O error occurs
	 */
	public abstract int size(File file) throws IOException;

	/**
	 * Determines the total size of this filesystem.
	 *
	 * @return
	 *   the total size of the file system in bytes, or
	 *   <code>-1</code> if this value cannot be estimated
	 * @throws SecurityException
	 *   if application is not granted access to the file system
	 */
	public abstract long spaceTotal();

	/**
	 * Determines the free memory that is available on the
	 * file system.
	 *
	 * @return
	 *  the available size in bytes on a file system, or
	 *   <code>-1</code> if this value cannot be estimated
	 * @throws SecurityException
	 *   if application is not granted access to the file system
	 */
	public abstract long spaceFree();

	/**
	 * Determines the used memory of the file system.
	 *
	 * @return
	 *  the used size in bytes on a file system, or
	 *   <code>-1</code> if this value cannot be estimated
	 * @throws SecurityException
	 *   if application is not granted access to the file system
	 */
	public abstract long spaceUsed();
}
