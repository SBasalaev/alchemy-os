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
 */

package alchemy.nec;

import alchemy.core.Context;
import alchemy.evm.ELibBuilder;
import alchemy.fs.File;
import alchemy.util.I18N;
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
 * Native E linker.
 * @author Sergey Basalaev
 */
public class NEL extends NativeApp {

	/** Highest supported library file format version.
	 * @see ELibBuilder#VERSION
	 */
	static private final int SUPPORTED = 0x0101;

	static private final String VERSION =
			I18N._("Native E linker version 1.2");

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
				IO.println(c.stderr, I18N._("Unknown argument: {0}", arg));
				return 1;
			} else if (wait_outname) {
				outname = arg;
				wait_outname = false;
			} else {
				infiles.addElement(arg);
			}
		}
		if (infiles.isEmpty()) {
			IO.println(c.stderr, I18N._("No files to process"));
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
				File infile = c.toFile(infiles.elementAt(fi).toString());
				DataInputStream data = new DataInputStream(c.fs().read(infile));
				if (data.readInt() != 0xC0DE0101)
					throw new Exception(I18N._("Unsupported object format in {0}", infile));
				int lflags = data.readUnsignedByte();
				if (lflags != 0)
					throw new Exception(I18N._("Relinkage of shared objects is not supported"));
				int poolsize = data.readUnsignedShort();
				for (int oi = 0; oi < poolsize; oi++) {
					int type = data.readUnsignedByte();
					Object obj;
					switch (type) {
						case '0':
							obj = null;
							break;
						case 'i':
							obj = new Integer(data.readInt());
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
							int index = reloctable[offset+data.readUnsignedShort()];
							String id = pool.elementAt(index).toString();
							LibInfo info = (LibInfo)symbols.get(id);
							if (info != null) {
								obj = new ExFunc(id, info);
								info.used = true;
							} else {
								obj = new NELFunc(id);
							}
							break;
						}
						case 'E':
							throw new Exception(I18N._("Partial linkage is not supported"));
						case 'H':
						case 'P': {
							String name = pool.elementAt(reloctable[offset+data.readUnsignedShort()]).toString();
							InFunc f = new InFunc(name);
							f.type = type;
							f.flags = data.readUnsignedByte();
							if ((f.flags & 1) == 0)
								throw new Exception(I18N._("Missing relocation table in {0}", f.name));
							f.stack = data.readUnsignedByte();
							f.locals = data.readUnsignedByte();
							f.code = new byte[data.readUnsignedShort()];
							data.readFully(f.code);
							f.relocs = new int[data.readShort()];
							for (int i=0; i<f.relocs.length; i++) {
								f.relocs[i] = data.readUnsignedShort();
							}
							f.reloffset = offset;
							obj = f;
							break;
						}
						default:
							throw new Exception(I18N._("Unknown object type: {0}", String.valueOf(type)));
					}
					int objindex = pool.indexOf(obj);
					if (objindex < 0) {
						objindex = pool.size();
						pool.addElement(obj);
					} else if (obj instanceof NELFunc) {
						NELFunc old = (NELFunc)pool.elementAt(objindex);
						if (old instanceof ExFunc) {
							if (obj instanceof InFunc) {
								throw new Exception(I18N._("Multiple definitions of {0}", obj));
							} else if (obj instanceof ExFunc) {
								//should be ok
							}
						} else if (old instanceof InFunc) {
							if (obj instanceof ExFunc || obj instanceof InFunc) {
								throw new Exception(I18N._("Multiple definitions of {0}", obj));
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
					throw new Exception(I18N._("Unresolved symbol: {0}", obj));
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
			File outfile = c.toFile(outname);
			DataOutputStream out = new DataOutputStream(c.fs().write(outfile));
			out.writeInt(0xC0DE0101);
			if (soname != null) {
				out.writeByte(5);
				out.writeUTF(soname);
			} else {
				out.writeByte(1);
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
				} else if (obj instanceof Integer) {
					out.writeByte('i');
					Integer ival = (Integer)obj;
					out.writeInt(ival.intValue());
				} else if (obj instanceof Long) {
					out.writeByte('l');
					Long lval = (Long)obj;
					out.writeLong(lval.longValue());
				} else if (obj instanceof Float) {
					out.writeByte('f');
					Float fval = (Float)obj;
					out.writeFloat(fval.floatValue());
				} else if (obj instanceof Double) {
					out.writeByte('d');
					Double dval = (Double)obj;
					out.writeDouble(dval.doubleValue());
				} else if (obj instanceof String) {
					out.writeByte('S');
					out.writeUTF(obj.toString());
				} else if (obj instanceof ExFunc) {
					out.writeByte('E');
					ExFunc ef = (ExFunc)obj;
					out.writeShort(ef.info.index);
					out.writeShort(pool.indexOf(ef.name));
				} else if (obj instanceof InFunc) {
					InFunc func = (InFunc)obj;
					out.writeByte(func.type);
					out.writeShort(pool.indexOf(func.name));
					out.writeByte(func.flags & 0xfe); //no relocs
					out.writeByte(func.stack);
					out.writeByte(func.locals);
					out.writeShort(func.code.length);
					out.write(func.code);
				}
			}
			out.close();
			c.fs().setExec(outfile, true);
		} catch (Exception e) {
			IO.println(c.stderr, I18N._("Error: {0}", e));
			e.printStackTrace();
			return 1;
		}
		return 0;
	}

	private LibInfo loadLibInfo(Context c, String libname) throws Exception {
		File libfile = c.resolveFile(libname, c.getEnv("LIBPATH"));
		InputStream in = c.fs().read(libfile);
		int magic = in.read() << 8 | in.read();
		LibInfo info;
		if (magic < 0) {
			in.close();
			throw new Exception(I18N._("File is too short: {0}", libfile));
		} else if (magic == ('#'<<8|'=')) {
			byte[] buf = IO.readFully(in);
			in.close();
			info = loadLibInfo(c, IO.utfDecode(buf).trim());
		} else if (magic == 0xC0DE) {
			info = loadFromECode(in);
		} else if (magic == ('#'<<8|'@')) {
			info = loadFromNative(in);
		} else {
			throw new Exception(I18N._("Unknown library format in {0}", libfile));
		}
		if (info.soname == null) info.soname = libname;
		return info;
	}

	private LibInfo loadFromECode(InputStream in) throws Exception {
		DataInputStream data = new DataInputStream(in);
		if (data.readUnsignedShort() > 0x0101)
			throw new Exception(I18N._("Unsupported format version"));
		LibInfo info = new LibInfo();
		Vector symbols = new Vector();
		int lflags = data.readUnsignedByte();
		if ((lflags & 1) == 0) { //not linked
			throw new Exception(I18N._("Not shared object passed to -l"));
		}
		if ((lflags & 4) != 0) { //has soname
			info.soname = data.readUTF();
		}
		//skipping dependencies
		int depsize = data.readUnsignedShort();
		for (int i=0; i<depsize; i++) {
			data.skipBytes(data.readUnsignedShort());
		}
		//reading pool
		int poolsize = data.readUnsignedShort();
		String[] symbolstr = new String[poolsize];
		for (int i=0; i<poolsize; i++) {
			int ch = data.readUnsignedByte();
			switch (ch) {
				case '0':
					break;
				case 'i':
				case 'l':
					data.skipBytes(4);
					break;
				case 'f':
				case 'd':
					data.skipBytes(8);
					break;
				case 'S':
					symbolstr[i] = data.readUTF();
					break;
				case 'E':
					data.skipBytes(4);
					break;
				case 'H':
				case 'P': {
					int nameref = data.readUnsignedShort();
					if (ch == 'P') symbols.addElement(symbolstr[nameref]);
					int fflags = data.readUnsignedByte();
					data.skipBytes(2);
					data.skipBytes(data.readUnsignedShort());
					if ((fflags & 1) != 0) {
						data.skipBytes(data.readUnsignedShort() << 1);
					}
					break;
				}
				case 'U':
					throw new Exception(I18N._("Unresolved symbol: {0}", data.readUTF()));
				default:
					throw new Exception(I18N._("Unknown object type: ", String.valueOf(ch)));
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
	int type;
	int stack;
	int locals;
	int flags;
	byte[] code;
	int[] relocs;
	int reloffset;

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
