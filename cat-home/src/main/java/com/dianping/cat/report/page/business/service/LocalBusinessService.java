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
import java.util.List;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.consumer.business.BusinessAnalyzer;
import com.dianping.cat.consumer.business.BusinessReportMerger;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.business.model.entity.Segment;
import com.dianping.cat.consumer.business.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.ApiPayload;
import com.dianping.cat.report.ReportBucket;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;

@Named(type = LocalModelService.class, value = LocalBusinessService.ID)
public class LocalBusinessService extends LocalModelService<BusinessReport> {

	public static final String ID = BusinessAnalyzer.ID;

	@Inject
	private ReportBucketManager m_bucketManager;

	public LocalBusinessService() {
		super(BusinessAnalyzer.ID);
	}

	@Override
	public String buildReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload)
							throws Exception {
		List<BusinessReport> reports = super.getReport(period, domain);
		BusinessReport report = null;

		if (reports != null) {
			report = new BusinessReport(domain);
			BusinessReportMerger merger = new BusinessReportMerger(report);

			for (BusinessReport tmp : reports) {
				tmp.accept(merger);
			}
		}

		if ((report == null || report.getBusinessItems().isEmpty()) && period.isLast()) {
			long startTime = request.getStartTime();
			report = getReportFromLocalDisk(startTime, domain);

			if (report == null) {
				report = new BusinessReport(domain);
				report.setStartTime(new Date(startTime));
				report.setEndTime(new Date(startTime + TimeHelper.ONE_HOUR - 1));
			}
		}
		BusinessReportFilter filter = new BusinessReportFilter(payload.getMin(), payload.getMax());
		return filter.buildXml(report);
	}

	private BusinessReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		BusinessReport report = new BusinessReport(domain);
		BusinessReportMerger merger = new BusinessReportMerger(report);

		report.setStartTime(new Date(timestamp));
		report.setEndTime(new Date(timestamp + TimeHelper.ONE_HOUR - 1));

		for (int i = 0; i < getAnalyzerCount(); i++) {
			ReportBucket bucket = null;
			try {
				bucket = m_bucketManager.getReportBucket(timestamp, BusinessAnalyzer.ID, i);
				String xml = bucket.findById(domain);

				if (xml != null) {
					BusinessReport tmp = DefaultSaxParser.parse(xml);

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

	public static class BusinessReportFilter extends	com.dianping.cat.consumer.business.model.transform.DefaultXmlBuilder {

		private int m_min;

		private int m_max;

		public BusinessReportFilter(int min, int max) {
			super(true, new StringBuilder(DEFAULT_SIZE));
			m_min = min;
			m_max = max;
		}

		@Override
		public void visitSegment(Segment segment) {
			int id = segment.getId();

			if (m_min == -1 && m_max == -1) {
				super.visitSegment(segment);
			} else if (id <= m_max && id >= m_min) {
				super.visitSegment(segment);
			}
		}

	}

}
