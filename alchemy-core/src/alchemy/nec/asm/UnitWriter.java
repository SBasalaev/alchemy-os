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

import alchemy.evm.Opcodes;
import alchemy.types.Float32;
import alchemy.types.Float64;
import alchemy.types.Int32;
import alchemy.types.Int64;
import alchemy.util.ArrayList;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Code writer for version 2.2
 * @author Sergey Basalaev
 */
public class UnitWriter {
	
	private int vmversion;
	private ArrayList objects = new ArrayList();
	
	public UnitWriter() { }
	
	public void visitVersion(int version) {
		this.vmversion = version;
	}
	
	public void visitSymbol(String symbol) {
		FuncObject f = new FuncObject(symbol);
		// if function is already in object, just skip it.
		if (!objects.contains(f)) objects.add(f);
	}
	
	public FunctionWriter visitFunction(String name, boolean shared, int arglen) {
		int index = objects.indexOf(new FuncObject(name));
		AsmFunc func = new AsmFunc(name);
		func.shared = shared;
		if (index < 0) {
			objects.add(func);
		} else {
			if (objects.get(index) instanceof AsmFunc) {
				throw new IllegalStateException("Function already visited: "+name);
			}
			objects.set(index, func);
		}
		return new FunctionWriter(func, objects, arglen);
	}
	
	public void writeTo(OutputStream stream) throws IOException {
		DataOutputStream out = new DataOutputStream(stream);
		out.writeShort(0xC0DE);
		out.writeShort(vmversion);
		out.writeByte(0);
		out.writeShort(objects.size());
		for (int i=0; i<objects.size(); i++) {
			Object obj = objects.get(i);
			if (obj.getClass() == Int32.class) {
				out.writeByte('i');
				out.writeInt(((Int32)obj).value);
			} else if (obj.getClass() == Int64.class) {
				out.writeByte('l');
				out.writeLong(((Int64)obj).value);
			} else if (obj.getClass() == Float32.class) {
				out.writeByte('f');
				out.writeFloat(((Float32)obj).value);
			} else if (obj.getClass() == Float64.class) {
				out.writeByte('d');
				out.writeDouble(((Float64)obj).value);
			} else if (obj.getClass() == String.class) {
				out.writeByte('S');
				out.writeUTF((String)obj);
			} else if (obj.getClass() == FuncObject.class) {
				out.writeByte('U');
				out.writeUTF(((FuncObject)obj).value);
			} else if (obj.getClass() == AsmFunc.class) {
				AsmFunc f = (AsmFunc)obj;
				out.writeByte('P');
				out.writeUTF(f.value);
				int fflags = 0;
				if (f.shared) fflags |= Opcodes.FFLAG_SHARED;
				if (f.relocs != null) fflags |= Opcodes.FFLAG_RELOCS;
				if (f.dbgtable != null) fflags |= Opcodes.FFLAG_LNUM;
				if (f.errtable != null) fflags |= Opcodes.FFLAG_ERRTBL;
				out.writeByte(fflags);
				out.writeByte(f.stacksize);
				out.writeByte(f.varcount);
				out.writeShort(f.code.length);
				out.write(f.code);
				if (f.relocs != null) writeChars(out, f.relocs);
				if (f.dbgtable != null) writeChars(out, f.dbgtable);
				if (f.errtable != null) writeChars(out, f.errtable);
			}
		}
	}
	
	private static void writeChars(DataOutputStream out, char[] chars) throws IOException {
		out.writeShort(chars.length);
		for (int i=0; i<chars.length; i++) {
			out.writeChar(chars[i]);
		}
	}
}