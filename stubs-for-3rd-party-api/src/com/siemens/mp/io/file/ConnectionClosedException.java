package com.siemens.mp.io.file;

/**
 * Represents an exception thrown when a method is invoked on
 * a file connection but the method cannot be completed because
 * the connection is closed.
 */
public class ConnectionClosedException extends RuntimeException {

	/**
	 * Creates a new instance of <code>ConnectionClosedExceptionException</code>
	 * without detail message.
	 */
	public ConnectionClosedException() { }

	/**
	 * Constructs an instance of <code>ConnectionClosedException</code>
	 * with the specified detail message.
	 * @param msg the detail message.
	 */
	public ConnectionClosedException(String msg) {
		super(msg);
	}
}
