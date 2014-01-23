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

import alchemy.nec.syntax.statement.Statement;
import alchemy.nec.syntax.type.FunctionType;
import alchemy.nec.syntax.type.Type;

/**
 * Function in Ether code.
 * @author Sergey Basalaev
 */
public class Function implements Scope {

	/** Name of the source file for this function. */
	public String source;
	/** Signature of this function. */
	public String signature;
	/** Type of this function. */
	public FunctionType type;
	/** Whether this function is a .new method. */
	public boolean isConstructor;
	/** Function arguments. */
	public Var[] args;
	/** Usage count of a function. */
	public int hits;
	/** Body of the function. */
	public Statement body;

	private final Scope parent;

	public Function(Scope scope, String name) {
		this.parent = scope;
	}

	public Type getType(String name) {
		return parent.getType(name);
	}

	public Var getVar(String name) {
		for (int i=args.length-1; i>=0; i--) {
			if (args[i].name.equals(name)) return args[i];
		}
		return parent.getVar(name);
	}

	public Function enclosingFunction() {
		return this;
	}
}
