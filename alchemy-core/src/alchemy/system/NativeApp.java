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

import alchemy.types.Int32;

/**
 * Skeleton for native application.
 * <p/>
 * To be loaded through the native interface subclass
 * must define public constructor without parameters.
 *
 * @author Sergey Basalaev
 */
public abstract class NativeApp extends Library {

	/** Constructor for subclasses. */
	public NativeApp() {
		putFunction(new MainFunction());
	}

	public abstract int main(Process p, String[] args) throws Exception;

	private class MainFunction extends Function {

		public MainFunction() {
			super("main");
		}

		public Object invoke(Process c, Object[] args) throws AlchemyException {
			try {
				return Int32.toInt32(main(c, (String[])args[0]));
			} catch (Throwable t) {
				AlchemyException ae = new AlchemyException(t);
				ae.addTraceElement(this, "native");
				throw ae;
			}
		}
	}
}
