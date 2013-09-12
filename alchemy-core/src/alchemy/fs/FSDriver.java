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

/**
 * Filesystem drivers should subclass this class.
 * 
 * In order to be usable in {@link Filesystem} subclass
 * must be named as <pre>alchemy.fs.<i>name</i>.Driver</pre>
 * and must have a public constructor with no arguments.
 * Initialization should happen in {@link #init(String) init} method.
 * <p>
 * Implementation can always assume that file name is in
 * {@link Filesystem#normalize(String) normalized} form.
 *
 * @author Sergey Basalaev
 */
public abstract class FSDriver {

	/** Constructor for subclasses. */
	protected FSDriver() { }

	/**
	 * Initializes this file system.
	 * <p>
	 * The default implementation does nothing.
	 *
	 * @param cfg configuration string
	 * @throws IOException
	 *   if file system fails to initialize with given arguments
	 */
	public void init(String cfg) throws IOException { }
	
	/**
	 * Finalizes this file system.
	 * This method is called when the file system is unmounted.
	 * <p>
	 * The default implementation does nothing.
	 */
	public void close() { }
	
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
	public abstract InputStream read(String file) throws IOException;
	
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
	public abstract OutputStream write(String file) throws IOException;

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
	public abstract OutputStream append(String file) throws IOException;

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
	public abstract String[] list(String file) throws IOException;
	
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
	public abstract boolean exists(String file);
	
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
	public abstract boolean isDirectory(String file);

	/**
	 * Creates new file in a filesystem.
	 *
	 * @param file a file to create
	 * @throws SecurityException
	 *   if system denies permission to create file
	 * @throws IOException
	 *   if file exists or an I/O error occurs
	 */
	public abstract void create(String file) throws IOException;

	/**
	 * Creates new directory in a filesystem.
	 *
	 * @param file a directory to create
	 * @throws SecurityException
	 *   if system denies permission to create directory
	 * @throws IOException
	 *   if file exists or an I/O error occurs
	 */
	public abstract void mkdir(String file) throws IOException;

	/**
	 * Removes file from the system.
	 *
	 * @param file a file to remove
	 * @throws SecurityException
	 *   if system denies permission to remove the file
	 * @throws IOException
	 *   if file is non-empty directory or an I/O error occurs
	 */
	public abstract void remove(String file) throws IOException;
	
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
	 *   if access permissions are insufficient to make a copy of file
	 * @throws IOException
	 *   if an I/O error occurs during copying
	 */
	public void copy(String source, String dest) throws IOException {
		Filesystem.copyFile(this, source, this, dest);
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
	 *   if access permissions are insufficient to move file
	 * @throws IOException
	 *   if <code>dest</code> exists or if an I/O error occurs during moving
	 */
	public void move(String source, String dest) throws IOException {
		if (exists(dest)) throw new IOException("Cannot move "+source+" to "+dest+", destination already exists");
		copy(source, dest);
		remove(source);
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
	public abstract long lastModified(String file) throws IOException;
	
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
	public abstract boolean canRead(String file);

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
	public abstract boolean canWrite(String file);

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
	public abstract boolean canExec(String file);

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
	public abstract void setRead(String file, boolean on) throws IOException;

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
	public abstract void setWrite(String file, boolean on) throws IOException;

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
	public abstract void setExec(String file, boolean on) throws IOException;

	/**
	 * Returns the size of the file.
	 * If file is accessible but the size cannot be computed
	 * this method returns <code>-1</code>.
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
	public abstract long size(String file) throws IOException;

	/**
	 * Determines the total size of the filesystem.
	 *
	 * @return
	 *   the total size of the file system in bytes, or
	 *   <code>-1</code> if this value cannot be estimated
	 * @throws SecurityException
	 *   if system denies reading this attribute
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
	 *   if system denies reading this attribute
	 */
	public abstract long spaceFree();

	/**
	 * Determines the used memory of the file system.
	 *
	 * @return
	 *  the used size in bytes on a file system, or
	 *   <code>-1</code> if this value cannot be estimated
	 * @throws SecurityException
	 *   if system denies reading this attribute
	 */
	public abstract long spaceUsed();
	
	/**
	 * Returns native URL to the given file.
	 * If there is no URL representation for the file,
	 * this method should return <code>null</code>.
	 * <p>
	 * Default implementation always returns <code>null</code>.
	 */
	public String getNativeURL(String path) {
		return null;
	}
}
