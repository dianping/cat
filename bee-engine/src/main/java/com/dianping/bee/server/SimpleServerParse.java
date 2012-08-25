/**
 * Project: bee-engine
 * 
 * File Created at 2012-8-23
 * 
 * Copyright 2012 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.bee.server;

import com.alibaba.cobar.parser.util.ParseUtil;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class SimpleServerParse {
	public static final int OTHER = -1;

	public static final int BEGIN = 1;

	public static final int COMMIT = 2;

	public static final int DELETE = 3;

	public static final int INSERT = 4;

	public static final int REPLACE = 5;

	public static final int ROLLBACK = 6;

	public static final int SELECT = 7;

	public static final int SET = 8;

	public static final int SHOW = 9;

	public static final int START = 10;

	public static final int UPDATE = 11;

	public static final int KILL = 12;

	public static final int SAVEPOINT = 13;

	public static final int USE = 14;

	public static final int EXPLAIN = 15;

	public static final int KILL_QUERY = 16;

	public static final int DESC = 17;

	public static int parse(String stmt) {
		for (int i = 0; i < stmt.length(); ++i) {
			switch (stmt.charAt(i)) {
			case ' ':
			case '\t':
			case '\r':
			case '\n':
				continue;
			case '/':
			case '#':
				i = ParseUtil.comment(stmt, i);
				continue;
			case 'B':
			case 'b':
				return beginCheck(stmt, i);
			case 'C':
			case 'c':
				return commitCheck(stmt, i);
			case 'D':
			case 'd':
				return descCheck(stmt, i);
				// return deleteCheck(stmt, i);
			case 'E':
			case 'e':
				return explainCheck(stmt, i);
			case 'I':
			case 'i':
				return insertCheck(stmt, i);
			case 'R':
			case 'r':
				return rCheck(stmt, i);
			case 'S':
			case 's':
				return sCheck(stmt, i);
			case 'U':
			case 'u':
				return uCheck(stmt, i);
			case 'K':
			case 'k':
				return killCheck(stmt, i);
			default:
				return OTHER;
			}
		}
		return OTHER;
	}

	// DESC' '
	static int descCheck(String stmt, int offset) {
		if (stmt.length() > offset + "ESC ".length()) {
			char c1 = stmt.charAt(++offset);
			char c2 = stmt.charAt(++offset);
			char c3 = stmt.charAt(++offset);
			char c4 = stmt.charAt(++offset);
			if ((c1 == 'E' || c1 == 'e') && (c2 == 'S' || c2 == 's') && (c3 == 'C' || c3 == 'c')
			      && (c4 == ' ' || c4 == '\t' || c4 == '\r' || c4 == '\n')) {
				return (offset << 8) | DESC;
			}
		}
		return OTHER;
	}

	// EXPLAIN' '
	static int explainCheck(String stmt, int offset) {
		if (stmt.length() > offset + "XPLAIN ".length()) {
			char c1 = stmt.charAt(++offset);
			char c2 = stmt.charAt(++offset);
			char c3 = stmt.charAt(++offset);
			char c4 = stmt.charAt(++offset);
			char c5 = stmt.charAt(++offset);
			char c6 = stmt.charAt(++offset);
			char c7 = stmt.charAt(++offset);
			if ((c1 == 'X' || c1 == 'x') && (c2 == 'P' || c2 == 'p') && (c3 == 'L' || c3 == 'l')
			      && (c4 == 'A' || c4 == 'a') && (c5 == 'I' || c5 == 'i') && (c6 == 'N' || c6 == 'n')
			      && (c7 == ' ' || c7 == '\t' || c7 == '\r' || c7 == '\n')) {
				return (offset << 8) | EXPLAIN;
			}
		}
		return OTHER;
	}

	// KILL' '
	static int killCheck(String stmt, int offset) {
		if (stmt.length() > offset + "ILL ".length()) {
			char c1 = stmt.charAt(++offset);
			char c2 = stmt.charAt(++offset);
			char c3 = stmt.charAt(++offset);
			char c4 = stmt.charAt(++offset);
			if ((c1 == 'I' || c1 == 'i') && (c2 == 'L' || c2 == 'l') && (c3 == 'L' || c3 == 'l')
			      && (c4 == ' ' || c4 == '\t' || c4 == '\r' || c4 == '\n')) {
				while (stmt.length() > ++offset) {
					switch (stmt.charAt(offset)) {
					case ' ':
					case '\t':
					case '\r':
					case '\n':
						continue;
					case 'Q':
					case 'q':
						return killQueryCheck(stmt, offset);
					default:
						return (offset << 8) | KILL;
					}
				}
				return OTHER;
			}
		}
		return OTHER;
	}

	// KILL QUERY' '
	static int killQueryCheck(String stmt, int offset) {
		if (stmt.length() > offset + "UERY ".length()) {
			char c1 = stmt.charAt(++offset);
			char c2 = stmt.charAt(++offset);
			char c3 = stmt.charAt(++offset);
			char c4 = stmt.charAt(++offset);
			char c5 = stmt.charAt(++offset);
			if ((c1 == 'U' || c1 == 'u') && (c2 == 'E' || c2 == 'e') && (c3 == 'R' || c3 == 'r')
			      && (c4 == 'Y' || c4 == 'y') && (c5 == ' ' || c5 == '\t' || c5 == '\r' || c5 == '\n')) {
				while (stmt.length() > ++offset) {
					switch (stmt.charAt(offset)) {
					case ' ':
					case '\t':
					case '\r':
					case '\n':
						continue;
					default:
						return (offset << 8) | KILL_QUERY;
					}
				}
				return OTHER;
			}
		}
		return OTHER;
	}

	// BEGIN
	static int beginCheck(String stmt, int offset) {
		if (stmt.length() > offset + 4) {
			char c1 = stmt.charAt(++offset);
			char c2 = stmt.charAt(++offset);
			char c3 = stmt.charAt(++offset);
			char c4 = stmt.charAt(++offset);
			if ((c1 == 'E' || c1 == 'e') && (c2 == 'G' || c2 == 'g') && (c3 == 'I' || c3 == 'i')
			      && (c4 == 'N' || c4 == 'n') && (stmt.length() == ++offset || ParseUtil.isEOF(stmt.charAt(offset)))) {
				return BEGIN;
			}
		}
		return OTHER;
	}

	// COMMIT
	static int commitCheck(String stmt, int offset) {
		if (stmt.length() > offset + 5) {
			char c1 = stmt.charAt(++offset);
			char c2 = stmt.charAt(++offset);
			char c3 = stmt.charAt(++offset);
			char c4 = stmt.charAt(++offset);
			char c5 = stmt.charAt(++offset);
			if ((c1 == 'O' || c1 == 'o') && (c2 == 'M' || c2 == 'm') && (c3 == 'M' || c3 == 'm')
			      && (c4 == 'I' || c4 == 'i') && (c5 == 'T' || c5 == 't')
			      && (stmt.length() == ++offset || ParseUtil.isEOF(stmt.charAt(offset)))) {
				return COMMIT;
			}
		}
		return OTHER;
	}

	// DELETE' '
	static int deleteCheck(String stmt, int offset) {
		if (stmt.length() > offset + 6) {
			char c1 = stmt.charAt(++offset);
			char c2 = stmt.charAt(++offset);
			char c3 = stmt.charAt(++offset);
			char c4 = stmt.charAt(++offset);
			char c5 = stmt.charAt(++offset);
			char c6 = stmt.charAt(++offset);
			if ((c1 == 'E' || c1 == 'e') && (c2 == 'L' || c2 == 'l') && (c3 == 'E' || c3 == 'e')
			      && (c4 == 'T' || c4 == 't') && (c5 == 'E' || c5 == 'e')
			      && (c6 == ' ' || c6 == '\t' || c6 == '\r' || c6 == '\n')) {
				return DELETE;
			}
		}
		return OTHER;
	}

	// INSERT' '
	static int insertCheck(String stmt, int offset) {
		if (stmt.length() > offset + 6) {
			char c1 = stmt.charAt(++offset);
			char c2 = stmt.charAt(++offset);
			char c3 = stmt.charAt(++offset);
			char c4 = stmt.charAt(++offset);
			char c5 = stmt.charAt(++offset);
			char c6 = stmt.charAt(++offset);
			if ((c1 == 'N' || c1 == 'n') && (c2 == 'S' || c2 == 's') && (c3 == 'E' || c3 == 'e')
			      && (c4 == 'R' || c4 == 'r') && (c5 == 'T' || c5 == 't')
			      && (c6 == ' ' || c6 == '\t' || c6 == '\r' || c6 == '\n')) {
				return INSERT;
			}
		}
		return OTHER;
	}

	static int rCheck(String stmt, int offset) {
		if (stmt.length() > ++offset) {
			switch (stmt.charAt(offset)) {
			case 'E':
			case 'e':
				return replaceCheck(stmt, offset);
			case 'O':
			case 'o':
				return rollabckCheck(stmt, offset);
			default:
				return OTHER;
			}
		}
		return OTHER;
	}

	// REPLACE' '
	static int replaceCheck(String stmt, int offset) {
		if (stmt.length() > offset + 6) {
			char c1 = stmt.charAt(++offset);
			char c2 = stmt.charAt(++offset);
			char c3 = stmt.charAt(++offset);
			char c4 = stmt.charAt(++offset);
			char c5 = stmt.charAt(++offset);
			char c6 = stmt.charAt(++offset);
			if ((c1 == 'P' || c1 == 'p') && (c2 == 'L' || c2 == 'l') && (c3 == 'A' || c3 == 'a')
			      && (c4 == 'C' || c4 == 'c') && (c5 == 'E' || c5 == 'e')
			      && (c6 == ' ' || c6 == '\t' || c6 == '\r' || c6 == '\n')) {
				return REPLACE;
			}
		}
		return OTHER;
	}

	// ROLLBACK
	static int rollabckCheck(String stmt, int offset) {
		if (stmt.length() > offset + 6) {
			char c1 = stmt.charAt(++offset);
			char c2 = stmt.charAt(++offset);
			char c3 = stmt.charAt(++offset);
			char c4 = stmt.charAt(++offset);
			char c5 = stmt.charAt(++offset);
			char c6 = stmt.charAt(++offset);
			if ((c1 == 'L' || c1 == 'l') && (c2 == 'L' || c2 == 'l') && (c3 == 'B' || c3 == 'b')
			      && (c4 == 'A' || c4 == 'a') && (c5 == 'C' || c5 == 'c') && (c6 == 'K' || c6 == 'k')
			      && (stmt.length() == ++offset || ParseUtil.isEOF(stmt.charAt(offset)))) {
				return ROLLBACK;
			}
		}
		return OTHER;
	}

	static int sCheck(String stmt, int offset) {
		if (stmt.length() > ++offset) {
			switch (stmt.charAt(offset)) {
			case 'A':
			case 'a':
				return savepointCheck(stmt, offset);
			case 'E':
			case 'e':
				return seCheck(stmt, offset);
			case 'H':
			case 'h':
				return showCheck(stmt, offset);
			case 'T':
			case 't':
				return startCheck(stmt, offset);
			default:
				return OTHER;
			}
		}
		return OTHER;
	}

	// SAVEPOINT
	static int savepointCheck(String stmt, int offset) {
		if (stmt.length() > offset + 8) {
			char c1 = stmt.charAt(++offset);
			char c2 = stmt.charAt(++offset);
			char c3 = stmt.charAt(++offset);
			char c4 = stmt.charAt(++offset);
			char c5 = stmt.charAt(++offset);
			char c6 = stmt.charAt(++offset);
			char c7 = stmt.charAt(++offset);
			char c8 = stmt.charAt(++offset);
			if ((c1 == 'V' || c1 == 'v') && (c2 == 'E' || c2 == 'e') && (c3 == 'P' || c3 == 'p')
			      && (c4 == 'O' || c4 == 'o') && (c5 == 'I' || c5 == 'i') && (c6 == 'N' || c6 == 'n')
			      && (c7 == 'T' || c7 == 't') && (c8 == ' ' || c8 == '\t' || c8 == '\r' || c8 == '\n')) {
				return SAVEPOINT;
			}
		}
		return OTHER;
	}

	static int seCheck(String stmt, int offset) {
		if (stmt.length() > ++offset) {
			switch (stmt.charAt(offset)) {
			case 'L':
			case 'l':
				return selectCheck(stmt, offset);
			case 'T':
			case 't':
				if (stmt.length() > ++offset) {
					char c = stmt.charAt(offset);
					if (c == ' ' || c == '\r' || c == '\n' || c == '\t' || c == '/' || c == '#') {
						return (offset << 8) | SET;
					}
				}
				return OTHER;
			default:
				return OTHER;
			}
		}
		return OTHER;
	}

	// SELECT' '
	static int selectCheck(String stmt, int offset) {
		if (stmt.length() > offset + 4) {
			char c1 = stmt.charAt(++offset);
			char c2 = stmt.charAt(++offset);
			char c3 = stmt.charAt(++offset);
			char c4 = stmt.charAt(++offset);
			if ((c1 == 'E' || c1 == 'e') && (c2 == 'C' || c2 == 'c') && (c3 == 'T' || c3 == 't')
			      && (c4 == ' ' || c4 == '\t' || c4 == '\r' || c4 == '\n' || c4 == '/' || c4 == '#')) {
				return (offset << 8) | SELECT;
			}
		}
		return OTHER;
	}

	// SHOW' '
	static int showCheck(String stmt, int offset) {
		if (stmt.length() > offset + 3) {
			char c1 = stmt.charAt(++offset);
			char c2 = stmt.charAt(++offset);
			char c3 = stmt.charAt(++offset);
			if ((c1 == 'O' || c1 == 'o') && (c2 == 'W' || c2 == 'w')
			      && (c3 == ' ' || c3 == '\t' || c3 == '\r' || c3 == '\n')) {
				return (offset << 8) | SHOW;
			}
		}
		return OTHER;
	}

	// START' '
	static int startCheck(String stmt, int offset) {
		if (stmt.length() > offset + 4) {
			char c1 = stmt.charAt(++offset);
			char c2 = stmt.charAt(++offset);
			char c3 = stmt.charAt(++offset);
			char c4 = stmt.charAt(++offset);
			if ((c1 == 'A' || c1 == 'a') && (c2 == 'R' || c2 == 'r') && (c3 == 'T' || c3 == 't')
			      && (c4 == ' ' || c4 == '\t' || c4 == '\r' || c4 == '\n')) {
				return (offset << 8) | START;
			}
		}
		return OTHER;
	}

	// UPDATE' ' | USE' '
	static int uCheck(String stmt, int offset) {
		if (stmt.length() > ++offset) {
			switch (stmt.charAt(offset)) {
			case 'P':
			case 'p':
				if (stmt.length() > offset + 5) {
					char c1 = stmt.charAt(++offset);
					char c2 = stmt.charAt(++offset);
					char c3 = stmt.charAt(++offset);
					char c4 = stmt.charAt(++offset);
					char c5 = stmt.charAt(++offset);
					if ((c1 == 'D' || c1 == 'd') && (c2 == 'A' || c2 == 'a') && (c3 == 'T' || c3 == 't')
					      && (c4 == 'E' || c4 == 'e') && (c5 == ' ' || c5 == '\t' || c5 == '\r' || c5 == '\n')) {
						return UPDATE;
					}
				}
				break;
			case 'S':
			case 's':
				if (stmt.length() > offset + 2) {
					char c1 = stmt.charAt(++offset);
					char c2 = stmt.charAt(++offset);
					if ((c1 == 'E' || c1 == 'e') && (c2 == ' ' || c2 == '\t' || c2 == '\r' || c2 == '\n')) {
						return (offset << 8) | USE;
					}
				}
				break;
			default:
				return OTHER;
			}
		}
		return OTHER;
	}

}
