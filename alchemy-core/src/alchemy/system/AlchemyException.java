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

package alchemy.system;

import alchemy.util.ArrayList;
import java.io.IOException;
import javax.microedition.media.MediaException;

/**
 * Exception thrown by function instances.
 * Maintains stack of function calls.
 *
 * @author Sergey Basalaev
 */
public class AlchemyException extends Exception {
	
	private ArrayList names = new ArrayList();
	private ArrayList infos = new ArrayList();

	public final int errcode;
	
	/**
	 * General code for successful program termination.
	 * Cannot be used to construct AlchemyException.
	 */
	public static final int SUCCESS = 0;
	/** General code for unsuccessful program termination. */
	public static final int FAIL = 1;
	/** Code for serious system failures. */
	public static final int ERR_SYSTEM = 100;
	/** Code for NullPointerException. */
	public static final int ERR_NULL = 101;
	/** Code for IOException. */
	public static final int ERR_IO = 102;
	/** Code for IndexOutOfBoundsException. */
	public static final int ERR_RANGE = 103;
	/** Code for NegativeArraySizeException. */
	public static final int ERR_NEGARRSIZE = 104;
	/** Code for IllegalArgumentException. */
	public static final int ERR_ILLARG = 105;
	/** Code for IllegalStateException. */
	public static final int ERR_ILLSTATE = 106;
	/** Code for SecurityException. */
	public static final int ERR_SECURITY = 107;
	/** Code for ClassCastException. */
	public static final int ERR_CLASSCAST = 108;
	/** Code for ArithmeticException. */
	public static final int ERR_DIVBYZERO = 109;
	/** Code for InterruptedException. */
	public static final int ERR_INTERRUPT = 110;
	/** Code for MediaException. */
	public static final int ERR_MEDIA = 111;

	/**
	 * Creates new AlchemyException from given exception.
	 * Appropriate error code is determined from the class
	 * of cause. Detail message 
	 */
	public AlchemyException(Throwable cause) {
		super(cause.getMessage());
		errcode = (cause instanceof NullPointerException) ? ERR_NULL
				: (cause instanceof Error) ? ERR_SYSTEM
				: (cause instanceof IOException) ? ERR_IO
				: (cause instanceof IllegalArgumentException) ? ERR_ILLARG
				: (cause instanceof IllegalStateException) ? ERR_ILLSTATE
				: (cause instanceof SecurityException) ? ERR_SECURITY
				: (cause instanceof IndexOutOfBoundsException) ? ERR_RANGE
				: (cause instanceof ClassCastException) ? ERR_CLASSCAST
				: (cause instanceof ArithmeticException) ? ERR_DIVBYZERO
				: (cause instanceof InterruptedException) ? ERR_INTERRUPT
				: (cause instanceof MediaException) ? ERR_MEDIA
				: (cause instanceof AlchemyException) ? ((AlchemyException)cause).errcode
				: FAIL;
	}

	/**
	 * Creates new AlchemyException with given error
	 * code and detail message.
	 */
	public AlchemyException(int errcode, String msg) {
		super(msg);
		this.errcode = errcode;
		if (errcode == 0) throw new IllegalArgumentException();
	}

	/**
	 * Adds element to the stack trace.
	 * 
	 * @param name  function name
	 * @param info  debugging info
	 */
	public void addTraceElement(String name, String info) {
		names.add(name);
		infos.add(info);
	}
	
	/** Returns length of the stack trace. */
	public int getTraceLength() {
		return names.size();
	}

	/** Returns function name at the given index of the stack trace. */
	public String getTraceElementName(int index) {
		return (String) names.get(index);
	}

	/** Returns debugging info at the given index of the stack trace. */
	public String getTraceElementInfo(int index) {
		return (String) infos.get(index);
	}

	public String toString() {
		String msg = getMessage();
		StringBuffer sb = new StringBuffer();
		// print description of error type
		switch (errcode) {
			case ERR_SYSTEM: sb.append("System error"); break;
			case ERR_NULL: sb.append("null value"); break;
			case ERR_IO: sb.append("I/O error"); break;
			case ERR_RANGE: sb.append("Out of range"); break;
			case ERR_NEGARRSIZE: sb.append("Negative array size"); break;
			case ERR_SECURITY: sb.append("Permission denied"); break;
			case ERR_ILLARG: sb.append("Illegal argument"); break;
			case ERR_ILLSTATE: sb.append("Illegal state"); break;
			case ERR_CLASSCAST: sb.append("Type mismatch"); break;
			case ERR_DIVBYZERO: sb.append("Divizion by zero"); break;
			case ERR_INTERRUPT: sb.append("Process interrupted"); break;
			case ERR_MEDIA: sb.append("Media error"); break;
			default:
				if (msg == null || msg.length() == 0) {
					sb.append("Unknown or user defined error");
				}
		}
		// print detail message
		if (msg != null && msg.length() != 0) {
			if (sb.length() != 0) sb.append(": ");
			sb.append(msg);
		}
		// print stack trace
		int size = names.size();
		for (int i=0; i < size; i++) {
			sb.append('\n')
			.append('@')
			.append(names.get(i))
			.append('(')
			.append(infos.get(i))
			.append(')');
		}
		return sb.toString();
	}
}
