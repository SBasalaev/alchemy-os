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

package alchemy.nec;

/**
 * Token types.
 * @author Sergey Basalaev
 */
public final class Token {
	
	/** Utility class. */
	private Token() { }
	
	/* CATEGORIES */
	
	/** End of stream. */
	public static final int EOF = -1;
	/** Integer literal. */
	public static final int INT = -2;
	/** Long literal. */
	public static final int LONG = -3;
	/** Float literal. */
	public static final int FLOAT = -4;
	/** Double literal. */
	public static final int DOUBLE = -5;
	/** Quoted string. */
	public static final int QUOTED = -6;
	/** Identifier. */
	public static final int WORD = -8;
	/** Boolean literal. */
	public static final int BOOL = -9;
	/** Character literal. */
	public static final int CHAR = -10;

	/* OPERATORS */

	/** Token <code>'..'</code>. */
	public static final int RANGE = -19;
	/** Token <code>'=='</code>. */
	public static final int EQEQ = -20;
	/** Token <code>'&lt;='</code>. */
	public static final int LTEQ = -21;
	/** Token <code>'&gt;='</code>. */
	public static final int GTEQ = -22;
	/** Token <code>'!='</code>. */
	public static final int NOTEQ = -23;
	/** Token <code>'&lt;&lt;'</code>. */
	public static final int LTLT = -24;
	/** Token <code>'&gt;&gt;'</code>. */
	public static final int GTGT = -25;
	/** Token <code>'&gt;&gt;&gt;'</code>. */
	public static final int GTGTGT = -26;
	/** Token <code>'&amp;&amp;'</code>. */
	public static final int AMPAMP = -27;
	/** Token <code>'||'</code>. */
	public static final int BARBAR = -28;

	/* ASSIGNMENTS */
	
	/** Token <code>'+='</code>. */
	public static final int PLUSEQ = -31;
	/** Token <code>'-='</code>. */
	public static final int MINUSEQ = -32;
	/** Token <code>'*='</code>. */
	public static final int STAREQ = -33;
	/** Token <code>'/='</code>. */
	public static final int SLASHEQ = -34;
	/** Token <code>'%='</code>. */
	public static final int PERCENTEQ = -35;
	/** Token <code>'|='</code>. */
	public static final int BAREQ = -36;
	/** Token <code>'&='</code>. */
	public static final int AMPEQ = -37;
	/** Token <code>'^='</code>. */
	public static final int HATEQ = -38;
	/** Token <code>'&lt;&lt;='</code>. */
	public static final int LTLTEQ = -39;
	/** Token <code>'&gt;&gt;='</code>. */
	public static final int GTGTEQ = -40;
	/** Token <code>'&gt;&gt;&gt;='</code>. */
	public static final int GTGTGTEQ = -41;

	/* KEYWORDS */

	/** Token <code>'in'</code>. */
	public static final int CAST = -50;
	public static final int CATCH = -51;
	public static final int CONST = -52;
	public static final int DEF = -53;
	public static final int DO = -54;
	public static final int ELSE = -55;
	public static final int FOR = -56;
	public static final int IF = -57;
	public static final int IN = -58;
	public static final int NEW = -59;
	public static final int NULL = -60;
	public static final int SUPER = -61;
	public static final int SWITCH = -62;
	public static final int TRY = -63;
	public static final int TYPE = -64;
	public static final int USE = -65;
	public static final int VAR = -66;
	public static final int WHILE = -67;

	public static boolean isAssignment(int token) {
		return (token >= -41 && token <= -30) || (token == '=');
	}

	public static int getAssignOperator(int token) {
		switch (token) {
			case PLUSEQ: return '+';
			case MINUSEQ: return '-';
			case STAREQ: return '*';
			case SLASHEQ: return '/';
			case PERCENTEQ: return '%';
			case BAREQ: return '|';
			case AMPEQ: return '&';
			case HATEQ: return '^';
			case LTLTEQ: return LTLT;
			case GTGTEQ: return GTGT;
			case GTGTGTEQ: return GTGTGT;
			default:
				throw new IllegalArgumentException("Not an assignment operator: " + token);
		}
	}
}
