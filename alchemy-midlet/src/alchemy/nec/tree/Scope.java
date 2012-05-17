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

/**
 * 
 * @author Sergey Basalaev
 */
public interface Scope {
	/**
	 * Returns type by its alias.
	 * Returns <code>null</code> if alias is not defined.
	 */
	Type getType(String alias);

	/**
	 * Returns variable for given identifier.
	 * Returns <code>null</code> if variable is not defined.
	 */
	Var getVar(String id);

	/**
	 * Adds variable to the current scope.
	 * @throws ParseException
	 *   if variable can't be added to this scope for some reason
	 * @return
	 *   <code>true</code> if variable overrides some previous
	 *   values in this scope, <code>false</code> if it is new variable
	 */
	boolean addVar(Var v) throws ParseException;

	/**
	 * Returns true if variable is defined and is local.
	 */
	boolean isLocal(String id);
}
