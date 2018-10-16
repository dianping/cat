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

import sun.misc.BASE64Encoder;

public class InvidStringBuilder {

	public static final int s_lengthLimit = 256;

	public static String getValidString(String key) {
		String result = null;

		if (key.length() > s_lengthLimit) {
			result = key.substring(0, s_lengthLimit);
		} else {
			result = key;
		}

		StringBuilder sb = new StringBuilder(32);
		int length = result.length();
		boolean needBase64 = true;

		for (int i = 0; i < length; i++) {
			final char charAt = result.charAt(i);

			if (charAt > 126 || charAt < 32) {
				sb.append('.');
			} else {
				sb.append(charAt);
				needBase64 = false;
			}
		}
		if (needBase64) {
			return "Base64." + new BASE64Encoder().encodeBuffer(key.getBytes()).trim();
		} else {
			return sb.toString();
		}
	}
}
