/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.siemens.mp.io;

import java.io.IOException;

/**
 * The File class holds basic methods for accessing files on
 * the phone's file system.
 * <p>
 * The first time you run any class method, a <b>storage</b>
 * subfolder under the application folder is created.
 * In general, ".." is forbidden in path specifications.
 * Depending on the phone model, different further restrictions
 * apply to the path specification:
 * </p><p>
 * <table width="90%" border="1" cellpadding="3" cellspacing="0">
 * <tbody><tr>
 * <td valign="top" width="1%" align="right">SL45i,&nbsp;6688i</td>
 * <td>On these phones only relative path specifications are allowed.
 * 'Relative' means relative to the application's path. This means that
 * a MIDlet may only access its <b>storage</b> folder and its subfolders.
 * There is no way to create a subfolder, so all files are placed in the
 * <b>storage</b> folder directly.
 * </td>
 * </tr>
 * <tr>
 * <td valign="top" width="1%" align="right">C55,&nbsp;C56,&nbsp;CT56,&nbsp;2128</td>
 * <td>In addition to the relative path specifications, these phones allow access to
 * the following folders: <b>Ringing Tone</b>, <b>Bitmap</b> and <b>Animation</b>.
 * An absolute path specification always has to start with <b>"a:\"</b>, that means
 * the path specifications to the folders above are
 * <b>"a:\Ringing Tone"</b>, <b>"a:\Bitmap"</b> and <b>"a:\Animation"</b>. For
 * security reasons the user has to explicitly confirm the access to one of those
 * folders. This happens only once per folder and per session.
 * </td>
 * </tr>
 * <tr>
 * <td valign="top" width="1%" align="right">
 * S55,&nbsp;S56,&nbsp;S57,&nbsp;S57 SL55, M55
 * </td>
 * <td>On these phones, path specifications are not restricted to any specific
 * folder; <b>"a:\"</b> specifies the file system's root and an absolute path
 * specification always has to start with this prefix. As with the phone
 * models above, the user has to explicitly confirm the access to any folder.
 * The phone keeps a history of the last 10 confirmed folders. Those 10 folders
 * need only to be confirmed once per session, whereas accessing more than 10 folders may
 * result in another confirmation request for a formerly already confirmed folder.</td>
 * </tr>
 * </tbody></table>
 */
public class File {

	public static final int INSIDE_STORAGE_PATH = 1;
	public static final int OUTSIDE_STORAGE_PATH = 0;
	public static final String STORAGE_DRIVE = "a:";

	public File() { }

	/**
	 * checkFileName
	 *
	 * @param  fileName  Name of file to be checked
	 * @return
	 *   <code>OUTSIDE_STORAGE_PATH</code> - if a full qualified path
	 *   name is used; <code>INSIDE_STORAGE_PATH</code> - if the specified
	 *   file is inside the MIDlet storage path
	 * @throws IllegalArgumentException
	 *   if specified file name violates security policies
	 */
	public static int checkFileName(String fileName) {
		return 0;
	}

	/**
	 * Writes a given string at the end of the file specified
	 * by the file name. It is useful for debug logging,
	 * because it requires no file opening and closing.
	 * 
	 * @param fileName    the name of the file to write to
	 * @param infoString  the string that will be written to the file 
	 * @return  &gt; 0 if successful
	 * @throws IOException  if some other kind of I/O error occurs
	 */
	public static int debugWrite(String fileName, String infoString) throws IOException {
		return 0;
	}

	/**
	 * Opens the file specified by the file name and prepares it
	 * for reading or writing. On success the file is opened in
	 * binary (untranslated) mode. If the specified file does not
	 * exist, it will be created.
	 * 
	 * @param fileName  the name of the file to be opened
	 * @return file descriptor or &lt; 0 if error occurred
	 * @throws IllegalArgumentException  if the path is invalid
	 * @throws IOException if some other kind of I/O error occurs
	 */
	public int open(String fileName) throws IOException {
		return 0;
	}

	/**
	 * Checks if the file specified by the file name exists.
	 * @param fileName  the name of the file
	 * @return  &lt; 0 if error occurred
	 * @throws IllegalArgumentException  if the path is invalid
	 */
	public static int exists(String fileName) {
		return 0;
	}

	/**
	 * Moves the file pointer to the specified location.
	 * The file pointer is a starting position for the next
	 * read or write operation. The file must be opened before
	 * this method call.
	 * 
	 * @param fileDescriptor  file descriptor
	 * @param seekpos         position to set the file pointer to i.e.
	 *                        the number of bytes from the beginning of the file
	 * @return  the offset, in bytes, of the new position from the beginning
	 *          of the file or &lt; 0 if an error occurred
	 * @throws IOException  if some kind of I/O error occurs
	 */
	public int seek(int fileDescriptor, int seekpos) throws IOException {
		return 0;
	}

	/**
	 * Returns the file length, in bytes. The file must be
	 * opened before this method call.
	 * 
	 * @param fileDescriptor  file descriptor
	 * @return  &lt; 0 if error occurred
	 * @throws IOException  if some kind of I/O error occurs
	 */
	public int length(int fileDescriptor) throws IOException {
		return 0;
	}

	/**
	 * Closes the file. After this method call, the specified
	 * file descriptor becomes invalid.
	 * 
	 * @param fileDescriptor  the file descriptor
	 * @return  &lt; 0 if error occurred
	 * @throws IOException  if some kind of I/O error occurs
	 */
	public int close(int fileDescriptor) throws IOException {
		return 0;
	}

	/**
	 * Writes <code>numBytes</code> bytes from buffer to a file.
	 * The file must be opened before this method call. The write
	 * operation begins at the current position of the file pointer.
	 * After the write operation, the file pointer is increased by
	 * the number of bytes actually written.
	 * 
	 * @param fileDescriptor  the file descriptor
	 * @param buf             the byte array with data to be written
	 * @param offset          offset of the data to be written within the bytearray
	 * @param numBytes        number of bytes to be written
	 * @return  &lt; 0 if error occurred
	 * @throws IOException  if some kind of I/O error occurs
	 */
	public int write(int fileDescriptor, byte[] buf, int offset, int numBytes) throws IOException {
		return 0;
	}

	/**
	 * Reads a maximum of <code>numBytes</code> bytes into a buffer
	 * from a file. The file must be opened before this method call.
	 * The read operation begins at the current position of the file
	 * pointer. After the read operation, the file pointer points to
	 * the next unread character.
	 * 
	 * @param fileDescriptor  the file descriptor
	 * @param buf             the buffer for the data to be read
	 * @param offset          offset within the buffer for data to be read
	 * @param numBytes        number of bytes to read
	 * @return
	 *   number of bytes actually read, which may be less than <code>numBytes</code>
	 *   if there are fewer than <code>numBytes</code> bytes left in the file,
	 *   or &lt; 0 if error occurred
	 * @throws IOException  if some kind of I/O error occurs
	 */
	public int read(int fileDescriptor, byte[] buf, int offset, int numBytes) throws IOException {
		return 0;
	}

	/**
	 * Deletes the file specified by its file name from the
	 * <b>storage</b> directory. Use this method with care,
	 * because the deleted file cannot be restored.
	 * 
	 * @param fileName  name of the file to be deleted
	 * @return  1 if successful
	 * @throws IllegalArgumentException  if the path is invalid
	 * @throws IOException if some other kind of I/O error occurs
	 */
	public static int delete(String fileName) throws IOException {
		return 0;
	}

	/**
	 * Returns the free disk space (in bytes), available on
	 * the phone's file system.
	 * @return  available disk space in bytes
	 * @throws IOException  if some kind of I/O error occurs
	 */
	public static int spaceAvailable() throws IOException {
		return 0;
	}

	/**
	 * Renames a file from source filename to destination filename.
	 * @param source  source name of the file
	 * @param dest    destination name of the file
	 * @return  &lt; 0 if error occurred
	 * @throws IllegalArgumentException  if the path is invalid
	 * @throws IOException if some other kind of I/O error occurs
	 */
	public static int rename(String source, String dest) throws IOException {
		return 0;
	}

	/**
	 * Creates a file copy.
	 * @param source  name of the source file
	 * @param dest    name of the destination file (the file copy)
	 * @return  &lt; 0 if error occurred
	 * @throws IllegalArgumentException  if the path is invalid
	 * @throws IOException if some other kind of I/O error occurs
	 */
	public static int copy(String source, String dest) throws IOException {
		return 0;
	}

	/**
	 * Checks if the given path specifies a directory.
	 * @param pathName  the name of the path to be checked
	 * @return  <code>true</code> if pathName specifies a directory;
	 *          <code>false</code> otherwise
	 * @throws IllegalArgumentException  if the path is invalid
	 * @throws IOException if some other kind of I/O error occurs
	 */
	public static boolean isDirectory(String pathName) throws IOException {
		return false;
	}

	/**
	 * Lists the content of a directory.
	 *
	 * @param pathName path to directory to be listed
	 * @return array of strings containing the files
	 *         and subdirectories within the path
	 * @throws IllegalArgumentException  if the path is invalid
	 * @throws IOException if some other kind of I/O error occurs
	 */
	public static String[] list(String pathName) throws IOException {
		return null;
	}

	public static String buildPath(String fileName) { return fileName; }

	/**
	 * Sets the size of a file.
	 * @param fileDescriptor  the file descriptor
	 * @param size            size to truncate the file stream to
	 * @throws IOException    if some kind of I/O error occurs
	 */
	public static void truncate(int fileDescriptor, int size) throws IOException {
		
	}
}
