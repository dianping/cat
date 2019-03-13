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
package com.dianping.cat.config;

import java.text.ParseException;

public class DefaultFormat extends Format {

	public static void main(String[] str) {
	}

	@Override
	public String parse(String input) throws ParseException {
		String pattern = getPattern();
		String item = "";
		String describe = "";
		int index = pattern.indexOf(":");

		if (index != -1 && pattern.length() > index + 1) {
			item = pattern.substring(0, index).trim();
			describe = pattern.substring(index + 1).trim();
		}
		if (!describe.isEmpty()) {
			int length = 1;
			try {
				length = Integer.parseInt(describe);
			} catch (NumberFormatException e) {
				throw new ParseException(pattern + "is illegal", 0);
			}
			if (input.length() != length) {
				throw new ParseException("not match " + pattern, 0);
			}
		}
		if (pattern.equals("*")) {
			return input;
		} else if (item.equals("md5")) {
			char[] charArray = input.toCharArray();
			for (Character ch : charArray) {
				if (!Character.isDigit(ch) && !Character.isLowerCase(ch)) {
					return input;
				}
			}
		} else if (item.equals("number")) {
			char[] charArray = input.toCharArray();
			for (Character ch : charArray) {
				if (!Character.isDigit(ch)) {
					return input;
				}
			}
		}
		return ("{" + pattern + "}");
	}
}
