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
package com.dianping.bee.server.parse;

import com.alibaba.cobar.parser.util.ParseUtil;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class SimpleServerParseShow {
	public static final int OTHER = -1;

	public static final int DATABASES = 1;

	public static final int TABLES = 2;

	public static final int STATUS = 3;

	public static final int VARIABLES = 4;

	public static final int TABLESTATUS = 5;

	public static final int COLLATION = 6;

	public static int parse(String stmt, int offset) {
		int i = offset;
		for (; i < stmt.length(); i++) {
			switch (stmt.charAt(i)) {
			case ' ':
				continue;
			case '/':
			case '#':
				i = ParseUtil.comment(stmt, i);
				continue;
			case 'T':
			case 't':
				return showTableCheck(stmt, i);
			case 'D':
			case 'd':
				return showDatabasesCheck(stmt, i);
			case 'S':
			case 's':
				return showStatusCheck(stmt, i);
			case 'V':
			case 'v':
				return showVariablesCheck(stmt, i);
			case 'C':
			case 'c':
				return showCollation(stmt, i);
			default:
				return OTHER;
			}
		}
		return OTHER;
	}

	// SHOW COLLATION
	static int showCollation(String stmt, int offset) {
		if (stmt.length() > offset + "ollation".length()) {
			char c1 = stmt.charAt(++offset);
			char c2 = stmt.charAt(++offset);
			char c3 = stmt.charAt(++offset);
			char c4 = stmt.charAt(++offset);
			char c5 = stmt.charAt(++offset);
			char c6 = stmt.charAt(++offset);
			char c7 = stmt.charAt(++offset);
			char c8 = stmt.charAt(++offset);
			if ((c1 == 'O' || c1 == 'o') && (c2 == 'L' || c2 == 'l') && (c3 == 'L' || c3 == 'l')
			      && (c4 == 'A' || c4 == 'a') && (c5 == 'T' || c5 == 't') && (c6 == 'I' || c6 == 'i')
			      && (c7 == 'O' || c7 == 'o') && (c8 == 'N' || c8 == 'n')
			      && (stmt.length() == ++offset || ParseUtil.isEOF(stmt.charAt(offset)))) {
				return COLLATION;
			}
		}
		return OTHER;
	}

	// SHOW DATABASES
	static int showDatabasesCheck(String stmt, int offset) {
		if (stmt.length() > offset + "atabases".length()) {
			char c1 = stmt.charAt(++offset);
			char c2 = stmt.charAt(++offset);
			char c3 = stmt.charAt(++offset);
			char c4 = stmt.charAt(++offset);
			char c5 = stmt.charAt(++offset);
			char c6 = stmt.charAt(++offset);
			char c7 = stmt.charAt(++offset);
			char c8 = stmt.charAt(++offset);
			if ((c1 == 'A' || c1 == 'a') && (c2 == 'T' || c2 == 't') && (c3 == 'A' || c3 == 'a')
			      && (c4 == 'B' || c4 == 'b') && (c5 == 'A' || c5 == 'a') && (c6 == 'S' || c6 == 's')
			      && (c7 == 'E' || c7 == 'e') && (c8 == 'S' || c8 == 's')
			      && (stmt.length() == ++offset || ParseUtil.isEOF(stmt.charAt(offset)))) {
				return DATABASES;
			}
		}
		return OTHER;
	}

	// SHOW STATUS
	static int showStatusCheck(String stmt, int offset) {
		if (stmt.length() > offset + "tatus".length()) {
			char c1 = stmt.charAt(++offset);
			char c2 = stmt.charAt(++offset);
			char c3 = stmt.charAt(++offset);
			char c4 = stmt.charAt(++offset);
			char c5 = stmt.charAt(++offset);
			if ((c1 == 'T' || c1 == 't') && (c2 == 'A' || c2 == 'a') && (c3 == 'T' || c3 == 't')
			      && (c4 == 'U' || c4 == 'u') && (c5 == 'S' || c5 == 's')
			      && (stmt.length() == ++offset || ParseUtil.isEOF(stmt.charAt(offset)))) {
				return STATUS;
			}
		}
		return OTHER;
	}

	// SHOW TABLES OR SHOW TABLE STATUS FROM [TABLE]
	static int showTableCheck(String stmt, int offset) {
		if (stmt.length() > offset + "ables".length()) {
			char c1 = stmt.charAt(++offset);
			char c2 = stmt.charAt(++offset);
			char c3 = stmt.charAt(++offset);
			char c4 = stmt.charAt(++offset);
			char c5 = stmt.charAt(++offset);
			if ((c1 == 'A' || c1 == 'a') && (c2 == 'B' || c2 == 'b') && (c3 == 'L' || c3 == 'l')
			      && (c4 == 'E' || c4 == 'e') && (c5 == 'S' || c5 == 's')
			      && (stmt.length() == ++offset || ParseUtil.isEOF(stmt.charAt(offset)))) {
				return TABLES;
			} else if (c5 == ' ') {
				return showTableStatusCheck(stmt, offset);
			}
		}
		return OTHER;
	}

	/**
	 * @param stmt
	 * @param offset
	 * @return
	 */
	static int showTableStatusCheck(String stmt, int offset) {
		if (stmt.length() > offset + "status from ".length()) {
			char c1 = stmt.charAt(++offset);
			char c2 = stmt.charAt(++offset);
			char c3 = stmt.charAt(++offset);
			char c4 = stmt.charAt(++offset);
			char c5 = stmt.charAt(++offset);
			char c6 = stmt.charAt(++offset);
			char c7 = stmt.charAt(++offset);
			char c8 = stmt.charAt(++offset);
			char c9 = stmt.charAt(++offset);
			char c10 = stmt.charAt(++offset);
			char c11 = stmt.charAt(++offset);
			char c12 = stmt.charAt(++offset);
			if ((c1 == 'S' || c1 == 's') && (c2 == 'T' || c2 == 't') && (c3 == 'A' || c3 == 'a')
			      && (c4 == 'T' || c4 == 't') && (c5 == 'U' || c5 == 'u') && (c6 == 'S' || c6 == 's') && (c7 == ' ')
			      && (c8 == 'F' || c8 == 'f') && (c9 == 'R' || c9 == 'r') && (c10 == 'o' || c10 == 'o')
			      && (c11 == 'M' || c11 == 'm') && (c12 == ' ')) {
				return TABLESTATUS;
			}
		}
		return OTHER;
	}

	// SHOW VARIABLES
	static int showVariablesCheck(String stmt, int offset) {
		if (stmt.length() > offset + "ariables".length()) {
			char c1 = stmt.charAt(++offset);
			char c2 = stmt.charAt(++offset);
			char c3 = stmt.charAt(++offset);
			char c4 = stmt.charAt(++offset);
			char c5 = stmt.charAt(++offset);
			char c6 = stmt.charAt(++offset);
			char c7 = stmt.charAt(++offset);
			char c8 = stmt.charAt(++offset);
			if ((c1 == 'A' || c1 == 'a') && (c2 == 'R' || c2 == 'r') && (c3 == 'I' || c3 == 'i')
			      && (c4 == 'A' || c4 == 'a') && (c5 == 'B' || c5 == 'b') && (c6 == 'L' || c6 == 'l')
			      && (c7 == 'E' || c7 == 'e') && (c8 == 'S' || c8 == 's')
			      && (stmt.length() == ++offset || ParseUtil.isEOF(stmt.charAt(offset)))) {
				return VARIABLES;
			}
		}
		return OTHER;
	}
}
