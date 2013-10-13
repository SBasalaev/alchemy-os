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
import alchemy.system.AlchemyException;
import alchemy.system.Function;
import alchemy.system.NativeLibrary;
import alchemy.system.Process;
import alchemy.util.Arrays;
import alchemy.util.PartiallyAppliedFunction;
import alchemy.util.Strings;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import javax.microedition.io.Connection;
import javax.microedition.io.StreamConnection;

/**
 * Core runtime library for Alchemy OS
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
			case 1: // Function.curry(a: Any): Function
				return new PartiallyAppliedFunction((Function)args[0], args[1]);
			case 2: { // Structure.clone(): Structure
				Object[] struct = (Object[])args[0];
				Object[] clone = new Object[struct.length];
				System.arraycopy(struct, 0, clone, 0, struct.length);
				return clone;
			}

			/* == Header: string.eh == */
			case 3: // Any.tostr(): String
				return Strings.toString(args[0]);
			case 4: // Char.tostr(): String
				return String.valueOf((char)ival(args[0]));
			case 5: // Int.tobin():String
				return Integer.toBinaryString(ival(args[0]));
			case 6: // Int.tooct():String
				return Integer.toOctalString(ival(args[0]));
			case 7: // Int.tohex():String
				return Integer.toHexString(ival(args[0]));
			case 8: // Int.tobase(base: Int): String
				return Integer.toString(ival(args[0]), ival(args[1]));
			case 9: // Long.tobase(base: Int): String
				return Long.toString(lval(args[0]), ival(args[1]));
			case 10: // String.tointbase(base: Int): Int
				return Ival(Integer.parseInt((String)args[0], ival(args[1])));
			case 11: // String.tolongbase(base: Int): Long
				return Lval(Long.parseLong((String)args[0], ival(args[1])));
			case 12: // String.tofloat(): Float
				return Fval(Float.parseFloat((String)args[0]));
			case 13: // String.todouble(): Double
				return Dval(Double.parseDouble((String)args[0]));
			case 14: { // String.get(at: Int): Char
				String str = (String)args[0];
				int at = ival(args[1]);
				if (at < 0) at += str.length();
				return Ival(str.charAt(at));
			}
			case 15: // String.len(): Int
				return Ival(((String)args[0]).length());
			case 16: { // String.range(from: Int, to: Int): String
				String str = (String) args[0];
				int from = ival(args[1]);
				int to = ival(args[2]);
				if (from < 0) from += str.length();
				if (to < 0) to += str.length();
				return str.substring(from, to);
			}
			case 17: { // String.indexof(ch: Char, from: Int = 0): Int
				String str = (String)args[0];
				int from = ival(args[2]);
				if (from < 0) from += str.length();
				return Ival(str.indexOf(ival(args[1]), from));
			}
			case 18: // String.lindexof(ch: Char): Int
				return Ival(((String)args[0]).lastIndexOf(ival(args[1])));
			case 19: { // String.find(sub: String, from: Int = 0): Int
				String str = (String)args[0];
				int from = ival(args[2]);
				if (from < 0) from += str.length();
				return Ival(str.indexOf((String)args[1], from));
			}
			case 20: // String.ucase(): String
				return ((String)args[0]).toUpperCase();
			case 21: // String.lcase(): String
				return ((String)args[0]).toLowerCase();
			case 22: // String.concat(str: String): String
				return ((String)args[0]).concat((String)args[1]);
			case 23: // String.cmp(str: String): Int
				return Ival(((String)args[0]).compareTo((String)args[1]));
			case 24: // String.trim(): String
				return ((String)args[0]).trim();
			case 25: // String.split(ch: Char, skipEmpty: Bool = false): [String]
				return Strings.split((String)args[0], (char)ival(args[1]), bval(args[2]));
			case 26: // String.format(args: [Any]): String
				return Strings.format((String)args[0], (Object[])args[1]);
			case 27: // String.chars(): [Char]
				return ((String)args[0]).toCharArray();
			case 28: // String.utfbytes(): [Byte]
				return Strings.utfEncode((String)args[0]);
			case 29: { // String.startswith(prefix: String, from: Int = 0): Bool
				String str = (String)args[0];
				int from = ival(args[2]);
				if (from < 0) from += str.length();
				return Ival(str.startsWith((String)args[1], from));
			}
			case 30: // String.replace(oldch: Char, newch: Char): String
				return ((String)args[0]).replace((char)ival(args[1]), (char)ival(args[2]));
			case 31: // String.hash(): Int
				return Ival(((String)args[0]).hashCode());
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

			/* == Header: math.eh == */
			case 39: // abs(val: Double): Double
				return Dval(Math.abs(dval(args[0])));
			case 40: // sin(val: Double): Double
				return Dval(Math.sin(dval(args[0])));
			case 41: // cos(val: Double): Double
				return Dval(Math.cos(dval(args[0])));
			case 42: // tan(val: Double): Double
				return Dval(Math.tan(dval(args[0])));
			case 43: // sqrt(val: Double): Double
				return Dval(Math.sqrt(dval(args[0])));
			case 44: // ipow(val: Double, pow: Int): Double
				return Dval(alchemy.util.Math.ipow(dval(args[0]), ival(args[1])));
			case 45: // exp(val: Double): Double
				return Dval(alchemy.util.Math.exp(dval(args[0])));
			case 46: // log(val: Double): Double
				return Dval(alchemy.util.Math.log(dval(args[0])));
			case 47: // asin(val: Double): Double
				return Dval(alchemy.util.Math.asin(dval(args[0])));
			case 48: // acos(val: Double): Double
				return Dval(alchemy.util.Math.acos(dval(args[0])));
			case 49: // atan(val: Double): Double
				return Dval(alchemy.util.Math.atan(dval(args[0])));
			case 50: // ibits2f(bits: Int): Float
				return Fval(Float.intBitsToFloat(ival(args[0])));
			case 51: // f2ibits(f: Float): Int
				return Ival(Float.floatToIntBits(fval(args[0])));
			case 52: // lbits2d(bits: Long): Double
				return Dval(Double.longBitsToDouble(lval(args[0])));
			case 53: // d2lbits(d: Double): Long
				return Lval(Double.doubleToLongBits(dval(args[0])));

			/* == Header: rnd.eh == */
			case 54: // Random.new(seed: Long)
				return new Random(lval(args[0]));
			case 55: // Random.next(n: Int): Int
				return Ival(((Random)args[0]).nextInt(ival(args[1])));
			case 56: // Random.nextInt(): Int
				return Ival(((Random)args[0]).nextInt());
			case 57: // Random.nextLong(): Long
				return Lval(((Random)args[0]).nextLong());
			case 58: // Random.nextFloat(): Float
				return Fval(((Random)args[0]).nextFloat());
			case 59: // Random.nextDouble(): Double
				return Dval(((Random)args[0]).nextDouble());
			case 60: // Random.setSeed(seed: Long)
				((Random)args[0]).setSeed(lval(args[1]));
				return null;

			/* == Header: io.eh == */
			case 61: { // Connection.close()
				Connection conn = (Connection) args[0];
				conn.close();
				p.removeConnection(conn);
				return null;
			}
			case 62: { // StreamConnection.openInput(): IStream
				ConnectionInputStream in = new ConnectionInputStream(((StreamConnection)args[0]).openInputStream());
				p.addConnection(in);
				return in;
			}
			case 63: { // StreamConnection.openOutput(): OStream
				ConnectionOutputStream out = new ConnectionOutputStream(((StreamConnection)args[0]).openOutputStream());
				p.addConnection(out);
				return out;
			}
			case 64: // IStream.read(): Int
				return Ival(((InputStream)args[0]).read());
			case 65: { // IStream.readArray(buf: [Byte], ofs: Int = 0, len: Int = -1): Int
				byte[] buf = (byte[])args[1];
				int len = ival(args[3]);
				if (len < 0) len = buf.length;
				return Ival(((InputStream)args[0]).read(buf, ival(args[2]), len));
			}
			case 66: // IStream.readFully(): [Byte]
				return IO.readFully((InputStream)args[0]);
			case 67: // IStream.skip(num: Long): Long
				return Lval(((InputStream)args[0]).skip(lval(args[1])));
			case 68: // IStream.available(): Int
				return Ival(((InputStream)args[0]).available());
			case 69: // IStream.reset()
				((InputStream)args[0]).reset();
				return null;
			case 70: // OStream.write(b: Int)
				((OutputStream)args[0]).write(ival(args[1]));
				return null;
			case 71: { // OStream.writeArray(buf: [Byte], ofs: Int, len: Int)
				byte[] buf = (byte[])args[1];
				int len = ival(args[3]);
				if (len < 0) len = buf.length;
				((OutputStream)args[0]).write(buf, ival(args[2]), len);
				return null;
			}
			case 72: // OStream.print(a: Any)
				IO.print((OutputStream)args[0], args[1]);
				return null;
			case 73: // OStream.println(a: Any)
				IO.println((OutputStream)args[0], args[1]);
				return null;
			case 74: // OStream.flush()
				((OutputStream)args[0]).flush();
				return null;
			case 75: // OStream.writeAll(input: IStream)
				IO.writeAll((InputStream)args[1], (OutputStream)args[0]);
				return null;
			case 76: // stdin(): IStream
				return p.stdin;
			case 77: // stdout(): OStream
				return p.stdout;
			case 78: // stderr(): OStream
				return p.stderr;
			case 79: // setin(in: IStream)
				p.stdin = ((InputStream)args[0]);
				return null;
			case 80: // setout(out: OStream)
				p.stdout = ((OutputStream)args[0]);
				return null;
			case 81: // seterr(err: OStream)
				p.stderr = ((OutputStream)args[0]);
				return null;
			case 82: // pathfile(path: String): String
				return Filesystem.fileName((String)args[0]);
			case 83: // pathdir(path: String): String
				return Filesystem.fileParent((String)args[0]);
			case 84: // abspath(path: String): String
				return p.toFile((String)args[0]);
			case 85: // relpath(path: String): String
				return Filesystem.relativePath(p.getCurrentDirectory(), p.toFile((String)args[0]));
			case 86: // fcreate(path: String)
				Filesystem.create(p.toFile((String)args[0]));
				return null;
			case 87: // fremove(path: String)
				Filesystem.remove(p.toFile((String)args[0]));
				return null;
			case 88: // fremoveTree(path: String)
				Filesystem.removeTree(p.toFile((String)args[0]));
				return null;
			case 89: // mkdir(path: String)
				Filesystem.mkdir(p.toFile((String)args[0]));
				return null;
			case 90: // mkdirTree(path: String)
				Filesystem.mkdirTree(p.toFile((String)args[0]));
				return null;
			case 91: // fcopy(source: String, dest: String)
				Filesystem.copy(p.toFile((String)args[0]), p.toFile((String)args[1]));
				return null;
			case 92: // fmove(source: String, dest: String)
				Filesystem.move(p.toFile((String)args[0]), p.toFile((String)args[1]));
				return null;
			case 93: // exists(path: String): Bool
				return Ival(Filesystem.exists(p.toFile((String)args[0])));
			case 94: // isDir(path: String): Bool
				return Ival(Filesystem.isDirectory(p.toFile((String)args[0])));
			case 95: { // fread(path: String): IStream
				ConnectionInputStream input = new ConnectionInputStream(Filesystem.read(p.toFile((String)args[0])));
				p.addConnection(input);
				return input;
			}
			case 96: { // fwrite(path: String): OStream
				ConnectionOutputStream output = new ConnectionOutputStream(Filesystem.write(p.toFile((String)args[0])));
				p.addConnection(output);
				return output;
			}
			case 97: { // fappend(path: String): OStream
				ConnectionOutputStream output = new ConnectionOutputStream(Filesystem.append(p.toFile((String)args[0])));
				p.addConnection(output);
				return output;
			}
			case 98: // flist(path: String): [String]
				return Filesystem.list(p.toFile((String)args[0]));
			case 99: // fmodified(path: String): Long
				return Lval(Filesystem.lastModified(p.toFile((String)args[0])));
			case 100: // fsize(path: String): Long
				return Lval(Filesystem.size(p.toFile((String)args[0])));
			case 101: // setRead(path: String, on: Bool)
				Filesystem.setRead(p.toFile((String)args[0]), bval(args[1]));
				return null;
			case 102: // setWrite(path: String, on: Bool)
				Filesystem.setWrite((String)args[0], bval(args[1]));
				return null;
			case 103: // setExec(path: String, on: Bool)
				Filesystem.setExec((String)args[0], bval(args[1]));
				return null;
			case 104: // canRead(path: String): Bool
				return Ival(Filesystem.canRead(p.toFile((String)args[0])));
			case 105: // canWrite(path: String): Bool
				return Ival(Filesystem.canWrite(p.toFile((String)args[0])));
			case 106: // canExec(path: String): Bool
				return Ival(Filesystem.canExec(p.toFile((String)args[0])));
			case 107: // getCwd(): String
				return p.getCurrentDirectory();
			case 108: // setCwd(dir: String)
				p.setCurrentDirectory(p.toFile((String)args[0]));
				return null;
			case 109: // spaceTotal(root: String): Long
				return Lval(Filesystem.spaceTotal(p.toFile((String)args[0])));
			case 110: // spaceFree(root: String): Long
				return Lval(Filesystem.spaceFree(p.toFile((String)args[0])));
			case 111: // spaceUsed(root: String): Long
				return Lval(Filesystem.spaceUsed(p.toFile((String)args[0])));
			case 112: { // readUrl(url: String): IStream
				return IO.readUrl((String)args[0]);
			}
			case 113: // matchesGlob(path: String, glob: String): Bool
				return Ival(IO.matchesPattern((String)args[0], (String)args[1]));
			default:
				return null;
		}
	}
}
