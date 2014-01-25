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

import alchemy.io.UTFReader;
import java.io.IOException;

/**
 * Splits Ether source into sequence of tokens.
 * @author Sergey Basalaev
 */
class Tokenizer {
	static private final int EOF_CHAR = -1;
	static private final int NO_CHAR = -2;
	static private final int RANGE_CHAR = -3;

	private final UTFReader r;
	private final String filename;
	private final CompilerEnv env;
	private boolean pushedBack;
	private int nextch = NO_CHAR;
	private int linenumber = 1;

	public int ttype;
	public int ivalue;
	public long lvalue;
	public float fvalue;
	public double dvalue;
	public String svalue;

	private static final int WORDCHAR = 1;
	private static final int OPCHAR = 2;
	private static int[] chtypes = new int[128];

	static {
		chtypes['_'] = WORDCHAR;
		for (int i='0'; i<='9'; i++) {
			chtypes[i] = WORDCHAR;
		}
		for (int i='a'; i<='z'; i++) {
			chtypes[i] = WORDCHAR;
		}
		for (int i='A'; i<='Z'; i++) {
			chtypes[i] = WORDCHAR;
		}
		chtypes['+'] = OPCHAR;
		chtypes['-'] = OPCHAR;
		chtypes['*'] = OPCHAR;
		chtypes['/'] = OPCHAR;
		chtypes['%'] = OPCHAR;
		chtypes['='] = OPCHAR;
		chtypes['<'] = OPCHAR;
		chtypes['>'] = OPCHAR;
		chtypes['|'] = OPCHAR;
		chtypes['&'] = OPCHAR;
		chtypes['^'] = OPCHAR;
		chtypes['!'] = OPCHAR;
		chtypes['~'] = OPCHAR;
	}

	/**
	 * Creates new tokenizer to read from this buffer.
	 * If <code>compat</code> is true then work in 2.1 compatibility mode.
	 */
	public Tokenizer(CompilerEnv env, String filename, UTFReader r) {
		this.r = r;
		this.filename = filename;
		this.env = env;
	}

	public void pushBack() {
		pushedBack = true;
	}

	public int nextToken() throws IOException, ParseException {
		if (pushedBack) {
			pushedBack = false;
			return ttype;
		}

		int ch = readChar();

		//skipping whitespaces
		while (ch >= 0 && ch <= ' ') ch = readChar();

		//EOF
		if (ch == EOF_CHAR) {
			return ttype = Token.EOF;
		}

		//range hack
		if (ch == RANGE_CHAR) {
			return ttype = Token.RANGE;
		}

		//character literal
		if (ch == '\'') {
			ch = readChar();
			if (ch == '\n' || ch == EOF_CHAR) {
				throw new ParseException("Unclosed character literal");
			}
			if (ch == '\\') ch = readEscape();
			ivalue = ch;
			ch = readChar();
			if (ch != '\'') {
				throw new ParseException("Unclosed character literal");
			}
			return ttype = Token.CHAR;
		}

		//string literal
		if (ch == '"') {
			StringBuffer str = new StringBuffer();
			ch = readChar();
			while (ch != '"' && ch != EOF_CHAR && ch != '\n') {
				if (ch == '\\') ch = readEscape();
				str.append((char)ch);
				ch = readChar();
			}
			if (ch != '"') {
				throw new ParseException("Unclosed string literal");
			}
			svalue = str.toString();
			return ttype = Token.QUOTED;
		}
		
		// escaped identifier literal
		if (ch == '`') {
			StringBuffer id = new StringBuffer();
			ch = readChar();
			while (ch != '`' && ch != EOF_CHAR && ch != '\n') {
				id.append((char)ch);
				ch = readChar();
			}
			if (ch != '`') {
				throw new ParseException("Unclosed identifier literal");
			}
			svalue = id.toString();
			return ttype = Token.WORD;
		}

		//dot, range and numbers
		if (ch >= '0' && ch <= '9' || ch == '.') {
			boolean dotseen = false;
			if (ch == '.') {
				ch = readChar();
				if (ch == '.') {
					return ttype = Token.RANGE;
				} else if (ch < '0' || ch > '9') {
					nextch = ch;
					return ttype = '.';
				}
				dotseen = true;
			}
			nextch = ch;
			StringBuffer number = new StringBuffer();
			if (dotseen) number.append('.');
			String dec = readDecimal();
			//check for possible hex literal
			if (dec.equals("0") && !dotseen) {
				ch = readChar();
				if (ch == 'x' || ch == 'X') {
					number.append(readHex());
					ch = readChar();
					if (ch == 'l' || ch == 'L') {
						try {
							lvalue = parseulong16(number.toString());
						} catch (Exception nfe) {
							throw new ParseException("Integer number too large: "+number);
						}
						return ttype = Token.LONG;
					} else {
						nextch = ch;
						try {
							ivalue = parseuint16(number.toString());
						} catch (Exception nfe) {
							throw new ParseException("Integer number too large: "+number);
						}
						return ttype = Token.INT;
					}
				} else {
					nextch = ch;
				}
			}
			number.append(dec);
			//check for possible fraction part
			if (!dotseen) {
				ch = readChar();
				if (ch == '.') {
					ch = readChar();
					if (ch == '.') {
						nextch = RANGE_CHAR;
					} else {
						dotseen = true;
						number.append('.');
						nextch = ch;
						if (ch >= '0' && ch <= '9') {
							number.append(readDecimal());
						}
					}
				} else {
					nextch = ch;
				}
			}
			//now all forms of nn, nn., .nn, nn.nn are read
			//checking for exponent
			ch = readChar();
			if (ch == 'e' || ch == 'E') {
				number.append((char)ch);
				ch = readChar();
				if (ch == '+' || ch == '-') {
					number.append((char)ch);
					ch = readChar();
				}
				if (ch >= '0' && ch <= '9') {
					nextch = ch;
					number.append(readDecimal());
				} else {
					throw new ParseException("Malformed floating point literal: "+number);
				}
				dotseen = true;
			} else {
				nextch = ch;
			}
			//checking for suffix
			ch = readChar();
			if (ch == 'f' || ch == 'F') {
				try {
					fvalue = Float.parseFloat(number.toString());
				} catch (Exception nfe) {
					throw new ParseException("Floating point number too large: "+number);
				}
				return ttype = Token.FLOAT;
			}
			if (ch == 'd' || ch == 'D') {
				try {
					dvalue = Double.parseDouble(number.toString());
				} catch (Exception nfe) {
					throw new ParseException("Floating point number too large: "+number);
				}
				return ttype = Token.DOUBLE;
			}
			if (dotseen) {
				nextch = ch;
				try {
					dvalue = Double.parseDouble(number.toString());
				} catch (Exception nfe) {
					throw new ParseException("Floating point number too large: "+number);
				}
				return ttype = Token.DOUBLE;
			}
			if (ch == 'l' || ch == 'L') {
				try {
					lvalue = Long.parseLong(number.toString(), 10);
				} catch (NumberFormatException nfe) {
					throw new ParseException("Integer number too large: "+number);
				}
				return ttype = Token.LONG;
			} else {
				nextch = ch;
				try {
					ivalue = Integer.parseInt(number.toString(), 10);
				} catch (Exception nfe) {
					throw new ParseException("Integer number too large: "+number);
				}
				return ttype = Token.INT;
			}
		}

		//identifiers and keywords
		if (ch <= 127 && chtypes[ch] == WORDCHAR) {
			StringBuffer idbuf = new StringBuffer();
			idbuf.append((char)ch);
			ch = readChar();
			while (ch != EOF_CHAR && ch <= 127 && chtypes[ch] == WORDCHAR) {
				idbuf.append((char)ch);
				ch = readChar();
			}
			nextch = ch;
			String id = idbuf.toString();
			svalue = id;
			if (id.equals("break"))
				if (env.hasOption(CompilerEnv.F_COMPAT21)) {
					env.warn(filename, linenumber, CompilerEnv.W_DEPRECATED, "'break' will be a keyword in Ether 2.2");
					return ttype = Token.WORD;
				} else {
					return ttype = Token.BREAK;
				}
			if (id.equals("cast"))
				return ttype = Token.CAST;
			if (id.equals("catch"))
				return ttype = Token.CATCH;
			if (id.equals("const"))
				return ttype = Token.CONST;
			if (id.equals("continue"))
				if (env.hasOption(CompilerEnv.F_COMPAT21)) {
					env.warn(filename, linenumber, CompilerEnv.W_DEPRECATED, "'continue' will be a keyword in Ether 2.2");
					return ttype = Token.WORD;
				} else {
					return ttype = Token.CONTINUE;
				}
			if (id.equals("def"))
				return ttype = Token.DEF;
			if (id.equals("do"))
				return ttype = Token.DO;
			if (id.equals("else"))
				return ttype = Token.ELSE;
			if (id.equals("false"))
				return ttype = Token.FALSE;
			if (id.equals("for"))
				return ttype = Token.FOR;
			if (id.equals("if"))
				return ttype = Token.IF;
			if (id.equals("in"))
				if (env.hasOption(CompilerEnv.F_COMPAT21)) {
					env.warn(filename, linenumber, CompilerEnv.W_DEPRECATED, "'in' will be a keyword in Ether 2.2");
					return ttype = Token.WORD;
				} else {
					return ttype = Token.IN;
				}
			if (id.equals("new"))
				return ttype = Token.NEW;
			if (id.equals("null"))
				return ttype = Token.NULL;
			if (id.equals("return"))
				if (env.hasOption(CompilerEnv.F_COMPAT21)) {
					env.warn(filename, linenumber, CompilerEnv.W_DEPRECATED, "'return' will be a keyword in Ether 2.2");
					return ttype = Token.WORD;
				} else {
					return ttype = Token.RETURN;
				}
			if (id.equals("super"))
				return ttype = Token.SUPER;
			if (id.equals("switch"))
				return ttype = Token.SWITCH;
			if (id.equals("throw")) {
				if (env.hasOption(CompilerEnv.F_COMPAT21)) {
					env.warn(filename, linenumber, CompilerEnv.W_DEPRECATED, "'throw' will be a keyword in Ether 2.2");
					return ttype = Token.WORD;
				} else {
					return ttype = Token.THROW;
				}
			}
			if (id.equals("true"))
				return ttype = Token.TRUE;
			if (id.equals("try"))
				return ttype = Token.TRY;
			if (id.equals("type"))
				return ttype = Token.TYPE;
			if (id.equals("use"))
				return ttype = Token.USE;
			if (id.equals("var"))
				return ttype = Token.VAR;
			if (id.equals("while"))
				return ttype = Token.WHILE;
			return ttype = Token.WORD;
		}

		//operators and comments
		if (ch <= 127 && chtypes[ch] == OPCHAR) {
			int ch2 = readChar();
			if (ch2 <= 127 && chtypes[ch2] == OPCHAR) {
				if (ch == '=' && ch2 == '=') return ttype = Token.EQEQ;
				if (ch == '<' && ch2 == '=') return ttype = Token.LTEQ;
				if (ch == '>' && ch2 == '=') return ttype = Token.GTEQ;
				if (ch == '<' && ch2 == '<') {
					ch = readChar();
					if (ch == '=') {
						return ttype = Token.LTLTEQ;
					} else {
						nextch = ch;
						return ttype = Token.LTLT;
					}
				}
				if (ch == '>' && ch2 == '>') {
					ch = readChar();
					if (ch == '>') {
						ch = readChar();
						if (ch == '=') {
							return ttype = Token.GTGTGTEQ;
						} else {
							nextch = ch;
							return ttype = Token.GTGTGT;
						}
					} else if (ch == '=') {
						return ttype = Token.GTGTEQ;
					} else {
						nextch = ch;
						return ttype = Token.GTGT;
					}
				}
				if (ch == '!' && ch2 == '=') return ttype = Token.NOTEQ;
				if (ch == '+' && ch2 == '=') return ttype = Token.PLUSEQ;
				if (ch == '-' && ch2 == '=') return ttype = Token.MINUSEQ;
				if (ch == '*' && ch2 == '=') return ttype = Token.STAREQ;
				if (ch == '/' && ch2 == '=') return ttype = Token.SLASHEQ;
				if (ch == '%' && ch2 == '=') return ttype = Token.PERCENTEQ;
				if (ch == '&' && ch2 == '=') return ttype = Token.AMPEQ;
				if (ch == '|' && ch2 == '=') return ttype = Token.BAREQ;
				if (ch == '&' && ch2 == '&') return ttype = Token.AMPAMP;
				if (ch == '|' && ch2 == '|') return ttype = Token.BARBAR;
				//line comment
				if (ch == '/' && ch2 == '/') {
					do ch = readChar();
					while (ch != '\n' && ch != EOF_CHAR);
					return nextToken();
				}
				//block comment
				if (ch == '/' && ch2 == '*') {
					ch = readChar();
					ch2 = readChar();
					while (ch2 != EOF_CHAR && (ch != '*' || ch2 != '/')) {
						ch = ch2;
						ch2 = readChar();
					}
					if (ch2 == EOF_CHAR) {
						throw new ParseException("Unclosed comment");
					}
					return nextToken();
				}
			} else {
				nextch = ch2;
			}
		}

		return ttype = ch;
	}

	private int readChar() throws IOException {
		int ch;
		if (nextch == NO_CHAR) {
			ch = r.read();
			if (ch == '\n') linenumber++;
		} else {
			ch = nextch;
			nextch = NO_CHAR;
		}
		return ch;
	}

	private int readEscape() throws IOException, ParseException {
		int ch = readChar();
		switch (ch) {
			case '\\': return '\\';
			case '\'': return '\'';
			case '"': return '"';
			case 'n': return '\n';
			case 't': return '\t';
			case 'r': return '\r';
			case 'b': return '\b';
			case 'f': return '\f';
			case 'u': { //four hex digits must follow
				int u1 = hexdigit(readChar());
				int u2 = hexdigit(readChar());
				int u3 = hexdigit(readChar());
				int u4 = hexdigit(readChar());
				if ((u1|u2|u3|u4) < 0) {
					throw new ParseException("Illegal unicode escape");
				}
				return (u1 << 12) | (u2 << 8) | (u3 << 4) | u4;
			}
			case '0': //octals
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7': {
				int octal = ch-'0';
				if (ch <= '3') {
					ch = readChar();
					if (ch >= '0' && ch <= '7') {
						octal <<= 3;
						octal |= ch-'0';
					} else {
						nextch = ch;
						return octal;
					}
				}
				ch = readChar();
				if (ch >= '0' && ch <= '7') {
					octal <<= 3;
					octal |= ch-'0';
				} else {
					nextch = ch;
				}
				return octal;
			}
		}
		throw new ParseException("Illegal escape sequence");
	}

	/**
	 * Helper function to read unicode escape.
	 */
	private int hexdigit(int ch) {
		if (ch >= '0' && ch <= '9') return ch-'0';
		if (ch >= 'a' && ch <= 'f') return ch-'a'+10;
		if (ch >= 'A' && ch <= 'F') return ch-'A'+10;
		return -1;
	}

	private String readDecimal() throws IOException {
		StringBuffer decimal = new StringBuffer();
		int ch = readChar();
		while (ch >= '0' && ch <= '9') {
			decimal.append((char)ch);
			ch = readChar();
		}
		nextch = ch;
		return decimal.toString();
	}

	private String readHex() throws IOException {
		StringBuffer hex = new StringBuffer();
		int ch = readChar();
		while ((ch >= '0' && ch <= '9') ||
		       (ch >= 'a' && ch <= 'f') ||
			   (ch >= 'A' && ch <= 'F')) {
			hex.append((char)ch);
			ch = readChar();
		}
		nextch = ch;
		return hex.toString();
	}

	/**
	 * Returns string representation of the current token.
	 */
	public String toString() {
		switch (ttype) {
			case Token.EOF:
				return "<EOF>";
			case Token.INT:
				return String.valueOf(ivalue);
			case Token.LONG:
				return String.valueOf(lvalue);
			case Token.FLOAT:
				return String.valueOf(fvalue);
			case Token.DOUBLE:
				return String.valueOf(dvalue);
			case Token.EQEQ:
				return "==";
			case Token.GTEQ:
				return ">=";
			case Token.GTGT:
				return ">>";
			case Token.GTGTGT:
				return ">>>";
			case Token.LTEQ:
				return "<=";
			case Token.LTLT:
				return "<<";
			case Token.NOTEQ:
				return "!=";
			case Token.AMPAMP:
				return "&&";
			case Token.BARBAR:
				return "||";
			case Token.PLUSEQ:
				return "+=";
			case Token.MINUSEQ:
				return "-=";
			case Token.STAREQ:
				return "*=";
			case Token.SLASHEQ:
				return "/=";
			case Token.PERCENTEQ:
				return "%=";
			case Token.BAREQ:
				return "|=";
			case Token.AMPEQ:
				return "&=";
			case Token.HATEQ:
				return "^=";
			case Token.LTLTEQ:
				return "<<=";
			case Token.GTGTEQ:
				return ">>=";
			case Token.GTGTGTEQ:
				return ">>>=";
			default:
				return (ttype < 0) ? svalue : String.valueOf((char)ttype);
		}
	}

	/**
	 * Returns number of the current line.
	 */
	public int lineNumber() {
		return linenumber;
	}
	
	private int parseuint16(String hex) {
		hex = hex.toLowerCase();
		if (hex.length() > 8) throw new NumberFormatException("Integer number too large: "+hex);
		int num=0;
		for (int i=0; i<hex.length(); i++) {
			num <<= 4;
			char ch = hex.charAt(i);
			if (ch >= '0' && ch <= '9') num |= ch-'0';
			else if (ch >= 'a' && ch <= 'f') num |= ch-'a'+10;
		}
		return num;
	}

	private long parseulong16(String hex) {
		hex = hex.toLowerCase();
		if (hex.length() > 16) throw new NumberFormatException("Integer number too large: "+hex);
		long num=0;
		for (int i=0; i<hex.length(); i++) {
			num <<= 4;
			char ch = hex.charAt(i);
			if (ch >= '0' && ch <= '9') num |= ch-'0';
			else if (ch >= 'a' && ch <= 'f') num |= ch-'a'+10;
		}
		return num;
	}
}
