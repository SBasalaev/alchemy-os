/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011  Sergey Basalaev <sbasalaev@gmail.com>
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
 *
 */

package alchemy.evm;

import alchemy.core.Context;
import alchemy.core.Function;
import alchemy.core.HashLibrary;
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
	static public final int VERSION = 0x0100;

	/** Determines whether this library has dependencies. */
	static private final int LFLAG_DEPS = 1;
	/** Determines whether this library defines a soname. */
	static private final int LFLAG_SONAME = 4;

	/** Determines whether the function has relocation table. */
	static private final int FFLAG_RELOC = 1;

	public Library build(Context c, InputStream in) throws IOException, InstantiationException {
		DataInputStream data = new DataInputStream(in);
		HashLibrary lib = new HashLibrary();
		//reading format version
		int ver = data.readUnsignedShort();
		if (!(ver >= VERSION && ver <= (VERSION|0xff)))
			throw new InstantiationException("Incompatible file format");
		//reading flags
		int lflags = data.readUnsignedByte();
		//reading soname
		String libname = null;
		if ((lflags & LFLAG_SONAME) != 0) {
			libname = data.readUTF();
		}
		//loading dependency libs
		Library[] libdeps = null;
		if ((lflags & LFLAG_DEPS) != 0) {
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
					cpool[cindex] = new Integer(data.readInt());
					break;
				case 'l': //long
					cpool[cindex] = new Long(data.readLong());
					break;
				case 'f': //float
					cpool[cindex] = new Float(data.readFloat());
					break;
				case 'd': //double
					cpool[cindex] = new Float(data.readDouble());
					break;
				case 'S': //string
					cpool[cindex] = data.readUTF();
					break;
				case 'E': { //external function
					int libref = data.readUnsignedShort();
					int nameref = data.readUnsignedShort();
					cpool[cindex] = libdeps[libref].getFunc((String)cpool[nameref]);
				} break;
				case 'H':   //private function
				case 'P': { //public function
					//reading data
					int nameref = data.readUnsignedShort();
					int fflags = data.readUnsignedByte();
					int stacksize = data.readUnsignedByte();
					int localsize = data.readUnsignedByte();
					int codesize = data.readUnsignedShort();
					byte[] code = new byte[codesize];
					data.readFully(code);
					//skipping relocation table
					if ((fflags & FFLAG_RELOC) != 0) {
						int relcount = data.readUnsignedShort();
						data.skipBytes(relcount << 1);
					}
					//constructing function
					Function func = new EFunction(libname, (String)cpool[nameref], cpool, stacksize, localsize, code);
					cpool[cindex] = func;
					if (ctype == 'P') lib.putFunc(func);
				} break;
				case 'U': { //unresolved function
					int nameref = data.readUnsignedShort();
					throw new InstantiationException("Unresolved function: "+cpool[nameref]);
				}
				default:
					throw new InstantiationException("Unknown data type: "+ctype);
			}
		}
		in.close();
		lib.lock();
		return lib;
	}

}
