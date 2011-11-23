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

package alchemy.libs;

import alchemy.core.Context;
import alchemy.core.Function;
import alchemy.core.Library;
import alchemy.fs.File;
import alchemy.l10n.I18N;
import alchemy.util.IO;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import javax.microedition.io.Connector;

/**
 * Core runtime function set.
 * @author Sergey Basalaev
 */
class LibCore20Func extends Function {

	private final int index;
	private Random rnd = new Random(0);

	public LibCore20Func(String name, int index) {
		super(name);
		this.index = index;
	}

	protected Object exec(Context c, Object[] args) throws Exception {
		switch (index) {
			case 0: // fname(f: File): String
				return ((File)args[0]).name();
			case 1: // fpath(f: File): String
				return ((File)args[0]).path();
			case 2: // fparent(f: File): File
				return ((File)args[0]).parent();
			case 3: // fcreate(f: File)
				c.fs().create((File)args[0]);
				return null;
			case 4: // fremove(f: File)
				c.fs().remove((File)args[0]);
				return null;
			case 5: // mkdir(f: File)
				c.fs().mkdir((File)args[0]);
				return null;
			case 6: // fcopy(src: File, dest: File)
				c.fs().copy((File)args[0], (File)args[1]);
				return null;
			case 7: // fmove(src: File, dest: File)
				c.fs().move((File)args[0], (File)args[1]);
				return null;
			case 8: // set_read(f: File, on: Bool)
				c.fs().setRead((File)args[0], bval(args[1]));
				return null;
			case 9: // set_write(f: File, on: Bool)
				c.fs().setWrite((File)args[0], bval(args[1]));
				return null;
			case 10: // set_exec(f: File, on: Bool)
				c.fs().setExec((File)args[0], bval(args[1]));
				return null;
			case 11: // can_read(f: File): Bool
				return Ival(c.fs().canRead((File)args[0]));
			case 12: // can_write(f: File): Bool
				return Ival(c.fs().canWrite((File)args[0]));
			case 13: // can_exec(f: File): Bool
				return Ival(c.fs().canExec((File)args[0]));
			case 14: // exists(f: File): Bool
				return Ival(c.fs().exists((File)args[0]));
			case 15: // is_dir(f: File): Bool
				return Ival(c.fs().isDirectory((File)args[0]));
			case 16: { // fopen_r(f: File): IStream
				InputStream stream = c.fs().read((File)args[0]);
				c.addStream(stream);
				return stream;
			}
			case 17: { // fopen_w(f: File): OStream
				OutputStream stream = c.fs().write((File)args[0]);
				c.addStream(stream);
				return stream;
			}
			case 18: { // fopen_a(f: File): OStream
				OutputStream stream = c.fs().append((File)args[0]);
				c.addStream(stream);
				return stream;
			}
			case 19: // flist(f: File): Array
				return c.fs().list((File)args[0]);
			case 20: // fmodified(f: File): Long
				return Lval(c.fs().lastModified((File)args[0]));
			case 21: // fsize(f: File): Int
				return Ival(c.fs().size((File)args[0]));
			case 22: // set_cwd(f: File)
				c.setCurDir((File)args[0]);
				return null;
			case 23: // relpath(f: File): String
				return LibCore20.relPath(c, (File)args[0]);
			case 24: // strlen(str: String): Int
				return Ival(((String)args[0]).length());
			case 25: // strchr(str: String, at: Int): Int
				return Ival(((String)args[0]).charAt(ival(args[1])));
			case 26: // strindex(str: String, ch: Int): Int
				return Ival(((String)args[0]).indexOf(ival(args[1])));
			case 27: // strlindex(str: String, ch: Int): Int
				return Ival(((String)args[0]).lastIndexOf(ival(args[1])));
			case 28: // strstr(str: String, sub: String): Int
				return Ival(((String)args[0]).indexOf((String)args[1]));
			case 29: // substr(str: String, from: Int, to: Int): String
				return ((String)args[0]).substring(ival(args[1]), ival(args[2]));
			case 30: // strucase(str: String): String
				return ((String)args[0]).toUpperCase();
			case 31: // strlcase(str: String): String
				return ((String)args[0]).toLowerCase();
			case 32: // strcat(s1: String, s2: String): String
				return ((String)args[0]).concat((String)args[1]);
			case 33: // strcmp(s1: String, s2: String): Int
				return Ival(((String)args[0]).compareTo((String)args[1]));
			case 34: // strchars(str: String): CArray
				return ((String)args[0]).toCharArray();
			case 35: // strtrim(str: String): String
				return ((String)args[0]).trim();
			case 36: // to_file(path: String): File
				return c.toFile(((String)args[0]));
			case 37: // getenv(key: String): String
				return c.getEnv(((String)args[0]));
			case 38: // setenv(key: String, value: String)
				c.setEnv(((String)args[0]), (String)args[1]);
				return null;
			case 39: // utfbytes(str: String): BArray
				return IO.utfEncode((String)args[0]);
			case 40: // datestr(date: Long): String
				return new Date(lval(args[0])).toString();
			case 41: { // year(date: Long): Int
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.YEAR));
			}
			case 42: { // month(date: Long): Int
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.MONTH));
			}
			case 43: { // day(date: Long): Int
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.DAY_OF_MONTH));
			}
			case 44: { // dow(date: Long): Int
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.DAY_OF_WEEK));
			}
			case 45: { // hour(date: Long): Int
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.HOUR_OF_DAY));
			}
			case 46: { // minute(date: Long): Int
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.MINUTE));
			}
			case 47: { // second(date: Long): Int
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.SECOND));
			}
			case 48: // sb_append(sb: StrBuf, a: Any): StrBuf
				return ((StringBuffer)args[0]).append(args[1]);
			case 49: // sb_addch(sb: StrBuf, ch: Int): StrBuf
				return ((StringBuffer)args[0]).append((char)ival(args[1]));
			case 50: // sb_delete(sb: StrBuf, from: Int, to: Int): StrBuf
				return ((StringBuffer)args[0]).delete(ival(args[1]), ival(args[2]));
			case 51: // sb_delch(sb: StrBuf, at: Int): StrBuf
				return ((StringBuffer)args[0]).deleteCharAt(ival(args[1]));
			case 52: // sb_insert(sb: StrBuf, at: Int, a: Any): StrBuf
				return ((StringBuffer)args[0]).insert(ival(args[1]), args[2]);
			case 53: // sb_insch(sb: StrBuf, at: Int, ch: Int): StrBuf
				return ((StringBuffer)args[0]).insert(ival(args[1]), (char)ival(args[2]));
			case 54: // sb_setch(sb: StrBuf, at: Int, ch: Int): StrBuf
				((StringBuffer)args[0]).setCharAt(ival(args[1]), (char)ival(args[2]));
				return args[0];
			case 55: // sb_len(sb: StrBuf): Int
				return Ival(((StringBuffer)args[0]).length());
			case 56: // millis(date: Long): Int
				return Ival((int)(lval(args[0]) % 1000));
			case 57: // systime(): Long
				return Lval(System.currentTimeMillis());
			case 58: // get_cwd(): File
				return c.getCurDir();
			case 59: // space_total(): Long
				return Lval(c.fs().spaceTotal());
			case 60: // space_free(): Long
				return Lval(c.fs().spaceFree());
			case 61: // space_used(): Long
				return Lval(c.fs().spaceUsed());
			case 62: // to_str(a: Any): String
				return String.valueOf(args[0]);
			case 63: // new_sb(): StrBuf
				return new StringBuffer();
			case 64: // fclose(stream: Any)
				if (args[0] instanceof InputStream) {
					((InputStream)args[0]).close();
					c.removeStream(args[0]);
					return null;
				} else if (args[0] instanceof OutputStream) {
					((OutputStream)args[0]).close();
					c.removeStream(args[0]);
					return null;
				} else {
					throw new ClassCastException();
				}
			case 65: // fread(in: IStream): Int
				return Ival(((InputStream)args[0]).read());
			case 66: // freadarray(in: IStream, buf: BArray, ofs: Int, len: Int): Int
				return Ival(((InputStream)args[0]).read((byte[])args[1], ival(args[2]), ival(args[3])));
			case 67: // fskip(in: IStream, n: Long): Long
				return Lval(((InputStream)args[0]).skip(lval(args[1])));
			case 68: // fwrite(out: OStream, b: Int)
				((OutputStream)args[0]).write(ival(args[1]));
				return null;
			case 69: // fwritearray(out: OStream, buf: BArray, ofs: Int, len: Int)
				((OutputStream)args[0]).write((byte[])args[1], ival(args[2]), ival(args[3]));
				return null;
			case 70: // fflush(out: OStream)
				((OutputStream)args[0]).flush();
				return null;
			case 71: { // exec_wait(cmd: String, args: Array): Int
				String prog = (String)args[0];
				Object[] oargs = (Object[])args[1];
				Context cc = new Context(c);
				String[] sargs = new String[oargs.length];
				for (int i=oargs.length-1; i>=0; i--) {
					sargs[i] = String.valueOf(oargs[i]);
				}
				return Ival(cc.startAndWait(prog, sargs));
			}
			case 72: { // exec(cmd: String, args: Array)
				String prog = (String)args[0];
				Object[] oargs = (Object[])args[1];
				Context cc = new Context(c);
				String[] sargs = new String[oargs.length];
				for (int i=oargs.length-1; i>=0; i--) {
					sargs[i] = String.valueOf(oargs[i]);
				}
				cc.start(prog, sargs);
				return null;
			}
			case 73: // bacopy(src: BArray, sofs: Int, dest: BArray, dofs: Int, len: Int)
			case 74: // cacopy(src: CArray, sofs: Int, dest: CArray, dofs: Int, len: Int)
			case 75: // acopy(src: Array, sofs: Int, dest: Array, dofs: Int, len: Int)
				System.arraycopy(args[0], ival(args[1]), args[2], ival(args[3]), ival(args[4]));
				return null;
			case 76: // fprint(out: OStream, a: Any): OStream
				IO.print((OutputStream)args[0], args[1]);
				return args[0];
			case 77: // fprintln(out: OStream, a: Any): OStream
				IO.println((OutputStream)args[0], args[1]);
				return args[0];
			case 78: // stdin(): IStream
				return c.stdin;
			case 79: // stdout(): OStream
				return c.stdout;
			case 80: // stderr(): OStream
				return c.stderr;
			case 81: // setin(in: IStream)
				c.stdin = (InputStream)args[0];
				return null;
			case 82: // setout(out: OStream)
				c.stdout = (OutputStream)args[0];
				return null;
			case 83: // seterr(out: OStream)
				c.stderr = (OutputStream)args[0];
				return null;
			case 84: // abs(a: Double): Double
				return Dval(Math.abs(dval(args[0])));
			case 85: // sin(a: Double): Double
				return Dval(Math.sin(dval(args[0])));
			case 86: // cos(a: Double): Double
				return Dval(Math.cos(dval(args[0])));
			case 87: // tan(a: Double): Double
				return Dval(Math.tan(dval(args[0])));
			case 88: // sqrt(a: Double): Double
				return Dval(Math.sqrt(dval(args[0])));
			case 89: // ipow(a: Double, n: Int): Double
				return Dval(LibCore20.ipow(dval(args[0]), ival(args[1])));
			case 90: // exp(a: Double): Double
				return Dval(LibCore20.exp(dval(args[0])));
			case 91: // log(a: Double): Double
				return Dval(LibCore20.log(dval(args[0])));
			case 92: // asin(a: Double): Double
				return Dval(LibCore20.asin(dval(args[0])));
			case 93: // acos(a: Double): Double
				return Dval(LibCore20.acos(dval(args[0])));
			case 94: // atan(a: Double): Double
				return Dval(LibCore20.atan(dval(args[0])));
			case 95: // ca2str(chars: CArray): String
				return new String((char[])args[0]);
			case 96: // ba2utf(bytes: BArray): String
				return IO.utfDecode((byte[])args[0]);
			case 97: // ibits2f(bits: Int): Float
				return Fval(Float.intBitsToFloat(ival(args[0])));
			case 98: // f2ibits(f: Float): Int
				return Ival(Float.floatToIntBits(fval(args[0])));
			case 99: // lbits2d(bits: Long): Double
				return Dval(Double.longBitsToDouble(lval(args[0])));
			case 100: // d2lbits(d: Double): Long
				return Lval(Double.doubleToLongBits(dval(args[0])));
			case 101: // new_ht(): Hashtable
				return new Hashtable();
			case 102: // ht_put(ht: Hashtable, key: Any, value: Any)
				((Hashtable)args[0]).put(args[1], args[2]);
				return null;
			case 103: // ht_get(ht: Hashtable, key: Any): Any
				return ((Hashtable)args[0]).get(args[1]);
			case 104: // ht_rm(ht: Hashtable, key: Any)
				((Hashtable)args[0]).remove(args[1]);
				return null;
			case 105: // hash(a: Any): Int
				return Ival(args[0].hashCode());
			case 106: // rnd(n: Int): Int
				return Ival(rnd.nextInt(ival(args[0])));
			case 107: // rndint(): Int
				return Ival(rnd.nextInt());
			case 108: // rndlong(): Long
				return Lval(rnd.nextLong());
			case 109: // rndfloat(): Float
				return Fval(rnd.nextFloat());
			case 110: // rnddouble(): Double
				return Dval(rnd.nextDouble());
			case 111: { // netread(url: String): IStream
				String addr = (String)args[0];
				if (!addr.startsWith("http:")&&!addr.startsWith("https:"))
					throw new IOException(I18N._("Unknown protocol in netread()"));
				InputStream stream = Connector.openInputStream(addr);
				c.addStream(stream);
				return stream;
			}
			case 112: { // readresource(res: String): IStream
				InputStream stream = Object.class.getResourceAsStream((String)args[0]);
				if (stream != null) c.addStream(stream);
				return stream;
			}
			case 113: // loadlibrary(name: String): Library
				return c.loadLibrary((String)args[0]);
			case 114: // loadfunc(lib: Library, sig: String): Any
				return ((Library)args[0]).getFunc((String)args[1]);
			case 115: {// clone(struct: Any): Any
				Object[] struct = (Object[])args[0];
				Object[] clone = new Object[struct.length];
				System.arraycopy(struct, 0, clone, 0, struct.length);
				return clone;
			}
			case 116: // strsplit(s: String): Array
				return IO.split((String)args[0], (char)ival(args[1]));
			case 117: // parsei(s: String): Int
				return Ival(Integer.parseInt((String)args[0]));
			case 118: // parsel(s: String): Long
				return Lval(Long.parseLong((String)args[0]));
			case 119: // parsef(s: String): Float
				return Fval(Float.parseFloat((String)args[0]));
			case 120: // parsed(s: String): Double
				return Dval(Double.parseDouble((String)args[0]));
			case 121: // !getstatic(key: Any): Any
				return c.get(args[0]);
			case 122: // !setstatic(key: Any, val: Any)
				c.set(args[0], args[1]);
				return null;
			case 123: // sprintf(fmt: String, args: Array): String
				return IO.printf((String)args[0], (Object[])args[1]);
			case 124: { // ht_keys(ht: Hashtable): Array
				Vector keyv = new Vector();
				for (Enumeration e = ((Hashtable)args[0]).keys(); e.hasMoreElements(); ) {
					keyv.addElement(e.nextElement());
				}
				Object[] keys = new Object[keyv.size()];
				for (int i=keys.length-1; i>=0; i--) {
					keys[i] = keyv.elementAt(i);
				}
				return keys;
			}
			default:
				return null;
		}
	}
	
	public String toString() {
		return "libcore:"+signature;
	}
}
