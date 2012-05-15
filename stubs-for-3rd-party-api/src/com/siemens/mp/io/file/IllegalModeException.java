package com.siemens.mp.io.file;

/**
 * Represents an exception thrown when a method is invoked
 * requiring a particular security mode (e.g. READ or WRITE),
 * but the connection opened is not in the mode required.
 * The application does pass all security checks, but the
 * connection object is in the wrong mode. 
 */
public class IllegalModeException extends RuntimeException {

	/**
	 * Creates a new instance of <code>IllegalModeException</code>
	 * without detail message.
	 */
	public IllegalModeException() { }

	/**
	 * Constructs an instance of <code>IllegalModeException</code>
	 * with the specified detail message.
	 * @param msg the detail message.
	 */
	public IllegalModeException(String msg) {
		super(msg);
	}
}
