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

import alchemy.evm.EtherLoader;
import alchemy.fs.Filesystem;
import alchemy.io.ConnectionInputStream;
import alchemy.io.ConnectionOutputStream;
import alchemy.io.IO;
import alchemy.io.Pipe;
import alchemy.io.TerminalInput;
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

			/* == Header: string.eh == */
			case 2: // Any.tostr(): String
				return Strings.toString(args[0]);
			case 3: // Char.tostr(): String
				return String.valueOf((char)ival(args[0]));
			case 4: // Int.tobin():String
				return Integer.toBinaryString(ival(args[0]));
			case 5: // Int.tooct():String
				return Integer.toOctalString(ival(args[0]));
			case 6: // Int.tohex():String
				return Integer.toHexString(ival(args[0]));
			case 7: // Int.tobase(base: Int): String
				return Integer.toString(ival(args[0]), ival(args[1]));
			case 8: // Long.tobase(base: Int): String
				return Long.toString(lval(args[0]), ival(args[1]));
			case 9: // String.tointbase(base: Int): Int
				return Ival(Integer.parseInt((String)args[0], ival(args[1])));
			case 10: // String.tolongbase(base: Int): Long
				return Lval(Long.parseLong((String)args[0], ival(args[1])));
			case 11: // String.tofloat(): Float
				return Fval(Float.parseFloat((String)args[0]));
			case 12: // String.todouble(): Double
				return Dval(Double.parseDouble((String)args[0]));
			case 13: { // String.get(at: Int): Char
				String str = (String)args[0];
				int at = ival(args[1]);
				if (at < 0) at += str.length();
				return Ival(str.charAt(at));
			}
			case 14: // String.len(): Int
				return Ival(((String)args[0]).length());
			case 15: { // String.range(from: Int, to: Int): String
				String str = (String) args[0];
				int from = ival(args[1]);
				int to = ival(args[2]);
				if (from < 0) from += str.length();
				if (to < 0) to += str.length();
				return str.substring(from, to);
			}
			case 16: { // String.indexof(ch: Char, from: Int = 0): Int
				String str = (String)args[0];
				int from = ival(args[2]);
				if (from < 0) from += str.length();
				return Ival(str.indexOf(ival(args[1]), from));
			}
			case 17: // String.lindexof(ch: Char): Int
				return Ival(((String)args[0]).lastIndexOf(ival(args[1])));
			case 18: { // String.find(sub: String, from: Int = 0): Int
				String str = (String)args[0];
				int from = ival(args[2]);
				if (from < 0) from += str.length();
				return Ival(str.indexOf((String)args[1], from));
			}
			case 19: // String.ucase(): String
				return ((String)args[0]).toUpperCase();
			case 20: // String.lcase(): String
				return ((String)args[0]).toLowerCase();
			case 21: // String.concat(str: String): String
				return ((String)args[0]).concat((String)args[1]);
			case 22: // String.cmp(str: String): Int
				return Ival(((String)args[0]).compareTo((String)args[1]));
			case 23: // String.trim(): String
				return ((String)args[0]).trim();
			case 24: // String.split(ch: Char, skipEmpty: Bool = false): [String]
				return Strings.split((String)args[0], (char)ival(args[1]), bval(args[2]));
			case 25: // String.format(args: [Any]): String
				return Strings.format((String)args[0], (Object[])args[1]);
			case 26: // String.chars(): [Char]
				return ((String)args[0]).toCharArray();
			case 27: // String.utfbytes(): [Byte]
				return Strings.utfEncode((String)args[0]);
			case 28: { // String.startswith(prefix: String, from: Int = 0): Bool
				String str = (String)args[0];
				int from = ival(args[2]);
				if (from < 0) from += str.length();
				return Ival(str.startsWith((String)args[1], from));
			}
			case 29: // String.replace(oldch: Char, newch: Char): String
				return ((String)args[0]).replace((char)ival(args[1]), (char)ival(args[2]));
			case 30: // String.hash(): Int
				return Ival(((String)args[0]).hashCode());
			case 31: // ca2str(ca: [Char]): String
				return new String((char[])args[0]);
			case 32: // ba2utf(ba: [Byte]): String
				return Strings.utfDecode((byte[])args[0]);

			/* == Header: error.eh == */
			case 33: // Error.code(): Int
				return Ival(((AlchemyException)args[0]).errcode);
			case 34: // Error.msg(): String
				return ((AlchemyException)args[0]).getMessage();
			case 35: // Error.traceLen(): Int
				return Ival(((AlchemyException)args[0]).getTraceLength());
			case 36: // Error.traceName(index: Int): String
				return ((AlchemyException)args[0]).getTraceElementName(ival(args[1]));
			case 37: // Error.traceDbg(index: Int): String
				return ((AlchemyException)args[0]).getTraceElementInfo(ival(args[1]));

			/* == Header: math.eh == */
			case 38: // abs(val: Double): Double
				return Dval(Math.abs(dval(args[0])));
			case 39: // sin(val: Double): Double
				return Dval(Math.sin(dval(args[0])));
			case 40: // cos(val: Double): Double
				return Dval(Math.cos(dval(args[0])));
			case 41: // tan(val: Double): Double
				return Dval(Math.tan(dval(args[0])));
			case 42: // sqrt(val: Double): Double
				return Dval(Math.sqrt(dval(args[0])));
			case 43: // ipow(val: Double, pow: Int): Double
				return Dval(alchemy.util.Math.ipow(dval(args[0]), ival(args[1])));
			case 44: // exp(val: Double): Double
				return Dval(alchemy.util.Math.exp(dval(args[0])));
			case 45: // log(val: Double): Double
				return Dval(alchemy.util.Math.log(dval(args[0])));
			case 46: // asin(val: Double): Double
				return Dval(alchemy.util.Math.asin(dval(args[0])));
			case 47: // acos(val: Double): Double
				return Dval(alchemy.util.Math.acos(dval(args[0])));
			case 48: // atan(val: Double): Double
				return Dval(alchemy.util.Math.atan(dval(args[0])));
			case 49: // ibits2f(bits: Int): Float
				return Fval(Float.intBitsToFloat(ival(args[0])));
			case 50: // f2ibits(f: Float): Int
				return Ival(Float.floatToIntBits(fval(args[0])));
			case 51: // lbits2d(bits: Long): Double
				return Dval(Double.longBitsToDouble(lval(args[0])));
			case 52: // d2lbits(d: Double): Long
				return Lval(Double.doubleToLongBits(dval(args[0])));

			/* == Header: rnd.eh == */
			case 53: // Random.new(seed: Long)
				return new Random(lval(args[0]));
			case 54: // Random.next(n: Int): Int
				return Ival(((Random)args[0]).nextInt(ival(args[1])));
			case 55: // Random.nextInt(): Int
				return Ival(((Random)args[0]).nextInt());
			case 56: // Random.nextLong(): Long
				return Lval(((Random)args[0]).nextLong());
			case 57: // Random.nextFloat(): Float
				return Fval(((Random)args[0]).nextFloat());
			case 58: // Random.nextDouble(): Double
				return Dval(((Random)args[0]).nextDouble());
			case 59: // Random.setSeed(seed: Long)
				((Random)args[0]).setSeed(lval(args[1]));
				return null;

			/* == Header: io.eh == */
			case 60: { // Connection.close()
				Connection conn = (Connection) args[0];
				conn.close();
				p.removeConnection(conn);
				return null;
			}
			case 61: { // StreamConnection.openInput(): IStream
				ConnectionInputStream in = new ConnectionInputStream(((StreamConnection)args[0]).openInputStream());
				p.addConnection(in);
				return in;
			}
			case 62: { // StreamConnection.openOutput(): OStream
				ConnectionOutputStream out = new ConnectionOutputStream(((StreamConnection)args[0]).openOutputStream());
				p.addConnection(out);
				return out;
			}
			case 63: // IStream.read(): Int
				return Ival(((InputStream)args[0]).read());
			case 64: { // IStream.readArray(buf: [Byte], ofs: Int = 0, len: Int = -1): Int
				byte[] buf = (byte[])args[1];
				int len = ival(args[3]);
				if (len < 0) len = buf.length;
				return Ival(((InputStream)args[0]).read(buf, ival(args[2]), len));
			}
			case 65: // IStream.readFully(): [Byte]
				return IO.readFully((InputStream)args[0]);
			case 66: // IStream.skip(num: Long): Long
				return Lval(((InputStream)args[0]).skip(lval(args[1])));
			case 67: // IStream.available(): Int
				return Ival(((InputStream)args[0]).available());
			case 68: // IStream.reset()
				((InputStream)args[0]).reset();
				return null;
			case 69: // OStream.write(b: Int)
				((OutputStream)args[0]).write(ival(args[1]));
				return null;
			case 70: { // OStream.writeArray(buf: [Byte], ofs: Int, len: Int)
				byte[] buf = (byte[])args[1];
				int len = ival(args[3]);
				if (len < 0) len = buf.length;
				((OutputStream)args[0]).write(buf, ival(args[2]), len);
				return null;
			}
			case 71: // OStream.print(a: Any)
				IO.print((OutputStream)args[0], args[1]);
				return null;
			case 72: // OStream.println(a: Any)
				IO.println((OutputStream)args[0], args[1]);
				return null;
			case 73: // OStream.flush()
				((OutputStream)args[0]).flush();
				return null;
			case 74: // OStream.writeAll(input: IStream)
				IO.writeAll((InputStream)args[1], (OutputStream)args[0]);
				return null;
			case 75: // stdin(): IStream
				return p.stdin;
			case 76: // stdout(): OStream
				return p.stdout;
			case 77: // stderr(): OStream
				return p.stderr;
			case 78: // setin(in: IStream)
				p.stdin = ((InputStream)args[0]);
				return null;
			case 79: // setout(out: OStream)
				p.stdout = ((OutputStream)args[0]);
				return null;
			case 80: // seterr(err: OStream)
				p.stderr = ((OutputStream)args[0]);
				return null;
			case 81: // pathfile(path: String): String
				return Filesystem.fileName((String)args[0]);
			case 82: // pathdir(path: String): String
				return Filesystem.fileParent((String)args[0]);
			case 83: // abspath(path: String): String
				return p.toFile((String)args[0]);
			case 84: // relpath(path: String): String
				return Filesystem.relativePath(p.getCurrentDirectory(), p.toFile((String)args[0]));
			case 85: // fcreate(path: String)
				Filesystem.create(p.toFile((String)args[0]));
				return null;
			case 86: // fremove(path: String)
				Filesystem.remove(p.toFile((String)args[0]));
				return null;
			case 87: // fremoveTree(path: String)
				Filesystem.removeTree(p.toFile((String)args[0]));
				return null;
			case 88: // mkdir(path: String)
				Filesystem.mkdir(p.toFile((String)args[0]));
				return null;
			case 89: // mkdirTree(path: String)
				Filesystem.mkdirTree(p.toFile((String)args[0]));
				return null;
			case 90: // fcopy(source: String, dest: String)
				Filesystem.copy(p.toFile((String)args[0]), p.toFile((String)args[1]));
				return null;
			case 91: // fmove(source: String, dest: String)
				Filesystem.move(p.toFile((String)args[0]), p.toFile((String)args[1]));
				return null;
			case 92: // exists(path: String): Bool
				return Ival(Filesystem.exists(p.toFile((String)args[0])));
			case 93: // isDir(path: String): Bool
				return Ival(Filesystem.isDirectory(p.toFile((String)args[0])));
			case 94: { // fread(path: String): IStream
				ConnectionInputStream input = new ConnectionInputStream(Filesystem.read(p.toFile((String)args[0])));
				p.addConnection(input);
				return input;
			}
			case 95: { // fwrite(path: String): OStream
				ConnectionOutputStream output = new ConnectionOutputStream(Filesystem.write(p.toFile((String)args[0])));
				p.addConnection(output);
				return output;
			}
			case 96: { // fappend(path: String): OStream
				ConnectionOutputStream output = new ConnectionOutputStream(Filesystem.append(p.toFile((String)args[0])));
				p.addConnection(output);
				return output;
			}
			case 97: // flist(path: String): [String]
				return Filesystem.list(p.toFile((String)args[0]));
			case 98: // fmodified(path: String): Long
				return Lval(Filesystem.lastModified(p.toFile((String)args[0])));
			case 99: // fsize(path: String): Long
				return Lval(Filesystem.size(p.toFile((String)args[0])));
			case 100: // setRead(path: String, on: Bool)
				Filesystem.setRead(p.toFile((String)args[0]), bval(args[1]));
				return null;
			case 101: // setWrite(path: String, on: Bool)
				Filesystem.setWrite(p.toFile((String)args[0]), bval(args[1]));
				return null;
			case 102: // setExec(path: String, on: Bool)
				Filesystem.setExec(p.toFile((String)args[0]), bval(args[1]));
				return null;
			case 103: // canRead(path: String): Bool
				return Ival(Filesystem.canRead(p.toFile((String)args[0])));
			case 104: // canWrite(path: String): Bool
				return Ival(Filesystem.canWrite(p.toFile((String)args[0])));
			case 105: // canExec(path: String): Bool
				return Ival(Filesystem.canExec(p.toFile((String)args[0])));
			case 106: // getCwd(): String
				return p.getCurrentDirectory();
			case 107: // setCwd(dir: String)
				p.setCurrentDirectory(p.toFile((String)args[0]));
				return null;
			case 108: // spaceTotal(root: String): Long
				return Lval(Filesystem.spaceTotal(p.toFile((String)args[0])));
			case 109: // spaceFree(root: String): Long
				return Lval(Filesystem.spaceFree(p.toFile((String)args[0])));
			case 110: // spaceUsed(root: String): Long
				return Lval(Filesystem.spaceUsed(p.toFile((String)args[0])));
			case 111: { // readUrl(url: String): IStream
				return IO.readUrl((String)args[0]);
			}
			case 112: // matchesGlob(path: String, glob: String): Bool
				return Ival(IO.matchesPattern((String)args[0], (String)args[1]));

			/* == Header: bufferio.eh == */
			case 113: // BufferIStream.new(buf: [Byte])
				return new ConnectionInputStream(new ByteArrayInputStream((byte[])args[0]));
			case 114: // BufferOStream.new()
				return new ConnectionOutputStream(new ByteArrayOutputStream());
			case 115: // BufferOStream.len(): Int
				return Ival(((ByteArrayOutputStream)args[0]).size());
			case 116: // BufferOStream.getBytes(): [Byte]
				return ((ByteArrayOutputStream)args[0]).toByteArray();
			case 117: // BufferOStream.reset()
				((ByteArrayOutputStream)args[0]).reset();
				return null;

			/* == Header: pipe.eh == */
			case 118: { // Pipe.new()
				Pipe pipe = new Pipe();
				p.addConnection(pipe);
				return pipe;
			}

			/* == Header: strbuf.eh == */
			case 119: // StrBuf.new(): StrBuf
				return new StringBuffer();
			case 120: // StrBuf.get(at: Int): Char
				return Ival(((StringBuffer)args[0]).charAt(ival(args[1])));
			case 121: // StrBuf.chars(from: Int, to: Int, buf: [Char], ofs: Int)
				((StringBuffer)args[0]).getChars(ival(args[1]), ival(args[2]), (char[])args[3], ival(args[4]));
				return null;
			case 122: // StrBuf.append(a: Any): StrBuf
				return ((StringBuffer)args[0]).append(Strings.toString(args[1]));
			case 123: // StrBuf.addch(ch: Char): StrBuf
				return ((StringBuffer)args[0]).append((char)ival(args[1]));
			case 124: // StrBuf.insert(at: Int, a: Any): StrBuf
				return ((StringBuffer)args[0]).insert(ival(args[1]), Strings.toString(args[2]));
			case 125: // StrBuf.insch(at: Int, ch: Char): StrBuf
				return ((StringBuffer)args[0]).insert(ival(args[1]), (char)ival(args[2]));
			case 126: // StrBuf.setch(at: Int, ch: Char): StrBuf
				((StringBuffer)args[0]).setCharAt(ival(args[1]), (char)ival(args[2]));
				return args[0];
			case 127: // StrBuf.delete(from: Int, to: Int): StrBuf
				return ((StringBuffer)args[0]).delete(ival(args[1]), ival(args[2]));
			case 128: // StrBuf.delch(at: Int): StrBuf
				return ((StringBuffer)args[0]).deleteCharAt(ival(args[1]));
			case 129: // StrBuf.len(): Int
				return Ival(((StringBuffer)args[0]).length());

			/* == Header: dl.eh == */
			case 130: // loadlibrary(libname: String): Library
				return p.loadLibrary((String)args[0]);
			case 131: { // buildlibrary(in: IStream): Library
				InputStream in = (InputStream)args[0];
				if (((in.read() << 8) | in.read()) != 0xC0DE)
					throw new InstantiationException("Not an Ether object");
				return EtherLoader.load(p, in);
			}
			case 132: // Library.getFunction(sig: String): Function
				return ((Library)args[0]).getFunction((String)args[1]);

			/* == Header: time.eh == */
			case 133: // datestr(time: Long): String
				return new Date(lval(args[0])).toString();
			case 134: { // year(time: Long): Int
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.YEAR));
			}
			case 135: { // month(time: Long): Int
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.MONTH));
			}
			case 136: { // day(time: Long): Int
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.DAY_OF_MONTH));
			}
			case 137: { // dow(time: Long): Int
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.DAY_OF_WEEK));
			}
			case 138: { // hour(time: Long): Int
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.HOUR_OF_DAY));
			}
			case 139: { // minute(time: Long): Int
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.MINUTE));
			}
			case 140: { // second(time: Long): Int
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.SECOND));
			}
			case 141: { // millis(time: Long): Int
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(lval(args[0])));
				return Ival(cal.get(Calendar.MILLISECOND));
			}
			case 142: { // timeof(year: Int, month: Int, day: Int, hour: Int, min: Int, sec: Int, millis: Int): Long
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
			case 143: // Dict.new(): Dict
				return new HashMap();
			case 144: // Dict.len(): Int
				return Ival(((HashMap)args[0]).size());
			case 145: // Dict.get(key: Any): Any
				return ((HashMap)args[0]).get(args[1]);
			case 146: // Dict.set(key: Any, value: Any)
				((HashMap)args[0]).set(args[1], args[2]);
				return null;
			case 147: // Dict.remove(key: Any)
				((HashMap)args[0]).remove(args[1]);
				return null;
			case 148: // Dict.clear()
				((HashMap)args[0]).clear();
				return null;
			case 149: // Dict.keys(): [Any]
				return ((HashMap)args[0]).keys();

			/* == Header: list.eh == */
			case 150: // List.new(): List
				return new ArrayList();
			case 151: // List.len(): Int
				return Ival(((ArrayList)args[0]).size());
			case 152: // List.get(at: Int): Any
				return ((ArrayList)args[0]).get(ival(args[1]));
			case 153: // List.set(at: Int, val: Any)
				((ArrayList)args[0]).set(ival(args[1]), args[2]);
				return null;
			case 154: // List.add(val: Any)
				((ArrayList)args[0]).add(args[1]);
				return null;
			case 155: // List.addFrom(arr: Array, ofs: Int = 0, len: Int = -1)
				((ArrayList)args[0]).addFrom(args[1], ival(args[2]), ival(args[3]));
				return null;
			case 156: // List.insert(at: Int, val: Any)
				((ArrayList)args[0]).insert(ival(args[1]), args[2]);
				return null;
			case 157: // List.insertfrom(at: Int, arr: Array, ofs: Int = 0, len: Int = -1)
				((ArrayList)args[0]).insertFrom(ival(args[1]), args[2], ival(args[3]), ival(args[4]));
				return null;
			case 158: // List.remove(at: Int)
				((ArrayList)args[0]).remove(ival(args[1]));
				return null;
			case 159: // List.clear()
				((ArrayList)args[0]).clear();
				return null;
			case 160: // List.range(from: Int, to: Int): List
				return ((ArrayList)args[0]).getRange(ival(args[1]), ival(args[2]));
			case 161: // List.indexof(val: Any, from: Int = 0): Int
				return Ival(((ArrayList)args[0]).indexOf(args[1], ival(args[2])));
			case 162: // List.lindexof(val: Any): Int
				return Ival(((ArrayList)args[0]).lastIndexOf(args[1]));
			case 163: { // List.filter(f: (Any):Bool): List
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
			case 164: { // List.filterself(f: (Any):Bool)
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
			case 165: { // List.map(f: (Any):Any): List
				ArrayList oldlist = (ArrayList)args[0];
				int len = oldlist.size();
				ArrayList newlist = new ArrayList(len);
				Function f = (Function)args[1];
				for (int i=0; i<len; i++) {
					newlist.add(f.invoke(p, new Object[] {oldlist.get(i)}));
				}
				return newlist;
			}
			case 166: { // List.mapself(f: (Any):Any)
				ArrayList list = (ArrayList)args[0];
				Function f = (Function)args[1];
				int len = list.size();
				for (int i=0; i<len; i++) {
					list.set(i, f.invoke(p, new Object[] {list.get(i)}));
				}
				return null;
			}
			case 167: { // List.reduce(f: (Any,Any):Any): Any
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
			case 168: { // List.sort(f: (Any,Any):Int): List
				ArrayList oldList = (ArrayList)args[0];
				Object[] data = new Object[oldList.size()];
				oldList.copyInto(data);
				Arrays.qsort(data, 0, data.length, p, (Function)args[1]);
				ArrayList newList = new ArrayList(data.length);
				newList.addFrom(data, 0, data.length);
				return newList;
			}
			case 169: { // List.sortself(f: (Any,Any):Int)
				ArrayList list = (ArrayList)args[0];
				Object[] data = new Object[list.size()];
				list.copyInto(data);
				Arrays.qsort(data, 0, data.length, p, (Function)args[1]);
				list.clear();
				list.addFrom(data, 0, data.length);
				return null;
			}
			case 170: { // List.reverse(): List
				ArrayList oldList = (ArrayList)args[0];
				int len  = oldList.size();
				ArrayList newList = new ArrayList(len);
				for (int i=len-1; i>=0; i--) {
					newList.add(oldList.get(i));
				}
				return newList;
			}
			case 171: { // List.toarray(): [Any]
				ArrayList list = (ArrayList)args[0];
				Object[] data = new Object[list.size()];
				list.copyInto(data);
				return data;
			}
			case 172: // List.copyInto(from: Int, buf: Array, ofs: Int, len: Int)
				((ArrayList)args[0]).copyInto(ival(args[1]), args[2], ival(args[3]), ival(args[4]));
				return null;

			/* == Header: thread.eh == */
			case 173: // currentThread(): Thread
				return Thread.currentThread();
			case 174: // Thread.new(run: ());
				return p.createThread((Function)args[0]);
			case 175: // Thread.start()
				((ProcessThread)args[0]).start();
				return null;
			case 176: // Thread.isAlive(): Bool
				return Ival(((ProcessThread)args[0]).isAlive());
			case 177: // Thread.interrupt()
				((ProcessThread)args[0]).interrupt();
				return null;
			case 178: // Thread.isInterrupted(): Bool
				return Ival(((ProcessThread)args[0]).isInterrupted());
			case 179: // Thread.join()
				((ProcessThread)args[0]).join();
				return null;
			case 180: // Lock.new()
				return new Lock();
			case 181: // Lock.lock()
				((Lock)args[0]).lock();
				return null;
			case 182: // Lock.tryLock(): Bool
				return Ival(((Lock)args[0]).tryLock());
			case 183: // Lock.unlock()
				((Lock)args[0]).unlock();
				return null;

			/* == Header: process.eh == */
			case 184: // currentProcess(): Process
				return p;
			case 185: { // Process.new(cmd: String, args: [String])
				Object[] objArgs = (Object[])args[1];
				String[] strArgs = new String[objArgs.length];
				System.arraycopy(objArgs, 0, strArgs, 0, objArgs.length);
				return new Process(p, (String)args[0], strArgs);
			}
			case 186: // Process.getState(): Int
				return Ival(((Process)args[0]).getState());
			case 187: // Process.getPriority(): Int
				return Ival(((Process)args[0]).getPriority());
			case 188: { // Process.setPriority(value: Int)
				Process process = (Process)args[0];
				if (process == p || process.getState() == Process.NEW) {
					process.setPriority(ival(args[1]));
					return null;
				} else {
					throw new SecurityException();
				}
			}
			case 189: // Process.getName(): String
				return ((Process)args[0]).getName();
			case 190: // Process.getArgs(): [String]
				return ((Process)args[0]).getArgs();
			case 191: { // Process.setEnv(key: String, value: String)
				Process process = (Process)args[0];
				if (process == p || process.getState() == Process.NEW) {
					process.setEnv((String)args[1], (String)args[2]);
					return null;
				} else {
					throw new SecurityException();
				}
			}
			case 192: { // Process.setIn(in: IStream)
				Process process = (Process)args[0];
				if (process == p || process.getState() == Process.NEW) {
					process.stdin = (InputStream)args[1];
					return null;
				} else {
					throw new SecurityException();
				}
			}
			case 193: { // Process.setOut(out: OStream)
				Process process = (Process)args[0];
				if (process == p || process.getState() == Process.NEW) {
					process.stdout = (OutputStream)args[1];
					return null;
				} else {
					throw new SecurityException();
				}
			}
			case 194: { // Process.setErr(err: OStream)
				Process process = (Process)args[0];
				if (process == p || process.getState() == Process.NEW) {
					process.stderr = (OutputStream)args[1];
					return null;
				} else {
					throw new SecurityException();
				}
			}
			case 195: { // Process.setCwd(cwd: String)
				Process process = (Process)args[0];
				if (process == p || process.getState() == Process.NEW) {
					process.setCurrentDirectory(p.toFile((String)args[1]));
					return null;
				} else {
					throw new SecurityException();
				}
			}
			case 196: // Process.start(): Process
				return ((Process)args[0]).start();
			case 197: // Process.waitFor(): Int
				return Ival(((Process)args[0]).waitFor());
			case 198: // Process.kill()
				((Process)args[0]).kill();
				return null;
			case 199: // Process.getExitCode(): Int
				return Ival(((Process)args[0]).getExitCode());
			case 200: // Process.getError(): Error
				return ((Process)args[0]).getError();

			/* == Header: sys.eh == */
			case 201: // systime(): Long
				return Lval(System.currentTimeMillis());
			case 202: // getenv(key: String): String
				return p.getEnv((String)args[0]);
			case 203: // setenv(key: String, val: String)
				p.setEnv((String)args[0], (String)args[1]);
				return null;
			case 204: // sleep(millis: Int)
				Thread.sleep(lval(args[0]));
				return null;
			case 205: // sysProperty(str: String): String
				return System.getProperty((String)args[0]);

			/* == Header: term.eh == */
			case 206: // isTerm(stream: IStream): Bool
				return Ival(args[0] instanceof TerminalInput);
			case 207: // TermIStream.clear()
				((TerminalInput)args[0]).clear();
				return null;
			case 208: // TermIStream.getPrompt(): String
				return ((TerminalInput)args[0]).getPrompt();
			case 209: // TermIStream.setPrompt(prompt: String)
				((TerminalInput)args[0]).setPrompt((String)args[1]);
				return null;
			default:
				return null;
		}
	}
}
