/*
 * This file is a part of Alchemy OS project.
 *  Copyright (C) 2013, Sergey Basalaev <sbasalaev@gmail.com>
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

package alchemy.util;

/**
 * Mathematical functions missing in JavaME.
 * @author Sergey Basalaev
 */
public final class Math {

	private static final double PI = 3.141592653589793;
	private static final double E = 2.718281828459045;
	private static final double SQRT2 = 1.4142135623730951; // √2
	private static final double QUADRT2 = 1.189207115002721; // √√2
	private static final double OCTRT2 = 1.0905077326652577; // √√√2
	private static final double SQRT3 = 1.7320508075688772; // √3
	private static final double LN2 = 0.6931471805599453; // ln(2)

	private Math() { }

	/**
	 * Calculates integer power of the value.
	 * @param x  double value
	 * @param n  integer power
	 * @return <code>val</code> in the power of <code>pow</code>
	 */
	public static double ipow(double x, int n) {
		if (x == 0) return 1.0;
		if (n < 0) {
			n = -n;
			x = 1.0/x;
		}
		double result = 1;
		// if _pow_ has bit _n_ then multiplying by _val^(2^n)_
		while (n != 0) {
			if (n%2 != 0) result *= x;
			x *= x;
			n >>>= 1;
		}
		return result;
	}

	/**
	 * Calculates exponent of the value.
	 * @param x  double value
	 * @return exponent of the value
	 */
	public static double exp(double x) {
		boolean neg = false;
		if (x < 0) {
			neg = true;
			x = -x;
		}
		if (x > 709.0) return neg ? 0 : Double.POSITIVE_INFINITY;
		int ip = (int)x;       //[val]
		double fp = x - ip;    //{val}
		double result = 1.0;
		//calculating E^{val} as Taylor series
		double add = fp;
		int n = 2;
		while (add > 1.0e-16) {
			result += add;
			add = add*fp/n;
			n++;
		}
		//calculating E^[val] using ipow
		result *= ipow(Math.E, ip);
		return neg ? 1.0/result : result;
	}

	/**
	 * Calculates natural logarithm of the value.
	 * @param x  double value
	 * @return natural logarithm of the value
	 */
	public static double log(double x) {
		boolean neg = false;
		if (x < 0) return Double.NaN;
		if (x < 1) {
			x = 1.0/x;
			neg = true;
		}
		//calculating base-2 logarithm
		double log2 = 0;
		while (x >= 64) {
			x /= 64.0;
			log2 += 6.0;
		}
		while (x >= 2) {
			x /= 2.0;
			log2 += 1.0;
		}
		if (x >= SQRT2) {
			x /= SQRT2;
			log2 += 0.5;
		}
		if (x >= QUADRT2) {
			x /= QUADRT2;
			log2 += 0.25;
		}
		if (x >= OCTRT2) {
			x /= OCTRT2;
			log2 += 0.125;
		}
		//calculating ln of rest
		x -= 1.0;
		double ln = 0, rest = x;
		int n=1;
		while (rest > 1.0e-16*n || rest < -1.0e-16*n) {
			ln += rest / n;
			rest = -rest * x;
			n++;
		}
		ln += log2 * LN2;
		return neg ? -ln : ln;
	}

	/**
	 * Returns arcsine of the value.
	 * @param x  double value
	 * @return arcsine of the value
	 */
	public static double asin(double x) {
		if (x > 1 || x < -1) return Double.NaN;
	    return atan(x / java.lang.Math.sqrt(1 - x*x));
	}

	/**
	 * Returns arccosine of the value.
	 * @param x  double value
	 * @return arccosine of the value
	 */
	public static double acos(double x) {
		return PI / 2.0 - asin(x);
	}

	/**
	 * Returns arctangent of the value.
	 * @param x  double value
	 * @return arctangent of the value
	 */
	public static double atan(double x) {
		//shrinking domain
		boolean neg = false, big = false;
		if (x < 0) {
			x = -x;
			neg = true;
		}
		if (x > 1) {
			x = 1/x;
			big = true;
		}
		int offsteps = 0;
		while (x > PI / 12.0) {
			x = (x * SQRT3 - 1) / (x + SQRT3);
			offsteps++;
		}
		//calculating as Taylor series
		double result = 0;
		double rest = x;
		x *= x;
		int n = 1;
		while (rest > 1.0e-16 || rest < -1.0e-16) {
			result += rest / n;
			n += 2;
			rest = -rest * x;
		}
		// reverting our transformations
		result += PI / 6.0 * offsteps;
		if (big) result = PI / 2.0 - result;
		return neg ? -result : result;
	}
}
