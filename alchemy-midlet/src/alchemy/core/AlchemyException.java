/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011-2012, Sergey Basalaev <sbasalaev@gmail.com>
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

package alchemy.core;

import java.io.IOException;
import java.util.Vector;

/**
 * Exception thrown by function instances.
 * Maintains stack of function calls.
 * @author Sergey Basalaev
 */
public class AlchemyException extends Exception {
	
	private Vector functions = new Vector();
	private Vector dbgInfo = new Vector();

	public final int errcode;
	
	public static final int SUCCESS = 0;
	public static final int ERR_NULL = 100;
	public static final int ERR_IO = 101;
	public static final int ERR_RANGE = 102;
	public static final int ERR_ILLARG = 103;
	public static final int ERR_ILLSTATE = 104;
	public static final int ERR_SECURITY = 105;
	public static final int ERR_SYSTEM = 200;
	
	public AlchemyException(Exception cause) {
		super(cause.getMessage());
		errcode = (cause instanceof NullPointerException) ? ERR_NULL
				: (cause instanceof IOException) ? ERR_IO
				: (cause instanceof IllegalArgumentException) ? ERR_ILLARG
				: (cause instanceof IllegalStateException) ? ERR_ILLSTATE
				: (cause instanceof SecurityException) ? ERR_SECURITY
				: (cause instanceof IndexOutOfBoundsException) ? ERR_RANGE
				: ERR_SYSTEM;
	}
	
	public AlchemyException(int errcode, String msg) {
		super(msg);
		this.errcode = errcode;
	}
	
	public void addTraceElement(Function f, String dbg) {
		functions.addElement(f);
		dbgInfo.addElement(dbg);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		switch (errcode) {
			case ERR_NULL:     sb.append("null value"); break;
			case ERR_IO:       sb.append("I/O error"); break;
			case ERR_RANGE:    sb.append("Out of range"); break;
			case ERR_SECURITY: sb.append("Permission denied"); break;
			case ERR_ILLARG:   sb.append("Illegal argument"); break;
			case ERR_ILLSTATE: sb.append("Illegal state"); break;
			case ERR_SYSTEM:   sb.append("System error");
		}
		if (getMessage() != null) {
			sb.append(": ").append(getMessage());
		}
		int size = functions.size();
		for (int i=0; i < size; i++) {
			sb.append('\n')
			.append('@')
			.append(functions.elementAt(i))
			.append('(')
			.append(dbgInfo.elementAt(i))
			.append(')');
		}
		return sb.toString();
	}
}
