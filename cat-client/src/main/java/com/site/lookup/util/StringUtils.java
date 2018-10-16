/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.site.lookup.util;

import java.util.Collection;

public class StringUtils {
	public static final boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	public static final boolean isNotEmpty(String str) {
		return str != null && str.length() > 0;
	}

	public static final String join(String[] array, String separator) {
		StringBuilder sb = new StringBuilder(1024);
		boolean first = true;

		for (String item : array) {
			if (first) {
				first = false;
			} else {
				sb.append(separator);
			}

			sb.append(item);
		}

		return sb.toString();
	}

	public static final String join(Collection<String> list, String separator) {
		StringBuilder sb = new StringBuilder(1024);
		boolean first = true;

		for (String item : list) {
			if (first) {
				first = false;
			} else {
				sb.append(separator);
			}

			sb.append(item);
		}

		return sb.toString();
	}

	public static final String normalizeSpace(String str) {
		int len = str.length();
		StringBuilder sb = new StringBuilder(len);
		boolean space = false;

		for (int i = 0; i < len; i++) {
			char ch = str.charAt(i);

			switch (ch) {
			case ' ':
			case '\t':
			case '\r':
			case '\n':
				space = true;
				break;
			default:
				if (space) {
					sb.append(' ');
					space = false;
				}

				sb.append(ch);
			}
		}

		return sb.toString();
	}

	public static final String trimAll(String str) {
		if (str == null) {
			return str;
		}

		int len = str.length();
		StringBuilder sb = new StringBuilder(len);

		for (int i = 0; i < len; i++) {
			char ch = str.charAt(i);

			switch (ch) {
			case ' ':
			case '\t':
			case '\r':
			case '\n':
				break;
			default:
				sb.append(ch);
			}
		}

		return sb.toString();
	}
}
