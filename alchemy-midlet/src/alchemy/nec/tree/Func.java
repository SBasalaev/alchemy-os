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
import java.util.Vector;

/**
 *
 * @author Sergey Basalaev
 */
public class Func implements Scope {
	private final Unit unit;

	/** Holds name and type. */
	public Var asVar;
	/** Holds implementation (if any). */
	public Expr body;
	/** Local variables. */
	public Vector locals;
	/** Determines whether this function is in use. */
	public boolean used = false;

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

	public Type getType(String alias) {
		return unit.getType(alias);
	}
}
