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

import alchemy.core.AlchemyException;
import alchemy.libs.core.PartiallyAppliedFunction;
import alchemy.core.Context;
import alchemy.core.Function;
import alchemy.core.Library;
import alchemy.fs.FSManager;
import alchemy.fs.Filesystem;
import alchemy.libs.core.Pipe;
import alchemy.nlib.NativeFunction;
import alchemy.util.IO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

/**
 * Core runtime function set.
 * @author Sergey Basalaev
 */
class LibCore30Func extends NativeFunction {

	private static final Random rnd = new Random(0);

	public LibCore30Func(String name, int index) {
		super(name, index);
	}

	protected Object execNative(Context c, Object[] args) throws Exception {
		switch (index) {
			case 0: // pathfile(f: String): String
				return Filesystem.fname(c.toFile((String)args[0]));
			case 1: // pathdir(f: String): String
				return Filesystem.fparent(c.toFile((String)args[0]));
			case 2: // abspath(f: String): String
				return c.toFile((String)args[0]);
			case 3: // fcreate(f: String)
				FSManager.fs().create(c.toFile((String)args[0]));
				return null;
			case 4: // fremove(f: String)
				FSManager.fs().remove(c.toFile((String)args[0]));
				return null;
			case 5: // mkdir(f: String)
				FSManager.fs().mkdir(c.toFile((String)args[0]));
				return null;
			case 6: // fcopy(src: String, dest: String)
				FSManager.fs().copy(c.toFile((String)args[0]), c.toFile((String)args[1]));
				return null;
			case 7: // fmove(src: String, dest: String)
				FSManager.fs().move(c.toFile((String)args[0]), c.toFile((String)args[1]));
				return null;
			case 8: // set_read(f: String, on: Bool)
				FSManager.fs().setRead(c.toFile((String)args[0]), bval(args[1]));
				return null;
			case 9: // set_write(f: String, on: Bool)
				FSManager.fs().setWrite(c.toFile((String)args[0]), bval(args[1]));
				return null;
			case 10: // set_exec(f: String, on: Bool)
				FSManager.fs().setExec(c.toFile((String)args[0]), bval(args[1]));
				return null;
			case 11: // can_read(f: String): Bool
				return Ival(FSManager.fs().canRead(c.toFile((String)args[0])));
			case 12: // can_write(f: String): Bool
				return Ival(FSManager.fs().canWrite(c.toFile((String)args[0])));
			case 13: // can_exec(f: String): Bool
				return Ival(FSManager.fs().canExec(c.toFile((String)args[0])));
			case 14: // exists(f: String): Bool
				return Ival(FSManager.fs().exists(c.toFile((String)args[0])));
			case 15: // is_dir(f: String): Bool
				return Ival(FSManager.fs().isDirectory(c.toFile((String)args[0])));
			case 16: { // fopen_r(f: String): IStream
				InputStream stream = FSManager.fs().read(c.toFile((String)args[0]));
				c.addStream(stream);
				return stream;
			}
			case 17: { // fopen_w(f: String): OStream
				OutputStream stream = FSManager.fs().write(c.toFile((String)args[0]));
				c.addStream(stream);
				return stream;
			}
			case 18: { // fopen_a(f: String): OStream
				OutputStream stream = FSManager.fs().append(c.toFile((String)args[0]));
				c.addStream(stream);
				return stream;
			}
			case 19: // flist(f: String): Array
				return FSManager.fs().list(c.toFile((String)args[0]));
			case 20: // fmodified(f: String): Long
				return Lval(FSManager.fs().lastModified(c.toFile((String)args[0])));
			case 21: // fsize(f: String): Long
				return Lval(FSManager.fs().size(c.toFile((String)args[0])));
			case 22: // set_cwd(f: String)
				c.setCurDir(c.toFile((String)args[0]));
				return null;
			case 23: // relpath(f: String): String
				return LibCore30.relPath(c, c.toFile((String)args[0]));
			case 24: // String.len(): Int
				return Ival(((String)args[0]).length());
			case 25: // String.ch(at: Int): Int
				return Ival(((String)args[0]).charAt(ival(args[1])));
			case 26: // String.indexof(ch: Int): Int
				return Ival(((String)args[0]).indexOf(ival(args[1])));
			case 27: // String.lindexof(ch: Int): Int
				return Ival(((String)args[0]).lastIndexOf(ival(args[1])));
			case 28: // String.find(sub: String): Int
				return Ival(((String)args[0]).indexOf((String)args[1]));
			case 29: // String.substr(from: Int, to: Int): String
				return ((String)args[0]).substring(ival(args[1]), ival(args[2]));
			case 30: // String.ucase(): String
				return ((String)args[0]).toUpperCase();
			case 31: // String.lcase(): String
				return ((String)args[0]).toLowerCase();
			case 32: // String.concat(str: String): String
				return ((String)args[0]).concat((String)args[1]);
			case 33: // String.cmp(str: String): Int
				return Ival(((String)args[0]).compareTo((String)args[1]));
			case 34: // String.chars(): CArray
				return ((String)args[0]).toCharArray();
			case 35: // String.trim(): String
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
			case 47: // StrBuf.append(a: Any): StrBuf
				return ((StringBuffer)args[0]).append(args[1]);
			case 48: // StrBuf.addch(ch: Int): StrBuf
				return ((StringBuffer)args[0]).append((char)ival(args[1]));
			case 49: // StrBuf.delete(from: Int, to: Int): StrBuf
				return ((StringBuffer)args[0]).delete(ival(args[1]), ival(args[2]));
			case 50: // StrBuf.delch(at: Int): StrBuf
				return ((StringBuffer)args[0]).deleteCharAt(ival(args[1]));
			case 51: // StrBuf.insert(at: Int, a: Any): StrBuf
				return ((StringBuffer)args[0]).insert(ival(args[1]), args[2]);
			case 52: // StrBuf.insch(at: Int, ch: Int): StrBuf
				return ((StringBuffer)args[0]).insert(ival(args[1]), (char)ival(args[2]));
			case 53: // StrBuf.setch(at: Int, ch: Int): StrBuf
				((StringBuffer)args[0]).setCharAt(ival(args[1]), (char)ival(args[2]));
				return args[0];
			case 54: // StrBuf.len(): Int
				return Ival(((StringBuffer)args[0]).length());
			case 55: // millis(date: Long): Int
				return Ival((int)(lval(args[0]) % 1000));
			case 56: // systime(): Long
				return Lval(System.currentTimeMillis());
			case 57: // get_cwd(): String
				return c.getCurDir();
			case 58: // space_total(root: String): Long
				return Lval(FSManager.fs().spaceTotal(c.toFile((String)args[0])));
			case 59: // space_free(root: String): Long
				return Lval(FSManager.fs().spaceFree(c.toFile((String)args[0])));
			case 60: // space_used(root: String): Long
				return Lval(FSManager.fs().spaceUsed(c.toFile((String)args[0])));
			case 61: // Any.tostr(): String
				return String.valueOf(args[0]);
			case 62: // new_strbuf(): StrBuf
				return new StringBuffer();
			case 63: // IStream.close()
				((InputStream)args[0]).close();
				return null;
			case 64: // IStream.read(): Int
				return Ival(((InputStream)args[0]).read());
			case 65: // IStream.readarray(buf: BArray, ofs: Int, len: Int): Int
				return Ival(((InputStream)args[0]).read((byte[])args[1], ival(args[2]), ival(args[3])));
			case 66: // IStream.skip(n: Long): Long
				return Lval(((InputStream)args[0]).skip(lval(args[1])));
			case 67: // OStream.write(b: Int)
				((OutputStream)args[0]).write(ival(args[1]));
				return null;
			case 68: // OStream.writearray(buf: BArray, ofs: Int, len: Int)
				((OutputStream)args[0]).write((byte[])args[1], ival(args[2]), ival(args[3]));
				return null;
			case 69: // OStream.flush(out: OStream)
				((OutputStream)args[0]).flush();
				return null;
			case 70: { // Process.start_wait(cmd: String, args: Array): Int
				Context cc = (Context)args[0];
				String prog = (String)args[1];
				Object[] oargs = (Object[])args[2];
				String[] sargs = new String[oargs.length];
				for (int i=oargs.length-1; i>=0; i--) {
					sargs[i] = String.valueOf(oargs[i]);
				}
				return Ival(cc.startAndWait(prog, sargs));
			}
			case 71: { // Process.start(cmd: String, args: Array)
				Context cc = (Context)args[0];
				String prog = (String)args[1];
				Object[] oargs = (Object[])args[2];
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
				return Dval(LibCore30.ipow(dval(args[0]), ival(args[1])));
			case 89: // exp(a: Double): Double
				return Dval(LibCore30.exp(dval(args[0])));
			case 90: // log(a: Double): Double
				return Dval(LibCore30.log(dval(args[0])));
			case 91: // asin(a: Double): Double
				return Dval(LibCore30.asin(dval(args[0])));
			case 92: // acos(a: Double): Double
				return Dval(LibCore30.acos(dval(args[0])));
			case 93: // atan(a: Double): Double
				return Dval(LibCore30.atan(dval(args[0])));
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
			case 100: // new_dict(): Dict
				return new Hashtable();
			case 101: // Dict.set(key: Any, value: Any)
				((Hashtable)args[0]).put(args[1], args[2]);
				return null;
			case 102: // Dict.get(key: Any): Any
				return ((Hashtable)args[0]).get(args[1]);
			case 103: // Dict.remove(key: Any)
				((Hashtable)args[0]).remove(args[1]);
				return null;
			case 104: // OStream.close
				((OutputStream)args[0]).close();
				return null;
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
			case 110: { // readurl(url: String): IStream
				String url = (String)args[0];
				int cl = url.indexOf(':');
				if (cl < 0) throw new IllegalArgumentException("No protocol in given URL");
				String protocol = url.substring(0, cl);
				String addr = url.substring(cl+1);
				InputStream in;
				if (protocol.equals("file")) {
					in = FSManager.fs().read(FSManager.normalize(addr));
				} else if (protocol.equals("res")) {
					in = this.getClass().getResourceAsStream(addr);
					if (in == null) throw new IOException("Resource not found: "+addr);
				} else if (protocol.equals("http") || protocol.equals("https")) {
					in = Connector.openInputStream(url);
				} else {
					throw new IllegalArgumentException("Unsupported protocol: "+protocol);
				}
				c.addStream(in);
				return in;
			}
			case 111: // Function.curry(arg: Any): Function
				return new PartiallyAppliedFunction((Function)args[0], args[1]);
			case 112: // loadlibrary(name: String): Library
				try {
					return c.loadLibrary((String)args[0]);
				} catch (Exception e) {
					return null;
				}
			case 113: // Library.getfunc(sig: String): Function
				return ((Library)args[0]).getFunction((String)args[1]);
			case 114: {// Structure.clone(): Structure
				Object[] struct = (Object[])args[0];
				Object[] clone = new Object[struct.length];
				System.arraycopy(struct, 0, clone, 0, struct.length);
				return clone;
			}
			case 115: // String.split(ch: Int): [String]
				return IO.split((String)args[0], (char)ival(args[1]));
			case 116: // String.toint(): Int
				try {
					return Ival(Integer.parseInt((String)args[0]));
				} catch (NumberFormatException nfe) {
					return null;
				}
			case 117: // String.tolong(): Long
				try {
					return Lval(Long.parseLong((String)args[0]));
				} catch (NumberFormatException nfe) {
					return null;
				}
			case 118: // String.tofloat(): Float
				try {
					return Fval(Float.parseFloat((String)args[0]));
				} catch (NumberFormatException nfe) {
					return null;
				}
			case 119: // String.todouble(): Double
				try {
					return Dval(Double.parseDouble((String)args[0]));
				} catch (NumberFormatException nfe) {
					return null;
				}
			case 120: // !getstatic(key: Any): Any
				return c.get(args[0]);
			case 121: // !setstatic(key: Any, val: Any)
				c.set(args[0], args[1]);
				return null;
			case 122: // String.format(args: Array): String
				return IO.printf((String)args[0], (Object[])args[1]);
			case 123: { // Dict.keys(): [Any]
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
			case 125: // Connection.close()
				((Connection)args[0]).close();
				c.removeStream(args[0]);
				return null;
			case 126: { // StreamConnection.open_input(): IStream
				InputStream in = ((StreamConnection)args[0]).openInputStream();
				c.addStream(in);
				return in;
			}
			case 127: { // StreamConnection.open_output(): OStream
				OutputStream out = ((StreamConnection)args[0]).openOutputStream();
				c.addStream(out);
				return out;
			}
			case 128: // Dict.size(): Int
				return Ival(((Hashtable)args[0]).size());
			case 129: // Dict.clear()
				((Hashtable)args[0]).clear();
				return null;
			case 130: // StrBuf.ch(at: Int): Int
				return Ival(((StringBuffer)args[0]).charAt(ival(args[1])));
			case 131: // Int.tobase(base: Int): String
				return Integer.toString(ival(args[0]), ival(args[1]));
			case 132: // Long.tobase(base: Int): String
				return Long.toString(lval(args[0]), ival(args[1]));
			case 133: // Int.tobin():String
				return Integer.toBinaryString(ival(args[0]));
			case 134: // Int.tooct():String
				return Integer.toOctalString(ival(args[0]));
			case 135: // Int.tohex():String
				return Integer.toHexString(ival(args[0]));
			case 136: // String.tointbase(base: Int): Int
				try {
					return Ival(Integer.parseInt((String)args[0], ival(args[1])));
				} catch (NumberFormatException nfe) {
					return null;
				}
			case 137: // String.tolongbase(base: Int): Long
				try {
					return Lval(Long.parseLong((String)args[0], ival(args[1])));
				} catch (NumberFormatException nfe) {
					return null;
				}
			case 138: // Error.code(): Int
				return Ival(((AlchemyException)args[0]).errcode);
			case 139: // Error.msg(): String
				return ((AlchemyException)args[0]).getMessage();
			case 140: // error(code: Int, msg: String)
				throw new AlchemyException(ival(args[0]), (String)args[1]);
			case 141: // String.hash(): Int
				return Ival(args[0].hashCode());
			case 142: // matches_glob(path: String, glob: String): Bool
				return Ival(IO.matchesPattern((String)args[0], (String)args[1]));
			case 143: // new_process(): Process
				return new Context(c);
			case 144: // Process.get_state(): Int
				return Ival(((Context)args[0]).getState());
			case 145: // Process.getenv(key: String): String
				return ((Context)args[0]).getEnv((String)args[1]);
			case 146: // Process.setenv(key: String, value: String)
				((Context)args[0]).setEnv((String)args[1], (String)args[2]);
				return null;
			case 147: // Process.get_in(): IStream
				return ((Context)args[0]).stdin;
			case 148: // Process.get_out(): OStream
				return ((Context)args[0]).stdout;
			case 149: // Process.get_err(): OStream
				return ((Context)args[0]).stderr;
			case 150: // Process.set_in(in: IStream)
				((Context)args[0]).stdin = (InputStream)args[1];
				return null;
			case 151: // Process.set_out(out: OStream)
				((Context)args[0]).stdout = (OutputStream)args[1];
				return null;
			case 152: // Process.set_err(err: OStream)
				((Context)args[0]).stderr = (OutputStream)args[1];
				return null;
			case 153: // Process.get_cwd(): String
				return ((Context)args[0]).getCurDir();
			case 154: // Process.set_cwd(dir: String)
				((Context)args[0]).setCurDir((String)args[1]);
				return null;
			case 155: // Process.get_priority(): Int
				return Ival(((Context)args[0]).getPriority());
			case 156: // Process.set_priority(value: Int)
				((Context)args[0]).setPriority(ival(args[1]));
				return null;
			case 157: // Process.get_name(): String
				return ((Context)args[0]).getName();
			case 158: // Process.interrupt()
				((Context)args[0]).interrupt();
				return null;
			case 159: // Process.get_exitcode(): Int
				return Ival(((Context)args[0]).getExitCode());
			case 160: // istream_from_ba(buf: BArray): IStream
				return new ByteArrayInputStream((byte[])args[0]);
			case 161: // new_baostream(): BArrayOStream
				return new ByteArrayOutputStream();
			case 162: // BArrayOStream.len(): Int
				return Ival(((ByteArrayOutputStream)args[0]).size());
			case 163: // BArrayOStream.tobarray(): BArray
				return ((ByteArrayOutputStream)args[0]).toByteArray();
			case 164: // BArrayOStream.reset()
				((ByteArrayOutputStream)args[0]).reset();
				return null;
			case 165: // new_pipe(): StreamConnection;
				return new Pipe();
			default:
				return null;
		}
	}

	protected String soname() {
		return "libcore.3.so";
	}
}
