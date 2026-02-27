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
package com.dianping.cat.report.page.business.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.spi.AlertEntity;
import com.dianping.cat.config.business.BusinessConfigManager;
import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.configuration.business.entity.BusinessReportConfig;
import com.dianping.cat.configuration.business.entity.CustomConfig;
import com.dianping.cat.consumer.business.BusinessAnalyzer;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.helper.Chinese;
import com.dianping.cat.helper.MetricType;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.business.entity.BusinessItem;
import com.dianping.cat.home.business.entity.Tag;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.metric.AbstractGraphCreator;
import com.dianping.cat.report.page.business.service.CachedBusinessReportService;
import com.dianping.cat.report.page.business.task.BusinessKeyHelper;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.page.business.config.BusinessTagConfigManager;

public class BusinessGraphCreator extends AbstractGraphCreator {

	@Inject
	private CachedBusinessReportService m_reportService;

	@Inject
	private BusinessConfigManager m_configManager;

	@Inject
	private BusinessDataFetcher m_dataFetcher;

	@Inject
	private ProjectService m_projectService;

	@Inject
	private BusinessTagConfigManager m_tagManager;

	@Inject
	private BusinessKeyHelper m_keyHelper;

	@Inject
	private CustomDataCalculator m_customDataCalculator;

	private Pair<String, Boolean> buildTitleAndPrivilege(BusinessReportConfig businessReportConfig, String itemId,
														 String type) {
		boolean isPrivilege = false;
		String title = null;
		String des = MetricType.getDesByName(type);
		BusinessItemConfig config = businessReportConfig.findBusinessItemConfig(itemId);

		if (config != null) {
			title = config.getTitle() + des;
			isPrivilege = config.isPrivilege();
		} else {
			CustomConfig customConfig = businessReportConfig.findCustomConfig(itemId);

			if (customConfig != null) {
				title = customConfig.getTitle() + des;
				isPrivilege = customConfig.isPrivilege();
			}
		}

		return new Pair<String, Boolean>(title, isPrivilege);
	}

	private Map<String, LineChart> buildCharts(final Map<String, double[]> datas, Map<String, double[]> baseLines,
							Date start, Date end, Map<String, BusinessReportConfig> configs) {
		Map<String, double[]> allCurrentValues = m_dataExtractor.extract(datas);
		Map<String, double[]> dataWithOutFutures = removeFutureData(end, allCurrentValues);

		Map<String, LineChart> charts = new LinkedHashMap<String, LineChart>();
		List<AlertEntity> alertKeys = m_alertManager.queryLastestAlarmKey(5);
		int step = m_dataExtractor.getStep();

		for (Entry<String, double[]> entry : dataWithOutFutures.entrySet()) {
			try {
				String key = entry.getKey();
				double[] value = entry.getValue();
				String domain = m_keyHelper.getDomain(key);
				BusinessReportConfig config = configs.get(domain);
				LineChart lineChart = new LineChart();

				buildLineChartTitle(alertKeys, lineChart, key, config);
				lineChart.setStart(start);
				lineChart.setSize(value.length);
				lineChart.setStep(step * TimeHelper.ONE_MINUTE);

				double[] baselines = baseLines.get(key);
				Map<Long, Double> all = convertToMap(datas.get(key), start, 1);
				Map<Long, Double> current = convertToMap(dataWithOutFutures.get(key), start, step);

				addLastMinuteData(current, all, m_lastMinute, end);
				lineChart.add(Chinese.CURRENT_VALUE, current);
				lineChart.add(Chinese.BASELINE_VALUE, convertToMap(m_dataExtractor.extract(baselines), start, step));
				charts.put(key, lineChart);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return charts;
	}

	protected String buildContactInfo(String domainName) {
		try {
			Project project = m_projectService.findByDomain(domainName);

			if (project != null) {
				String owners = project.getOwner();
				String phones = project.getPhone();
				StringBuilder builder = new StringBuilder();

				builder.append("[项目: ").append(domainName);
				if (!StringUtils.isEmpty(owners)) {
					builder.append(" 负责人: ").append(owners);
				}
				if (!StringUtils.isEmpty(phones)) {
					builder.append(" 手机: ").append(phones).append(" ]");
				}
				return builder.toString();
			}
		} catch (Exception ex) {
			Cat.logError("build contact info error for doamin: " + domainName, ex);
		}
		return null;
	}

	public Map<String, LineChart> buildGraphByDomain(Date start, Date end, String domain) {
		BusinessReportConfig config = m_configManager.queryConfigByDomain(domain);
		HashMap<String, LineChart> result = new LinkedHashMap<String, LineChart>();

		if (config != null) {
			Map<String, double[]> datas = prepareBusinessItemDatas(start, end, domain, config);
			Map<String, double[]> baseLines = prepareBusinessItemBaseLines(start, end, datas.keySet());

			Map<String, CustomConfig> customConfigs = config.getCustomConfigs();
			Map<String, double[]> customBaseLines = prepareCustomBaseLines(start, end, domain, customConfigs);
			Map<String, double[]> customDatas = prepareCustomDatas(start, end, domain, customConfigs, datas);

			Map<String, BusinessReportConfig> configs = new HashMap<String, BusinessReportConfig>();
			configs.put(domain, config);

			result.putAll(buildCharts(datas, baseLines, start, end, configs));
			result.putAll(buildCharts(customDatas, customBaseLines, start, end, configs));
		}

		return result;
	}

	public Map<String, LineChart> buildGraphByTag(Date start, Date end, String tag) {
		Tag tagConfig = m_tagManager.findTag(tag);

		if (tagConfig != null) {
			List<BusinessItem> items = tagConfig.getBusinessItems();
			Map<String, double[]> all = new LinkedHashMap<String, double[]>();
			Map<String, double[]> needed = new LinkedHashMap<String, double[]>();
			Map<String, double[]> baseLines = new HashMap<String, double[]>();
			Map<String, BusinessReportConfig> configs = new HashMap<String, BusinessReportConfig>();
			Map<String, Map<String, CustomConfig>> customConfigs = new HashMap<String, Map<String, CustomConfig>>();
			Map<String, Set<String>> businessItemConfigs = new HashMap<String, Set<String>>();

			buildTagConfigs(items, configs, customConfigs, businessItemConfigs);

			for (Entry<String, Set<String>> businessItemConfigItem : businessItemConfigs.entrySet()) {
				String domain = businessItemConfigItem.getKey();
				BusinessReportConfig config = configs.get(domain);
				Map<String, double[]> datas = prepareBusinessItemDatas(start, end, domain, config);
				all.putAll(datas);

				for (String key : businessItemConfigItem.getValue()) {
					for (MetricType metricType : MetricType.values()) {
						String id = m_keyHelper.generateKey(key, domain, metricType.getName());
						double[] baseline = queryBaseline(BusinessAnalyzer.ID, id, start, end);
						baseLines.put(id, baseline);
						double[] data = all.get(id);

						if (data != null) {
							needed.put(id, data);
						}
					}
				}
			}

			for (Entry<String, Map<String, CustomConfig>> customConfigItem : customConfigs.entrySet()) {
				String domain = customConfigItem.getKey();
				Map<String, CustomConfig> value = customConfigItem.getValue();
				Map<String, double[]> customDatas = prepareCustomDatas(start, end, domain, value, all);
				Map<String, double[]> customBaseLines = prepareCustomBaseLines(start, end, domain, value);
				needed.putAll(customDatas);
				baseLines.putAll(customBaseLines);
			}

			return buildCharts(needed, baseLines, start, end, configs);
		} else {
			return new HashMap<String, LineChart>();
		}
	}

	private void buildTagConfigs(List<BusinessItem> items, Map<String, BusinessReportConfig> configs,
							Map<String, Map<String, CustomConfig>> customConfigs, Map<String, Set<String>> businessItemConfigs) {
		for (BusinessItem item : items) {
			String domain = item.getDomain();
			String itemId = item.getItemId();
			BusinessReportConfig config = configs.get(domain);
			Map<String, CustomConfig> customConfig = customConfigs.get(domain);
			Set<String> businessItemConfig = businessItemConfigs.get(domain);

			if (config == null) {
				config = m_configManager.queryConfigByDomain(domain);

				if (config != null) {
					configs.put(domain, config);
				}
				customConfig = new HashMap<String, CustomConfig>();
				businessItemConfig = new HashSet<String>();

				customConfigs.put(domain, customConfig);
				businessItemConfigs.put(domain, businessItemConfig);
			}

			BusinessItemConfig businessItem = config.findBusinessItemConfig(itemId);

			if (businessItem == null) {
				CustomConfig customItem = config.findCustomConfig(itemId);

				if (customItem != null) {
					customConfig.put(itemId, customItem);
				}
			} else {
				businessItemConfig.add(itemId);
			}
		}
	}

	private Map<String, double[]> buildGraphData(BusinessReport report, BusinessReportConfig config) {
		Map<String, double[]> values = new LinkedHashMap<String, double[]>();
		Map<String, double[]> datas = m_dataFetcher.buildGraphData(report);
		Map<String, BusinessItemConfig> businessItemConfigs = config.getBusinessItemConfigs();
		List<BusinessItemConfig> items = new ArrayList<BusinessItemConfig>(businessItemConfigs.values());

		Collections.sort(items, new Comparator<BusinessItemConfig>() {

			@Override
			public int compare(BusinessItemConfig m1, BusinessItemConfig m2) {
				return (int) ((m1.getViewOrder() - m2.getViewOrder()) * 100);
			}
		});

		for (BusinessItemConfig itemConfig : items) {
			String key = itemConfig.getId();

			if (itemConfig.getShowAvg()) {
				String avgKey = m_keyHelper.generateKey(key, report.getDomain(), MetricType.AVG.name());
				putKey(datas, values, avgKey);
			}
			if (itemConfig.getShowCount()) {
				String countKey = m_keyHelper.generateKey(key, report.getDomain(), MetricType.COUNT.name());
				putKey(datas, values, countKey);
			}
			if (itemConfig.getShowSum()) {
				String sumKey = m_keyHelper.generateKey(key, report.getDomain(), MetricType.SUM.name());
				putKey(datas, values, sumKey);
			}
		}

		return values;
	}

	private void buildLineChartTitle(List<AlertEntity> alertKeys, LineChart chart, String key,
							BusinessReportConfig businessReportConfig) {
		String domain = businessReportConfig.getId();
		String itemId = m_keyHelper.getBusinessItemId(key);
		String type = m_keyHelper.getType(key);
		Pair<String, Boolean> titleAndPrivilege = buildTitleAndPrivilege(businessReportConfig, itemId, type);
		String title = titleAndPrivilege.getKey();

		chart.setTitle(title);
		chart.setId(key);

		if (titleAndPrivilege.getValue()) {
			chart.setyEnabled(false);
		}

		if (containMetric(alertKeys, itemId, domain)) {
			String contactInfo = buildContactInfo(domain);

			chart.setHtmlTitle("<span style='color:red'>" + title + "<br><small>" + contactInfo + "</small></span>");
		} else {
			chart.setHtmlTitle(title);
		}
	}

	private boolean containMetric(List<AlertEntity> alertKeys, String metricId, String domain) {
		for (AlertEntity alertMetric : alertKeys) {
			if (alertMetric.getDomain().equals(domain) && alertMetric.getMetric().equals(metricId)) {
				return true;
			}
		}
		return false;
	}

	private Map<String, double[]> prepareBusinessItemBaseLines(Date start, Date end, Set<String> keys) {
		Map<String, double[]> baselines = new HashMap<String, double[]>();

		for (String key : keys) {
			double[] baseline = queryBaseline(BusinessAnalyzer.ID, key, start, end);
			baselines.put(key, baseline);
		}
		return baselines;
	}

	private Map<String, double[]> prepareBusinessItemDatas(Date startDate, Date endDate, String domain,
							BusinessReportConfig config) {
		long start = startDate.getTime(), end = endDate.getTime();
		int totalSize = (int) ((end - start) / TimeHelper.ONE_MINUTE);
		Map<String, double[]> oldCurrentValues = new LinkedHashMap<String, double[]>();
		int index = 0;

		for (; start < end; start += TimeHelper.ONE_HOUR) {
			BusinessReport report = m_reportService.queryBusinessReport(domain, new Date(start));
			Map<String, double[]> currentValues = buildGraphData(report, config);

			mergeMap(oldCurrentValues, currentValues, totalSize, index);
			index++;
		}
		return oldCurrentValues;
	}

	private Map<String, double[]> prepareCustomBaseLines(Date start, Date end, String currentDomain,
							Map<String, CustomConfig> customConfigs) {
		Map<String, double[]> baseLineCache = new HashMap<String, double[]>();
		Map<String, double[]> customBaseLines = new LinkedHashMap<String, double[]>();
		int totalSize = (int) ((end.getTime() - start.getTime()) / TimeHelper.ONE_MINUTE);

		for (CustomConfig customConfig : customConfigs.values()) {
			try {
				String pattern = customConfig.getPattern();

				List<CustomInfo> customInfos = m_customDataCalculator.translatePattern(pattern);

				for (CustomInfo customInfo : customInfos) {
					String customKey = m_keyHelper.generateKey(customInfo.getKey(), customInfo.getDomain(),	customInfo.getType());
					baseLineCache.put(customKey, queryBaseline(BusinessAnalyzer.ID, customKey, start, end));
				}
				double[] baseLine = m_customDataCalculator.calculate(pattern, customInfos, baseLineCache, totalSize);
				String key = m_keyHelper.generateKey(customConfig.getId(), currentDomain, MetricType.AVG.getName());

				customBaseLines.put(key, baseLine);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return customBaseLines;
	}

	private Map<String, double[]> prepareCustomDatas(Date start, Date end, String currentDomain,
							Map<String, CustomConfig> customConfigs, Map<String, double[]> datas) {
		Map<String, double[]> customDatas = new LinkedHashMap<String, double[]>();
		Map<String, double[]> businessItemDataCache = new HashMap<String, double[]>();
		Set<String> domains = new HashSet<String>();
		int totalSize = (int) ((end.getTime() - start.getTime()) / TimeHelper.ONE_MINUTE);

		domains.add(currentDomain);
		businessItemDataCache.putAll(datas);

		for (CustomConfig customConfig : customConfigs.values()) {
			try {
				String pattern = customConfig.getPattern();
				List<CustomInfo> customInfos = m_customDataCalculator.translatePattern(pattern);

				for (CustomInfo customInfo : customInfos) {
					String domain = customInfo.getDomain();

					if (!domains.contains(domain)) {
						BusinessReportConfig config = m_configManager.queryConfigByDomain(domain);

						domains.add(domain);
						businessItemDataCache.putAll(prepareBusinessItemDatas(start, end, domain, config));
					}
				}
				double[] data = m_customDataCalculator.calculate(pattern, customInfos, businessItemDataCache, totalSize);
				String key = m_keyHelper.generateKey(customConfig.getId(), currentDomain, MetricType.AVG.getName());

				customDatas.put(key, data);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return customDatas;
	}
}
