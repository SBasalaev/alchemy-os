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
	/** Keyword. */
	public static final int KEYWORD = -7;
	/** Identifier. */
	public static final int WORD = -8;
	/** Boolean literal. */
	public static final int BOOL = -9;
	/** Character literal. */
	public static final int CHAR = -10;

	/* OPERATORS */
	
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
	
	public static boolean isAssignment(int token) {
		return (token <= -30) || (token == '=');
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
