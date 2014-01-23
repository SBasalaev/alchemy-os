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

package alchemy.nec.syntax;

import alchemy.nec.syntax.type.Type;
import alchemy.util.ArrayList;
import alchemy.util.HashMap;

/**
 * Compilation unit.
 * @author Sergey Basalaev
 */
public final class Unit implements Scope {

	/** String -&gt; Var */
	private final HashMap vars = new HashMap();
	/** String -&gt; Type */
	private final HashMap types = new HashMap();

	public final ArrayList implementedFunctions = new ArrayList();

	public Function getFunction(String name) {
		Var v = getVar(name);
		if (v != null && v.isConstant && v.type.kind == Type.TYPE_FUNCTION)
			return (Function)v.defaultValue;
		return null;
	}

	public Var getVar(String name) {
		Var v = (Var)vars.get(name);
		return v;
	}

	public boolean addVar(Var v) {
		vars.set(v.name, v);
		return false;
	}

	public Type getType(String name) {
		return (Type)types.get(name);
	}

	public void addType(Type type) {
		types.set(type.name, type);
	}

	public Function enclosingFunction() {
		return null;
	}
}
