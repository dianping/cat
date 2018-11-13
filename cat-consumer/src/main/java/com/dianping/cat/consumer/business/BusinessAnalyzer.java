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
package com.dianping.cat.consumer.business;

import com.dianping.cat.Constants;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.config.business.BusinessConfigManager;
import com.dianping.cat.config.business.ConfigItem;
import com.dianping.cat.consumer.business.model.entity.BusinessItem;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.business.model.entity.Segment;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.DefaultReportManager.StoragePolicy;
import com.dianping.cat.report.ReportManager;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.lookup.util.StringUtils;

import java.util.List;

@Named(type = MessageAnalyzer.class, value = BusinessAnalyzer.ID, instantiationStrategy = Named.PER_LOOKUP)
public class BusinessAnalyzer extends AbstractMessageAnalyzer<BusinessReport> implements LogEnabled {

	public static final String ID = "business";

	@Inject(ID)
	private ReportManager<BusinessReport> m_reportManager;

	@Inject
	private BusinessConfigManager m_configManager;

	@Override
	public void doCheckpoint(boolean atEnd) {
		if (atEnd && !isLocalMode()) {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE_AND_DB, m_index);
		} else {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public BusinessReport getReport(String domain) {
		long period = getStartTime();
		return m_reportManager.getHourlyReport(period, domain, false);
	}

	@Override
	public ReportManager<BusinessReport> getReportManager() {
		return m_reportManager;
	}

	@Override
	public boolean isEligable(MessageTree tree) {
		return tree.getMetrics().size() > 0;
	}

	@Override
	protected void loadReports() {
		m_reportManager.loadHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
	}

	private ConfigItem parseValue(String status, String data) {
		ConfigItem config = new ConfigItem();

		if ("C".equals(status)) {
			if (StringUtils.isEmpty(data)) {
				data = "1";
			}
			int count = (int) Double.parseDouble(data);

			config.setCount(count);
			config.setValue((double) count);
			config.setShowCount(true);
		} else if ("T".equals(status)) {
			double duration = Double.parseDouble(data);

			config.setCount(1);
			config.setValue(duration);
			config.setShowAvg(true);
		} else if ("S".equals(status)) {
			double sum = Double.parseDouble(data);

			config.setCount(1);
			config.setValue(sum);
			config.setShowSum(true);
		} else if ("S,C".equals(status)) {
			String[] datas = data.split(",");

			config.setCount(Integer.parseInt(datas[0]));
			config.setValue(Double.parseDouble(datas[1]));
			config.setShowSum(true);
		} else {
			return null;
		}

		return config;
	}

	@Override
	protected void process(MessageTree tree) {
		String domain = tree.getDomain();
		BusinessReport report = m_reportManager.getHourlyReport(getStartTime(), domain, true);
		List<Metric> metrics = tree.getMetrics();

		for (Metric metric : metrics) {
			processMetric(report, metric, domain);
		}
	}

	private void processMetric(BusinessReport report, Metric metric, String domain) {
		boolean isMonitor = Constants.CAT.equals(domain) && StringUtils.isNotEmpty(metric.getType());

		if (!isMonitor) {
			String name = metric.getName();
			String data = (String) metric.getData();
			String status = metric.getStatus();
			ConfigItem config = parseValue(status, data);

			if (config != null) {
				long current = metric.getTimestamp() / 1000 / 60;
				int min = (int) (current % 60);
				BusinessItem businessItem = report.findOrCreateBusinessItem(name);
				Segment seg = businessItem.findOrCreateSegment(min);

				businessItem.setType(status);

				seg.incCount(config.getCount());
				seg.incSum(config.getValue());
				seg.setAvg(seg.getSum() / seg.getCount());

				config.setTitle(name);

				boolean result = m_configManager.insertBusinessConfigIfNotExist(domain, name, config);

				if (!result) {
					m_logger.error(String.format("error when insert business config info, domain %s, metricName %s", domain,	name));
				}
			}
		}
	}

}
