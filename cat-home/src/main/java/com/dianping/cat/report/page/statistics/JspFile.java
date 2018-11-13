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
package com.dianping.cat.report.page.statistics;

public enum JspFile {
	HOURLY_REPORT("/jsp/report/bug/bug.jsp"),

	HISTORY_REPORT("/jsp/report/bug/bugHistory.jsp"),

	HTTP_JSON("/jsp/report/bug/bugApi.jsp"),

	SERVICE_REPORT("/jsp/report/service/service.jsp"),

	SERVICE_HISTORY_REPORT("/jsp/report/service/serviceHistory.jsp"),

	HEAVY_HISTORY_REPORT("/jsp/report/heavy/heavyHistory.jsp"),

	HEAVY_REPORT("/jsp/report/heavy/heavy.jsp"),

	UTILIZATION_HISTORY_REPORT("/jsp/report/utilization/utilizationHistory.jsp"),

	UTILIZATION_REPORT("/jsp/report/utilization/utilization.jsp"),

	BROWSER_HISTORY_REPORT("/jsp/report/browser/browserHistory.jsp"),

	BROWSER_REPORT("/jsp/report/browser/browser.jsp"),

	ALERT_HISTORY_REPORT("/jsp/report/exceptionAlert/alertHistory.jsp"),

	ALERT_REPORT_DETAIL("/jsp/report/exceptionAlert/exceptionDetail.jsp"),

	ALERT_REPORT("/jsp/report/exceptionAlert/alert.jsp"),

	ALERT_SUMMARY("/jsp/report/summary/summary.jsp"),

	JAR_REPORT("/jsp/report/jar/jar.jsp"),

	SYSTEM_REPORT("/jsp/report/statistics/system.jsp"),

	CLIENT_REPORT("/jsp/report/statistics/clientReport.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
