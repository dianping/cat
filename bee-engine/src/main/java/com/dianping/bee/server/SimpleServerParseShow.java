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
public class SimpleServerParseShow {
	public static final int OTHER = -1;

	public static final int DATABASES = 1;

	public static final int TABLES = 2;

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
				return showTablesCheck(stmt, i);
			case 'D':
			case 'd':
				return showDatabasesCheck(stmt, i);
			default:
				return OTHER;
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

	// SHOW TABLES
	static int showTablesCheck(String stmt, int offset) {
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
			}
		}
		return OTHER;
	}
}
