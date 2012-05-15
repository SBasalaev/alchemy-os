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
import java.util.Enumeration;
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
	 * Functions defined in this unit.
	 */
	public Vector funcs = new Vector();
	
	/**
	 * Global variables.
	 */
	private Vector vars = new Vector();

	public Type getType(String alias) {
		return (Type)types.get(alias);
	}

	public Var getVar(String id) {
		for (Enumeration e = vars.elements(); e.hasMoreElements(); ) {
			Var v = (Var)e.nextElement();
			if (v.name.equals(id)) return v;
		}
		return null;
	}

	public boolean addVar(Var v) throws ParseException {
		if (getVar(v.name) != null)
			throw new ParseException("Variable "+v.name+" already exists at the outer scope");
		vars.addElement(v);
		return false;
	}

	public boolean isLocal(String id) {
		return false;
	}

	public void putType(String alias, Type type) {
		types.put(alias, type);
	}

	public Func getFunc(String name) {
		for (Enumeration e = funcs.elements(); e.hasMoreElements(); ) {
			Func f = (Func)e.nextElement();
			if (f.signature.equals(name)) return f;
		}
		return null;
	}
}
