package com.dianping.cat.report.task.alert.business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.metric.MetricConfigManager;
import com.dianping.cat.consumer.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.metric.config.entity.Tag;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.productline.ProductLineConfig;
import com.dianping.cat.home.rule.entity.Condition;
import com.dianping.cat.home.rule.entity.Config;
import com.dianping.cat.report.task.alert.AlertResultEntity;
import com.dianping.cat.report.task.alert.AlertType;
import com.dianping.cat.report.task.alert.BaseAlert;
import com.dianping.cat.report.task.alert.MetricType;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.system.config.BaseRuleConfigManager;
import com.dianping.cat.system.config.BusinessRuleConfigManager;

public class BusinessAlert extends BaseAlert {

	public static final String ID = AlertType.Business.getName();

	@Inject
	protected MetricConfigManager m_metricConfigManager;

	@Inject
	protected BusinessRuleConfigManager m_ruleConfigManager;

	private Map<String, Map<MetricType, List<Config>>> buildMonitorConfigs(String productline,
	      List<MetricItemConfig> configs) {
		Map<String, Map<MetricType, List<Config>>> monitorConfigs = new HashMap<String, Map<MetricType, List<Config>>>();

		for (MetricItemConfig config : configs) {
			Map<MetricType, List<Config>> monitorConfigsByItem = new HashMap<MetricType, List<Config>>();
			String metricKey = config.getId();

			if (config.isShowAvg()) {
				List<Config> tmpConfigs = getRuleConfigManager().queryConfigs(productline, metricKey, MetricType.AVG);

				monitorConfigsByItem.put(MetricType.AVG, tmpConfigs);
			}
			if (config.isShowCount()) {
				List<Config> tmpConfigs = getRuleConfigManager().queryConfigs(productline, metricKey, MetricType.COUNT);

				monitorConfigsByItem.put(MetricType.COUNT, tmpConfigs);
			}
			if (config.isShowSum()) {
				List<Config> tmpConfigs = getRuleConfigManager().queryConfigs(productline, metricKey, MetricType.SUM);

				monitorConfigsByItem.put(MetricType.SUM, tmpConfigs);
			}
			monitorConfigs.put(metricKey, monitorConfigsByItem);
		}
		return monitorConfigs;
	}

	@Override
	public String getName() {
		return ID;
	}

	@Override
	protected Map<String, ProductLine> getProductlines() {
		return m_productLineConfigManager.queryMetricProductLines();
	}

	@Override
	protected BaseRuleConfigManager getRuleConfigManager() {
		return m_ruleConfigManager;
	}

	public boolean needAlert(MetricItemConfig config) {
		if (config.getAlarm()) {
			return true;
		}
		List<Tag> tags = config.getTags();

		for (Tag tag : tags) {
			if (MetricConfigManager.DEFAULT_TAG.equals(tag.getName())) {
				return true;
			}
		}
		return false;
	}

	private void processMetricItemConfig(MetricItemConfig config, int minute,
	      Map<MetricType, List<Config>> monitorConfigs, ProductLine productLine, MetricReport lastReport,
	      MetricReport currentReport) {
		if (needAlert(config)) {
			String product = productLine.getId();
			String domain = config.getDomain();
			String metric = config.getMetricKey();
			String metricKey = m_metricConfigManager.buildMetricKey(domain, config.getType(), metric);
			List<AlertResultEntity> results = new ArrayList<AlertResultEntity>();

			if (config.isShowAvg()) {
				List<AlertResultEntity> tmpResults = processMetricType(minute, monitorConfigs.get(MetricType.AVG),
				      lastReport, currentReport, metricKey);

				results.addAll(tmpResults);
			}
			if (config.isShowCount()) {
				List<AlertResultEntity> tmpResults = processMetricType(minute, monitorConfigs.get(MetricType.COUNT),
				      lastReport, currentReport, metricKey);

				results.addAll(tmpResults);
			}
			if (config.isShowSum()) {
				List<AlertResultEntity> tmpResults = processMetricType(minute, monitorConfigs.get(MetricType.SUM),
				      lastReport, currentReport, metricKey);

				results.addAll(tmpResults);
			}

			sendAlerts(product, metricKey, metric, results);
		}
	}

	protected List<AlertResultEntity> processMetricType(int minute, List<Config> configs, MetricReport lastReport,
	      MetricReport currentReport, String metricKey) {
		Pair<Integer, List<Condition>> resultPair = queryCheckMinuteAndConditions(configs);
		int maxMinute = resultPair.getKey();
		Pair<double[], double[]> datas = m_dataExtractor.extractData(minute, maxMinute, lastReport, currentReport,
		      metricKey, MetricType.AVG);

		double[] baseline = datas.getKey();
		double[] value = datas.getValue();
		List<Condition> conditions = resultPair.getValue();

		return m_dataChecker.checkData(value, baseline, conditions);
	}

	@Override
	protected void processProductLine(ProductLine productLine) {
		String productId = productLine.getId();
		List<String> domains = m_productLineConfigManager.queryDomainsByProductLine(productId,
		      ProductLineConfig.METRIC_PRODUCTLINE);
		List<MetricItemConfig> configs = m_metricConfigManager.queryMetricItemConfigs(domains);
		long current = (System.currentTimeMillis()) / 1000 / 60;
		int minute = (int) (current % (60)) - DATA_AREADY_MINUTE;
		Map<String, Map<MetricType, List<Config>>> monitorConfigs = buildMonitorConfigs(productId, configs);
		int maxMinute = calMaxMinute(monitorConfigs);
		MetricReport currentReport = null;
		MetricReport lastReport = null;
		boolean isDataReady = false;

		if (minute >= maxMinute - 1) {
			currentReport = fetchMetricReport(productId, ModelPeriod.CURRENT);

			if (currentReport != null) {
				isDataReady = true;
			}
		} else if (minute < 0) {
			lastReport = fetchMetricReport(productId, ModelPeriod.LAST);

			if (lastReport != null) {
				isDataReady = true;
			}
		} else {
			currentReport = fetchMetricReport(productId, ModelPeriod.CURRENT);
			lastReport = fetchMetricReport(productId, ModelPeriod.LAST);

			if (lastReport != null && currentReport != null) {
				isDataReady = true;
			}
		}

		if (isDataReady) {
			for (MetricItemConfig config : configs) {
				try {
					processMetricItemConfig(config, minute, monitorConfigs.get(config.getId()), productLine, lastReport,
					      currentReport);
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		}
	}

}