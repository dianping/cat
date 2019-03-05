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
package com.dianping.cat.report.page.state.service;

import java.util.Date;
import java.util.List;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.StateReportMerger;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.ApiPayload;
import com.dianping.cat.report.ReportBucket;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;

@Named(type = LocalModelService.class, value = LocalStateService.ID)
public class LocalStateService extends LocalModelService<StateReport> {

	public static final String ID = StateAnalyzer.ID;

	@Inject
	private ReportBucketManager m_bucketManager;

	public LocalStateService() {
		super(StateAnalyzer.ID);
	}

	@Override
	public String buildReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload)
							throws Exception {
		List<StateReport> reports = super.getReport(period, domain);
		StateReport report = null;

		if (reports != null) {
			report = new StateReport(domain);
			StateReportMerger merger = new StateReportMerger(report);

			for (StateReport tmp : reports) {
				tmp.accept(merger);
			}
		}
		if ((report == null || report.getMachines().isEmpty()) && period.isLast()) {
			long startTime = request.getStartTime();
			report = getReportFromLocalDisk(startTime, domain);

			if (report == null) {
				report = new StateReport(domain);
				report.setStartTime(new Date(startTime));
				report.setEndTime(new Date(startTime + TimeHelper.ONE_HOUR - 1));
			}
		}
		return new StateReportFilter().buildXml(report);
	}

	private StateReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		StateReport report = new StateReport(domain);
		StateReportMerger merger = new StateReportMerger(report);

		report.setStartTime(new Date(timestamp));
		report.setEndTime(new Date(timestamp + TimeHelper.ONE_HOUR - 1));

		for (int i = 0; i < getAnalyzerCount(); i++) {
			ReportBucket bucket = null;
			try {
				bucket = m_bucketManager.getReportBucket(timestamp, StateAnalyzer.ID, i);
				String xml = bucket.findById(domain);

				if (xml != null) {
					StateReport tmp = DefaultSaxParser.parse(xml);

					tmp.accept(merger);
				}
			} finally {
				if (bucket != null) {
					m_bucketManager.closeBucket(bucket);
				}
			}
		}
		return report;
	}

	public static class StateReportFilter extends com.dianping.cat.consumer.state.model.transform.DefaultXmlBuilder {
		public StateReportFilter() {
			super(true, new StringBuilder(DEFAULT_SIZE));
		}
	}

}
