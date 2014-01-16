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

package alchemy.util;

import alchemy.system.AlchemyException;
import alchemy.system.Function;
import alchemy.system.Process;
import alchemy.system.ProcessKilledException;

/**
 * Function with part of arguments already applied.
 * May be used to implement currying.
 *
 * @author Sergey Basalaev
 */
public class PartiallyAppliedFunction extends Function {

	private final Object[] fixedArgs;
	private final Function f;
	
	public PartiallyAppliedFunction(Function f, Object[] args) {
		super(f.name+".apply[" + args.length + ']');
		this.fixedArgs = args;
		this.f = f;
	}

	public Object invoke(Process p, Object[] args) throws AlchemyException, ProcessKilledException {
		try {
			Object[] newArgs = new Object[fixedArgs.length+args.length];
			System.arraycopy(fixedArgs, 0, newArgs, 0, fixedArgs.length);
			System.arraycopy(args, 0, newArgs, fixedArgs.length, args.length);
			return f.invoke(p, newArgs);
		} catch (AlchemyException ae) {
			ae.addTraceElement(this, "native");
			throw ae;
		} catch (ProcessKilledException pke) {
			throw pke;
		} catch (Throwable e) {
			AlchemyException ae = new AlchemyException(e);
			ae.addTraceElement(this, "native");
			throw ae;
		}
	}
}
