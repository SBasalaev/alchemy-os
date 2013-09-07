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

import alchemy.core.AlchemyException;
import alchemy.core.Process;
import alchemy.core.Function;
import alchemy.core.Library;
import alchemy.evm.EtherLoader;
import alchemy.fs.FSManager;
import alchemy.fs.Filesystem;
import alchemy.libs.core.PartiallyAppliedFunction;
import alchemy.libs.core.Pipe;
import alchemy.nlib.NativeLibrary;
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
 * Alchemy core runtime library.
 * 
 * @author Sergey Basalaev
 * @version 3.1
 */
public class LibCore31 extends NativeLibrary {

	private static Random rnd = new Random();
	
	/**
	 * Constructor without parameters to load
	 * library through the native interface.
	 * @throws IOException if I/O error occured while reading
	 *         function definitions file
	 */
	public LibCore31() throws IOException {
		load("/libcore31.symbols");
	}

	/**
	 * Calculates relative path from the current directory.
	 * @param c execution path
	 * @param f file to calculate path from
	 * @return string with relative path
	 */
	public static String relPath(Process c, String f) {
		if (c.getCurDir().equals(f)) return ".";
		//initializing cpath and fpath
		String tmp = c.getCurDir();
		if (tmp.length() == 0) return f.substring(1);
		char[] cpath = new char[tmp.length()+1];
		tmp.getChars(0, tmp.length(), cpath, 0);
		tmp = f;
		char[] fpath = new char[tmp.length()+1];
		tmp.getChars(0, tmp.length(), fpath, 0);
		int cind = 0;
		//searching the first different character
		while (cpath[cind] == fpath[cind]) cind++;
		//reverting to the beginning of name
		if (!(cpath[cind] == 0 && fpath[cind] == '/') && !(cpath[cind] == '/' && fpath[cind] == 0)) {
			do --cind;
			while (cpath[cind] != '/');
		}
		int fpos = cind;
		//while we have directories in cpath append ".."
		StringBuffer relpath = new StringBuffer();
		boolean needslash = false;
		while (cpath[cind] != 0) {
			if (cpath[cind] == '/') {
				relpath.append(needslash ? "/.." : "..");
				needslash = true;
			}
			cind++;
		}
		//append file remainder
		if (fpath[fpos] != 0) {
			if (!needslash) fpos++;
			relpath.append(fpath, fpos, fpath.length-fpos-1);
		}
		return relpath.toString();
	}

	/**
	 * Calculates integer power of the value.
	 * @param val  double value
	 * @param pow  integer power
	 * @return <code>val</code> in the power of <code>pow</code>
	 */
	public static double ipow(double val, int pow) {
		if (val == 0) return 1.0;
		if (pow < 0) {
			pow = -pow;
			val = 1.0/val;
		}
		double result = 1;
		// if _pow_ has bit _n_ then multiplying by _val^(2^n)_
		while (pow != 0) {
			if (pow%2 != 0) result *= val;
			val *= val;
			pow >>>= 1;
		}
		return result;
	}

	/**
	 * Calculates exponent of the value.
	 * @param val  double value
	 * @return exponent of the value
	 */
	public static double exp(double val) {
		boolean neg = false;
		if (val < 0) {
			neg = true;
			val = -val;
		}
		if (val > 709.0) return neg ? 0 : Double.POSITIVE_INFINITY;
		int ip = (int)val;       //[val]
		double fp = val - ip;    //{val}
		double result = 1.0;
		//calculating  E^{val}
		//as Taylor series
		double add = fp;
		int n = 2;
		while (add > 1.0e-16) {
			result += add;
			add = add*fp/n;
			n++;
		}
		//calculating  E^[val]
		//using ipow
		result *= ipow(Math.E, ip);
		return neg ? 1.0/result : result;
	}

	/**
	 * Calculates natural logarithm of the value.
	 * @param val  double value
	 * @return natural algoritm of the value
	 */
	public static double log(double val) {
		boolean neg = false;
		if (val < 0) return Double.NaN;
		if (val < 1) {
			val = 1.0/val;
			neg = true;
		}
		//calculating exponent
		double exp = 0;
		while (val >= 64) {
			val /= 64.0;
			exp += 6.0;
		}
		while (val >= 2) {
			val /= 2.0;
			exp += 1.0;
		}
		if (val >= 1.414213562373095d) { //√2
			val /= 1.414213562373095d;
			exp += 0.5;
		}
		if (val >= 1.189207115002721d) { //√√2
			val /= 1.189207115002721d;
			exp += 0.25;
		}
		if (val >= 1.090507732665257d) { //√√√2
			val /= 1.090507732665257d;
			exp += 0.125;
		}
		//calculating ln of rest
		val -= 1.0;
		double ln = 0, rest = val;
		int n=1;
		while (rest > 1.0e-16*n || rest < -1.0e-16*n) {
			ln += rest / n;
			rest = -rest * val;
			n++;
		}
		ln += exp * 0.693147180559945d;
		return neg ? -ln : ln;
	}

	/**
	 * Returns arcsine of the value.
	 * @param val  double value
	 * @return arcsine of the value
	 */
	public static double asin(double val) {
		if (val > 1 || val < -1) return Double.NaN;
	    boolean neg = false;
		if (val < 0) {
			val = -val;
			neg = true;
		}
		//linear approximation where iterational method is impractical
		//it really sucks and should be rewritten
		if (val > 0.999999d) {
			//1.56938... is asin(0.999999)
			val = Math.PI/2 + (1-val) * 1000000 * (1.5693821131146521d - Math.PI/2);
			return neg ? -val : val;
		}
		//calculating as Taylor series
		double rest = val;
		double x2 = val*val;
		int n = 1;
		while (rest > 1.0e-16) {
			rest *= n * x2;
			n++;
			rest /= n;
			n++;
			val += rest / n;
		}
		return val;
	}

	/**
	 * Returns arccosine of the value.
	 * @param val  double value
	 * @return arccosine of the value
	 */
	public static double acos(double val) {
		return Math.PI/2.0d - asin(val);
	}

	/**
	 * Returns arctangent of the value.
	 * Slow and not very accurate though.
	 * @param val  double value
	 * @return arctangent of the value
	 */
	public static double atan(double val) {
		//smokin' method from mobylab.ru
		//shrinking domain
		boolean neg = false, big = false;
		if (val < 0) {
			val = -val;
			neg = true;
		}
		if (val > 1) {
			val = 1/val;
			big = true;
		}
		int offsteps = 0;
		while (val > Math.PI/12.0d) {
			// 1.732... is √3
			val = (val * 1.732050807568877 - 1) / (val + 1.732050807568877);
			offsteps++;
		}
		//calculating as Taylor series
		double result = 0;
		double rest = val;
		val *= val;
		int n = 1;
		while (rest > 1.0e-16) {
			result += rest/n;
			n += 2;
			rest = -rest*val;
		}
		//undoing our focuses with domain
		result += Math.PI/6 * offsteps;
		if (big) result = Math.PI/2.0d - result;
		return neg ? -result : result;
	}


	public String soname() {
		return "libcore.3.so";
	}
	
	public static void arraycopy(Object src, int sofs, Object dest, int dofs, int len) {
		if (dest.getClass().isInstance(src)) {
			System.arraycopy(src, sofs, dest, dofs, len);
			return;
		}
		if (src instanceof Object[]) {
			final Object[] from = (Object[])src;
			if (dest instanceof byte[]) {
				final byte[] to = (byte[])dest;
				for (int i=0; i<len; i++) to[dofs+i] = (byte)ival(from[sofs+i]);
				return;
			} else if (dest instanceof short[]) {
				final short[] to = (short[])dest;
				for (int i=0; i<len; i++) to[dofs+i] = (short)ival(from[sofs+i]);
				return;
			} else if (dest instanceof char[]) {
				final char[] to = (char[])dest;
				for (int i=0; i<len; i++) to[dofs+i] = (char) ival(from[sofs+i]);
				return;
			} else if (dest instanceof boolean[]) {
				final boolean[] to = (boolean[])dest;
				for (int i=0; i<len; i++) to[dofs+i] = bval(from[sofs+i]);
				return;
			} else if (dest instanceof int[]) {
				final int[] to = (int[])dest;
				for (int i=0; i<len; i++) to[dofs+i] = ival(from[sofs+i]);
				return;
			} else if (dest instanceof long[]) {
				final long[] to = (long[])dest;
				for (int i=0; i<len; i++) to[dofs+i] = lval(from[sofs+i]);
				return;
			} else if (dest instanceof float[]) {
				final float[] to = (float[])dest;
				for (int i=0; i<len; i++) to[dofs+i] = fval(from[sofs+i]);
				return;
			} else if (dest instanceof double[]) {
				final double[] to = (double[])dest;
				for (int i=0; i<len; i++) to[dofs+i] = dval(from[sofs+i]);
				return;
			}
		}
		if (dest instanceof Object[]) {
			final Object[] to = (Object[])dest;
			if (src instanceof byte[]) {
				final byte[] from = (byte[])src;
				for (int i=0; i<len; i++) to[dofs+i] = Ival(from[sofs+i]);
				return;
			} else if (src instanceof short[]) {
				final short[] from = (short[])src;
				for (int i=0; i<len; i++) to[dofs+i] = Ival(from[sofs+i]);
				return;
			} else if (src instanceof char[]) {
				final char[] from = (char[])src;
				for (int i=0; i<len; i++) to[dofs+i] = Ival(from[sofs+i]);
				return;
			} else if (src instanceof boolean[]) {
				final boolean[] from = (boolean[])src;
				for (int i=0; i<len; i++) to[dofs+i] = Ival(from[sofs+i]);
				return;
			} else if (src instanceof int[]) {
				final int[] from = (int[])src;
				for (int i=0; i<len; i++) to[dofs+i] = Ival(from[sofs+i]);
				return;
			} else if (src instanceof long[]) {
				final long[] from = (long[])src;
				for (int i=0; i<len; i++) to[dofs+i] = Lval(from[sofs+i]);
				return;
			} else if (src instanceof float[]) {
				final float[] from = (float[])src;
				for (int i=0; i<len; i++) to[dofs+i] = Fval(from[sofs+i]);
				return;
			} else if (src instanceof double[]) {
				final double[] from = (double[])src;
				for (int i=0; i<len; i++) to[dofs+i] = Dval(from[sofs+i]);
				return;
			}
		}
		throw new ClassCastException();
	}

	protected Object invokeNative(int index, Process p, Object[] args) throws Exception {
		switch (index) {
			case 0: // pathfile(f: String): String
				return Filesystem.fname(p.toFile((String)args[0]));
			case 1: // pathdir(f: String): String
				return Filesystem.fparent(p.toFile((String)args[0]));
			case 2: // abspath(f: String): String
				return p.toFile((String)args[0]);
			case 3: // fcreate(f: String)
				FSManager.fs().create(p.toFile((String)args[0]));
				return null;
			case 4: // fremove(f: String)
				FSManager.fs().remove(p.toFile((String)args[0]));
				return null;
			case 5: // mkdir(f: String)
				FSManager.fs().mkdir(p.toFile((String)args[0]));
				return null;
			case 6: // fcopy(src: String, dest: String)
				FSManager.fs().copy(p.toFile((String)args[0]), p.toFile((String)args[1]));
				return null;
			case 7: // fmove(src: String, dest: String)
				FSManager.fs().move(p.toFile((String)args[0]), p.toFile((String)args[1]));
				return null;
			case 8: // set_read(f: String, on: Bool)
				FSManager.fs().setRead(p.toFile((String)args[0]), bval(args[1]));
				return null;
			case 9: // set_write(f: String, on: Bool)
				FSManager.fs().setWrite(p.toFile((String)args[0]), bval(args[1]));
				return null;
			case 10: // set_exec(f: String, on: Bool)
				FSManager.fs().setExec(p.toFile((String)args[0]), bval(args[1]));
				return null;
			case 11: // can_read(f: String): Bool
				return Ival(FSManager.fs().canRead(p.toFile((String)args[0])));
			case 12: // can_write(f: String): Bool
				return Ival(FSManager.fs().canWrite(p.toFile((String)args[0])));
			case 13: // can_exec(f: String): Bool
				return Ival(FSManager.fs().canExec(p.toFile((String)args[0])));
			case 14: // exists(f: String): Bool
				return Ival(FSManager.fs().exists(p.toFile((String)args[0])));
			case 15: // is_dir(f: String): Bool
				return Ival(FSManager.fs().isDirectory(p.toFile((String)args[0])));
			case 16: { // fopen_r(f: String): IStream
				InputStream stream = FSManager.fs().read(p.toFile((String)args[0]));
				p.addStream(stream);
				return stream;
			}
			case 17: { // fopen_w(f: String): OStream
				OutputStream stream = FSManager.fs().write(p.toFile((String)args[0]));
				p.addStream(stream);
				return stream;
			}
			case 18: { // fopen_a(f: String): OStream
				OutputStream stream = FSManager.fs().append(p.toFile((String)args[0]));
				p.addStream(stream);
				return stream;
			}
			case 19: // flist(f: String): [String]
				return FSManager.fs().list(p.toFile((String)args[0]));
			case 20: // fmodified(f: String): Long
				return Lval(FSManager.fs().lastModified(p.toFile((String)args[0])));
			case 21: // fsize(f: String): Long
				return Lval(FSManager.fs().size(p.toFile((String)args[0])));
			case 22: // set_cwd(f: String)
				p.setCurDir(p.toFile((String)args[0]));
				return null;
			case 23: // relpath(f: String): String
				return LibCore31.relPath(p, p.toFile((String)args[0]));
			case 24: // String.len(): Int
				return Ival(((String)args[0]).length());
			case 25: // String.ch(at: Int): Char
				return Ival(((String)args[0]).charAt(ival(args[1])));
			case 26: // String.indexof(ch: Char): Int
				return Ival(((String)args[0]).indexOf(ival(args[1])));
			case 27: // String.lindexof(ch: Char): Int
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
				return ((String)args[0]).concat(IO.stringValue(args[1]));
			case 33: // String.cmp(str: String): Int
				return Ival(((String)args[0]).compareTo((String)args[1]));
			case 34: // String.chars(): [Char]
				return ((String)args[0]).toCharArray();
			case 35: // String.trim(): String
				return ((String)args[0]).trim();
			case 36: // getenv(key: String): String
				return p.getEnv(((String)args[0]));
			case 37: // setenv(key: String, value: String)
				p.setEnv(((String)args[0]), (String)args[1]);
				return null;
			case 38: // utfbytes(str: String): [Byte]
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
				return ((StringBuffer)args[0]).append(IO.stringValue(args[1]));
			case 48: // StrBuf.addch(ch: Char): StrBuf
				return ((StringBuffer)args[0]).append((char)ival(args[1]));
			case 49: // StrBuf.delete(from: Int, to: Int): StrBuf
				return ((StringBuffer)args[0]).delete(ival(args[1]), ival(args[2]));
			case 50: // StrBuf.delch(at: Int): StrBuf
				return ((StringBuffer)args[0]).deleteCharAt(ival(args[1]));
			case 51: // StrBuf.insert(at: Int, a: Any): StrBuf
				return ((StringBuffer)args[0]).insert(ival(args[1]), IO.stringValue(args[2]));
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
				return p.getCurDir();
			case 58: // space_total(root: String): Long
				return Lval(FSManager.fs().spaceTotal(p.toFile((String)args[0])));
			case 59: // space_free(root: String): Long
				return Lval(FSManager.fs().spaceFree(p.toFile((String)args[0])));
			case 60: // space_used(root: String): Long
				return Lval(FSManager.fs().spaceUsed(p.toFile((String)args[0])));
			case 61: // Any.tostr(): String
				return IO.stringValue(args[0]);
			case 62: // new_strbuf(): StrBuf
				return new StringBuffer();
			case 63: // IStream.close()
				((InputStream)args[0]).close();
				return null;
			case 64: // IStream.read(): Int
				return Ival(((InputStream)args[0]).read());
			case 65: // IStream.readarray(buf: [Byte], ofs: Int, len: Int): Int
				return Ival(IO.readarray((InputStream)args[0], (byte[])args[1], ival(args[2]), ival(args[3])));
			case 66: // IStream.skip(n: Long): Long
				return Lval(IO.skip((InputStream)args[0], lval(args[1])));
			case 67: // OStream.write(b: Int)
				((OutputStream)args[0]).write(ival(args[1]));
				return null;
			case 68: // OStream.writearray(buf: [Byte], ofs: Int, len: Int)
				IO.writearray((OutputStream)args[0], (byte[])args[1], ival(args[2]), ival(args[3]));
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
			case 72: // bacopy(src: [Byte], sofs: Int, dest: [Byte], dofs: Int, len: Int)
				// DEPRECATED: remove in 2.2
			case 73: // cacopy(src: [Char], sofs: Int, dest: [Char], dofs: Int, len: Int)
				// DEPRECATED: remove in 2.2
			case 74: // acopy(src: Array, sofs: Int, dest: Array, dofs: Int, len: Int)
				arraycopy(args[0], ival(args[1]), args[2], ival(args[3]), ival(args[4]));
				return null;
			case 75: // fprint(out: OStream, a: Any): OStream
				IO.print((OutputStream)args[0], IO.stringValue(args[1]));
				return args[0];
			case 76: // fprintln(out: OStream, a: Any): OStream
				IO.println((OutputStream)args[0], IO.stringValue(args[1]));
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
				return Dval(LibCore31.ipow(dval(args[0]), ival(args[1])));
			case 89: // exp(a: Double): Double
				return Dval(LibCore31.exp(dval(args[0])));
			case 90: // log(a: Double): Double
				return Dval(LibCore31.log(dval(args[0])));
			case 91: // asin(a: Double): Double
				return Dval(LibCore31.asin(dval(args[0])));
			case 92: // acos(a: Double): Double
				return Dval(LibCore31.acos(dval(args[0])));
			case 93: // atan(a: Double): Double
				return Dval(LibCore31.atan(dval(args[0])));
			case 94: // ca2str(chars: [Char]): String
				return new String((char[])args[0]);
			case 95: // ba2utf(bytes: [Byte]): String
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
				p.addStream(in);
				return in;
			}
			case 111: // Function.curry(arg: Any): Function
				return new PartiallyAppliedFunction((Function)args[0], args[1]);
			case 112: // loadlibrary(name: String): Library
				try {
					return p.loadLibrary((String)args[0]);
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
			case 115: // String.split(ch: Char): [String]
				return IO.split((String)args[0], (char) ival(args[1]));
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
			case 120: // getstatic(key: String): Any
				return p.get(args[0]);
			case 121: // setstatic(key: String, val: Any)
				p.set(args[0], args[1]);
				return null;
			case 122: // String.format(args: [Any]): String
				return IO.sprintf((String)args[0], (Object[])args[1]);
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
				p.removeStream(args[0]);
				return null;
			case 126: { // StreamConnection.open_input(): IStream
				InputStream in = ((StreamConnection)args[0]).openInputStream();
				p.addStream(in);
				return in;
			}
			case 127: { // StreamConnection.open_output(): OStream
				OutputStream out = ((StreamConnection)args[0]).openOutputStream();
				p.addStream(out);
				return out;
			}
			case 128: // Dict.size(): Int
				return Ival(((Hashtable)args[0]).size());
			case 129: // Dict.clear()
				((Hashtable)args[0]).clear();
				return null;
			case 130: // StrBuf.ch(at: Int): Char
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
				return ((Process)args[0]).getCurDir();
			case 154: // Process.set_cwd(dir: String)
				((Process)args[0]).setCurDir((String)args[1]);
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
			case 176: // String.replace(oldch: Char, newch: Char): String
				return ((String)args[0]).replace((char)ival(args[1]), (char)ival(args[2]));
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
