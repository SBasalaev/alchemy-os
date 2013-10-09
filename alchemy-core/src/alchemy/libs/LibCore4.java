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

import alchemy.system.NativeLibrary;
import alchemy.system.Process;
import alchemy.util.Arrays;
import alchemy.util.Strings;
import java.io.IOException;

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
			/* == Header: string.eh == */
			case 1: // Any.tostr(): String
				return Strings.toString(args[0]);
			case 2: // Char.tostr(): String
				return String.valueOf((char)ival(args[0]));
			case 3: // Int.tobin():String
				return Integer.toBinaryString(ival(args[0]));
			case 4: // Int.tooct():String
				return Integer.toOctalString(ival(args[0]));
			case 5: // Int.tohex():String
				return Integer.toHexString(ival(args[0]));
			case 6: // Int.tobase(base: Int): String
				return Integer.toString(ival(args[0]), ival(args[1]));
			case 7: // Long.tobase(base: Int): String
				return Long.toString(lval(args[0]), ival(args[1]));
			case 8: // String.tointbase(base: Int): Int
				return Ival(Integer.parseInt((String)args[0], ival(args[1])));
			case 9: // String.tolongbase(base: Int): Long
				return Lval(Long.parseLong((String)args[0], ival(args[1])));
			case 10: // String.tofloat(): Float
				return Fval(Float.parseFloat((String)args[0]));
			case 11: // String.todouble(): Double
				return Dval(Double.parseDouble((String)args[0]));
			case 12: { // String.get(at: Int): Char
				String str = (String)args[0];
				int at = ival(args[1]);
				if (at < 0) at += str.length();
				return Ival(str.charAt(at));
			}
			case 13: // String.len(): Int
				return Ival(((String)args[0]).length());
			case 14: { // String.range(from: Int, to: Int): String
				String str = (String) args[0];
				int from = ival(args[1]);
				int to = ival(args[2]);
				if (from < 0) from += str.length();
				if (to < 0) to += str.length();
				return str.substring(from, to);
			}
			case 15: { // String.indexof(ch: Char, from: Int = 0): Int
				String str = (String)args[0];
				int from = ival(args[2]);
				if (from < 0) from += str.length();
				return Ival(str.indexOf(ival(args[1]), from));
			}
			case 16: // String.lindexof(ch: Char): Int
				return Ival(((String)args[0]).lastIndexOf(ival(args[1])));
			case 17: { // String.find(sub: String, from: Int = 0): Int
				String str = (String)args[0];
				int from = ival(args[2]);
				if (from < 0) from += str.length();
				return Ival(str.indexOf((String)args[1], from));
			}
			case 18: // String.ucase(): String
				return ((String)args[0]).toUpperCase();
			case 19: // String.lcase(): String
				return ((String)args[0]).toLowerCase();
			case 20: // String.concat(str: String): String
				return ((String)args[0]).concat((String)args[1]);
			case 21: // String.cmp(str: String): Int
				return Ival(((String)args[0]).compareTo((String)args[1]));
			case 22: // String.trim(): String
				return ((String)args[0]).trim();
			case 23: // String.split(ch: Char, skipEmpty: Bool = false): [String]
				return Strings.split((String)args[0], (char)ival(args[1]), bval(args[2]));
			case 24: // String.format(args: [Any]): String
				return Strings.format((String)args[0], (Object[])args[1]);
			case 25: // String.chars(): [Char]
				return ((String)args[0]).toCharArray();
			case 26: // String.utfbytes(): [Byte]
				return Strings.utfEncode((String)args[0]);
			case 27: { // String.startswith(prefix: String, from: Int = 0): Bool
				String str = (String)args[0];
				int from = ival(args[2]);
				if (from < 0) from += str.length();
				return Ival(str.startsWith((String)args[1], from));
			}
			case 28: // String.replace(oldch: Char, newch: Char): String
				return ((String)args[0]).replace((char)ival(args[1]), (char)ival(args[2]));
			case 29: // String.hash(): Int
				return Ival(((String)args[0]).hashCode());
			case 30: // ca2str(ca: [Char]): String
				return new String((char[])args[0]);
			case 31: // ba2utf(ba: [Byte]): String
				return Strings.utfDecode((byte[])args[0]);
			default:
				return null;
		}
	}
}
