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
package com.dianping.cat.report.page.heartbeat.service;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.heartbeat.HeartbeatReportMerger;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.ApiPayload;
import com.dianping.cat.report.ReportBucket;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;

@Named(type = LocalModelService.class, value = LocalHeartbeatService.ID)
public class LocalHeartbeatService extends LocalModelService<HeartbeatReport> {

	public static final String ID = HeartbeatAnalyzer.ID;

	@Inject
	private ReportBucketManager m_bucketManager;

	public LocalHeartbeatService() {
		super(HeartbeatAnalyzer.ID);
	}

	private String filterReport(ApiPayload payload, HeartbeatReport report) {
		String ipAddress = payload.getIpAddress();

		if (StringUtils.isEmpty(ipAddress)) {
			Set<String> ips = report.getIps();
			if (ips.size() > 0) {
				ipAddress = SortHelper.sortIpAddress(ips).get(0);
			}
		}
		HeartBeatReportFilter filter = new HeartBeatReportFilter(ipAddress, payload.getMin(), payload.getMax());

		return filter.buildXml(report);
	}

	@Override
	public String buildReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload)
							throws Exception {
		List<HeartbeatReport> reports = super.getReport(period, domain);
		HeartbeatReport report = null;

		if (reports != null) {
			report = new HeartbeatReport(domain);
			HeartbeatReportMerger merger = new HeartbeatReportMerger(report);

			for (HeartbeatReport tmp : reports) {
				tmp.accept(merger);
			}
		}

		if ((report == null || report.getIps().isEmpty()) && period.isLast()) {
			long startTime = request.getStartTime();
			report = getReportFromLocalDisk(startTime, domain);
		}

		return filterReport(payload, report);
	}

	private HeartbeatReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		HeartbeatReport report = new HeartbeatReport(domain);
		HeartbeatReportMerger merger = new HeartbeatReportMerger(report);

		report.setStartTime(new Date(timestamp));
		report.setEndTime(new Date(timestamp + TimeHelper.ONE_HOUR - 1));

		for (int i = 0; i < getAnalyzerCount(); i++) {
			ReportBucket bucket = null;
			try {
				bucket = m_bucketManager.getReportBucket(timestamp, HeartbeatAnalyzer.ID, i);
				String xml = bucket.findById(domain);

				if (xml != null) {
					HeartbeatReport tmp = DefaultSaxParser.parse(xml);

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

	public static class HeartBeatReportFilter
							extends	com.dianping.cat.consumer.heartbeat.model.transform.DefaultXmlBuilder {
		private String m_ip;

		private int m_min;

		private int m_max;

		public HeartBeatReportFilter(String ip, int min, int max) {
			super(true, new StringBuilder(DEFAULT_SIZE));
			m_ip = ip;
			m_min = min;
			m_max = max;
		}

		@Override
		public void visitPeriod(Period period) {
			int minute = period.getMinute();

			if (m_min == -1 && m_max == -1) {
				super.visitPeriod(period);
			} else if (minute <= m_max && minute >= m_min) {
				super.visitPeriod(period);
			}
		}

		@Override
		public void visitMachine(com.dianping.cat.consumer.heartbeat.model.entity.Machine machine) {
			if (machine.getIp().equals(m_ip) || StringUtils.isEmpty(m_ip) || Constants.ALL.equals(m_ip)) {
				super.visitMachine(machine);
			}
		}
	}
}
