/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2011-2014, Sergey Basalaev <sbasalaev@gmail.com>
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

import alchemy.evm.EtherLoader;
import alchemy.fs.Filesystem;
import alchemy.io.ConnectionInputStream;
import alchemy.io.ConnectionOutputStream;
import alchemy.io.IO;
import alchemy.io.Pipe;
import alchemy.io.TerminalInput;
import alchemy.platform.Platform;
import alchemy.system.AlchemyException;
import alchemy.system.Function;
import alchemy.system.Library;
import alchemy.system.NativeLibrary;
import alchemy.system.Process;
import alchemy.system.ProcessThread;
import alchemy.types.Int32;
import alchemy.util.ArrayList;
import alchemy.util.Arrays;
import alchemy.util.HashMap;
import alchemy.util.Lock;
import alchemy.util.PartiallyAppliedFunction;
import alchemy.util.Strings;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import javax.microedition.io.Connection;
import javax.microedition.io.StreamConnection;

/**
 * Core runtime library for Alchemy OS.
 *
 * @author Sergey Basalaev
 * @version 4.0
 */
public final class LibCore4 extends NativeLibrary {

	public LibCore4() throws IOException {
		load("/symbols/core4");
		name = "libcore.4.so";
	}

	protected Object invokeNative(int index, Process p, Object[] args) throws Exception {
		switch (index) {
			/* == Header: builtin.eh == */
			case 0: // acopy(src: Array, sofs: Int, dest: Array, dofs: Int, len: Int)
				Arrays.arrayCopy(args[0], ival(args[1]), args[2], ival(args[3]), ival(args[4]));
				return null;
			case 1: // Function.apply(args: [Any]): Function
				return new PartiallyAppliedFunction((Function)args[0], (Object[])args[1]);
			case 2: // throw(code: Int = FAIL, msg: String = null)
				throw new AlchemyException(ival(args[0]), (String)args[1]);
			case 3: // Any.hash(): Int
				return Ival(args[0] == null ? 0 : args[0].hashCode());
			case 4: // Any.tostr(): String
				return Strings.toString(args[0]);
			case 5: // Char.tostr(): String
				return String.valueOf((char)ival(args[0]));

			/* == Header: string.eh == */
			case 6: // Int.tobin():String
				return Integer.toBinaryString(ival(args[0]));
			case 7: // Int.tooct():String
				return Integer.toOctalString(ival(args[0]));
			case 8: // Int.tohex():String
				return Integer.toHexString(ival(args[0]));
			case 9: // Int.tobase(base: Int): String
				return Integer.toString(ival(args[0]), ival(args[1]));
			case 10: // Long.tobase(base: Int): String
				return Long.toString(lval(args[0]), ival(args[1]));
			case 11: // String.tointbase(base: Int): Int
				return Ival(Integer.parseInt((String)args[0], ival(args[1])));
			case 12: // String.tolongbase(base: Int): Long
				return Lval(Long.parseLong((String)args[0], ival(args[1])));
			case 13: // String.tofloat(): Float
				return Fval(Float.parseFloat((String)args[0]));
			case 14: // String.todouble(): Double
				return Dval(Double.parseDouble((String)args[0]));
			case 15: { // String.get(at: Int): Char
				String str = (String)args[0];
				int at = ival(args[1]);
				if (at < 0) at += str.length();
				return Ival(str.charAt(at));
			}
			case 16: // String.len(): Int
				return Ival(((String)args[0]).length());
			case 17: { // String.range(from: Int, to: Int): String
				String str = (String) args[0];
				int from = ival(args[1]);
				int to = ival(args[2]);
				if (from < 0) from += str.length();
				if (to < 0) to += str.length();
				return str.substring(from, to);
			}
			case 18: { // String.indexof(ch: Char, from: Int = 0): Int
				String str = (String)args[0];
				int from = ival(args[2]);
				if (from < 0) from += str.length();
				return Ival(str.indexOf(ival(args[1]), from));
			}
			case 19: // String.lindexof(ch: Char): Int
				return Ival(((String)args[0]).lastIndexOf(ival(args[1])));
			case 20: { // String.find(sub: String, from: Int = 0): Int
				String str = (String)args[0];
				int from = ival(args[2]);
				if (from < 0) from += str.length();
				return Ival(str.indexOf((String)args[1], from));
			}
			case 21: // String.ucase(): String
				return ((String)args[0]).toUpperCase();
			case 22: // String.lcase(): String
				return ((String)args[0]).toLowerCase();
			case 23: // String.concat(str: String): String
				return ((String)args[0]).concat((String)args[1]);
			case 24: // String.cmp(str: String): Int
				return Ival(((String)args[0]).compareTo((String)args[1]));
			case 25: // String.trim(): String
				return ((String)args[0]).trim();
			case 26: // String.split(ch: Char, skipEmpty: Bool = false): [String]
				return Strings.split((String)args[0], (char)ival(args[1]), bval(args[2]));
			case 27: // String.format(args: [Any]): String
				return Strings.format((String)args[0], (Object[])args[1]);
			case 28: // String.chars(): [Char]
				return ((String)args[0]).toCharArray();
			case 29: // String.utfbytes(): [Byte]
				return Strings.utfEncode((String)args[0]);
			case 30: { // String.startswith(prefix: String, from: Int = 0): Bool
				String str = (String)args[0];
				int from = ival(args[2]);
				if (from < 0) from += str.length();
				return Ival(str.startsWith((String)args[1], from));
			}
			case 31: // String.replace(oldch: Char, newch: Char): String
				return ((String)args[0]).replace((char)ival(args[1]), (char)ival(args[2]));
			case 32: // ca2str(ca: [Char]): String
				return new String((char[])args[0]);
			case 33: // ba2utf(ba: [Byte]): String
				return Strings.utfDecode((byte[])args[0]);

			/* == Header: error.eh == */
			case 34: // Error.code(): Int
				return Ival(((AlchemyException)args[0]).errcode);
			case 35: // Error.msg(): String
				return ((AlchemyException)args[0]).getMessage();
			case 36: // Error.traceLen(): Int
				return Ival(((AlchemyException)args[0]).getTraceLength());
			case 37: // Error.traceName(index: Int): String
				return ((AlchemyException)args[0]).getTraceElementName(ival(args[1]));
			case 38: // Error.traceDbg(index: Int): String
				return ((AlchemyException)args[0]).getTraceElementInfo(ival(args[1]));
			case 39: // Error.trace(): String
				return ((AlchemyException)args[0]).trace();
			case 40: // errstring(code: Int): String
				return AlchemyException.errstring(ival(args[0]));

			/* == Header: math.eh == */
			case 41: // abs(val: Double): Double
				return Dval(Math.abs(dval(args[0])));
			case 42: // sin(val: Double): Double
				return Dval(Math.sin(dval(args[0])));
			case 43: // cos(val: Double): Double
				return Dval(Math.cos(dval(args[0])));
			case 44: // tan(val: Double): Double
				return Dval(Math.tan(dval(args[0])));
			case 45: // sqrt(val: Double): Double
				return Dval(Math.sqrt(dval(args[0])));
			case 46: // ipow(val: Double, pow: Int): Double
				return Dval(alchemy.util.Math.ipow(dval(args[0]), ival(args[1])));
			case 47: // exp(val: Double): Double
				return Dval(alchemy.util.Math.exp(dval(args[0])));
			case 48: // log(val: Double): Double
				return Dval(alchemy.util.Math.log(dval(args[0])));
			case 49: // asin(val: Double): Double
				return Dval(alchemy.util.Math.asin(dval(args[0])));
			case 50: // acos(val: Double): Double
				return Dval(alchemy.util.Math.acos(dval(args[0])));
			case 51: // atan(val: Double): Double
				return Dval(alchemy.util.Math.atan(dval(args[0])));
			case 52: // ibits2f(bits: Int): Float
				return Fval(Float.intBitsToFloat(ival(args[0])));
			case 53: // f2ibits(f: Float): Int
				return Ival(Float.floatToIntBits(fval(args[0])));
			case 54: // lbits2d(bits: Long): Double
				return Dval(Double.longBitsToDouble(lval(args[0])));
			case 55: // d2lbits(d: Double): Long
				return Lval(Double.doubleToLongBits(dval(args[0])));

			/* == Header: rnd.eh == */
			case 56: // Random.new(seed: Long)
				return new Random(lval(args[0]));
			case 57: // Random.next(n: Int): Int
				return Ival(((Random)args[0]).nextInt(ival(args[1])));
			case 58: // Random.nextInt(): Int
				return Ival(((Random)args[0]).nextInt());
			case 59: // Random.nextLong(): Long
				return Lval(((Random)args[0]).nextLong());
			case 60: // Random.nextFloat(): Float
				return Fval(((Random)args[0]).nextFloat());
			case 61: // Random.nextDouble(): Double
				return Dval(((Random)args[0]).nextDouble());
			case 62: // Random.setSeed(seed: Long)
				((Random)args[0]).setSeed(lval(args[1]));
				return null;

			/* == Header: io.eh == */
			case 63: { // Connection.close()
				Connection conn = (Connection) args[0];
				conn.close();
				p.removeConnection(conn);
				return null;
			}
			case 64: { // StreamConnection.openInput(): IStream
				ConnectionInputStream in = new ConnectionInputStream(((StreamConnection)args[0]).openInputStream());
				p.addConnection(in);
				return in;
			}
			case 65: { // StreamConnection.openOutput(): OStream
				ConnectionOutputStream out = new ConnectionOutputStream(((StreamConnection)args[0]).openOutputStream());
				p.addConnection(out);
				return out;
			}
			case 66: // IStream.read(): Int
				return Ival(((InputStream)args[0]).read());
			case 67: { // IStream.readArray(buf: [Byte], ofs: Int = 0, len: Int = -1): Int
				byte[] buf = (byte[])args[1];
				int len = ival(args[3]);
				if (len < 0) len = buf.length;
				return Ival(((InputStream)args[0]).read(buf, ival(args[2]), len));
			}
			case 68: // IStream.readFully(): [Byte]
				return IO.readFully((InputStream)args[0]);
			case 69: // IStream.skip(num: Long): Long
				return Lval(((InputStream)args[0]).skip(lval(args[1])));
			case 70: // IStream.available(): Int
				return Ival(((InputStream)args[0]).available());
			case 71: // IStream.reset()
				((InputStream)args[0]).reset();
				return null;
			case 72: // OStream.write(b: Int)
				((OutputStream)args[0]).write(ival(args[1]));
				return null;
			case 73: { // OStream.writeArray(buf: [Byte], ofs: Int, len: Int)
				byte[] buf = (byte[])args[1];
				int len = ival(args[3]);
				if (len < 0) len = buf.length;
				((OutputStream)args[0]).write(buf, ival(args[2]), len);
				return null;
			}
			case 74: // OStream.print(a: Any)
				IO.print((OutputStream)args[0], args[1]);
				return null;
			case 75: // OStream.println(a: Any)
				IO.println((OutputStream)args[0], args[1]);
				return null;
			case 76: // OStream.flush()
				((OutputStream)args[0]).flush();
				return null;
			case 77: // OStream.writeAll(input: IStream)
				IO.writeAll((InputStream)args[1], (OutputStream)args[0]);
				return null;
			case 78: // stdin(): IStream
				return p.stdin;
			case 79: // stdout(): OStream
				return p.stdout;
			case 80: // stderr(): OStream
				return p.stderr;
			case 81: // setin(in: IStream)
				p.stdin = ((InputStream)args[0]);
				return null;
			case 82: // setout(out: OStream)
				p.stdout = ((OutputStream)args[0]);
				return null;
			case 83: // seterr(err: OStream)
				p.stderr = ((OutputStream)args[0]);
				return null;
			case 84: // pathfile(path: String): String
				return Filesystem.fileName((String)args[0]);
			case 85: // pathdir(path: String): String
				return Filesystem.fileParent((String)args[0]);
			case 86: // abspath(path: String): String
				return p.toFile((String)args[0]);
			case 87: // relpath(path: String): String
				return Filesystem.relativePath(p.getCurrentDirectory(), p.toFile((String)args[0]));
			case 88: // fcreate(path: String)
				Filesystem.create(p.toFile((String)args[0]));
				return null;
			case 89: // fremove(path: String)
				Filesystem.remove(p.toFile((String)args[0]));
				return null;
			case 90: // fremoveTree(path: String)
				Filesystem.removeTree(p.toFile((String)args[0]));
				return null;
			case 91: // mkdir(path: String)
				Filesystem.mkdir(p.toFile((String)args[0]));
				return null;
			case 92: // mkdirTree(path: String)
				Filesystem.mkdirTree(p.toFile((String)args[0]));
				return null;
			case 93: // fcopy(source: String, dest: String)
				Filesystem.copy(p.toFile((String)args[0]), p.toFile((String)args[1]));
				return null;
			case 94: // fmove(source: String, dest: String)
				Filesystem.move(p.toFile((String)args[0]), p.toFile((String)args[1]));
				return null;
			case 95: // exists(path: String): Bool
				return Ival(Filesystem.exists(p.toFile((String)args[0])));
			case 96: // isDir(path: String): Bool
				return Ival(Filesystem.isDirectory(p.toFile((String)args[0])));
			case 97: { // fread(path: String): IStream
				ConnectionInputStream input = new ConnectionInputStream(Filesystem.read(p.toFile((String)args[0])));
				p.addConnection(input);
				return input;
			}
			case 98: { // fwrite(path: String): OStream
				ConnectionOutputStream output = new ConnectionOutputStream(Filesystem.write(p.toFile((String)args[0])));
				p.addConnection(output);
				return output;
			}
			case 99: { // fappend(path: String): OStream
				ConnectionOutputStream output = new ConnectionOutputStream(Filesystem.append(p.toFile((String)args[0])));
				p.addConnection(output);
				return output;
			}
			case 100: // flist(path: String): [String]
				return Filesystem.list(p.toFile((String)args[0]));
			case 101: // fmodified(path: String): Long
				return Lval(Filesystem.lastModified(p.toFile((String)args[0])));
			case 102: // fsize(path: String): Long
				return Lval(Filesystem.size(p.toFile((String)args[0])));
			case 103: // setRead(path: String, on: Bool)
				Filesystem.setRead(p.toFile((String)args[0]), bval(args[1]));
				return null;
			case 104: // setWrite(path: String, on: Bool)
				Filesystem.setWrite(p.toFile((String)args[0]), bval(args[1]));
				return null;
			case 105: // setExec(path: String, on: Bool)
				Filesystem.setExec(p.toFile((String)args[0]), bval(args[1]));
				return null;
			case 106: // canRead(path: String): Bool
				return Ival(Filesystem.canRead(p.toFile((String)args[0])));
			case 107: // canWrite(path: String): Bool
				return Ival(Filesystem.canWrite(p.toFile((String)args[0])));
			case 108: // canExec(path: String): Bool
				return Ival(Filesystem.canExec(p.toFile((String)args[0])));
			case 109: // getCwd(): String
				return p.getCurrentDirectory();
			case 110: // setCwd(dir: String)
				p.setCurrentDirectory(p.toFile((String)args[0]));
				return null;
			case 111: // spaceTotal(root: String): Long
				return Lval(Filesystem.spaceTotal(p.toFile((String)args[0])));
			case 112: // spaceFree(root: String): Long
				return Lval(Filesystem.spaceFree(p.toFile((String)args[0])));
			case 113: // spaceUsed(root: String): Long
				return Lval(Filesystem.spaceUsed(p.toFile((String)args[0])));
			case 114: { // readUrl(url: String): IStream
				return IO.readUrl((String)args[0]);
			}
			case 115: // matchesGlob(path: String, glob: String): Bool
				return Ival(IO.matchesPattern((String)args[0], (String)args[1]));

			/* == Header: bufferio.eh == */
			case 116: // BufferIStream.new(buf: [Byte])
				return new BufferIStream((byte[])args[0]);
			case 117: // BufferOStream.new()
				return new BufferOStream();
			case 118: // BufferOStream.len(): Int
				return Ival(((BufferOStream)args[0]).size());
			case 119: // BufferOStream.getBytes(): [Byte]
				return ((BufferOStream)args[0]).toByteArray();
			case 120: // BufferOStream.reset()
				((BufferOStream)args[0]).reset();
				return null;

			/* == Header: pipe.eh == */
			case 121: { // Pipe.new()
				Pipe pipe = new Pipe();
				p.addConnection(pipe);
				return pipe;
			}

			/* == Header: strbuf.eh == */
			case 122: // StrBuf.new(): StrBuf
				return new StringBuffer();
			case 123: // StrBuf.get(at: Int): Char
				return Ival(((StringBuffer)args[0]).charAt(ival(args[1])));
			case 124: // StrBuf.chars(from: Int, to: Int, buf: [Char], ofs: Int)
				((StringBuffer)args[0]).getChars(ival(args[1]), ival(args[2]), (char[])args[3], ival(args[4]));
				return null;
			case 125: // StrBuf.append(a: Any): StrBuf
				return ((StringBuffer)args[0]).append(Strings.toString(args[1]));
			case 126: // StrBuf.addch(ch: Char): StrBuf
				return ((StringBuffer)args[0]).append((char)ival(args[1]));
			case 127: // StrBuf.insert(at: Int, a: Any): StrBuf
				return ((StringBuffer)args[0]).insert(ival(args[1]), Strings.toString(args[2]));
			case 128: // StrBuf.insch(at: Int, ch: Char): StrBuf
				return ((StringBuffer)args[0]).insert(ival(args[1]), (char)ival(args[2]));
			case 129: // StrBuf.setch(at: Int, ch: Char): StrBuf
				((StringBuffer)args[0]).setCharAt(ival(args[1]), (char)ival(args[2]));
				return args[0];
			case 130: // StrBuf.delete(from: Int, to: Int): StrBuf
				return ((StringBuffer)args[0]).delete(ival(args[1]), ival(args[2]));
			case 131: // StrBuf.delch(at: Int): StrBuf
				return ((StringBuffer)args[0]).deleteCharAt(ival(args[1]));
			case 132: // StrBuf.len(): Int
				return Ival(((StringBuffer)args[0]).length());

			/* == Header: dl.eh == */
			case 133: // loadlibrary(libname: String): Library
				return p.loadLibrary((String)args[0]);
			case 134: { // buildlibrary(in: IStream): Library
				InputStream in = (InputStream)args[0];
				if (((in.read() << 8) | in.read()) != 0xC0DE)
					throw new InstantiationException("Not an Ether object");
				return EtherLoader.load(p, in);
			}
			case 135: // Library.getFunction(sig: String): Function
				return ((Library)args[0]).getFunction((String)args[1]);

			/* == Header: time.eh == */
			case 136: // datestr(time: Long): String
				return new Date(lval(args[0])).toString();
			case 137: { // year(time: Long): Int
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.YEAR));
			}
			case 138: { // month(time: Long): Int
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.MONTH));
			}
			case 139: { // day(time: Long): Int
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.DAY_OF_MONTH));
			}
			case 140: { // dow(time: Long): Int
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.DAY_OF_WEEK));
			}
			case 141: { // hour(time: Long): Int
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.HOUR_OF_DAY));
			}
			case 142: { // minute(time: Long): Int
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.MINUTE));
			}
			case 143: { // second(time: Long): Int
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.SECOND));
			}
			case 144: { // millis(time: Long): Int
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.MILLISECOND));
			}
			case 145: { // timeof(year: Int, month: Int, day: Int, hour: Int, min: Int, sec: Int, millis: Int): Long
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

			/* == Header: dict.eh == */
			case 146: // Dict.new(): Dict
				return new HashMap();
			case 147: // Dict.len(): Int
				return Ival(((HashMap)args[0]).size());
			case 148: // Dict.get(key: Any): Any
				return ((HashMap)args[0]).get(args[1]);
			case 149: // Dict.set(key: Any, value: Any)
				((HashMap)args[0]).set(args[1], args[2]);
				return null;
			case 150: // Dict.remove(key: Any)
				((HashMap)args[0]).remove(args[1]);
				return null;
			case 151: // Dict.clear()
				((HashMap)args[0]).clear();
				return null;
			case 152: // Dict.keys(): [Any]
				return ((HashMap)args[0]).keys();

			/* == Header: list.eh == */
			case 153: { // List.new(a: Array = null): List
				ArrayList list = new ArrayList();
				if (args[0] != null) list.addFrom(args[0], 0, -1);
				return list;
			}
			case 154: // List.len(): Int
				return Ival(((ArrayList)args[0]).size());
			case 155: // List.get(at: Int): Any
				return ((ArrayList)args[0]).get(ival(args[1]));
			case 156: // List.set(at: Int, val: Any)
				((ArrayList)args[0]).set(ival(args[1]), args[2]);
				return null;
			case 157: // List.add(val: Any)
				((ArrayList)args[0]).add(args[1]);
				return null;
			case 158: // List.addFrom(arr: Array, ofs: Int = 0, len: Int = -1)
				((ArrayList)args[0]).addFrom(args[1], ival(args[2]), ival(args[3]));
				return null;
			case 159: // List.insert(at: Int, val: Any)
				((ArrayList)args[0]).insert(ival(args[1]), args[2]);
				return null;
			case 160: // List.insertfrom(at: Int, arr: Array, ofs: Int = 0, len: Int = -1)
				((ArrayList)args[0]).insertFrom(ival(args[1]), args[2], ival(args[3]), ival(args[4]));
				return null;
			case 161: // List.remove(at: Int)
				((ArrayList)args[0]).remove(ival(args[1]));
				return null;
			case 162: // List.clear()
				((ArrayList)args[0]).clear();
				return null;
			case 163: // List.range(from: Int, to: Int): List
				return ((ArrayList)args[0]).getRange(ival(args[1]), ival(args[2]));
			case 164: // List.indexof(val: Any, from: Int = 0): Int
				return Ival(((ArrayList)args[0]).indexOf(args[1], ival(args[2])));
			case 165: // List.lindexof(val: Any): Int
				return Ival(((ArrayList)args[0]).lastIndexOf(args[1]));
			case 166: { // List.filter(f: (Any):Bool): List
				ArrayList oldlist = (ArrayList)args[0];
				int len = oldlist.size();
				ArrayList newlist = new ArrayList(len);
				Function f = (Function)args[1];
				for (int i=0; i<len; i++) {
					Object e = oldlist.get(i);
					if (f.invoke(p, new Object[] {oldlist.get(i)}) == Int32.ONE) {
						newlist.add(e);
					}
				}
				return newlist;
			}
			case 167: { // List.filterself(f: (Any):Bool)
				ArrayList list = (ArrayList)args[0];
				Function f = (Function)args[1];
				int len = list.size();
				int i=0;
				while (i < len) {
					if (f.invoke(p, new Object[] {list.get(i)}) == Int32.ONE) {
						i++;
					} else {
						list.remove(i);
						len--;
					}
				}
				return null;
			}
			case 168: { // List.map(f: (Any):Any): List
				ArrayList oldlist = (ArrayList)args[0];
				int len = oldlist.size();
				ArrayList newlist = new ArrayList(len);
				Function f = (Function)args[1];
				for (int i=0; i<len; i++) {
					newlist.add(f.invoke(p, new Object[] {oldlist.get(i)}));
				}
				return newlist;
			}
			case 169: { // List.mapself(f: (Any):Any)
				ArrayList list = (ArrayList)args[0];
				Function f = (Function)args[1];
				int len = list.size();
				for (int i=0; i<len; i++) {
					list.set(i, f.invoke(p, new Object[] {list.get(i)}));
				}
				return null;
			}
			case 170: { // List.reduce(f: (Any,Any):Any): Any
				ArrayList list = (ArrayList)args[0];
				Function f = (Function)args[1];
				int len = list.size();
				if (len == 0) return null;
				Object ret = list.get(0);
				Object[] fparams = new Object[2];
				for (int i=1; i<len; i++) {
					fparams[0] = ret;
					fparams[1] = list.get(i);
					ret = f.invoke(p, fparams);
				}
				return ret;
			}
			case 171: { // List.sort(f: (Any,Any):Int): List
				ArrayList oldList = (ArrayList)args[0];
				Object[] data = new Object[oldList.size()];
				oldList.copyInto(data);
				if (data.length > 0) {
					Arrays.qsort(data, 0, data.length-1, p, (Function)args[1]);
				}
				ArrayList newList = new ArrayList(data.length);
				newList.addFrom(data, 0, data.length);
				return newList;
			}
			case 172: { // List.sortself(f: (Any,Any):Int)
				ArrayList list = (ArrayList)args[0];
				Object[] data = new Object[list.size()];
				list.copyInto(data);
				if (data.length > 0) {
					Arrays.qsort(data, 0, data.length-1, p, (Function)args[1]);
				}
				list.clear();
				list.addFrom(data, 0, data.length);
				return null;
			}
			case 173: { // List.reverse(): List
				ArrayList oldList = (ArrayList)args[0];
				int len  = oldList.size();
				ArrayList newList = new ArrayList(len);
				for (int i=len-1; i>=0; i--) {
					newList.add(oldList.get(i));
				}
				return newList;
			}
			case 174: { // List.toarray(): [Any]
				ArrayList list = (ArrayList)args[0];
				Object[] data = new Object[list.size()];
				list.copyInto(data);
				return data;
			}
			case 175: // List.copyInto(from: Int, buf: Array, ofs: Int, len: Int)
				((ArrayList)args[0]).copyInto(ival(args[1]), args[2], ival(args[3]), ival(args[4]));
				return null;

			/* == Header: thread.eh == */
			case 176: // currentThread(): Thread
				return Thread.currentThread();
			case 177: // Thread.new(run: ());
				return p.createThread((Function)args[0]);
			case 178: // Thread.start()
				((ProcessThread)args[0]).start();
				return null;
			case 179: // Thread.isAlive(): Bool
				return Ival(((ProcessThread)args[0]).isAlive());
			case 180: // Thread.interrupt()
				((ProcessThread)args[0]).interrupt();
				return null;
			case 181: // Thread.isInterrupted(): Bool
				return Ival(((ProcessThread)args[0]).isInterrupted());
			case 182: // Thread.join()
				((ProcessThread)args[0]).join();
				return null;
			case 183: // Lock.new()
				return new Lock();
			case 184: // Lock.lock()
				((Lock)args[0]).lock();
				return null;
			case 185: // Lock.tryLock(): Bool
				return Ival(((Lock)args[0]).tryLock());
			case 186: // Lock.unlock()
				((Lock)args[0]).unlock();
				return null;

			/* == Header: process.eh == */
			case 187: // currentProcess(): Process
				return p;
			case 188: { // Process.new(cmd: String, args: [String])
				Object[] objArgs = (Object[])args[1];
				String[] strArgs = new String[objArgs.length];
				System.arraycopy(objArgs, 0, strArgs, 0, objArgs.length);
				return new Process(p, (String)args[0], strArgs);
			}
			case 189: // Process.getState(): Int
				return Ival(((Process)args[0]).getState());
			case 190: // Process.getPriority(): Int
				return Ival(((Process)args[0]).getPriority());
			case 191: { // Process.setPriority(value: Int)
				Process process = (Process)args[0];
				if (process == p || process.getState() == Process.NEW) {
					process.setPriority(ival(args[1]));
					return null;
				} else {
					throw new SecurityException();
				}
			}
			case 192: // Process.getName(): String
				return ((Process)args[0]).getName();
			case 193: // Process.getArgs(): [String]
				return ((Process)args[0]).getArgs();
			case 194: { // Process.setEnv(key: String, value: String)
				Process process = (Process)args[0];
				if (process == p || process.getState() == Process.NEW) {
					process.setEnv((String)args[1], (String)args[2]);
					return null;
				} else {
					throw new SecurityException();
				}
			}
			case 195: { // Process.setIn(in: IStream)
				Process process = (Process)args[0];
				if (process == p || process.getState() == Process.NEW) {
					process.stdin = (InputStream)args[1];
					return null;
				} else {
					throw new SecurityException();
				}
			}
			case 196: { // Process.setOut(out: OStream)
				Process process = (Process)args[0];
				if (process == p || process.getState() == Process.NEW) {
					process.stdout = (OutputStream)args[1];
					return null;
				} else {
					throw new SecurityException();
				}
			}
			case 197: { // Process.setErr(err: OStream)
				Process process = (Process)args[0];
				if (process == p || process.getState() == Process.NEW) {
					process.stderr = (OutputStream)args[1];
					return null;
				} else {
					throw new SecurityException();
				}
			}
			case 198: { // Process.setCwd(cwd: String)
				Process process = (Process)args[0];
				if (process == p || process.getState() == Process.NEW) {
					process.setCurrentDirectory(p.toFile((String)args[1]));
					return null;
				} else {
					throw new SecurityException();
				}
			}
			case 199: // Process.start(): Process
				return ((Process)args[0]).start();
			case 200: // Process.waitFor(): Int
				return Ival(((Process)args[0]).waitFor());
			case 201: // Process.kill()
				((Process)args[0]).kill();
				return null;
			case 202: // Process.getExitCode(): Int
				return Ival(((Process)args[0]).getExitCode());
			case 203: // Process.getError(): Error
				return ((Process)args[0]).getError();

			/* == Header: sys.eh == */
			case 204: // systime(): Long
				return Lval(System.currentTimeMillis());
			case 205: // getenv(key: String): String
				return p.getEnv((String)args[0]);
			case 206: // setenv(key: String, val: String)
				p.setEnv((String)args[0], (String)args[1]);
				return null;
			case 207: // sleep(millis: Long)
				Thread.sleep(lval(args[0]));
				return null;
			case 208: // sysProperty(str: String): String
				return System.getProperty((String)args[0]);
			case 209: // platformRequest(url: String): Bool
				return Ival(Platform.getPlatform().getCore().platformRequest((String)args[0]));

			/* == Header: term.eh == */
			case 210: // isTerm(stream: IStream): Bool
				return Ival(args[0] instanceof TerminalInput);
			case 211: // TermIStream.clear()
				((TerminalInput)args[0]).clear();
				return null;
			case 212: // TermIStream.getPrompt(): String
				return ((TerminalInput)args[0]).getPrompt();
			case 213: // TermIStream.setPrompt(prompt: String)
				((TerminalInput)args[0]).setPrompt((String)args[1]);
				return null;

			/* == Header: array.eh == */
			case 214: { // [Byte].sort(from: Int = 0, to: Int = -1)
				byte[] array = (byte[]) args[0];
				int from = ival(args[1]);
				int to = ival(args[2]);
				if (to < 0) to = array.length-from;
				Arrays.qsort(array, from, to-1);
				return null;
			}
			case 215: { // [Char].sort(from: Int = 0, to: Int = -1)
				char[] array = (char[]) args[0];
				int from = ival(args[1]);
				int to = ival(args[2]);
				if (to < 0) to = array.length-from;
				Arrays.qsort(array, from, to-1);
				return null;
			}
			case 216: { // [Short].sort(from: Int = 0, to: Int = -1)
				short[] array = (short[]) args[0];
				int from = ival(args[1]);
				int to = ival(args[2]);
				if (to < 0) to = array.length-from;
				Arrays.qsort(array, from, to-1);
				return null;
			}
			case 217: { // [Int].sort(from: Int = 0, to: Int = -1)
				int[] array = (int[]) args[0];
				int from = ival(args[1]);
				int to = ival(args[2]);
				if (to < 0) to = array.length-from;
				Arrays.qsort(array, from, to-1);
				return null;
			}
			case 218: { // [Long].sort(from: Int = 0, to: Int = -1)
				long[] array = (long[]) args[0];
				int from = ival(args[1]);
				int to = ival(args[2]);
				if (to < 0) to = array.length-from;
				Arrays.qsort(array, from, to-1);
				return null;
			}
			case 219: { // [Float].sort(from: Int = 0, to: Int = -1)
				float[] array = (float[]) args[0];
				int from = ival(args[1]);
				int to = ival(args[2]);
				if (to < 0) to = array.length-from;
				Arrays.qsort(array, from, to-1);
				return null;
			}
			case 220: { // [Double].sort(from: Int = 0, to: Int = -1)
				double[] array = (double[]) args[0];
				int from = ival(args[1]);
				int to = ival(args[2]);
				if (to < 0) to = array.length-from;
				Arrays.qsort(array, from, to-1);
				return null;
			}
			case 221: { // [Any].sort(from: Int = 0, to: Int = -1, f: (Any,Any):Int)
				Object[] array = (Object[]) args[0];
				int from = ival(args[1]);
				int to = ival(args[2]);
				if (to < 0) to = array.length-from;
				Arrays.qsort(array, from, to-1, p, (Function)args[3]);
				return null;
			}
			default:
				return null;
		}
	}

	private static final class BufferOStream extends ByteArrayOutputStream implements Connection {
	}

	private static final class BufferIStream extends ByteArrayInputStream implements Connection {
		public BufferIStream(byte[] buf) { super(buf); }
	}
}
