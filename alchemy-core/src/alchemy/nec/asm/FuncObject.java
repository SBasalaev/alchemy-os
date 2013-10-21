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

package alchemy.nec.asm;

public class FuncObject {
	final String value;

	public FuncObject(String value) {
		this.value = value;
	}

	public boolean equals(Object obj) {
		if (obj instanceof FuncObject) {
			return ((FuncObject)obj).value.equals(value);
		}
		return false;
	}
}

/* Assembler function. */
class AsmFunc extends FuncObject {
	boolean shared;
	int stacksize;
	int varcount;
	byte[] code;
	char[] relocs;
	char[] dbgtable;
	char[] errtable;

	public AsmFunc(String value) {
		super(value);
	}
}