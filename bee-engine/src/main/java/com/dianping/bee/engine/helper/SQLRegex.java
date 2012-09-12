/**
 * Project: bee-engine
 * 
 * File Created at 2012-9-12
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
package com.dianping.bee.engine.helper;

import java.util.regex.Pattern;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class SQLRegex {

	/**
	 * Support ? and %
	 * 
	 * @param str
	 * @param expr
	 * @return
	 */
	public static boolean like(final String str, final String expr) {
		String regex = quotemeta(expr);
		if (null == regex)
			return true;
		regex = regex.replace("_", ".").replace("%", ".*?");
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		return p.matcher(str).matches();
	}

	private static String quotemeta(String s) {
		if (s == null) {
			return null;
		}

		int len = s.length();
		if (len == 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder(len * 2);
		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			if ("[](){}.*+?$^|#\\".indexOf(c) != -1) {
				sb.append("\\");
			}
			sb.append(c);
		}
		return sb.toString();
	}
}
