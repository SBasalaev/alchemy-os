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

package alchemy.nlib;

import alchemy.core.AlchemyException;
import alchemy.core.Context;
import alchemy.core.Function;
import alchemy.core.Library;

/**
 * Skeleton for native application.
 * <p/>
 * NOTE: To be loaded through the native interface
 * subclass must define public constructor without
 * parameters.
 * @author Sergey Basalaev
 */
public abstract class NativeApp extends Library {

	private Function main;

	/** Constructor for subclasses. */
	public NativeApp() {
		main = new MainFunction();
	}

	public abstract int main(Context c, String[] args) throws Exception;

	public final Function getFunction(String sig) {
		return "main".equals(sig) ? main : null;
	}

	private class MainFunction extends Function {

		public MainFunction() {
			super("main");
		}

		public Object exec(Context c, Object[] args) throws AlchemyException {
			try {
				return Ival(main(c, (String[])args[0]));
			} catch (Exception e) {
				AlchemyException ae = new AlchemyException(e);
				ae.addTraceElement(this, "native");
				throw ae;
			}
		}
	}
}
