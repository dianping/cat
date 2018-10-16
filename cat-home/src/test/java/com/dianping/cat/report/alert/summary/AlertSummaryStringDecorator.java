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
package com.dianping.cat.report.alert.summary;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.home.alert.summary.entity.Alert;
import com.dianping.cat.home.alert.summary.entity.AlertSummary;
import com.dianping.cat.home.alert.summary.entity.Category;

public class AlertSummaryStringDecorator implements AlertSummaryDecorator {

	public static final String ID = "AlertSummaryDecorator";

	private static final String css = "<style> th, .alert-content { white-space: nowrap; } </style>";

	private static final String tableHead = " <table class=\"table table-bordered table-striped table-hover\"> <thead> <tr> <th>告警类型&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</th> <th colspan=\"5\">详细警告信息</th> </tr> </thead> <tbody>";

	private static final String tableTail = " </tbody></table>";

	private static final String[] networkHeaders = { "告警设备", "告警指标", "告警时间", "告警级别", "告警内容" };

	private static final String[] businessHeaders = { "告警指标", "告警时间", "告警级别", "告警内容" };

	private static final String[] exceptionHeaders = { "异常名称", "告警时间", "告警级别", "告警内容" };

	private static final String[] systemHeaders = { "告警参数-机器", "告警时间", "告警级别", "告警内容" };

	private static final String[] dependencyEdgeHeaders = { "依赖项目", "告警指标", "告警时间", "告警级别", "告警内容" };

	private static final String[] dependencyExceptionHeaders = { "依赖项目", "异常名称", "告警时间", "告警级别", "告警内容" };

	public String generateHtml(AlertSummary alertSummary) {
		StringBuilder builder = new StringBuilder();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String domain = alertSummary.getDomain();
		Date date = alertSummary.getAlertDate();
		String dateStr = dateFormat.format(date);

		builder.append(css);
		builder.append(generateTitle(domain, dateStr));
		builder.append(tableHead);
		builder.append(generateCategoryHtml(alertSummary.findCategory("network"), "网络告警", networkHeaders));
		builder.append(generateCategoryHtml(alertSummary.findCategory("business"), "业务告警", businessHeaders));
		builder.append(generateCategoryHtml(alertSummary.findCategory("exception"), "异常告警", exceptionHeaders));
		builder.append(
								generateCategoryHtml(alertSummary.findCategory("dependency-business"), "超时依赖调用",	dependencyEdgeHeaders));
		builder.append(
								generateCategoryHtml(alertSummary.findCategory("dependency-exception"), "依赖异常告警",	dependencyExceptionHeaders));
		builder.append(generateCategoryHtml(alertSummary.findCategory("system"), "系统告警", systemHeaders));
		builder.append(tableTail);

		return builder.toString();
	}

	private String generateTitle(String domain, String dateStr) {
		return "<h4> 项目名：&nbsp;" + domain + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;告警时间：&nbsp;" + dateStr
								+ " </h4>";
	}

	private String generateCategoryHtml(Category category, String categoryName, String[] headers) {
		List<Alert> alerts = category.getAlerts();
		int rowspan = alerts.size() + 1;
		StringBuilder builder = new StringBuilder();

		builder.append(generateTitleRow(categoryName, headers, rowspan));
		for (Alert alert : alerts) {
			builder.append(generateDataRow(alert));
		}

		return builder.toString();
	}

	private String generateTitleRow(String categoryName, String[] headers, int rowspan) {
		StringBuilder builder = new StringBuilder();

		builder.append("<tr> <td class=\"text-success\" rowspan=\"").append(rowspan).append("\"");
		builder.append("<strong>").append(categoryName).append("</strong></td>");

		int length = headers.length;

		if (length == 5) {
			builder.append("<th>").append(headers[0]).append("</th>");
		} else if (length == 4) {
			builder.append("<th colspan=\"2\">").append(headers[0]).append("</th>");
		}
		for (int i = 1; i < length; i++) {
			builder.append("<th>").append(headers[i]).append("</th>");
		}

		builder.append("</tr>");

		return builder.toString();
	}

	private String generateDataRow(Alert alert) {
		StringBuilder builder = new StringBuilder();
		String domain = alert.getDomain();

		builder.append("<tr>");

		if (StringUtils.isEmpty(domain)) {
			builder.append(generateTd(alert.getMetric(), null, 2));
		} else {
			builder.append(generateTd(domain, null, 1));
			builder.append(generateTd(alert.getMetric(), null, 1));
		}
		builder.append(generateTd(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(alert.getAlertTime()), null, 1));
		builder.append(generateTd(alert.getType(), null, 1));
		builder.append(generateTd(alert.getContext(), "alert-content", 1));

		builder.append("</tr>");

		return builder.toString();
	}

	private String generateTd(String content, String className, int colspan) {
		StringBuilder builder = new StringBuilder();

		builder.append("<td");

		if (!StringUtils.isEmpty(className)) {
			builder.append(" class=\"").append(className).append("\"");
		}

		if (colspan > 1) {
			builder.append(" colspan=\"").append(colspan).append("\"");
		}

		builder.append(">").append(content).append("</td>");

		return builder.toString();
	}

}
