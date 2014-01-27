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
import java.io.OutputStream;

/**
 * Compiler environment.
 * @author Sergey Basalaev
 */
public final class CompilerEnv {
	/** Identifiers for optional features. */
	public static final String[] OPTION_STRINGS = {
		"compat",
	};

	/** Option for 2.1 compatibility mode. */
	public static final int F_COMPAT21 = 0;

	/** Identifiers for warning categories. */
	public static final String[] WARNING_STRINGS = {
		"deprecated",
		"main",
		"overrides",
		"empty",
		"typecast",
		"cast",
		"hidden",
		"divzero",
		"return",
	};

	/**
	 * Warning category for errors.
	 * Using this constant in {@link #warn(String, int, int, String) warn()}
	 * method increases error counter.
	 */
	public static final int W_ERROR = -1;
	/** Warn about use of deprecated items. */
	public static final int W_DEPRECATED = 0;
	/** Warn about incorrect semantic of main function. */
	public static final int W_MAIN = 1;
	/** Warn about possible problems with overriding methods. */
	public static final int W_OVERRIDES = 2;
	/** Warn about empty body of conditional and loop statements. */
	public static final int W_EMPTY = 3;
	/** Warn about potentially unsafe type casts. */
	public static final int W_TYPECAST = 4;
	/** Warn about unnecessary casts. */
	public static final int W_CAST = 5;
	/** Warn if item hides another item with the same name on the outer level. */
	public static final int W_HIDDEN = 6;
	/** Warn about division by constant integer zero. */
	public static final int W_DIVZERO = 7;
	/** Warn if return statement is not given explicitely. */
	public static final int W_RETURN = 8;

	/** Bit-mask of enabled option flags. */
	private final int options;
	/** Bit-mask of enabled warning flags. */
	private int warnings;
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

	public void suppressWarnings() {
		warnings = 0;
	}

	public boolean hasOption(int option) {
		return (options & (1 << option)) != 0;
	}

	/**
	 * Prints warning message on stderr and increases warning count.
	 * Argument <emph>category</emph> is one of W_* constants.
	 * If it is W_ERROR, then error count increases.
	 */
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

	/** Returns string of options for debugging messages. */
	public String optionString() {
		StringBuffer buf = new StringBuffer();
		if (debug) buf.append(" -g");
		for (int i=0; i<OPTION_STRINGS.length; i++) {
			if ((options & (1 << i)) != 0) buf.append(" -f").append(OPTION_STRINGS[i]);
		}
		for (int i=0; i<WARNING_STRINGS.length; i++) {
			if ((warnings & (1 << i)) != 0) buf.append(" -W").append(WARNING_STRINGS[i]);
		}
		return buf.toString();
	}

	public void exceptionHappened(String component, String info, Exception e) {
		OutputStream err = io.stderr;
		IO.println(err, "There is a bug in compiler. Please report it with your source code and the following error messages.");
		IO.println(err, "Component: " + component);
		IO.println(err, "Compiler options:" + optionString());
		IO.println(err, info);
		IO.println(err, "Exception: " + e);
		e.printStackTrace();
		errcount++;
	}
}
