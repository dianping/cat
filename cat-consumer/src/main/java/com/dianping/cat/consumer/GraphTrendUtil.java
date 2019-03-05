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
package com.dianping.cat.consumer;

import org.codehaus.plexus.util.StringUtils;

public class GraphTrendUtil {
	public static final String GRAPH_SPLITTER = ";";

	public static final char GRAPH_CHAR_SPLITTER = ';';

	public static Double[] parseToDouble(String str, int length) {
		Double[] result = new Double[length];

		if (StringUtils.isNotBlank(str)) {
			String[] strs = str.split(GraphTrendUtil.GRAPH_SPLITTER);

			for (int i = 0; i < length; i++) {
				try {
					result[i] = Double.parseDouble(strs[i]);
				} catch (Exception e) {
					result[i] = 0.0;
				}
			}
		} else {
			for (int i = 0; i < length; i++) {
				result[i] = 0.0;
			}
		}
		return result;
	}

	public static Long[] parseToLong(String str, int length) {
		Long[] result = new Long[length];

		if (StringUtils.isNotBlank(str)) {
			String[] strs = str.split(GraphTrendUtil.GRAPH_SPLITTER);

			for (int i = 0; i < length; i++) {
				try {
					result[i] = Long.parseLong(strs[i]);
				} catch (Exception e) {
					result[i] = 0L;
				}
			}
		} else {
			for (int i = 0; i < length; i++) {
				result[i] = 0L;
			}
		}
		return result;
	}
}
