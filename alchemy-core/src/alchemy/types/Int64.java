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

package alchemy.types;

/**
 * Boxed long value.
 *
 * @author Sergey Basalaev
 */
public final class Int64 {
	
	public static final Int64 ZERO = new Int64(0L);
	public static final Int64 ONE = new Int64(1L);
	
	public final long value;
	
	public Int64(long val) {
		this.value = val;
	}
	
	public boolean equals(Object other) {
		if (other == null) return false;
		if (!(other instanceof Int64)) return false;
		return ((Int64)other).value == this.value;
	}
	
	public int hashCode() {
		return (int)(value ^ (value >>> 32));
	}

	public String toString() {
		return Long.toString(value, 10);
	}
}
