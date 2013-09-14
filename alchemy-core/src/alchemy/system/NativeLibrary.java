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

import alchemy.io.UTFReader;
import alchemy.types.Float32;
import alchemy.types.Float64;
import alchemy.types.Int32;
import alchemy.types.Int64;
import java.io.IOException;

/**
 * Library implemented as Java code.
 * Function names for native library should be provided
 * in a resource file available on classpath. These names
 * are loaded using {@link #load(String) load} method.
 * The order of names implies indexation of functions starting
 * with zero. The invokeNative method should be implemented using
 * switch on index:
 * <pre>
 * protected Object invokeNative(int index, Context c, Object[] args) throws Exception {
 *   switch (index) {
 *     case 0: {
 *       // implement function with index 0
 *     }
 *     case 1: {
 *       // implement function with index 1
 *     }
 *     ...
 *     default:
 *       return null
 *   }
 * }
 * </pre>
 * Native libraries are loaded through the reflection
 * (using Class.forName()) so they must have public
 * constructor with no arguments.
 *
 * @author Sergey Basalaev
 */
public abstract class NativeLibrary extends Library {
	
	protected NativeLibrary() { }
	
	/** Loads native function names from named resource. */
	public final void load(String symbols) throws IOException {
		UTFReader r = new UTFReader(getClass().getResourceAsStream(symbols));
		int index = functions.size();
		String name;
		while ((name = r.readLine()) != null) {
			functions.set(name, new NativeFunction(this, name, index));
			index++;
		}
		r.close();
	}

	/** Invokes native function. */
	protected abstract Object invokeNative(int index, Process p, Object[] args) throws Exception;
	
	/** Returns SONAME of this library. */
	public abstract String soname();
	
	public Function getFunction(String sig) {
		return (Function)functions.get(sig);
	}

	/** Boxing method for integer values. */
	protected static Int32 Ival(int value) {
		return Int32.toInt32(value);
	}

	/** Boxing method for boolean values.
	 * Method converts <code>true</code> to <code>Int(1)</code>
	 * and <code>false</code> to <code>Int(0)</code>.
	 */
	protected static Int32 Ival(boolean value) {
		return value ? Int32.ONE : Int32.ZERO;
	}

	/** Boxing method for long values. */
	protected static Int64 Lval(long value) {
		return new Int64(value);
	}

	/** Boxing method for float values. */
	protected static Float32 Fval(float value) {
		return new Float32(value);
	}

	/** Boxing method for double values. */
	protected static Float64 Dval(double value) {
		return new Float64(value);
	}

	/** Unboxing method for Int values. */
	protected static int ival(Object obj) {
		return ((Int32)obj).value;
	}

	/** Unboxing method for Int values.
	 * Method returns <code>false</code> iff <code>obj</code>
	 * is <code>Int(0)</code>.
	 */
	protected static boolean bval(Object obj) {
		return ((Int32)obj).value != 0;
	}

	/** Unboxing method for Long values. */
	protected static long lval(Object obj) {
		return ((Int64)obj).value;
	}

	/** Unboxing method for Float values. */
	protected static float fval(Object obj) {
		return ((Float32)obj).value;
	}

	/** Unboxing method for Double values. */
	protected static double dval(Object obj) {
		return ((Float64)obj).value;
	}
}
