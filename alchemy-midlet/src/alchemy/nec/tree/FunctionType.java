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

/**
 * Type of a function.
 * <pre>
 *  (type1,...,typeN):rettype
 *  (type1,...,typeN)
 * </pre>
 * 
 * @author Sergey Basalaev
 */
public class FunctionType extends Type {
	public Type rettype;
	public Type[] args;

	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (!(obj.getClass() == FunctionType.class)) return false;
		final FunctionType other = (FunctionType)obj;
		if (!this.rettype.equals(other.rettype)) return false;
		if (this.args.length != other.args.length) return false;
		for (int i=0; i<args.length; i++) {
			if (!this.args[i].equals(other.args[i])) return false;
		}
		return true;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer().append('(');
		for (int i=0; i<args.length; i++) {
			if (i != 0) buf.append(',');
			buf.append(args[i]);
		}
		buf.append(')');
		if (!rettype.equals(BuiltinType.NONE)) {
			buf.append(':').append(rettype);
		}
		return buf.toString();
	}

	public Type superType() {
		return BuiltinType.FUNCTION;
	}
}
