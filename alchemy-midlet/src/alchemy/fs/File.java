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

/**
 * Represents path to the file in the directory tree.
 * Path is a sequence of filenames each starting with slash '/'
 * (e.g. "/path/to/the/file"). Path for the root directory
 * is represented by empty string.
 * <p/>
 * While constructing <code>File</code> object the path is normalized:
 * <ol>
 *   <li>
 *     All sequences of slashes are replaced with a single slash.
 *   </li>
 *   <li>
 *     Every occurence of <code>'/.'</code> is removed from the sequence.
 *   </li>
 *   <li>
 *     Every occurence of <code>'/<i>name</i>/..'</code> is removed
 *     from the sequence.
 *   </li>
 *   <li>
 *     All leading <code>'/..'</code> are removed from the sequence.
 *   </li>
 * </ol>
 * Normalized path is unique for each file. It can be obtained
 * using {@link #path()} method.
 *
 * @author Sergey Basalaev
 */
public final class File {

	private final String path;

	/**
	 * Creates new <code>File</code> for the specified path.
	 * @param  path  path to the file
	 * @throws IllegalArgumentException
	 *   if parameter is not a valid path
	 * @throws NullPointerException
	 *   if parameter is <code>null</code>
	 */
	public File(String path) {
		this.path = normalize(path);
	}

	/**
	 * Creates new <code>File</code> for the specified parent
	 * directory and a path relative to it.
	 * @param parent   parent file
	 * @param relpath  path relative to the parent file
	 */
	public File(File parent, String relpath) {
		this.path = normalize(parent.path+'/'+relpath);
	}

	/**
	 * Normalizes path.
	 * Normalization includes:
	 * <ol>
	 *   <li>
	 *     Replacing all sequences of slashes with single slash.
	 *   </li>
	 *   <li>
	 *     Removing every occurence of <code>'/.'</code>.
	 *   </li>
	 *   <li>
	 *     Removing every occurence of <code>'/<i>name</i>/..'</code>.
	 *   </li>
	 * </ol>
	 *
	 * @param path  path to the file
	 *
	 * @throws NullPointerException if parameter is <code>null</code>
	 * @return normalized path
	 */
	private static String normalize(String path) {
		if (path.length() == 0) return path;
		if (path.charAt(0) != '/') path = "/"+path;
		if (path.charAt(path.length()-1) == '/') path = path+'.';
		//we know string starts with '/' and not ends with '/'
		StringBuffer sb = new StringBuffer();
		while (path.length() > 0) {
			int start = 1;
			while (path.charAt(start) == '/') start++;
			int end = path.indexOf('/', start);
			if (end < 0) end = path.length();
			start--;
			String next = path.substring(start, end);
			path = path.substring(end);
			if (next.equals("/.")) {
				//do nothing with it
			} else if (next.equals("/..")) {
				int len = sb.length();
				if (len > 0) {
					len--;
					while (sb.charAt(len) != '/') len--;
					sb.delete(len, sb.length());
				}
			} else {
				sb.append(next);
			}
		}
		return sb.toString();
	}

	/**
	 * Returns a parent for this file.
	 * For root directory this method returns <code>null</code>.
	 *
	 * @return a parent for this file or <code>null</code>
	 *         if this file have no parent
	 */
	public File parent() {
		if (path.length() == 0) return null;
		return new File(path.substring(0, path.lastIndexOf('/')));
	}

	/**
	 * Returns path to this file.
	 * @return path to this file
	 */
	public String path() {
		return path;
	}

	/**
	 * Returns the name of this file.
	 * For root directory this method returns empty string.
	 *
	 * @return a file name
	 */
	public String name() {
		return path.substring(path.lastIndexOf('/')+1);
	}

	/**
	 * Tests whether this object equals to another one.
	 *
	 * @param obj another object
	 * @return <code>true</code> if objects are the same,
	 *         <code>false</code> otherwise
	 */
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final File other = (File) obj;
		return this.path.equals(other.path);
	}

	/**
	 * Returns a hash code for this object.
	 *
	 * @return a hash code for the object
	 */
	public int hashCode() {
		return 413 ^ this.path.hashCode();
	}

	/**
	 * Returns a string representation of this object.
	 * A file path is returned.
	 *
	 * @return a string representation of the object
	 */
	public String toString() {
		return path;
	}

}
