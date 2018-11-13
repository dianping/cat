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
package com.dianping.cat.report.page.problem;

public enum JspFile {

	ALL("/jsp/report/problem/problemStatics.jsp"),

	DETAIL("/jsp/report/problem/problemDetail.jsp"),

	GROUP("/jsp/report/problem/problemGroup.jsp"),

	HOUR_GRAPH("/jsp/report/problem/problemHourlyGraphs.jsp"),

	HISTORY("/jsp/report/problem/problemHistoryReport.jsp"),

	HISTORY_GRAPH("/jsp/report/problem/problemHistoryGraphs.jsp"),

	MOBILE("/jsp/report/problem/problem_mobile.jsp"),

	THREAD("/jsp/report/problem/problemThread.jsp"),

	GROUP_GRAPHS("/jsp/report/problem/problemHourlyGraphs.jsp"),

	HISTORY_GROUP_GRAPH("/jsp/report/problem/problemHistoryGraphs.jsp"),

	HISTORY_GROUP_REPORT("/jsp/report/problem/problemHistoryGroupReport.jsp"),

	HOURLY_GROUP_REPORT("/jsp/report/problem/problemGroupStatics.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
