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

package alchemy.libs;

import alchemy.fs.Filesystem;
import alchemy.io.ConnectionInputStream;
import alchemy.io.ConnectionOutputStream;
import alchemy.io.IO;
import alchemy.io.Pipe;
import alchemy.system.Library;
import alchemy.system.NativeLibrary;
import alchemy.system.Process;
import alchemy.util.ArrayList;
import alchemy.util.Strings;
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
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

/**
 * Alchemy core runtime library.
 *
 * @deprecated Will be removed after new API is finished
 * @author Sergey Basalaev
 * @version 3.0
 */
public class LibCore3 extends NativeLibrary {

	private static Random rnd = new Random();
	
	/**
	 * Constructor without parameters to load
	 * library through the native interface.
	 * @throws IOException if I/O error occured while reading
	 *         function definitions file
	 */
	public LibCore3() throws IOException {
		load("/symbols/core3");
		name = "libcore.3.so";
	}

	protected Object invokeNative(int index, Process p, Object[] args) throws Exception {
		switch (index) {
			case 0: // pathfile(f: String): String
				return Filesystem.fileName(p.toFile((String)args[0]));
			case 1: // pathdir(f: String): String
				return Filesystem.fileParent(p.toFile((String)args[0]));
			case 2: // abspath(f: String): String
				return p.toFile((String)args[0]);
			case 3: // fcreate(f: String)
				Filesystem.create(p.toFile((String)args[0]));
				return null;
			case 4: // fremove(f: String)
				Filesystem.remove(p.toFile((String)args[0]));
				return null;
			case 5: // mkdir(f: String)
				Filesystem.mkdir(p.toFile((String)args[0]));
				return null;
			case 6: // fcopy(src: String, dest: String)
				Filesystem.copy(p.toFile((String)args[0]), p.toFile((String)args[1]));
				return null;
			case 7: // fmove(src: String, dest: String)
				Filesystem.move(p.toFile((String)args[0]), p.toFile((String)args[1]));
				return null;
			case 8: // set_read(f: String, on: Bool)
				Filesystem.setRead(p.toFile((String)args[0]), bval(args[1]));
				return null;
			case 9: // set_write(f: String, on: Bool)
				Filesystem.setWrite(p.toFile((String)args[0]), bval(args[1]));
				return null;
			case 10: // set_exec(f: String, on: Bool)
				Filesystem.setExec(p.toFile((String)args[0]), bval(args[1]));
				return null;
			case 11: // can_read(f: String): Bool
				return Ival(Filesystem.canRead(p.toFile((String)args[0])));
			case 12: // can_write(f: String): Bool
				return Ival(Filesystem.canWrite(p.toFile((String)args[0])));
			case 13: // can_exec(f: String): Bool
				return Ival(Filesystem.canExec(p.toFile((String)args[0])));
			case 14: // exists(f: String): Bool
				return Ival(Filesystem.exists(p.toFile((String)args[0])));
			case 15: // is_dir(f: String): Bool
				return Ival(Filesystem.isDirectory(p.toFile((String)args[0])));
			case 16: { // fopen_r(f: String): IStream
				InputStream stream = Filesystem.read(p.toFile((String)args[0]));
				ConnectionInputStream in = new ConnectionInputStream(stream);
				p.addConnection(in);
				return in;
			}
			case 17: { // fopen_w(f: String): OStream
				OutputStream stream = Filesystem.write(p.toFile((String)args[0]));
				ConnectionOutputStream out = new ConnectionOutputStream(stream);
				p.addConnection(out);
				return out;
			}
			case 18: { // fopen_a(f: String): OStream
				OutputStream stream = Filesystem.append(p.toFile((String)args[0]));
				ConnectionOutputStream out = new ConnectionOutputStream(stream);
				p.addConnection(out);
				return out;
			}
			case 19: // flist(f: String): [String]
				return Filesystem.list(p.toFile((String)args[0]));
			case 20: // fmodified(f: String): Long
				return Lval(Filesystem.lastModified(p.toFile((String)args[0])));
			case 21: // fsize(f: String): Long
				return Lval(Filesystem.size(p.toFile((String)args[0])));
			case 22: // set_cwd(f: String)
				p.setCurrentDirectory(p.toFile((String)args[0]));
				return null;
			case 23: // relpath(f: String): String
				return relPath(p, p.toFile((String)args[0]));
			case 36: // getenv(key: String): String
				return p.getEnv(((String)args[0]));
			case 37: // setenv(key: String, value: String)
				p.setEnv(((String)args[0]), (String)args[1]);
				return null;
			case 38: // utfbytes(str: String): [Byte]
				return Strings.utfEncode((String)args[0]);
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
				return ((StringBuffer)args[0]).append(Strings.toString(args[1]));
			case 48: // StrBuf.addch(ch: Char): StrBuf
				return ((StringBuffer)args[0]).append((char)ival(args[1]));
			case 49: // StrBuf.delete(from: Int, to: Int): StrBuf
				return ((StringBuffer)args[0]).delete(ival(args[1]), ival(args[2]));
			case 50: // StrBuf.delch(at: Int): StrBuf
				return ((StringBuffer)args[0]).deleteCharAt(ival(args[1]));
			case 51: // StrBuf.insert(at: Int, a: Any): StrBuf
				return ((StringBuffer)args[0]).insert(ival(args[1]), Strings.toString(args[2]));
			case 52: // StrBuf.insch(at: Int, ch: Char): StrBuf
				return ((StringBuffer)args[0]).insert(ival(args[1]), (char) ival(args[2]));
			case 53: // StrBuf.setch(at: Int, ch: Char): StrBuf
				((StringBuffer)args[0]).setCharAt(ival(args[1]), (char) ival(args[2]));
				return args[0];
			case 54: // StrBuf.len(): Int
				return Ival(((StringBuffer)args[0]).length());
			case 55: // millis(date: Long): Int
				return Ival((int)(lval(args[0]) % 1000));
			case 56: // systime(): Long
				return Lval(System.currentTimeMillis());
			case 57: // get_cwd(): String
				return p.getCurrentDirectory();
			case 58: // space_total(root: String): Long
				return Lval(Filesystem.spaceTotal(p.toFile((String)args[0])));
			case 59: // space_free(root: String): Long
				return Lval(Filesystem.spaceFree(p.toFile((String)args[0])));
			case 60: // space_used(root: String): Long
				return Lval(Filesystem.spaceUsed(p.toFile((String)args[0])));
			case 61: // Any.tostr(): String
				return Strings.toString(args[0]);
			case 62: // new_strbuf(): StrBuf
				return new StringBuffer();
			case 63: // IStream.close()
				((InputStream)args[0]).close();
				return null;
			case 64: // IStream.read(): Int
				return Ival(((InputStream)args[0]).read());
			case 65: // IStream.readarray(buf: [Byte], ofs: Int, len: Int): Int
				return Ival(((InputStream)args[0]).read((byte[])args[1], ival(args[2]), ival(args[3])));
			case 66: // IStream.skip(n: Long): Long
				return Lval(((InputStream)args[0]).skip(lval(args[1])));
			case 67: // OStream.write(b: Int)
				((OutputStream)args[0]).write(ival(args[1]));
				return null;
			case 68: // OStream.writearray(buf: [Byte], ofs: Int, len: Int)
				((OutputStream)args[0]).write((byte[])args[1], ival(args[2]), ival(args[3]));
				return null;
			case 69: // OStream.flush(out: OStream)
				((OutputStream)args[0]).flush();
				return null;
			case 70: { // Process.start_wait(cmd: String, args: [String]): Int
				Process cc = (Process)args[0];
				String prog = (String)args[1];
				Object[] oargs = (Object[])args[2];
				String[] sargs = new String[oargs.length];
				for (int i=oargs.length-1; i>=0; i--) {
					sargs[i] = String.valueOf(oargs[i]);
				}
				return Ival(cc.startAndWait(prog, sargs));
			}
			case 71: { // Process.start(cmd: String, args: [String])
				Process cc = (Process)args[0];
				String prog = (String)args[1];
				Object[] oargs = (Object[])args[2];
				String[] sargs = new String[oargs.length];
				for (int i=oargs.length-1; i>=0; i--) {
					sargs[i] = String.valueOf(oargs[i]);
				}
				cc.start(prog, sargs);
				return null;
			}
			case 75: // fprint(out: OStream, a: Any): OStream
				IO.print((OutputStream)args[0], Strings.toString(args[1]));
				return args[0];
			case 76: // fprintln(out: OStream, a: Any): OStream
				IO.println((OutputStream)args[0], Strings.toString(args[1]));
				return args[0];
			case 77: // stdin(): IStream
				return p.stdin;
			case 78: // stdout(): OStream
				return p.stdout;
			case 79: // stderr(): OStream
				return p.stderr;
			case 80: // setin(in: IStream)
				p.stdin = (InputStream)args[0];
				return null;
			case 81: // setout(out: OStream)
				p.stdout = (OutputStream)args[0];
				return null;
			case 82: // seterr(out: OStream)
				p.stderr = (OutputStream)args[0];
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
				return Dval(ipow(dval(args[0]), ival(args[1])));
			case 89: // exp(a: Double): Double
				return Dval(exp(dval(args[0])));
			case 90: // log(a: Double): Double
				return Dval(log(dval(args[0])));
			case 91: // asin(a: Double): Double
				return Dval(asin(dval(args[0])));
			case 92: // acos(a: Double): Double
				return Dval(acos(dval(args[0])));
			case 93: // atan(a: Double): Double
				return Dval(atan(dval(args[0])));
			case 94: // ca2str(chars: [Char]): String
				return new String((char[])args[0]);
			case 95: // ba2utf(bytes: [Byte]): String
				return Strings.utfDecode((byte[])args[0]);
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
			case 104: // OStream.close()
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
				if (cl < 0) throw new IllegalArgumentException("Protocol missing");
				String protocol = url.substring(0, cl);
				String addr = url.substring(cl+1);
				InputStream in;
				if (protocol.equals("file")) {
					in = Filesystem.read(Filesystem.normalize(addr));
				} else if (protocol.equals("res")) {
					in = this.getClass().getResourceAsStream(addr);
					if (in == null) throw new IOException("Resource not found: "+addr);
				} else if (protocol.equals("http") || protocol.equals("https")) {
					in = Connector.openInputStream(url);
				} else {
					throw new IllegalArgumentException("Unsupported protocol: "+protocol);
				}
				ConnectionInputStream connin = new ConnectionInputStream(in);
				p.addConnection(connin);
				return connin;
			}
			case 112: // loadlibrary(name: String): Library
				try {
					return p.loadLibrary((String)args[0]);
				} catch (Exception e) {
					return null;
				}
			case 113: // Library.getfunc(sig: String): Function
				return ((Library)args[0]).getFunction((String)args[1]);
			case 120: // getstatic(key: String): Any
				return p.get(args[0]);
			case 121: // setstatic(key: String, val: Any)
				p.set(args[0], args[1]);
				return null;
			case 123: { // Dict.keys(): [Any]
				ArrayList keyv = new ArrayList();
				for (Enumeration e = ((Hashtable)args[0]).keys(); e.hasMoreElements(); ) {
					keyv.add(e.nextElement());
				}
				Object[] keys = new Object[keyv.size()];
				for (int i=keys.length-1; i>=0; i--) {
					keys[i] = keyv.get(i);
				}
				return keys;
			}
			case 124: // sleep(millis: Int)
				Thread.sleep(ival(args[0]));
				return null;
			case 125: { // Connection.close()
				Connection conn = ((Connection)args[0]);
				conn.close();
				p.removeConnection(conn);
				return null;
			}
			case 126: { // StreamConnection.open_input(): IStream
				ConnectionInputStream in = new ConnectionInputStream(((StreamConnection)args[0]).openInputStream());
				p.addConnection(in);
				return in;
			}
			case 127: { // StreamConnection.open_output(): OStream
				ConnectionOutputStream out = new ConnectionOutputStream(((StreamConnection)args[0]).openOutputStream());
				p.addConnection(out);
				return out;
			}
			case 128: // Dict.size(): Int
				return Ival(((Hashtable)args[0]).size());
			case 129: // Dict.clear()
				((Hashtable)args[0]).clear();
				return null;
			case 130: // StrBuf.ch(at: Int): Char
				return Ival(((StringBuffer)args[0]).charAt(ival(args[1])));
			case 142: // matches_glob(path: String, glob: String): Bool
				return Ival(IO.matchesPattern((String)args[0], (String)args[1]));
			case 143: // new_process(): Process
				return new Process(p);
			case 144: // Process.get_state(): Int
				return Ival(((Process)args[0]).getState());
			case 145: // Process.getenv(key: String): String
				return ((Process)args[0]).getEnv((String)args[1]);
			case 146: // Process.setenv(key: String, value: String)
				((Process)args[0]).setEnv((String)args[1], (String)args[2]);
				return null;
			case 147: // Process.get_in(): IStream
				return ((Process)args[0]).stdin;
			case 148: // Process.get_out(): OStream
				return ((Process)args[0]).stdout;
			case 149: // Process.get_err(): OStream
				return ((Process)args[0]).stderr;
			case 150: // Process.set_in(in: IStream)
				((Process)args[0]).stdin = (InputStream)args[1];
				return null;
			case 151: // Process.set_out(out: OStream)
				((Process)args[0]).stdout = (OutputStream)args[1];
				return null;
			case 152: // Process.set_err(err: OStream)
				((Process)args[0]).stderr = (OutputStream)args[1];
				return null;
			case 153: // Process.get_cwd(): String
				return ((Process)args[0]).getCurrentDirectory();
			case 154: // Process.set_cwd(dir: String)
				((Process)args[0]).setCurrentDirectory((String)args[1]);
				return null;
			case 155: // Process.get_priority(): Int
				return Ival(((Process)args[0]).getPriority());
			case 156: // Process.set_priority(value: Int)
				((Process)args[0]).setPriority(ival(args[1]));
				return null;
			case 157: // Process.get_name(): String
				return ((Process)args[0]).getName();
			case 158: // Process.interrupt()
				((Process)args[0]).interrupt();
				return null;
			case 159: // Process.get_exitcode(): Int
				return Ival(((Process)args[0]).getExitCode());
			case 160: // istream_from_ba(buf: [Byte]): IStream
				return new ByteArrayInputStream((byte[])args[0]);
			case 161: // new_baostream(): BArrayOStream
				return new ByteArrayOutputStream();
			case 162: // BArrayOStream.len(): Int
				return Ival(((ByteArrayOutputStream)args[0]).size());
			case 163: // BArrayOStream.tobarray(): [Byte]
				return ((ByteArrayOutputStream)args[0]).toByteArray();
			case 164: // BArrayOStream.reset()
				((ByteArrayOutputStream)args[0]).reset();
				return null;
			case 165: // new_pipe(): StreamConnection
				return new Pipe();
			case 166: // IStream.available(): Int
				return Ival(((InputStream)args[0]).available());
			case 167: // this_process(): Process;
				return p;
			case 168: // chstr(ch: Char): String
				return String.valueOf((char)ival(args[0]));
			case 169: // IStream.reset()
				((InputStream)args[0]).reset();
				return null;
			case 170: { // timeof(year: Int, month: Int, day: Int, hour: Int, min: Int, sec: Int, millis: Int): Long
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.YEAR, ival(args[0]));
				cal.set(Calendar.MONTH, ival(args[1]));
				cal.set(Calendar.DAY_OF_MONTH, ival(args[2]));
				cal.set(Calendar.HOUR_OF_DAY, ival(args[3]));
				cal.set(Calendar.MINUTE, ival(args[4]));
				cal.set(Calendar.SECOND, ival(args[5]));
				cal.set(Calendar.MILLISECOND, ival(args[6]));
				return Lval(cal.getTime().getTime());
			}
			case 171: { // getstaticdef(key: String, dflt: Any): Any
				Object obj = p.get(args[0]);
				if (obj == null) {
					p.set(args[0], args[1]);
					obj = args[1];
				}
				return obj;
			}
			case 172: // String.startswith(str: String, offset: Int): Bool
				return Ival(((String)args[0]).startsWith((String)args[1], ival(args[2])));
			case 173: // IStream.readfully(): [Byte]
				return IO.readFully((InputStream)args[0]);
			case 174: // OStream.writeall(input: IStream)
				IO.writeAll((InputStream)args[1], (OutputStream)args[0]);
				return null;
			case 175: // sys_property(key: String): String
				return System.getProperty((String)args[0]);
			case 177: { // buildlibrary(in: IStream)
				InputStream in = (InputStream) args[0];
				if (in.read() != 0xC0 || in.read() != 0xDE)
					throw new InstantiationException("Not an Ether library");
				return EtherLoader.load(p, in);
			}
			case 178: // StrBuf.chars(from: Int, to: Int, buf: [Char], ofs: Int)
				((StringBuffer)args[0]).getChars(ival(args[1]), ival(args[2]), (char[])(args[3]), ival(args[4]));
				return null;
			default:
				return null;
		}
	}
}
