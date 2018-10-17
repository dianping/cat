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
package com.dianping.cat.report.page.dependency.service;

import java.util.Date;
import java.util.List;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.dependency.DependencyReportMerger;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.ApiPayload;
import com.dianping.cat.report.ReportBucket;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;

@Named(type = LocalModelService.class, value = LocalDependencyService.ID)
public class LocalDependencyService extends LocalModelService<DependencyReport> {

	public static final String ID = DependencyAnalyzer.ID;

	@Inject
	private ReportBucketManager m_bucketManager;

	public LocalDependencyService() {
		super(DependencyAnalyzer.ID);
	}

	@Override
	public String buildReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload)
							throws Exception {
		List<DependencyReport> reports = super.getReport(period, domain);
		DependencyReport report = null;

		if (reports != null) {
			report = new DependencyReport(domain);
			DependencyReportMerger merger = new DependencyReportMerger(report);

			for (DependencyReport tmp : reports) {
				tmp.accept(merger);
			}
		}

		if ((report == null || report.getSegments().isEmpty()) && period.isLast()) {
			long startTime = request.getStartTime();
			report = getReportFromLocalDisk(startTime, domain);
		}
		return new DependencyReportFilter().buildXml(report);
	}

	private DependencyReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		DependencyReport report = new DependencyReport(domain);
		DependencyReportMerger merger = new DependencyReportMerger(report);

		report.setStartTime(new Date(timestamp));
		report.setEndTime(new Date(timestamp + TimeHelper.ONE_HOUR - 1));

		for (int i = 0; i < getAnalyzerCount(); i++) {
			ReportBucket bucket = null;
			try {
				bucket = m_bucketManager.getReportBucket(timestamp, DependencyAnalyzer.ID, i);
				String xml = bucket.findById(domain);

				if (xml != null) {
					DependencyReport tmp = DefaultSaxParser.parse(xml);

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

	public static class DependencyReportFilter
							extends	com.dianping.cat.consumer.dependency.model.transform.DefaultXmlBuilder {
		public DependencyReportFilter() {
			super(true, new StringBuilder(DEFAULT_SIZE));
		}
	}

}
