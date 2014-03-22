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

package alchemy.types;

/**
 * Boxed double value.
 * @author Sergey Basalaev
 */
public final class Float64 {

	public final double value;

	public Float64(double val) {
		if (Double.isNaN(val)) this.value = Double.NaN;
		else if (val == 0.0) this.value = 0.0;
		else this.value = val;
	}

	public boolean equals(Object other) {
		if (other == null) return false;
		if (!(other instanceof Float64)) return false;
		return ((Float64)other).value == this.value;
	}

	public int hashCode() {
		long d = Double.doubleToLongBits(value);
		return (int)(d ^ (d >>> 32));
	}

	public String toString() {
		return Double.toString(value);
	}
}
