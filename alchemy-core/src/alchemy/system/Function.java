/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011-2014, Sergey Basalaev <sbasalaev@gmail.com>
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

/**
 * Function is an atomic piece of program execution.
 * Subclasses should provide implementation of
 * the {@link #invoke(Process, Object[]) invoke()} method.
 * <p>
 * Primitive types must be wrapped to the following classes:
 * </p>
 * <table border="1">
 * <thead>
 * <th>Native type</th> <th>Wrapper class</th>
 * </thead>
 * <tbody>
 * <tr>
 * <td><code>boolean, byte, short, char, int</code></td>
 * <td><code>alchemy.types.Int32</code></td>
 * </tr>
 * <tr>
 * <td><code>long</code></td>
 * <td><code>alchemy.types.Int64</code></td>
 * </tr>
 * <tr>
 * <td><code>float</code></td>
 * <td><code>alchemy.types.Float32</code></td>
 * </tr>
 * <tr>
 * <td><code>double</code></td>
 * <td><code>alchemy.types.Float64</code></td>
 * </tr>
 * </tbody>
 * </table>
 * <p>
 * Functions should properly handle exceptional cases.
 * Any runtime exceptions should be wrapped in
 * AlchemyException class. The function is also supposed
 * to put itself in a stack trace using
 * {@link AlchemyException#addTraceElement(Function, String) }.
 * If debugging information is available, second argument should
 * be in form <i>source:line</i> otherwise it can be any information
 * helpful in debugging. A typical exception handling:
 * <pre>
 * Object invoke(Process p, Object[] args) throws AlchemyException {
 *   try {
 *     // function body
 *     ...
 *   } catch (AlchemyException e) {
 *     e.addTraceElement(this.name, debugString);
 *     throw e;
 *   } catch (Throwable t) {
 *     AlchemyException e = new AlchemyException(t);
 *     e.addTraceElement(this.name, debugString);
 *     throw e;
 *   }
 * }
 * </pre>
 *
 * @author Sergey Basalaev
 */
public abstract class Function {

	/** Function signature. */
	public final String name;
	/** Owner of this function. */
	public final Library library;

	/**
	 * Constructor for subclasses.
	 * @param library  owner library, may be null if this function is generated in runtime
	 * @param name     function name
	 */
	protected Function(Library library, String name) {
		if (name == null) throw new NullPointerException();
		this.name = name;
		this.library = library;
	}

	/**
	 * Invokes this function.
	 * @param p     calling process
	 * @param args  function arguments
	 * @return function result
	 * @throws AlchemyException if an exception occurs
	 * @throws ProcessKilledException if the process was killed
	 */
	public abstract Object invoke(Process p, Object[] args) throws AlchemyException, ProcessKilledException;

	/**
	 * Returns string representation of this object.
	 * This method returns string in form
	 * <pre>
	 * libraryname:functionname
	 * </pre>
	 */
	public final String toString() {
		return ((library != null && library.name != null) ? library.name : "<dynamic>") + ':' + name;
	}
}
