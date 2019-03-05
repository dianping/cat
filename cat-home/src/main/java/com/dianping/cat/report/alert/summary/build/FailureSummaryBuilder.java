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
package com.dianping.cat.report.alert.summary.build;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.report.graph.PieChart.Item;
import com.dianping.cat.report.page.problem.transform.PieGraphChartVisitor;
import com.dianping.cat.report.page.problem.transform.ProblemStatistics;
import com.dianping.cat.report.page.problem.transform.ProblemStatistics.StatusStatistics;
import com.dianping.cat.report.page.problem.transform.ProblemStatistics.TypeStatistics;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

@Named(type = SummaryBuilder.class, value = FailureSummaryBuilder.ID)
public class FailureSummaryBuilder extends SummaryBuilder {

	public static final String ID = "FailureDecorator";

	@Inject(type = ModelService.class, value = ProblemAnalyzer.ID)
	private ModelService<ProblemReport> m_service;

	private void addDistributeInfo(Map<Object, Object> resultMap, ProblemReport report) {
		PieGraphChartVisitor pieChart = new PieGraphChartVisitor("error", null);
		Map<Object, Object> distributes = new HashMap<Object, Object>();

		pieChart.visitProblemReport(report);
		for (Item item : pieChart.getPieChart().getItems()) {
			distributes.put(item.getTitle(), item.getNumber());
		}
		resultMap.put("distributeMap", distributes);
	}

	private void addFailureInfo(Map<Object, Object> resultMap, ProblemReport report) {
		ProblemStatistics problemStatistics = new ProblemStatistics();
		problemStatistics.setAllIp(true);
		problemStatistics.visitProblemReport(report);
		TypeStatistics failureStatus = problemStatistics.getStatus().get("error");

		if (failureStatus != null) {
			Map<Object, Object> statusMap = new HashMap<Object, Object>();

			for (StatusStatistics status : failureStatus.getStatus().values()) {
				statusMap.put(status.getStatus(), status.getCount());
			}

			resultMap.put("count", failureStatus.getCount());
			resultMap.put("statusMap", statusMap);
		}
	}

	@Override
	public Map<Object, Object> generateModel(String domain, Date endTime) {
		Map<Object, Object> result = new HashMap<Object, Object>();
		ModelRequest request = new ModelRequest(domain, getCurrentHour()).setProperty("queryType", "view");
		request.setProperty("type", "error");
		ProblemReport report = null;

		if (m_service.isEligable(request)) {
			ModelResponse<ProblemReport> response = m_service.invoke(request);
			report = response.getModel();
		}

		try {
			addFailureInfo(result, report);
			addDistributeInfo(result, report);
		} catch (Exception ex) {
			Cat.logError(ex);
		}
		return result;
	}

	private long getCurrentHour() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal.getTimeInMillis();
	}

	@Override
	public String getID() {
		return ID;
	}

	@Override
	protected String getTemplateAddress() {
		return "errorInfo.ftl";
	}

}
