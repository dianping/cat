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
package com.dianping.cat.report.page.business.service;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.business.BusinessAnalyzer;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

public class CachedBusinessReportService {

	private final Map<String, BusinessReport> m_businessReports = new LinkedHashMap<String, BusinessReport>() {

		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Entry<String, BusinessReport> eldest) {
			return size() > 1000;
		}
	};

	@Inject
	private BusinessReportService m_reportService;

	@Inject(type = ModelService.class, value = BusinessAnalyzer.ID)
	private ModelService<BusinessReport> m_service;

	public BusinessReport queryBusinessReport(String domain, Date start) {
		long time = start.getTime();
		ModelPeriod period = ModelPeriod.getByTime(time);

		if (period == ModelPeriod.CURRENT || period == ModelPeriod.LAST) {
			ModelRequest request = new ModelRequest(domain, time);

			if (m_service.isEligable(request)) {
				ModelResponse<BusinessReport> response = m_service.invoke(request);
				BusinessReport report = response.getModel();

				return report;
			} else {
				throw new RuntimeException("Internal error: no eligable business service registered for " + request + "!");
			}
		} else {
			return getReportFromCache(domain, time);
		}
	}

	private BusinessReport getReportFromCache(String domain, long time) {
		String key = domain + time;
		BusinessReport result = m_businessReports.get(key);

		if (result == null) {
			Date start = new Date(time);
			Date end = new Date(time + TimeHelper.ONE_HOUR);

			result = m_reportService.queryReport(domain, start, end);
			m_businessReports.put(key, result);
		}
		return result;
	}
}
