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

package alchemy.nec;

/**
 * Token types.
 * @author Sergey Basalaev
 */
public interface Token {
	/* CATEGORIES */
	
	/** End of stream. */
	int EOF = -1;
	/** Integer literal. */
	int INT = -2;
	/** Long literal. */
	int LONG = -3;
	/** Float literal. */
	int FLOAT = -4;
	/** Double literal. */
	int DOUBLE = -5;
	/** Quoted string. */
	int QUOTED = -6;
	/** Keyword. */
	int KEYWORD = -7;
	/** Identifier. */
	int IDENTIFIER = -8;
	/** Boolean literal. */
	int BOOL = -9;

	/* OPERATORS */
	
	/** Token <code>'=='</code>. */
	int EQEQ = -20;
	/** Token <code>'&lt;='</code>. */
	int LTEQ = -21;
	/** Token <code>'&gt;='</code>. */
	int GTEQ = -22;
	/** Token <code>'!='</code>. */
	int NOTEQ = -23;
	/** Token <code>'&lt;&lt;'</code>. */
	int LTLT = -24;
	/** Token <code>'&gt;&gt;'</code>. */
	int GTGT = -25;
	/** Token <code>'&gt;&gt;&gt;'</code>. */
	int GTGTGT = -26;
	/** Token <code>'&amp;&amp;'</code>. */
	int AMPAMP = -27;
	/** Token <code>'||'</code>. */
	int BARBAR = -28;
	
	/* ASSIGNMENTS */
	
	/** Token <code>'+='</code>. */
	int PLUSEQ = -31;
	/** Token <code>'-='</code>. */
	int MINUSEQ = -32;
	/** Token <code>'*='</code>. */
	int STAREQ = -33;
	/** Token <code>'/='</code>. */
	int SLASHEQ = -34;
	/** Token <code>'%='</code>. */
	int PERCENTEQ = -35;
	/** Token <code>'|='</code>. */
	int BAREQ = -36;
	/** Token <code>'&='</code>. */
	int AMPEQ = -37;
	/** Token <code>'^='</code>. */
	int HATEQ = -38;
	/** Token <code>'&lt;&lt;='</code>. */
	int LTLTEQ = -39;
	/** Token <code>'&gt;&gt;='</code>. */
	int GTGTEQ = -40;
	/** Token <code>'&gt;&gt;&gt;='</code>. */
	int GTGTGTEQ = -41;
}
