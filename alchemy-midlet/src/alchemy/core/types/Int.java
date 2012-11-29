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

package alchemy.core.types;

/**
 * Boxed integer.
 * Works faster then java.lang.Integer due to caching.
 * Provides convenient constants for -1, 0, 1.
 *
 * @author Sergey Basalaev
 */
public class Int {
	private static final Int[] cache;
	
	public static final Int ONE;
	public static final Int ZERO;
	public static final Int M_ONE;
	
	static {
		cache = new Int[256+128];
		for (int i=0; i<cache.length; i++) cache[i] = new Int(i-128);
		M_ONE = cache[127];
		ZERO = cache[128];
		ONE = cache[129];
	}
	
	public final int value;
	
	protected Int(int val) { this.value = val; }

	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (!(obj instanceof Int)) return false;
		return this.value == ((Int)obj).value;
	}

	public int hashCode() { return value; }
	
	/**
	 * Converts integer number into Int instance.
	 */
	public static Int toInt(int i) {
		if (i >= -128 && i < 256) return cache[i+128];
		else return new Int(i);
	}
	
	public static Int toInt(Int i) {
		if (i instanceof Char) {
			return Int.toInt(i.value);
		} else {
			return i;
		}
	}
	
	public String toString() {
		return Integer.toString(value);
	}
	
	public Char toChar() {
		return Char.toChar((char)value);
	}
}
