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
package com.dianping.cat.consumer.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StringUtils {
	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	public static boolean isNotEmpty(String str) {
		return str != null && str.length() > 0;
	}

	public static List<String> spilt(String str, int start, char c) {
		int length = str.length();
		List<String> strs = new ArrayList<String>();

		for (int index = start; index < length; index++) {
			StringBuilder sb = new StringBuilder();

			for (int j = index; j < length; j++) {
				final char charAt = str.charAt(j);

				if (j == length - 1) {
					sb.append(str.charAt(j));
					strs.add(sb.toString());
					index = j++;
					break;
				} else if (charAt == c) {
					strs.add(sb.toString());
					index = j;
					break;
				} else {
					sb.append(str.charAt(j));
				}
			}
		}

		return strs;
	}

	public static String join(Collection<String> list, String separator) {
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

	public static String join(double[] count, char spit) {
		boolean first = true;
		StringBuilder sb = new StringBuilder(128);

		for (double i : count) {
			if (first) {
				sb.append(i);
				first = false;
			} else {
				sb.append(spit).append(i);
			}
		}
		return sb.toString();
	}

	public static String join(int[] count, char spit) {
		boolean first = true;
		StringBuilder sb = new StringBuilder(128);

		for (int i : count) {
			if (first) {
				sb.append(i);
				first = false;
			} else {
				sb.append(spit).append(i);
			}
		}
		return sb.toString();
	}

	public static String join(long[] count, char spit) {
		boolean first = true;
		StringBuilder sb = new StringBuilder(128);

		for (long i : count) {
			if (first) {
				sb.append(i);
				first = false;
			} else {
				sb.append(spit).append(i);
			}
		}
		return sb.toString();
	}

	public static String join(String[] array, String separator) {
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

	public static String normalizeSpace(String str) {
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

	public static List<String> split(String str, char c) {
		List<String> results = new ArrayList<String>();

		for (int i = 0; i < str.length(); ) {
			StringBuilder sb = new StringBuilder();

			for (int j = i; j < str.length(); j++) {
				char tmp = str.charAt(j);

				if (j == str.length() - 1) {
					sb.append(tmp);
					results.add(sb.toString());

					i = j + 1;
					break;
				} else if (tmp == c) {
					results.add(sb.toString());

					i = j + 1;
					break;
				} else {
					sb.append(tmp);
				}
			}
		}

		return results;
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
