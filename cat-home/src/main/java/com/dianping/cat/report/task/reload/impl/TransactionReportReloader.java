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
package com.dianping.cat.report.task.reload.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionReportMerger;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultNativeBuilder;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.report.ReportManager;
import com.dianping.cat.report.task.reload.AbstractReportReloader;
import com.dianping.cat.report.task.reload.ReportReloadEntity;
import com.dianping.cat.report.task.reload.ReportReloader;

@Named(type = ReportReloader.class, value = TransactionAnalyzer.ID)
public class TransactionReportReloader extends AbstractReportReloader {

	@Inject(TransactionAnalyzer.ID)
	protected ReportManager<TransactionReport> m_reportManager;

	private List<TransactionReport> buildMergedReports(Map<String, List<TransactionReport>> mergedReports) {
		List<TransactionReport> results = new ArrayList<TransactionReport>();

		for (Entry<String, List<TransactionReport>> entry : mergedReports.entrySet()) {
			String domain = entry.getKey();
			TransactionReport report = new TransactionReport(domain);
			TransactionReportMerger merger = new TransactionReportMerger(report);

			report.setStartTime(report.getStartTime());
			report.setEndTime(report.getEndTime());

			for (TransactionReport r : entry.getValue()) {
				r.accept(merger);
			}
			results.add(merger.getTransactionReport());
		}

		return results;
	}

	@Override
	public String getId() {
		return TransactionAnalyzer.ID;
	}

	@Override
	public List<ReportReloadEntity> loadReport(long time) {
		List<ReportReloadEntity> results = new ArrayList<ReportReloadEntity>();
		Map<String, List<TransactionReport>> mergedReports = new HashMap<String, List<TransactionReport>>();

		for (int i = 0; i < getAnalyzerCount(); i++) {
			Map<String, TransactionReport> reports = m_reportManager.loadLocalReports(time, i);

			for (Entry<String, TransactionReport> entry : reports.entrySet()) {
				String domain = entry.getKey();
				TransactionReport r = entry.getValue();
				List<TransactionReport> rs = mergedReports.get(domain);

				if (rs == null) {
					rs = new ArrayList<TransactionReport>();

					mergedReports.put(domain, rs);
				}
				rs.add(r);
			}
		}

		List<TransactionReport> reports = buildMergedReports(mergedReports);

		for (TransactionReport r : reports) {
			HourlyReport report = new HourlyReport();

			report.setCreationDate(new Date());
			report.setDomain(r.getDomain());
			report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
			report.setName(getId());
			report.setPeriod(new Date(time));
			report.setType(1);

			byte[] content = DefaultNativeBuilder.build(r);
			ReportReloadEntity entity = new ReportReloadEntity(report, content);

			results.add(entity);
		}
		return results;
	}
}
