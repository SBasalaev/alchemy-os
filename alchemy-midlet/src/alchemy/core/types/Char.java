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
 * Boxed character.
 * 
 * @author Sergey Basalaev
 */
public class Char extends Int {
	
	private static final Char[] cache;
	
	static {
		cache = new Char[256];
		for (char i=0; i < 256; i++) cache[i] = new Char(i);
	}
	
	public Char(char ch) {
		super(ch);
	}

	public String toString() {
		return String.valueOf((char)value);
	}
	
	public static Char toChar(char ch) {
		if (ch < 256) return cache[ch];
		else return new Char(ch);
	}
}
