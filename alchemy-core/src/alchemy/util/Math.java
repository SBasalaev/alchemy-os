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
	private static final double ASIN0999999 = 1.5693821131146521; // asin(0.999999)
	private static final double LN2 = 0.6931471805599453; // ln(2)

	private Math() { }

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
	 * @param val  double value
	 * @return natural logarithm of the value
	 */
	public static double log(double val) {
		boolean neg = false;
		if (val < 0) return Double.NaN;
		if (val < 1) {
			val = 1.0/val;
			neg = true;
		}
		//calculating base-2 logarithm
		double log2 = 0;
		while (val >= 64) {
			val /= 64.0;
			log2 += 6.0;
		}
		while (val >= 2) {
			val /= 2.0;
			log2 += 1.0;
		}
		if (val >= SQRT2) {
			val /= SQRT2;
			log2 += 0.5;
		}
		if (val >= QUADRT2) {
			val /= QUADRT2;
			log2 += 0.25;
		}
		if (val >= OCTRT2) {
			val /= OCTRT2;
			log2 += 0.125;
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
		ln += log2 * LN2;
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
		if (val > 0.999999) {
			val = PI / 2.0d + (1-val) * 1000000 * (ASIN0999999 - Math.PI / 2.0d);
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
		return PI / 2.0 - asin(val);
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
		while (val > PI / 12.0) {
			val = (val * SQRT3 - 1) / (val + SQRT3);
			offsteps++;
		}
		//calculating as Taylor series
		double result = 0;
		double rest = val;
		val *= val;
		int n = 1;
		while (rest > 1.0e-16) {
			result += rest / n;
			n += 2;
			rest = -rest * val;
		}
		// reverting our transformations
		result += PI / 6.0 * offsteps;
		if (big) result = PI / 2.0 - result;
		return neg ? -result : result;
	}
}
