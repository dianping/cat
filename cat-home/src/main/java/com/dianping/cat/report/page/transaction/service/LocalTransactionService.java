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
package com.dianping.cat.report.page.transaction.service;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionReportMerger;
import com.dianping.cat.consumer.transaction.model.entity.*;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.ApiPayload;
import com.dianping.cat.report.ReportBucket;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.util.Date;
import java.util.List;

@Named(type = LocalModelService.class, value = LocalTransactionService.ID)
public class LocalTransactionService extends LocalModelService<TransactionReport> {

	public static final String ID = TransactionAnalyzer.ID;

	@Inject
	private ReportBucketManager m_bucketManager;

	public LocalTransactionService() {
		super(TransactionAnalyzer.ID);
	}

	@Override
	public String buildReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload)
							throws Exception {
		List<TransactionReport> reports = super.getReport(period, domain);
		TransactionReport report = null;

		if (reports != null) {
			report = new TransactionReport(domain);
			TransactionReportMerger merger = new TransactionReportMerger(report);

			for (TransactionReport tmp : reports) {
				tmp.accept(merger);
			}
		}

		if ((report == null || report.getIps().isEmpty()) && period.isLast()) {
			long startTime = request.getStartTime();
			report = getReportFromLocalDisk(startTime, domain);
		}
		return filterReport(payload, report);
	}

	private String filterReport(ApiPayload payload, TransactionReport report) {
		String type = payload.getType();
		String name = payload.getName();
		String ip = payload.getIpAddress();
		int min = payload.getMin();
		int max = payload.getMax();
		String xml;

		try {
			TransactionReportFilter filter = new TransactionReportFilter(type, name, ip, min, max);

			xml = filter.buildXml(report);
		} catch (Exception e) {
			TransactionReportFilter filter = new TransactionReportFilter(type, name, ip, min, max);

			xml = filter.buildXml(report);
		}
		return xml;
	}

	private TransactionReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		TransactionReport report = new TransactionReport(domain);
		TransactionReportMerger merger = new TransactionReportMerger(report);

		report.setStartTime(new Date(timestamp));
		report.setEndTime(new Date(timestamp + TimeHelper.ONE_HOUR - 1));

		for (int i = 0; i < getAnalyzerCount(); i++) {
			ReportBucket bucket = null;
			try {
				bucket = m_bucketManager.getReportBucket(timestamp, TransactionAnalyzer.ID, i);
				String xml = bucket.findById(domain);

				if (xml != null) {
					TransactionReport tmp = DefaultSaxParser.parse(xml);

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

	public static class TransactionReportFilter
							extends	com.dianping.cat.consumer.transaction.model.transform.DefaultXmlBuilder {
		private String m_ipAddress;

		private String m_name;

		private String m_type;

		private int m_min;

		private int m_max;

		public TransactionReportFilter(String type, String name, String ip, int min, int max) {
			super(true, new StringBuilder(DEFAULT_SIZE));
			m_type = type;
			m_name = name;
			m_ipAddress = ip;
			m_min = min;
			m_max = max;
		}

		@Override
		public void visitAllDuration(AllDuration duration) {
		}

		@Override
		public void visitDuration(Duration duration) {
			if (m_type != null && m_name != null) {
				super.visitDuration(duration);
			}
		}

		@Override
		public void visitMachine(com.dianping.cat.consumer.transaction.model.entity.Machine machine) {
			if (m_ipAddress == null || m_ipAddress.equals(Constants.ALL)) {
				super.visitMachine(machine);
			} else if (machine.getIp().equals(m_ipAddress)) {
				super.visitMachine(machine);
			}
		}

		@Override
		public void visitName(TransactionName name) {
			if (m_type != null) {
				visitTransactionName(name);
			}
		}

		@Override
		public void visitRange(Range range) {
			if (m_type != null && m_name != null) {
				int minute = range.getValue();

				if (m_min == -1 && m_max == -1) {
					super.visitRange(range);
				} else if (minute <= m_max && minute >= m_min) {
					super.visitRange(range);
				}
			}
		}

		private void visitTransactionName(TransactionName name) {
			super.visitName(name);
		}

		@Override
		public void visitTransactionReport(TransactionReport transactionReport) {
			synchronized (transactionReport) {
				super.visitTransactionReport(transactionReport);
			}
		}

		@Override
		public void visitType(TransactionType type) {
			if (m_type == null) {
				super.visitType(type);
			} else if (type.getId().equals(m_type)) {
				super.visitType(type);
			}
		}

	}

}
