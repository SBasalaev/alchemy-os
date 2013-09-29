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

/**
 * Block expression.
 * <pre>
 * {
 *     <i>expr1</i>;
 *     ...
 *     <i>exprN</i>;
 * }
 * </pre>
 * 
 * @author Sergey Basalaev
 */
public class BlockExpr extends Expr implements Scope {
	
	public ArrayList exprs = new ArrayList();
	/** Local variables. */
	public ArrayList locals = new ArrayList();

	private final Scope parent;

	public BlockExpr(Scope parent) {
		this.parent = parent;
	}

	public Type rettype() {
		Expr last = (Expr)exprs.last();
		return last.rettype();
	}

	public int lineNumber() {
		if (exprs.isEmpty()) return -1;
		return ((Expr)exprs.first()).lineNumber();
	}

	public NamedType getType(String alias) {
		return parent.getType(alias);
	}

	public Var getVar(String id) {
		for (int i=locals.size()-1; i>=0; i--) {
			Var v = (Var)locals.get(i);
			if (v.name.equals(id)) return v;
		}
		return parent.getVar(id);
	}

	public boolean isLocal(String id) {
		for (int i=locals.size()-1; i>=0; i--) {
			Var v = (Var)locals.get(i);
			if (v.name.equals(id)) return true;
		}
		return parent.isLocal(id);
	}

	public boolean addVar(Var v) throws ParseException {
		for (int i=locals.size()-1; i>=0; i--) {
			Var var = (Var)locals.get(i);
			if (var.name.equals(v.name))
				throw new ParseException("Variable "+v.name+" already exists in this scope");
		}
		locals.add(v);
		return parent.getVar(v.name) != null;
	}

	public Object accept(ExprVisitor v, Object data) {
		return v.visitBlock(this, data);
	}

	public String funcName() {
		return parent.funcName();
	}
}