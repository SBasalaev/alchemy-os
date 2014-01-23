/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2014, Sergey Basalaev <sbasalaev@gmail.com>
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

package alchemy.nec.syntax.statement;

import alchemy.nec.syntax.Function;
import alchemy.nec.syntax.Scope;
import alchemy.nec.syntax.Var;
import alchemy.nec.syntax.type.Type;
import alchemy.util.ArrayList;
import alchemy.util.HashMap;

/**
 * Block statement.
 * <pre>
 * {
 *     <i>statement1</i>;
 *     ...
 *     <i>statementN</i>;
 * }
 * </pre>
 * 
 * @author Sergey Basalaev
 */
public final class BlockStatement extends Statement implements Scope {

	private final Scope parent;

	public final HashMap vars = new HashMap();
	public final ArrayList statements = new ArrayList();

	public BlockStatement(Scope scope) {
		super(STAT_BLOCK);
		parent = scope;
	}

	public int lineNumber() {
		if (statements.isEmpty()) return -1;
		return ((Statement)statements.get(0)).lineNumber();
	}

	public Type getType(String name) {
		return parent.getType(name);
	}

	public Var getVar(String name) {
		Var var = (Var) vars.get(name);
		return (var != null) ? var : parent.getVar(name);
	}

	public boolean addVar(Var var) {
		vars.set(var.name, var);
		return parent.getVar(var.name) != null;
	}

	public Function enclosingFunction() {
		return parent.enclosingFunction();
	}

	public Object accept(StatementVisitor v, Object args) {
		return v.visitBlockStatement(this, args);
	}
}
