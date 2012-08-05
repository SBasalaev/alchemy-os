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

package alchemy.nec.tree;

import alchemy.nec.ParseException;
import java.util.Vector;

/**
 *
 * @author Sergey Basalaev
 */
public class Func implements Scope {
	private final Unit unit;

	/** Function signature. Not neccessarily equals variable name. */
	public String signature;
	/** Type of this function. */
	public FunctionType type;
	/** Holds implementation (if any). */
	public Expr body;
	/** Local variables. */
	public Vector locals;
	/** Number of times the function is used.
	 * Public functions initially get 1.
	 * Private and external functions initially get 0.
	 */
	public int hits;
	
	public Func(Unit u) {
		this.unit = u;
	}

	public boolean isLocal(String id) {
		for (int i=locals.size()-1; i>=0; i--) {
			Var v = (Var)locals.elementAt(i);
			if (v.name.equals(id)) return true;
		}
		return unit.isLocal(id);
	}

	public Var getVar(String id) {
		for (int i=locals.size()-1; i>=0; i--) {
			Var v = (Var)locals.elementAt(i);
			if (v.name.equals(id)) return v;
		}
		return unit.getVar(id);
	}

	public boolean addVar(Var v) throws ParseException {
		for (int i=locals.size()-1; i>=0; i--) {
			Var var = (Var)locals.elementAt(i);
			if (var.name.equals(v.name))
				throw new ParseException("Variable "+v.name+" already exists in this scope");
		}
		locals.addElement(v);
		return unit.getVar(v.name) != null;
	}

	public NamedType getType(String alias) {
		return unit.getType(alias);
	}

	public String funcName() {
		return signature;
	}
}
