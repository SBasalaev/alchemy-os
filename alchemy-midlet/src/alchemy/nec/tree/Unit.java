/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011  Sergey Basalaev <sbasalaev@gmail.com>
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

package alchemy.nec.tree;

import alchemy.nec.ParseException;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Compilation unit.
 * @author Sergey Basalaev
 */
public class Unit implements Scope {

	/**
	 * Type aliases.
	 * String -> Type.
	 */
	Hashtable types = new Hashtable();

	/**
	 * Global functions.
	 */
	public Vector funcs = new Vector();

	public Type getType(String alias) {
		return (Type)types.get(alias);
	}

	public Var getVar(String id) {
		Func f = getFunc(id);
		return f == null ? null : f.asVar;
	}

	/** Always throws exceptions. Unit scope can only contain constants. */
	public boolean addVar(Var v) throws ParseException {
		throw new ParseException("Cannot add variable to outer scope");
	}

	public boolean isLocal(String id) {
		return false;
	}

	public void putType(String alias, Type type) {
		types.put(alias, type);
	}

	public Func getFunc(String name) {
		for (int i=funcs.size()-1; i>=0; i--) {
			Func f = (Func)funcs.elementAt(i);
			if (f.asVar.name.equals(name)) return f;
		}
		return null;
	}
}
