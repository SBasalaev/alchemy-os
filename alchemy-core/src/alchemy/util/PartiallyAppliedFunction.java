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

package alchemy.util;

import alchemy.system.AlchemyException;
import alchemy.system.Function;
import alchemy.system.Process;

/**
 * Function with first argument already applied.
 * May be used to implement currying.
 * 
 * @author Sergey Basalaev
 */
public class PartiallyAppliedFunction extends Function {

	private final Object argument;
	private final Function f;
	
	public PartiallyAppliedFunction(Function f, Object argument) {
		super(f.name+".curry");
		this.argument = argument;
		this.f = f;
	}

	public Object invoke(Process p, Object[] args) throws AlchemyException {
		try {
			Object[] newargs = new Object[args.length+1];
			System.arraycopy(args, 0, newargs, 1, args.length);
			newargs[0] = argument;
			return f.invoke(p, newargs);
		} catch (AlchemyException ae) {
			ae.addTraceElement(this, "native");
			throw ae;
		} catch (Exception e) {
			AlchemyException ae = new AlchemyException(e);
			ae.addTraceElement(this, "native");
			throw ae;
		}
	}
}
