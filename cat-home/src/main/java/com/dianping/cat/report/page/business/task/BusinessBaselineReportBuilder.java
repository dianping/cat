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
package com.dianping.cat.report.page.business.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.config.business.BusinessConfigManager;
import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.configuration.business.entity.BusinessReportConfig;
import com.dianping.cat.consumer.business.BusinessAnalyzer;
import com.dianping.cat.consumer.business.model.entity.BusinessItem;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.helper.MetricType;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.dal.report.Baseline;
import com.dianping.cat.report.page.business.service.BusinessReportService;
import com.dianping.cat.report.page.metric.service.BaselineService;
import com.dianping.cat.report.page.metric.task.BaselineConfig;
import com.dianping.cat.report.page.metric.task.BaselineConfigManager;
import com.dianping.cat.report.page.metric.task.BaselineCreator;
import com.dianping.cat.report.task.TaskBuilder;

@Named(type = TaskBuilder.class, value = BusinessBaselineReportBuilder.ID)
public class BusinessBaselineReportBuilder implements TaskBuilder {

	public static final String ID = BusinessAnalyzer.ID;

	private static final int POINT_NUMBER = 60 * 24;

	@Inject
	private BusinessReportService m_reportService;

	@Inject
	private BusinessConfigManager m_configManager;

	@Inject
	private BaselineConfigManager m_baselineConfigManager;

	@Inject
	private BusinessPointParser m_parser;

	@Inject
	private BaselineCreator m_baselineCreator;

	@Inject
	private BaselineService m_baselineService;

	@Inject
	private BusinessKeyHelper m_keyHelper;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		Map<String, BusinessReport> reports = new HashMap<String, BusinessReport>();

		BusinessReportConfig config = m_configManager.queryConfigByDomain(domain);
		Map<String, BusinessItemConfig> itemConfigs = config.getBusinessItemConfigs();

		BaselineConfig baselineConfig = m_baselineConfigManager.queryBaseLineConfig(domain);
		List<Integer> days = baselineConfig.getDays();
		Date targetDate = new Date(period.getTime() + baselineConfig.getTargetDate() * TimeHelper.ONE_DAY);

		for (BusinessItemConfig itemConfig : itemConfigs.values()) {
			String itemId = itemConfig.getId();

			for (MetricType type : MetricType.values()) {
				List<double[]> values = new ArrayList<double[]>();

				for (Integer day : days) {
					Date date = new Date(period.getTime() + day * TimeHelper.ONE_DAY);
					List<BusinessItem> businessItems = buildOneDayBusinessItems(domain, itemId, date, reports);

					double[] oneDayValue = m_parser.buildDailyData(businessItems, type);
					values.add(oneDayValue);
				}

				String key = m_keyHelper.generateKey(itemId, domain, type.getName());

				double[] result = m_baselineCreator.createBaseLine(values, baselineConfig.getWeights(), POINT_NUMBER);
				storeBaseLine(name, key, targetDate, result);

				Date tomorrow = new Date(period.getTime() + TimeHelper.ONE_DAY);
				boolean exist = m_baselineService.hasDailyBaseline(name, key, tomorrow);

				if (!exist) {
					storeBaseLine(name, key, tomorrow, result);
				}
			}
		}
		return true;
	}

	private List<BusinessItem> buildOneDayBusinessItems(String domain, String itemId, Date date,
							Map<String, BusinessReport> reports) {
		List<BusinessItem> items = new ArrayList<BusinessItem>();

		for (int i = 0; i < 24; i++) {
			Date start = new Date(date.getTime() + i * TimeHelper.ONE_HOUR);
			Date end = new Date(start.getTime() + TimeHelper.ONE_HOUR);
			String reportKey = itemId + start.getTime();
			BusinessReport report = reports.get(reportKey);

			if (report == null) {
				report = m_reportService.queryReport(domain, start, end);
				reports.put(reportKey, report);
			}

			BusinessItem item = report.findBusinessItem(itemId);

			if (item == null) {
				item = new BusinessItem(itemId);
			}

			items.add(item);
		}
		return items;
	}

	private void storeBaseLine(String name, String key, Date targetDate, double[] result) {
		Baseline baseline = new Baseline();
		baseline.setDataInDoubleArray(result);
		baseline.setIndexKey(key);
		baseline.setReportName(name);
		baseline.setReportPeriod(targetDate);
		m_baselineService.insertBaseline(baseline);
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		throw new RuntimeException("Business base line report don't support hourly report!");
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		throw new RuntimeException("Business base line report don't support monthly report!");
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		throw new RuntimeException("Business base line report don't support weekly report!");
	}

}
