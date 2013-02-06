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

package alchemy.evm;

import alchemy.core.Context;
import alchemy.core.Function;
import alchemy.core.HashLibrary;
import alchemy.core.Int;
import alchemy.core.LibBuilder;
import alchemy.core.Library;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Library builder for Embedded Virtual Machine.
 * @author Sergey Basalaev
 */
public class ELibBuilder implements LibBuilder {

	/**
	 * Version of library file format.
	 * <ul>
	 * <li>
	 * Two highest bytes are zeroes.
	 * <li>
	 * Byte 1 is MAJOR version. Formats with two different
	 * major versions are incompatible.
	 * <li>
	 * Byte 0 is MINOR version. Formats with the same major
	 * version are backward compatible, that is file's minor
	 * version must be equal to or less than this value.
	 * </ul>
	 */
	static public final int VERSION = 0x0201;

	/*
	 * New in format 1.1
	 *  Instructions: call calv newarray aload astore alen newba
	 *                baload bastore balen newca caload castore calen
	 * 
	 * New in format 2.0
	 *  Simplify format to be loaded and processed faster
	 *  Add line number table to functions
	 *  Add error table to functions
	 *  Instructions: if_icmpge if_icmpgt if_icmple if_icmplt
	 *                tableswitch lookupswitch
	 * 
	 * New in format 2.1
	 *  Cast instructions: i2c
	 *  Array instructions: newza zaload zastore zalen newsa saload
	 *    sastore salen newia iaload iastore ialen newla laload lastore
	 *    lalen newfa faload fastore falen newda daload dastore dalen
	 *  Jump instructions: jsr ret if_acmpeq if_acmpne
	 *  Variable instructions: iinc
	 */
	
	public Library build(Context c, InputStream in) throws IOException, InstantiationException {
		DataInputStream data = new DataInputStream(in);
		HashLibrary lib = new HashLibrary();
		//reading format version
		int ver = data.readUnsignedShort();
		if ((ver|0xff) != (VERSION|0xff)  ||  (ver&0xff) > (VERSION&0xff))
			throw new InstantiationException("Incompatible executable format: "+ver);
		//reading object type
		int lflags = data.readUnsignedByte();
		//reading soname
		String libname = null;
		if ((lflags & Opcodes.LFLAG_SONAME) != 0) {
			libname = data.readUTF();
		}
		//loading dependency libs
		Library[] libdeps = null;
		if ((lflags & Opcodes.LFLAG_DEPS) != 0) {
			int depcount = data.readUnsignedShort();
			libdeps = new Library[depcount];
			for (int i=0; i<depcount; i++) {
				libdeps[i] = c.loadLibrary(data.readUTF());
			}
		}
		//constructing constant pool
		int ccount = data.readUnsignedShort();
		Object[] cpool = new Object[ccount];
		for (int cindex=0; cindex<ccount; cindex++) {
			int ctype = data.readUnsignedByte();
			switch (ctype) {
				case '0': //null, aligning object
					break;
				case 'i': //integer
					cpool[cindex] = Int.toInt(data.readInt());
					break;
				case 'l': //long
					cpool[cindex] = new Long(data.readLong());
					break;
				case 'f': //float
					cpool[cindex] = new Float(data.readFloat());
					break;
				case 'd': //double
					cpool[cindex] = new Double(data.readDouble());
					break;
				case 'S': //string
					cpool[cindex] = data.readUTF();
					break;
				case 'E': { //external function
					int libref = data.readUnsignedShort();
					String name = data.readUTF();
					cpool[cindex] = libdeps[libref].getFunction(name);
				} break;
				case 'P': { // function
					//reading data
					String fname = data.readUTF();
					int fflags = data.readUnsignedByte();
					int stacksize = data.readUnsignedByte();
					int localsize = data.readUnsignedByte();
					int codesize = data.readUnsignedShort();
					byte[] code = new byte[codesize];
					char[] lnumtable = null;
					char[] errtable = null;
					data.readFully(code);
					if ((fflags & Opcodes.FFLAG_RELOCS) != 0) {
						data.skipBytes(data.readUnsignedShort()*2);
					}
					if ((fflags & Opcodes.FFLAG_LNUM) != 0) {
						lnumtable = new char[data.readUnsignedShort()];
						for (int j=0; j < lnumtable.length; j++) {
							lnumtable[j] = data.readChar();
						}
					}
					if ((fflags & Opcodes.FFLAG_ERRTBL) != 0) {
						errtable = new char[data.readUnsignedShort()];
						for (int j=0; j < errtable.length; j++) {
							errtable[j] = data.readChar();
						}
					}
					//constructing function
					Function func = new EtherFunction(libname, fname, cpool, stacksize, localsize, code, lnumtable, errtable);
					cpool[cindex] = func;
					if ((fflags & Opcodes.FFLAG_SHARED) != 0) lib.putFunc(func);
				} break;
				default:
					throw new InstantiationException("Unknown data type: "+ctype);
			}
		}
		lib.lock();
		return lib;
	}

}
