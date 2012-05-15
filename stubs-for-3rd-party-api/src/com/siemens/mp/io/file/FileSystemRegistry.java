package com.siemens.mp.io.file;

import java.util.Enumeration;

/**
 * The FileSystemRegistry is a central registry for file system
 * listeners interested in the adding and removing (or mounting
 * and unmounting) of file systems on a device. 
 */
public class FileSystemRegistry {
	
	private FileSystemRegistry() { }
	
	/**
	 * This method is used to register a FileSystemListener that is
	 * notified in case of adding and removing a new file system root.
	 * Multiple file system listeners can be added. If file systems
	 * are not supported on a device, false is returned from the method
	 * (this check is performed prior to security checks).
	 * 
	 * @param listener
	 *   The new FileSystemListener to be registered
	 *   in order to handle adding/removing file system roots. 
	 * @return
	 *   boolean indicating if file system listener was successfully
	 *   added or not
	 * @throws SecurityException
	 *   if application is not given permission to read files
	 * @throws NullPointerException
	 *   if listener is <code>null</code>
	 */
	public static boolean addFileSystemListener(FileSystemListener listener) {
		throw new UnsupportedOperationException("This method is just a stub");
	}

	/**
	 * This method is used to remove a registered FileSystemListener.
	 * If file systems are not supported on a device, false is returned
	 * from the method.
	 * 
	 * @param listener
	 *   The FileSystemListener to be removed. 
	 * @return
	 *   boolean indicating if file system listener was successfully
	 *   removed or not
	 * @throws NullPointerException
	 *   if listener is <code>null</code>
	 */
	public static boolean removeFileSystemListener(FileSystemListener listener) {
		throw new UnsupportedOperationException("This method is just a stub");
	}
	
	/**
	 * This method returns the currently mounted root file systems on
	 * a device as String objects in an Enumeration. If there are no
	 * roots available on the device, a zero length Enumeration is
	 * returned. If file systems are not supported on a device, a zero
	 * length Enumeration is also returned (this check is performed prior
	 * to security checks).
	 * 
	 * @return
	 *   an Enumeration of mounted file systems as String objects.
	 * @throws SecurityException
	 *   if application is not given permission to read files
	 * @see FileConnection
	 */
	public static Enumeration listRoots() {
		throw new UnsupportedOperationException("This method is just a stub");		
	}
}
