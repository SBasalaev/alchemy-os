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

package alchemy.nec.tree;

import alchemy.nec.ParseException;
import alchemy.util.ArrayList;
import java.util.Hashtable;

/**
 * Compilation unit.
 * @author Sergey Basalaev
 */
public class Unit implements Scope {

	/**
	 * Known type names.
	 * String -> Type.
	 */
	Hashtable types = new Hashtable();

	/**
	 * Functions defined in this unit.
	 */
	public ArrayList funcs = new ArrayList();
	
	/**
	 * Global variables.
	 */
	private ArrayList vars = new ArrayList();

	public NamedType getType(String name) {
		return (NamedType)types.get(name);
	}

	public Var getVar(String id) {
		for (int i=vars.size()-1; i>=0; i--) {
			Var v = (Var)vars.get(i);
			if (v.name.equals(id)) return v;
		}
		return null;
	}

	public boolean addVar(Var v) throws ParseException {
		if (getVar(v.name) != null)
			throw new ParseException("Variable "+v.name+" already exists at the outer scope");
		vars.add(v);
		return false;
	}

	public boolean isLocal(String id) {
		return false;
	}

	public void putType(Type type) throws ParseException {
		if (!(type instanceof NamedType))
			throw new ParseException("Compiler error, trying to add composite type to the cache: "+type);
		types.put(type.toString(), type);
	}

	public Func getFunc(String name) {
		for (int i=funcs.size()-1; i>=0; i--) {
			Func f = (Func)funcs.get(i);
			if (f.signature.equals(name)) return f;
		}
		return null;
	}

	public String funcName() {
		return "";
	}
}
