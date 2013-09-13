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
 * Boxed float value.
 * @author Sergey Basalaev
 */
public final class Float32 {
	
	public final float value;
	
	public Float32(float val) {
		this.value = val;
	}
	
	public boolean equals(Object other) {
		if (other == null) return false;
		if (!(other instanceof Float32)) return false;
		return ((Float32)other).value == this.value;
	}
	
	public int hashCode() {
		return Float.floatToIntBits(value);
	}

	public String toString() {
		return Float.toString(value);
	}
}
