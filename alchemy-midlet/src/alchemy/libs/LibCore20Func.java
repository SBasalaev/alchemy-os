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

package alchemy.libs;

import alchemy.core.Context;
import alchemy.core.Function;
import alchemy.core.Library;
import alchemy.nlib.NativeFunction;
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
class LibCore20Func extends NativeFunction {

	private static final Random rnd = new Random(0);

	public LibCore20Func(String name, int index) {
		super(name, index);
	}

	protected Object exec(Context c, Object[] args) throws Exception {
		switch (index) {
			case 0: // pathfile(f: String): String
				return c.toFile((String)args[0]).name();
			case 1: // pathdir(f: String): String
				return c.toFile((String)args[0]).parent().path();
			case 2: // abspath(f: String): String
				return c.toFile((String)args[0]).path();
			case 3: // fcreate(f: String)
				c.fs().create(c.toFile((String)args[0]));
				return null;
			case 4: // fremove(f: String)
				c.fs().remove(c.toFile((String)args[0]));
				return null;
			case 5: // mkdir(f: String)
				c.fs().mkdir(c.toFile((String)args[0]));
				return null;
			case 6: // fcopy(src: String, dest: String)
				c.fs().copy(c.toFile((String)args[0]), c.toFile((String)args[1]));
				return null;
			case 7: // fmove(src: String, dest: String)
				c.fs().move(c.toFile((String)args[0]), c.toFile((String)args[1]));
				return null;
			case 8: // set_read(f: String, on: Bool)
				c.fs().setRead(c.toFile((String)args[0]), bval(args[1]));
				return null;
			case 9: // set_write(f: String, on: Bool)
				c.fs().setWrite(c.toFile((String)args[0]), bval(args[1]));
				return null;
			case 10: // set_exec(f: String, on: Bool)
				c.fs().setExec(c.toFile((String)args[0]), bval(args[1]));
				return null;
			case 11: // can_read(f: String): Bool
				return Ival(c.fs().canRead(c.toFile((String)args[0])));
			case 12: // can_write(f: String): Bool
				return Ival(c.fs().canWrite(c.toFile((String)args[0])));
			case 13: // can_exec(f: String): Bool
				return Ival(c.fs().canExec(c.toFile((String)args[0])));
			case 14: // exists(f: String): Bool
				return Ival(c.fs().exists(c.toFile((String)args[0])));
			case 15: // is_dir(f: String): Bool
				return Ival(c.fs().isDirectory(c.toFile((String)args[0])));
			case 16: { // fopen_r(f: String): IStream
				InputStream stream = c.fs().read(c.toFile((String)args[0]));
				c.addStream(stream);
				return stream;
			}
			case 17: { // fopen_w(f: String): OStream
				OutputStream stream = c.fs().write(c.toFile((String)args[0]));
				c.addStream(stream);
				return stream;
			}
			case 18: { // fopen_a(f: String): OStream
				OutputStream stream = c.fs().append(c.toFile((String)args[0]));
				c.addStream(stream);
				return stream;
			}
			case 19: // flist(f: String): Array
				return c.fs().list(c.toFile((String)args[0]));
			case 20: // fmodified(f: String): Long
				return Lval(c.fs().lastModified(c.toFile((String)args[0])));
			case 21: // fsize(f: String): Int
				return Ival(c.fs().size(c.toFile((String)args[0])));
			case 22: // set_cwd(f: String)
				c.setCurDir(c.toFile((String)args[0]));
				return null;
			case 23: // relpath(f: String): String
				return LibCore20.relPath(c, c.toFile((String)args[0]));
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
			case 36: // getenv(key: String): String
				return c.getEnv(((String)args[0]));
			case 37: // setenv(key: String, value: String)
				c.setEnv(((String)args[0]), (String)args[1]);
				return null;
			case 38: // utfbytes(str: String): BArray
				return IO.utfEncode((String)args[0]);
			case 39: // datestr(date: Long): String
				return new Date(lval(args[0])).toString();
			case 40: { // year(date: Long): Int
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.YEAR));
			}
			case 41: { // month(date: Long): Int
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.MONTH));
			}
			case 42: { // day(date: Long): Int
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.DAY_OF_MONTH));
			}
			case 43: { // dow(date: Long): Int
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.DAY_OF_WEEK));
			}
			case 44: { // hour(date: Long): Int
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.HOUR_OF_DAY));
			}
			case 45: { // minute(date: Long): Int
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.MINUTE));
			}
			case 46: { // second(date: Long): Int
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.SECOND));
			}
			case 47: // sb_append(sb: StrBuf, a: Any): StrBuf
				return ((StringBuffer)args[0]).append(args[1]);
			case 48: // sb_addch(sb: StrBuf, ch: Int): StrBuf
				return ((StringBuffer)args[0]).append((char)ival(args[1]));
			case 49: // sb_delete(sb: StrBuf, from: Int, to: Int): StrBuf
				return ((StringBuffer)args[0]).delete(ival(args[1]), ival(args[2]));
			case 50: // sb_delch(sb: StrBuf, at: Int): StrBuf
				return ((StringBuffer)args[0]).deleteCharAt(ival(args[1]));
			case 51: // sb_insert(sb: StrBuf, at: Int, a: Any): StrBuf
				return ((StringBuffer)args[0]).insert(ival(args[1]), args[2]);
			case 52: // sb_insch(sb: StrBuf, at: Int, ch: Int): StrBuf
				return ((StringBuffer)args[0]).insert(ival(args[1]), (char)ival(args[2]));
			case 53: // sb_setch(sb: StrBuf, at: Int, ch: Int): StrBuf
				((StringBuffer)args[0]).setCharAt(ival(args[1]), (char)ival(args[2]));
				return args[0];
			case 54: // sb_len(sb: StrBuf): Int
				return Ival(((StringBuffer)args[0]).length());
			case 55: // millis(date: Long): Int
				return Ival((int)(lval(args[0]) % 1000));
			case 56: // systime(): Long
				return Lval(System.currentTimeMillis());
			case 57: // get_cwd(): String
				return c.getCurDir().path();
			case 58: // space_total(): Long
				return Lval(c.fs().spaceTotal());
			case 59: // space_free(): Long
				return Lval(c.fs().spaceFree());
			case 60: // space_used(): Long
				return Lval(c.fs().spaceUsed());
			case 61: // to_str(a: Any): String
				return String.valueOf(args[0]);
			case 62: // new_sb(): StrBuf
				return new StringBuffer();
			case 63: // fclose(stream: Any)
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
			case 64: // fread(in: IStream): Int
				return Ival(((InputStream)args[0]).read());
			case 65: // freadarray(in: IStream, buf: BArray, ofs: Int, len: Int): Int
				return Ival(((InputStream)args[0]).read((byte[])args[1], ival(args[2]), ival(args[3])));
			case 66: // fskip(in: IStream, n: Long): Long
				return Lval(((InputStream)args[0]).skip(lval(args[1])));
			case 67: // fwrite(out: OStream, b: Int)
				((OutputStream)args[0]).write(ival(args[1]));
				return null;
			case 68: // fwritearray(out: OStream, buf: BArray, ofs: Int, len: Int)
				((OutputStream)args[0]).write((byte[])args[1], ival(args[2]), ival(args[3]));
				return null;
			case 69: // fflush(out: OStream)
				((OutputStream)args[0]).flush();
				return null;
			case 70: { // exec_wait(cmd: String, args: Array): Int
				String prog = (String)args[0];
				Object[] oargs = (Object[])args[1];
				Context cc = new Context(c);
				String[] sargs = new String[oargs.length];
				for (int i=oargs.length-1; i>=0; i--) {
					sargs[i] = String.valueOf(oargs[i]);
				}
				return Ival(cc.startAndWait(prog, sargs));
			}
			case 71: { // exec(cmd: String, args: Array)
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
			case 72: // bacopy(src: BArray, sofs: Int, dest: BArray, dofs: Int, len: Int)
			case 73: // cacopy(src: CArray, sofs: Int, dest: CArray, dofs: Int, len: Int)
			case 74: // acopy(src: Array, sofs: Int, dest: Array, dofs: Int, len: Int)
				System.arraycopy(args[0], ival(args[1]), args[2], ival(args[3]), ival(args[4]));
				return null;
			case 75: // fprint(out: OStream, a: Any): OStream
				IO.print((OutputStream)args[0], args[1]);
				return args[0];
			case 76: // fprintln(out: OStream, a: Any): OStream
				IO.println((OutputStream)args[0], args[1]);
				return args[0];
			case 77: // stdin(): IStream
				return c.stdin;
			case 78: // stdout(): OStream
				return c.stdout;
			case 79: // stderr(): OStream
				return c.stderr;
			case 80: // setin(in: IStream)
				c.stdin = (InputStream)args[0];
				return null;
			case 81: // setout(out: OStream)
				c.stdout = (OutputStream)args[0];
				return null;
			case 82: // seterr(out: OStream)
				c.stderr = (OutputStream)args[0];
				return null;
			case 83: // abs(a: Double): Double
				return Dval(Math.abs(dval(args[0])));
			case 84: // sin(a: Double): Double
				return Dval(Math.sin(dval(args[0])));
			case 85: // cos(a: Double): Double
				return Dval(Math.cos(dval(args[0])));
			case 86: // tan(a: Double): Double
				return Dval(Math.tan(dval(args[0])));
			case 87: // sqrt(a: Double): Double
				return Dval(Math.sqrt(dval(args[0])));
			case 88: // ipow(a: Double, n: Int): Double
				return Dval(LibCore20.ipow(dval(args[0]), ival(args[1])));
			case 89: // exp(a: Double): Double
				return Dval(LibCore20.exp(dval(args[0])));
			case 90: // log(a: Double): Double
				return Dval(LibCore20.log(dval(args[0])));
			case 91: // asin(a: Double): Double
				return Dval(LibCore20.asin(dval(args[0])));
			case 92: // acos(a: Double): Double
				return Dval(LibCore20.acos(dval(args[0])));
			case 93: // atan(a: Double): Double
				return Dval(LibCore20.atan(dval(args[0])));
			case 94: // ca2str(chars: CArray): String
				return new String((char[])args[0]);
			case 95: // ba2utf(bytes: BArray): String
				return IO.utfDecode((byte[])args[0]);
			case 96: // ibits2f(bits: Int): Float
				return Fval(Float.intBitsToFloat(ival(args[0])));
			case 97: // f2ibits(f: Float): Int
				return Ival(Float.floatToIntBits(fval(args[0])));
			case 98: // lbits2d(bits: Long): Double
				return Dval(Double.longBitsToDouble(lval(args[0])));
			case 99: // d2lbits(d: Double): Long
				return Lval(Double.doubleToLongBits(dval(args[0])));
			case 100: // new_ht(): Hashtable
				return new Hashtable();
			case 101: // ht_put(ht: Hashtable, key: Any, value: Any)
				((Hashtable)args[0]).put(args[1], args[2]);
				return null;
			case 102: // ht_get(ht: Hashtable, key: Any): Any
				return ((Hashtable)args[0]).get(args[1]);
			case 103: // ht_rm(ht: Hashtable, key: Any)
				((Hashtable)args[0]).remove(args[1]);
				return null;
			case 104: // hash(a: Any): Int
				return Ival(args[0].hashCode());
			case 105: // rnd(n: Int): Int
				return Ival(rnd.nextInt(ival(args[0])));
			case 106: // rndint(): Int
				return Ival(rnd.nextInt());
			case 107: // rndlong(): Long
				return Lval(rnd.nextLong());
			case 108: // rndfloat(): Float
				return Fval(rnd.nextFloat());
			case 109: // rnddouble(): Double
				return Dval(rnd.nextDouble());
			case 110: { // netread(url: String): IStream
				String addr = (String)args[0];
				if (!addr.startsWith("http:")&&!addr.startsWith("https:"))
					throw new IOException("Unknown protocol in netread()");
				InputStream stream = Connector.openInputStream(addr);
				c.addStream(stream);
				return stream;
			}
			case 111: { // readresource(res: String): IStream
				InputStream stream = Object.class.getResourceAsStream((String)args[0]);
				if (stream != null) c.addStream(stream);
				return stream;
			}
			case 112: // loadlibrary(name: String): Library
				try {
					return c.loadLibrary((String)args[0]);
				} catch (Exception e) {
					return null;
				}
			case 113: // loadfunc(lib: Library, sig: String): Any
				return ((Library)args[0]).getFunction((String)args[1]);
			case 114: {// clone(struct: Any): Any
				Object[] struct = (Object[])args[0];
				Object[] clone = new Object[struct.length];
				System.arraycopy(struct, 0, clone, 0, struct.length);
				return clone;
			}
			case 115: // strsplit(s: String): Array
				return IO.split((String)args[0], (char)ival(args[1]));
			case 116: // parsei(s: String): Int
				return Ival(Integer.parseInt((String)args[0]));
			case 117: // parsel(s: String): Long
				return Lval(Long.parseLong((String)args[0]));
			case 118: // parsef(s: String): Float
				return Fval(Float.parseFloat((String)args[0]));
			case 119: // parsed(s: String): Double
				return Dval(Double.parseDouble((String)args[0]));
			case 120: // !getstatic(key: Any): Any
				return c.get(args[0]);
			case 121: // !setstatic(key: Any, val: Any)
				c.set(args[0], args[1]);
				return null;
			case 122: // sprintf(fmt: String, args: Array): String
				return IO.printf((String)args[0], (Object[])args[1]);
			case 123: { // ht_keys(ht: Hashtable): Array
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
			case 124: // sleep(millis: Int)
				Thread.sleep(ival(args[0]));
				return null;
			default:
				return null;
		}
	}
	
	public String toString() {
		return "libcore.2.so:"+signature;
	}
}
