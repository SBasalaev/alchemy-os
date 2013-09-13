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

package alchemy.nlib;

import alchemy.core.Process;
import alchemy.core.Function;
import alchemy.core.Int;
import alchemy.core.Library;
import alchemy.io.UTFReader;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Native library.
 * Native function signatures and indices are determined
 * from the resource file which contains function names
 * in implementation order.
 * 
 * @author Sergey Basalaev
 */
public abstract class NativeLibrary extends Library {

	/** Maps function name to function object. */
	private Hashtable functions = new Hashtable();
	
	protected NativeLibrary() { }
	
	/** Loads native functions using specified symbols file. */
	public final void load(String symbols) throws IOException {
		UTFReader r = new UTFReader(getClass().getResourceAsStream(symbols));
		int index = functions.size();
		String name;
		while ((name = r.readLine()) != null) {
			functions.put(name, new NativeFunction(this, name, index));
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
	protected static Int Ival(int value) {
		return Int.toInt(value);
	}

	/** Boxing method for boolean values.
	 * Method converts <code>true</code> to <code>Int(1)</code>
	 * and <code>false</code> to <code>Int(0)</code>.
	 */
	protected static Int Ival(boolean value) {
		return value ? Int.ONE : Int.ZERO;
	}

	/** Boxing method for long values. */
	protected static Long Lval(long value) {
		return new Long(value);
	}

	/** Boxing method for float values. */
	protected static Float Fval(float value) {
		return new Float(value);
	}

	/** Boxing method for double values. */
	protected static Double Dval(double value) {
		return new Double(value);
	}

	/** Unboxing method for Int values. */
	protected static int ival(Object obj) {
		return ((Int)obj).value;
	}

	/** Unboxing method for Int values.
	 * Method returns <code>false</code> iff <code>obj</code>
	 * is <code>Int(0)</code>.
	 */
	protected static boolean bval(Object obj) {
		return ((Int)obj).value != 0;
	}

	/** Unboxing method for Long values. */
	protected static long lval(Object obj) {
		return ((Long)obj).longValue();
	}

	/** Unboxing method for Float values. */
	protected static float fval(Object obj) {
		return ((Float)obj).floatValue();
	}

	/** Unboxing method for Double values. */
	protected static double dval(Object obj) {
		return ((Double)obj).doubleValue();
	}
}
