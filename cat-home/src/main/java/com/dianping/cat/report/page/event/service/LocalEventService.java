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
package com.dianping.cat.report.page.event.service;

import java.util.Date;
import java.util.List;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.event.EventReportMerger;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.ApiPayload;
import com.dianping.cat.report.ReportBucket;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;

@Named(type = LocalModelService.class, value = LocalEventService.ID)
public class LocalEventService extends LocalModelService<EventReport> {

	public static final String ID = EventAnalyzer.ID;

	@Inject
	private ReportBucketManager m_bucketManager;

	public LocalEventService() {
		super(EventAnalyzer.ID);
	}

	private String filterReport(ApiPayload payload, EventReport report) {
		String ipAddress = payload.getIpAddress();
		String type = payload.getType();
		String name = payload.getName();
		EventReportFilter filter = new EventReportFilter(type, name, ipAddress);

		return filter.buildXml(report);
	}

	@Override
	public String buildReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload)
							throws Exception {
		List<EventReport> reports = super.getReport(period, domain);
		EventReport report = null;

		if (reports != null) {
			report = new EventReport(domain);
			EventReportMerger merger = new EventReportMerger(report);

			for (EventReport tmp : reports) {
				tmp.accept(merger);
			}
		}

		if ((report == null || report.getIps().isEmpty()) && period.isLast()) {
			long startTime = request.getStartTime();
			report = getReportFromLocalDisk(startTime, domain);
		}
		return filterReport(payload, report);
	}

	private EventReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		EventReport report = new EventReport(domain);
		EventReportMerger merger = new EventReportMerger(report);

		report.setStartTime(new Date(timestamp));
		report.setEndTime(new Date(timestamp + TimeHelper.ONE_HOUR - 1));

		for (int i = 0; i < getAnalyzerCount(); i++) {
			ReportBucket bucket = null;
			try {
				bucket = m_bucketManager.getReportBucket(timestamp, EventAnalyzer.ID, i);
				String xml = bucket.findById(domain);

				if (xml != null) {
					EventReport tmp = DefaultSaxParser.parse(xml);

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

	public static class EventReportFilter extends com.dianping.cat.consumer.event.model.transform.DefaultXmlBuilder {
		private String m_ipAddress;

		private String m_name;

		private String m_type;

		public EventReportFilter(String type, String name, String ip) {
			super(true, new StringBuilder(DEFAULT_SIZE));
			m_type = type;
			m_name = name;
			m_ipAddress = ip;
		}

		@Override
		public void visitMachine(com.dianping.cat.consumer.event.model.entity.Machine machine) {
			if (m_ipAddress == null || m_ipAddress.equals(Constants.ALL)) {
				super.visitMachine(machine);
			} else if (machine.getIp().equals(m_ipAddress)) {
				super.visitMachine(machine);
			}
		}

		@Override
		public void visitName(EventName name) {
			if (m_type != null) {
				super.visitName(name);
			}
		}

		@Override
		public void visitRange(com.dianping.cat.consumer.event.model.entity.Range range) {
			if (m_type != null && m_name != null) {
				super.visitRange(range);
			}
		}

		@Override
		public void visitType(EventType type) {
			if (m_type == null) {
				super.visitType(type);
			} else if (type.getId().equals(m_type)) {
				type.setSuccessMessageUrl(null);
				type.setFailMessageUrl(null);

				super.visitType(type);
			}
		}
	}

}
