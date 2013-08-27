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

import alchemy.core.Context;
import alchemy.nlib.NativeFunction;
import alchemy.nlib.NativeLibrary;
import java.io.IOException;

/**
 * Alchemy core runtime library.
 * 
 * @author Sergey Basalaev
 * @version 3.1
 */
public class LibCore31 extends NativeLibrary {

	/**
	 * Constructor without parameters to load
	 * library through the native interface.
	 * @throws IOException if I/O error occured while reading
	 *         function definitions file
	 */
	public LibCore31() throws IOException {
		load("/libcore31.symbols");
	}

	public NativeFunction loadFunction(String name, int index) {
		return new LibCore31Func(name, index);
	}

	/**
	 * Calculates relative path from the current directory.
	 * @param c execution path
	 * @param f file to calculate path from
	 * @return string with relative path
	 */
	public static String relPath(Context c, String f) {
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
}
