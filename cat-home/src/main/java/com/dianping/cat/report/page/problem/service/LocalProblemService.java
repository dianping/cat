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
package com.dianping.cat.report.page.problem.service;

import java.util.Date;
import java.util.List;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.ProblemReportMerger;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.ApiPayload;
import com.dianping.cat.report.ReportBucket;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;

@Named(type = LocalModelService.class, value = LocalProblemService.ID)
public class LocalProblemService extends LocalModelService<ProblemReport> {

	public static final String ID = ProblemAnalyzer.ID;

	@Inject
	private ReportBucketManager m_bucketManager;

	public LocalProblemService() {
		super(ProblemAnalyzer.ID);
	}

	private String filterReport(ApiPayload payload, ProblemReport report) {
		String ipAddress = payload.getIpAddress();
		String type = payload.getType();
		String queryType = payload.getQueryType();
		String name = payload.getName();
		ProblemReportFilter filter = new ProblemReportFilter(ipAddress, type, queryType, name);

		return filter.buildXml(report);
	}

	@Override
	public String buildReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload)
							throws Exception {
		List<ProblemReport> reports = super.getReport(period, domain);
		ProblemReport report = null;

		if (reports != null) {
			report = new ProblemReport(domain);
			ProblemReportMerger merger = new ProblemReportMerger(report);

			for (ProblemReport tmp : reports) {
				tmp.accept(merger);
			}
		}

		if ((report == null || report.getIps().isEmpty()) && period.isLast()) {
			long startTime = request.getStartTime();
			report = getReportFromLocalDisk(startTime, domain);
		}
		return filterReport(payload, report);
	}

	private ProblemReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		ProblemReport report = new ProblemReport(domain);
		ProblemReportMerger merger = new ProblemReportMerger(report);

		report.setStartTime(new Date(timestamp));
		report.setEndTime(new Date(timestamp + TimeHelper.ONE_HOUR - 1));

		for (int i = 0; i < getAnalyzerCount(); i++) {
			ReportBucket bucket = null;
			try {
				bucket = m_bucketManager.getReportBucket(timestamp, ProblemAnalyzer.ID, i);
				String xml = bucket.findById(domain);

				if (xml != null) {
					ProblemReport tmp = DefaultSaxParser.parse(xml);

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

	public static class ProblemReportFilter extends com.dianping.cat.consumer.problem.model.transform.DefaultXmlBuilder {
		private String m_ipAddress;

		// view is show the summary,detail show the thread info
		private String m_type;

		private String m_queryType;

		private String m_status;

		public ProblemReportFilter(String ipAddress, String type, String queryType, String name) {
			super(true, new StringBuilder(DEFAULT_SIZE));
			m_ipAddress = ipAddress;
			m_type = type;
			m_status = name;
			m_queryType = queryType;
		}

		@Override
		public void visitDuration(com.dianping.cat.consumer.problem.model.entity.Duration duration) {
			super.visitDuration(duration);
		}

		@Override
		public void visitEntry(Entry entry) {
			if (m_type == null) {
				super.visitEntry(entry);
			} else {
				if (m_status == null) {
					if (entry.getType().equals(m_type)) {
						super.visitEntry(entry);
					}
				} else {
					if (entry.getType().equals(m_type) && entry.getStatus().equals(m_status)) {
						super.visitEntry(entry);
					}
				}
			}
		}

		@Override
		public void visitMachine(Machine machine) {
			if (m_ipAddress == null) {
				super.visitMachine(machine);
			} else if (machine.getIp().equals(m_ipAddress)) {
				super.visitMachine(machine);
			}
		}

		@Override
		public void visitSegment(Segment segment) {
			super.visitSegment(segment);
		}

		@Override
		public void visitThread(JavaThread thread) {
			if ("detail".equals(m_queryType)) {
				super.visitThread(thread);
			}
		}
	}
}
