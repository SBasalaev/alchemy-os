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

/**
 * Expression type.
 * Subclasses need to implement <code>equals()</code>
 * and provide <code>toString()</code> with representation
 * of type in E code.
 * 
 * @author Sergey Basalaev
 */
public abstract class Type {
	
	/**
	 * Returns supertype of this type.
	 * For forward declarations returns <code>null</code>.
	 */
	public abstract Type superType();
	
	public final boolean isSupertypeOf(Type type) {
		if (type.equals(BuiltinType.NULL)) {
			return true;
		}
		if (this instanceof FunctionType && type instanceof FunctionType) {
			final FunctionType fthis = (FunctionType)this;
			final FunctionType fsub = (FunctionType)type;
			if (fthis.args.length != fsub.args.length) return false;
			boolean ok = fthis.rettype.isSupertypeOf(fsub.rettype);
			for (int i=0; ok && i < fthis.args.length; i++) {
				ok = fthis.args[i].isSubtypeOf(fsub.args[i]);
			}
			return ok;
		}
		while (type != null) {
			if (this.equals(type)) return true;
			type = type.superType();
		}
		return false;
	}
	
	public final boolean isSubtypeOf(Type type) {
		return type.isSupertypeOf(this);
	}
	
	public static Type commonSupertype(Type type1, Type type2) {
		if (type1.equals(BuiltinType.NONE) || type2.equals(BuiltinType.NONE))
			return BuiltinType.NONE;
		while (type1 != null) {
			if (type1.isSupertypeOf(type2)) return type1;
			type1 = type1.superType();
		}
		return BuiltinType.ANY;
	}
}