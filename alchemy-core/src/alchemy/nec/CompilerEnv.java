/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2013-2014, Sergey Basalaev <sbasalaev@gmail.com>
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

package alchemy.nec;

import alchemy.io.IO;
import alchemy.system.Process;

/**
 * Compiler environment.
 * @author Sergey Basalaev
 */
public final class CompilerEnv {
	/** Identifiers for optional features. */
	public static final String[] OPTION_STRINGS = {"compat"};

	/** Option for 2.1 compatibility mode. */
	public static final int F_COMPAT21 = 0;

	/** Identifiers for warning categories. */
	public static final String[] WARNING_STRINGS = {"deprecated", "main", "operators", "included", "empty", "typesafe"};

	/**
	 * Warning category for errors.
	 * Using this constant in {@link #warn(String, int, int, String) warn()}
	 * method increases error counter.
	 */
	public static final int W_ERROR = -1;
	public static final int W_DEPRECATED = 0;
	public static final int W_MAIN = 1;
	public static final int W_OPERATORS = 2;
	public static final int W_INCLUDED = 3;
	public static final int W_EMPTY = 4;
	public static final int W_TYPESAFE = 5;

	/** Bit-mask of enabled option flags. */
	private final int options;
	/** Bit-mask of enabled warning flags. */
	private final int warnings;
	/** Whether to generate debugging info. */
	public final boolean debug;
	/** Process instance for IO operations. */
	public final Process io;

	private int warncount = 0;
	private int errcount = 0;

	public CompilerEnv(Process p, int optionflags, int warningflags, boolean debug) {
		this.options = optionflags;
		this.warnings = warningflags;
		this.debug = debug;
		this.io = p;
	}

	public int getWarningCount() {
		return warncount;
	}

	public int getErrorCount() {
		return errcount;
	}

	public boolean hasOption(int option) {
		return (options & (1 << option)) != 0;
	}

	public void warn(String file, int line, int category, String msg) {
		boolean isError = category == W_ERROR;
		if (isError || (warnings | (1 << category)) != 0) {
			if (isError) {
				errcount++;
			} else {
				warncount++;
			}
			String output = file + ':' + line + ": ["
					+ ((isError) ? "Error" : "Warning " + WARNING_STRINGS[category])
					+ "]\n " + msg;
			IO.println(io.stderr, output);
		}
	}
}
