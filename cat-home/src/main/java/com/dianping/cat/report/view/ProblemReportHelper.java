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
package com.dianping.cat.report.view;

import java.util.Map;

public class ProblemReportHelper {

	public static String creatLinkString(String baseUrl, String classStyle, Map<String, String> params, String text) {
		StringBuilder sb = new StringBuilder();
		sb.append("<a ");
		sb.append("href=\"").append(baseUrl);
		for (java.util.Map.Entry<String, String> param : params.entrySet()) {
			sb.append("&").append(param.getKey()).append("=").append(param.getValue());
		}
		sb.append("\" class=\"").append(classStyle).append("\"");
		sb.append(" onclick=\"return show(this);\"").append(" >");
		if (text.trim().length() == 0) {
			sb.append("&nbsp;&nbsp");
		} else {
			sb.append(text);
		}
		sb.append("</a>");
		return sb.toString();
	}

}
