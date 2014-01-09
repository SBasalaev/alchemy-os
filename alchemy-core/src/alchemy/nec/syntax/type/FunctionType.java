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

package alchemy.nec.syntax.type;

/**
 * Type of a function.
 * <pre>
 *  (type1,...,typeN):rettype
 *  (type1,...,typeN)
 * </pre>
 * 
 * @author Sergey Basalaev
 */
public final class FunctionType extends Type {

	public final Type rettype;
	public final Type[] argtypes;

	public FunctionType(Type rettype, Type[] argtypes) {
		super("Function", TYPE_FUNCTION);
		this.rettype = rettype;
		this.argtypes = argtypes;
	}

	public boolean equals(Type other) {
		if (other.kind != TYPE_FUNCTION) return false;
		FunctionType f = (FunctionType) other;
		int len = f.argtypes.length;
		if (len != this.argtypes.length) return false;
		if (!f.rettype.equals(this.rettype)) return false;
		for (int i=0; i<len; i++) {
			if (!f.argtypes[i].equals(this.argtypes[i])) return false;
		}
		return true;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append('(');
		int len = this.argtypes.length;
		for (int i=0; i<len; i++) {
			if (i != 0) buf.append(',');
			buf.append(argtypes[i]);
		}
		buf.append(')');
		if (this.rettype != BuiltinType.NONE) {
			buf.append(':').append(this.rettype);
		}
		return buf.toString();
	}

	public Type superType() {
		return BuiltinType.FUNCTION;
	}
}
