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

package alchemy.nec;

import alchemy.core.Context;
import alchemy.core.types.Int;
import alchemy.evm.ELibBuilder;
import alchemy.evm.Opcodes;
import alchemy.fs.FSManager;
import alchemy.nlib.NativeApp;
import alchemy.util.IO;
import alchemy.util.Properties;
import alchemy.util.UTFReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Native Ether linker.
 * // TODO: move to alchemy.nes.asm
 * @author Sergey Basalaev
 */
public class NEL extends NativeApp {

	/** Highest supported library file format version.
	 * @see ELibBuilder#VERSION
	 */
	static private final int SUPPORTED = 0x0201;

	static private final String VERSION =
			"Native E linker version 1.3";

	static private final String HELP =
			"Usage: el [options] <input>...\nOptions:\n" +
			"-o <output>\n write to this file\n" +
			"-l<lib>\n link with given library\n" +
			"-L<path>\n append path to LIBPATH\n" +
			"-s<soname>\n use this soname\n" +
			"-h\n print this help and exit\n" +
			"-v\n print version and exit";

	public NEL() { }

	public int main(Context c, String[] args) {
		//parsing arguments
		String outname = "a.out";
		String soname = null;
		Vector infiles = new Vector();
		Vector linklibs = new Vector();
		linklibs.addElement("libcore.so"); //always link with libcore
		linklibs.addElement("libcoree.so"); //always link with libcoree
		boolean wait_outname = false;
		for (int i=0; i<args.length; i++) {
			String arg = args[i];
			if (arg.equals("-h")) {
				IO.println(c.stdout, HELP);
				return 0;
			} else if (arg.equals("-v")) {
				IO.println(c.stdout, VERSION);
				return 0;
			} else if (arg.equals("-o")) {
				wait_outname = true;
			} else if (arg.startsWith("-l") && arg.length() > 2) {
				if (arg.indexOf('/') < 0) {
					linklibs.addElement("lib"+arg.substring(2)+".so");
				} else {
					linklibs.addElement(arg.substring(2));
				}
			} else if (arg.startsWith("-L")) {
				c.setEnv("LIBPATH", c.getEnv("LIBPATH")+':'+arg.substring(3));
			} else if (arg.startsWith("-s") && arg.length() > 2) {
				soname = arg.substring(2);
			} else if (arg.charAt(0) == '-') {
				IO.println(c.stderr, "Unknown argument: "+arg);
				return 1;
			} else if (wait_outname) {
				outname = arg;
				wait_outname = false;
			} else {
				infiles.addElement(arg);
			}
		}
		if (infiles.isEmpty()) {
			IO.println(c.stderr, "No files to process");
			return 1;
		}
		try {
			//loading symbols from libraries
			Hashtable symbols = new Hashtable();
			Vector libinfos = new Vector();
			for (int li=0; li < linklibs.size(); li++) {
				String libname = linklibs.elementAt(li).toString();
				LibInfo info = loadLibInfo(c, libname);
				libinfos.addElement(info);
				for (Enumeration e = info.symbols.elements(); e.hasMoreElements();) {
					symbols.put(e.nextElement(), info);
				}
			}
			//processing objects
			Vector pool = new Vector();
			int[] reloctable = new int[128];
			int count = 0, offset;
			for (int fi=0; fi < infiles.size(); fi++) {
				offset = count;
				String infile = c.toFile(infiles.elementAt(fi).toString());
				DataInputStream data = new DataInputStream(FSManager.fs().read(infile));
				if (data.readInt() != 0xC0DE0201)
					throw new Exception("Unsupported object format in "+infile);
				int lflags = data.readUnsignedByte();
				if (lflags != 0)
					throw new Exception("Relinkage of shared objects is not supported");
				int poolsize = data.readUnsignedShort();
				for (int oi = 0; oi < poolsize; oi++) {
					int type = data.readUnsignedByte();
					Object obj;
					switch (type) {
						case '0':
							obj = null;
							break;
						case 'i':
							obj = Int.toInt(data.readInt());
							break;
						case 'l':
							obj = new Long(data.readLong());
							break;
						case 'f':
							obj = new Float(data.readFloat());
							break;
						case 'd':
							obj = new Double(data.readDouble());
							break;
						case 'S':
							obj = data.readUTF();
							break;
						case 'U': {
							String id = data.readUTF();
							LibInfo info = (LibInfo)symbols.get(id);
							if (info != null) {
								obj = new ExFunc(id, info);
								info.used = true;
							} else {
								obj = new NELFunc(id);
							}
							break;
						}
						case 'P': {
							String name = data.readUTF();
							InFunc f = new InFunc(name);
							f.flags = data.readUnsignedByte();
							f.stack = data.readUnsignedByte();
							f.locals = data.readUnsignedByte();
							f.code = new byte[data.readUnsignedShort()];
							data.readFully(f.code);
							f.relocs = new int[data.readShort()];
							for (int i=0; i<f.relocs.length; i++) {
								f.relocs[i] = data.readUnsignedShort();
							}
							f.reloffset = offset;
							if ((f.flags & Opcodes.FFLAG_LNUM) != 0) {
								f.lnumtable = new char[data.readUnsignedShort()];
								for (int j=0; j< f.lnumtable.length; j++) {
									f.lnumtable[j] = data.readChar();
								}
							}
							if ((f.flags & Opcodes.FFLAG_ERRTBL) != 0) {
								f.errtable = new char[data.readUnsignedShort()];
								for (int j=0; j< f.errtable.length; j++) {
									f.errtable[j] = data.readChar();
								}
							}
							obj = f;
							break;
						}
						default:
							throw new Exception("Unknown object type: "+String.valueOf(type));
					}
					int objindex = pool.indexOf(obj);
					if (objindex < 0) {
						objindex = pool.size();
						pool.addElement(obj);
					} else if (obj instanceof NELFunc) {
						NELFunc old = (NELFunc)pool.elementAt(objindex);
						if (old instanceof ExFunc) {
							if (obj instanceof InFunc) {
								throw new Exception("Multiple definitions of "+obj);
							} else if (obj instanceof ExFunc) {
								//should be ok
							}
						} else if (old instanceof InFunc) {
							if (obj instanceof ExFunc || obj instanceof InFunc) {
								throw new Exception("Multiple definitions of "+obj);
							}
						} else { //old is unresolved, replace it without a doubt
							pool.setElementAt(obj, objindex);
						}
					}
					if (reloctable.length == count) {
						int[] newrelocs = new int[count << 1];
						System.arraycopy(reloctable, 0, newrelocs, 0, count);
						reloctable = newrelocs;
					}
					reloctable[count] = objindex;
					count++;
				}
				data.close();
			}
			//relocating
			for (Enumeration e = pool.elements(); e.hasMoreElements(); ) {
				Object obj = e.nextElement();
				if (obj instanceof InFunc) {
					InFunc f = (InFunc)obj;
					for (int ri=f.relocs.length-1; ri >= 0; ri--) {
						int r = f.relocs[ri]; // address in code with number to fix
						int oldaddr = ((f.code[r] & 0xff) << 8) | (f.code[r+1] & 0xff);
						int newaddr = reloctable[oldaddr+f.reloffset];
						f.code[r] = (byte)(newaddr >> 8);
						f.code[r+1] = (byte)newaddr;
					}
				} else if (obj.getClass() == NELFunc.class) {
					throw new Exception("Unresolved symbol: "+obj);
				}
			}
			//indexing libraries, throwing out unused
			int li = 0;
			while (li < libinfos.size()) {
				LibInfo info = (LibInfo)libinfos.elementAt(li);
				if (info.used) {
					info.index = li;
					li++;
				} else {
					libinfos.removeElementAt(li);
				}
			}
			//writing output
			String outfile = c.toFile(outname);
			DataOutputStream out = new DataOutputStream(FSManager.fs().write(outfile));
			out.writeInt(0xC0DE0201);
			if (soname != null) {
				out.writeByte(Opcodes.LFLAG_SONAME | Opcodes.LFLAG_DEPS);
				out.writeUTF(soname);
			} else {
				out.writeByte(Opcodes.LFLAG_DEPS);
			}
			out.writeShort(libinfos.size());
			for (int i=0; i < libinfos.size(); i++) {
				out.writeUTF(((LibInfo)libinfos.elementAt(i)).soname);
			}
			out.writeShort(pool.size());
			for (Enumeration e = pool.elements(); e.hasMoreElements(); ) {
				Object obj = e.nextElement();
				if (obj == null) {
					out.writeByte('0');
				} else if (obj.getClass() == Int.class) {
					out.writeByte('i');
					Int ival = (Int)obj;
					out.writeInt(ival.value);
				} else if (obj.getClass() == Long.class) {
					out.writeByte('l');
					Long lval = (Long)obj;
					out.writeLong(lval.longValue());
				} else if (obj.getClass() == Float.class) {
					out.writeByte('f');
					Float fval = (Float)obj;
					out.writeFloat(fval.floatValue());
				} else if (obj.getClass() == Double.class) {
					out.writeByte('d');
					Double dval = (Double)obj;
					out.writeDouble(dval.doubleValue());
				} else if (obj.getClass() == String.class) {
					out.writeByte('S');
					out.writeUTF(obj.toString());
				} else if (obj instanceof ExFunc) {
					out.writeByte('E');
					ExFunc ef = (ExFunc)obj;
					out.writeShort(ef.info.index);
					out.writeUTF(ef.name);
				} else if (obj instanceof InFunc) {
					InFunc func = (InFunc)obj;
					out.writeByte('P');
					out.writeUTF(func.name);
					out.writeByte(func.flags & ~Opcodes.FFLAG_RELOCS);
					out.writeByte(func.stack);
					out.writeByte(func.locals);
					out.writeShort(func.code.length);
					out.write(func.code);
					if ((func.flags & Opcodes.FFLAG_LNUM) != 0) {
						out.writeShort(func.lnumtable.length);
						for (int j=0; j<func.lnumtable.length; j++) {
							out.writeChar(func.lnumtable[j]);
						}
					}
					if ((func.flags & Opcodes.FFLAG_ERRTBL) != 0) {
						out.writeShort(func.errtable.length);
						for (int j=0; j<func.errtable.length; j++) {
							out.writeChar(func.errtable[j]);
						}
					}
				}
			}
			out.close();
			FSManager.fs().setExec(outfile, true);
		} catch (Exception e) {
			IO.println(c.stderr, "Error: "+e);
			// e.printStackTrace();
			return 1;
		}
		return 0;
	}

	private LibInfo loadLibInfo(Context c, String libname) throws Exception {
		String libfile = c.resolveFile(libname, c.getEnv("LIBPATH"));
		InputStream in = FSManager.fs().read(libfile);
		int magic = in.read() << 8 | in.read();
		LibInfo info;
		if (magic < 0) {
			in.close();
			throw new Exception("File is too short: "+libfile);
		} else if (magic == ('#'<<8|'=')) {
			byte[] buf = IO.readFully(in);
			in.close();
			info = loadLibInfo(c, IO.utfDecode(buf).trim());
		} else if (magic == 0xC0DE) {
			info = loadFromECode(in);
		} else if (magic == ('#'<<8|'@')) {
			info = loadFromNative(in);
		} else {
			throw new Exception("Unknown library format in "+libfile);
		}
		if (info.soname == null) info.soname = libname;
		return info;
	}

	private LibInfo loadFromECode(InputStream in) throws Exception {
		DataInputStream data = new DataInputStream(in);
		if (data.readUnsignedShort() > SUPPORTED)
			throw new Exception("Unsupported format version");
		LibInfo info = new LibInfo();
		Vector symbols = new Vector();
		int lflags = data.readUnsignedByte();
		if ((lflags & Opcodes.LFLAG_SONAME) != 0) { //has soname
			info.soname = data.readUTF();
		}
		//skipping dependencies
		int depsize = data.readUnsignedShort();
		for (int i=0; i<depsize; i++) {
			data.skipBytes(data.readUnsignedShort());
		}
		//reading pool
		int poolsize = data.readUnsignedShort();
		for (int i=0; i<poolsize; i++) {
			int ch = data.readUnsignedByte();
			switch (ch) {
				case '0':
					break;
				case 'i':
				case 'f':
					data.skipBytes(4);
					break;
				case 'l':
				case 'd':
					data.skipBytes(8);
					break;
				case 'S':
					data.skipBytes(data.readUnsignedShort());
					break;
				case 'E':
					data.skipBytes(2);
					data.skipBytes(data.readUnsignedShort());
					break;
				case 'P': {
					String name = data.readUTF();
					int flags = data.readUnsignedByte();
					data.skipBytes(2);
					data.skipBytes(data.readUnsignedShort());
					if ((flags & Opcodes.FFLAG_SHARED) != 0) {
						symbols.addElement(name);
					}
					if ((flags & Opcodes.FFLAG_LNUM) != 0) {
						data.skipBytes(data.readUnsignedShort()*2);
					}
					if ((flags & Opcodes.FFLAG_ERRTBL) != 0) {
						data.skipBytes(data.readUnsignedShort()*2);
					}
					break;
				}
				default:
					throw new Exception("Unknown object type: "+ch);
			}
		}
		in.close();
		info.symbols = symbols;
		return info;
	}

	private LibInfo loadFromNative(InputStream in) throws IOException {
		UTFReader r = new UTFReader(in);
		r.readLine(); //skip classname
		Properties prop = Properties.readFrom(r);
		r.close();
		LibInfo info = new LibInfo();
		info.soname = prop.get("soname");
		String symbolfile = prop.get("symbols");
		if (symbolfile == null) {
			info.symbols = new Vector();
		} else {
			Vector vsym = new Vector();
			InputStream symstream = getClass().getResourceAsStream(symbolfile);
			byte[] buf = new byte[symstream.available()];
			symstream.read(buf);
			symstream.close();
			final String[] syms = IO.split(IO.utfDecode(buf), '\n');
			for (int i=0; i < syms.length; i++) {
				vsym.addElement(syms[i]);
			}
			info.symbols = vsym;
		}
		return info;
	}
}

class LibInfo {
	/** Shared object name. */
	String soname;
	/** Symbols that this library provides. */
	Vector symbols;
	/** Index assigned to this library by linker. */
	int index;
	/** Are symbols from this library actually used? */
	boolean used;
}

/** Unknown function. */
class NELFunc {
	String name;

	public NELFunc(String name) {
		this.name = name;
	}

	public boolean equals(Object o) {
		if (o == null) return false;
		if (!(o instanceof NELFunc)) return false;
		return ((NELFunc)o).name.equals(this.name);
	}

	public String toString() {
		return '\''+name+'\'';
	}
}

/** Internal function. */
class InFunc extends NELFunc {
	int stack;
	int locals;
	int flags;
	byte[] code;
	int[] relocs;
	int reloffset;
	
	char[] lnumtable;
	char[] errtable;

	public InFunc(String name) {
		super(name);
	}
}

/** External function. */
class ExFunc extends NELFunc {
	LibInfo info;
	
	public ExFunc(String name, LibInfo info) {
		super(name);
		this.info = info;
	}
}
