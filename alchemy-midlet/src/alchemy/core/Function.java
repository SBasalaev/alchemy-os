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

package alchemy.core;

/**
 * Function is an atomic piece of program execution.
 * Subclasses should provide implementation of
 * the {@link #exec(Context, Object[]) exec()} method.
 * <p>
 * The {@link #signature signature} of a function is used in
 * stack trace output generated by {@link Context#dumpCallStack()}.
 * Also {@link HashLibrary} uses signatures as hash keys.
 * </p><p>
 * When calling one function from another method
 * {@link #call(Context, Object[]) call()} should be used
 * in preference to <code>exec()</code>.
 * <p></p>
 * This class also supplies a set of boxing/unboxing methods
 * to reduce code.
 * </p><p>
 * Primitive types and arrays must be wrapped to the following classes:
 * </p>
 * <table border="1">
 * <thead>
 * <th>Native type</th> <th>Wrapper lass</th>
 * </thead>
 * <tbody>
 * <tr>
 * <td><code>boolean, byte, short, int</code></td>
 * <td><code>alchemy.core.types.Int</code></td>
 * </tr>
 * <tr>
 * <td><code>char</code></td>
 * <td><code>alchemy.core.types.Char</code></td>
 * </tr>
 * <tr>
 * <td><code>long</code></td>
 * <td><code>java.lang.Long</code></td>
 * </tr>
 * <tr>
 * <td><code>float</code></td>
 * <td><code>java.lang.Float</code></td>
 * </tr>
 * <tr>
 * <td><code>double</code></td>
 * <td><code>java.lang.Double</code></td>
 * </tr>
 * </tbody>
 * </table>
 * 
 * @author Sergey Basalaev
 */
public abstract class Function {

	/** Function signature. */
	public final String signature;

	/**
	 * Constructor for subclasses
	 * @param sig    function signature
	 */
	protected Function(String sig) {
		if (sig == null) throw new NullPointerException();
		this.signature = sig;
	}

	/**
	 * Executes this function.
	 * @param c     execution context
	 * @param args  function arguments
	 * @return function result
	 * @throws Exception if an exception occurs
	 */
	public abstract Object exec(Context c, Object[] args) throws AlchemyException;

	/**
	 * Returns string representation of this object.
	 * This method is used in output generated by
	 * <code>dumpCallStack</code>. By default this
	 * method returns function {@link #signature}
	 * though subclasses may provide additional
	 * information such as library name.
	 *
	 * @see Context#dumpCallStack()
	 */
	public String toString() {
		return signature;
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
