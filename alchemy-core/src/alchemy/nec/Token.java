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
	public static final int FALSE = -68;
	public static final int TRUE = -69;
	public static final int BREAK = -70;
	public static final int CONTINUE = -71;
	public static final int RETURN = -72;

	public static boolean isAssignment(int token) {
		return (token >= -41 && token <= -30) || (token == '=');
	}

	public static boolean isOperator(int token) {
		return (token == IN) || (token >= -28 && token <= -19) || "+-/*%^&|<>".indexOf(token) >= 0;
	}

	public static int getBinaryOperator(int assignment) {
		switch (assignment) {
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
				throw new IllegalArgumentException("Not an assignment operator: " + assignment);
		}
	}

	public static String toString(int token) {
		switch (token) {
			case INT:
			case LONG:
			case FLOAT:
			case DOUBLE:
				return "<number>";
			case QUOTED:
				return "<string>";
			case WORD:
				return "<word>";
			case CHAR:
				return "<character>";
			case EOF:
				return "<EOF>";
			case RANGE:
				return "..";
			case EQEQ:
				return "==";
			case LTEQ:
				return "<=";
			case GTEQ:
				return ">=";
			case NOTEQ:
				return "!=";
			case LTLT:
				return "<<";
			case GTGT:
				return ">>";
			case GTGTGT:
				return ">>>";
			case AMPAMP:
				return "&&";
			case BARBAR:
				return "||";
			case PLUSEQ:
				return "+=";
			case MINUSEQ:
				return "-=";
			case STAREQ:
				return "*=";
			case SLASHEQ:
				return "/=";
			case PERCENTEQ:
				return "%=";
			case BAREQ:
				return "|=";
			case AMPEQ:
				return "&=";
			case HATEQ:
				return "^=";
			case LTLTEQ:
				return "<<=";
			case GTGTEQ:
				return ">>=";
			case GTGTGTEQ:
				return ">>>=";
			case CAST:
				return "cast";
			case CATCH:
				return "catch";
			case CONST:
				return "const";
			case DEF:
				return "def";
			case DO:
				return "do";
			case ELSE:
				return "else";
			case FOR:
				return "for";
			case IF:
				return "if";
			case IN:
				return "in";
			case NEW:
				return "new";
			case NULL:
				return "null";
			case SUPER:
				return "super";
			case SWITCH:
				return "switch";
			case TRY:
				return "try";
			case TYPE:
				return "type";
			case USE:
				return "use";
			case VAR:
				return "var";
			case WHILE:
				return "while";
			case FALSE:
				return "false";
			case TRUE:
				return "true";
			case BREAK:
				return "break";
			case CONTINUE:
				return "continue";
			case RETURN:
				return "return";
			default:
				return String.valueOf((char)token);
		}
	}
}
