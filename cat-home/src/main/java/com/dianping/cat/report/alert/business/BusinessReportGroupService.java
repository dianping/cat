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
package com.dianping.cat.report.alert.business;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.consumer.business.BusinessAnalyzer;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

@Named
public class BusinessReportGroupService {

	@Inject(BusinessAnalyzer.ID)
	private ModelService<BusinessReport> m_service;

	private BusinessReport fetchMetricReport(String product, ModelPeriod period, int min, int max) {
		ModelRequest request = new ModelRequest(product, period.getStartTime()).setProperty("requireAll", "ture");

		request.setProperty("min", String.valueOf(min));
		request.setProperty("max", String.valueOf(max));

		ModelResponse<BusinessReport> response = m_service.invoke(request);

		if (response != null) {
			return response.getModel();
		} else {
			return null;
		}
	}

	public BusinessReportGroup prepareDatas(String domain, int minute, int duration) {
		BusinessReport currentReport = null;
		BusinessReport lastReport = null;
		boolean dataReady = false;

		if (minute >= duration - 1) {
			int min = minute - duration + 1;
			int max = minute;

			currentReport = fetchMetricReport(domain, ModelPeriod.CURRENT, min, max);

			if (currentReport != null) {
				dataReady = true;
			}
		} else if (minute < 0) {
			int min = minute + 60 - duration + 1;
			int max = minute + 60;

			lastReport = fetchMetricReport(domain, ModelPeriod.LAST, min, max);

			if (lastReport != null) {
				dataReady = true;
			}
		} else {
			int lastLength = duration - minute - 1;
			int lastMin = 60 - lastLength;

			currentReport = fetchMetricReport(domain, ModelPeriod.CURRENT, 0, minute);
			lastReport = fetchMetricReport(domain, ModelPeriod.LAST, lastMin, 59);

			if (lastReport != null && currentReport != null) {
				dataReady = true;
			}
		}
		BusinessReportGroup reports = new BusinessReportGroup();

		reports.setLast(lastReport).setCurrent(currentReport).setDataReady(dataReady);
		return reports;
	}
}
