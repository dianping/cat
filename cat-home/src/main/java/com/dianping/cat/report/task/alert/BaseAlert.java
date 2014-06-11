package com.dianping.cat.report.task.alert;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.consumer.metric.MetricConfigManager;
import com.dianping.cat.consumer.metric.ProductLineConfigManager;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Segment;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.rule.entity.Condition;
import com.dianping.cat.home.rule.entity.Config;
import com.dianping.cat.report.baseline.BaselineService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.system.config.BaseRuleConfigManager;
import com.dianping.cat.system.tool.MailSMS;

public abstract class BaseAlert {

	@Inject
	protected BaseRuleConfigManager m_metricRuleConfigManager;

	@Inject
	protected MailSMS m_mailSms;

	@Inject
	protected AlertInfo m_alertInfo;

	@Inject
	private DataChecker m_dataChecker;

	@Inject
	protected MetricConfigManager m_metricConfigManager;

	@Inject
	protected ProductLineConfigManager m_productLineConfigManager;

	@Inject
	protected BaselineService m_baselineService;

	@Inject
	protected RemoteMetricReportService m_service;

	protected static final int DATA_AREADY_MINUTE = 1;

	protected static final long DURATION = TimeUtil.ONE_MINUTE;

	protected Logger m_logger;

	protected Map<String, MetricReport> m_currentReports = new HashMap<String, MetricReport>();

	protected Map<String, MetricReport> m_lastReports = new HashMap<String, MetricReport>();

	protected Pair<Boolean, String> computeAlertInfo(int minute, String product, String metricKey, MetricType type) {
		double[] value = null;
		double[] baseline = null;
		List<Config> configs = m_metricRuleConfigManager.queryConfigs(metricKey, type);
		int maxMinute = queryCheckMinute(configs);

		if (minute >= maxMinute - 1) {
			MetricReport report = fetchMetricReport(product, ModelPeriod.CURRENT);

			if (report != null) {
				int start = minute + 1 - maxMinute;
				int end = minute;

				value = queryRealData(start, end, metricKey, report, type);
				baseline = queryBaseLine(start, end, metricKey, new Date(ModelPeriod.CURRENT.getStartTime()), type);

				return m_dataChecker.checkData(value, baseline, configs);
			}
		} else if (minute < 0) {
			MetricReport lastReport = fetchMetricReport(product, ModelPeriod.LAST);

			if (lastReport != null) {
				int start = 60 + minute + 1 - (maxMinute);
				int end = 60 + minute;

				value = queryRealData(start, end, metricKey, lastReport, type);
				baseline = queryBaseLine(start, end, metricKey, new Date(ModelPeriod.LAST.getStartTime()), type);
				return m_dataChecker.checkData(value, baseline, configs);
			}
		} else {
			MetricReport currentReport = fetchMetricReport(product, ModelPeriod.CURRENT);
			MetricReport lastReport = fetchMetricReport(product, ModelPeriod.LAST);

			if (currentReport != null && lastReport != null) {
				int currentStart = 0, currentEnd = minute;
				double[] currentValue = queryRealData(currentStart, currentEnd, metricKey, currentReport, type);
				double[] currentBaseline = queryBaseLine(currentStart, currentEnd, metricKey,
				      new Date(ModelPeriod.CURRENT.getStartTime()), type);

				int lastStart = 60 + 1 - (maxMinute - minute);
				int lastEnd = 59;
				double[] lastValue = queryRealData(lastStart, lastEnd, metricKey, lastReport, type);
				double[] lastBaseline = queryBaseLine(lastStart, lastEnd, metricKey,
				      new Date(ModelPeriod.LAST.getStartTime()), type);

				value = mergerArray(lastValue, currentValue);
				baseline = mergerArray(lastBaseline, currentBaseline);
				return m_dataChecker.checkData(value, baseline, configs);
			}
		}
		return null;
	}

	protected MetricReport fetchMetricReport(String product, ModelPeriod period) {
		if (period == ModelPeriod.CURRENT) {
			MetricReport report = m_currentReports.get(product);

			if (report != null) {
				return report;
			} else {
				ModelRequest request = new ModelRequest(product, ModelPeriod.CURRENT.getStartTime()).setProperty(
				      "requireAll", "ture");

				report = m_service.invoke(request);
				if (report != null) {
					m_currentReports.put(product, report);
				}
				return report;
			}
		} else if (period == ModelPeriod.LAST) {
			MetricReport report = m_lastReports.get(product);

			if (report != null) {
				return report;
			} else {
				ModelRequest request = new ModelRequest(product, ModelPeriod.LAST.getStartTime()).setProperty("requireAll",
				      "ture");

				report = m_service.invoke(request);
				if (report != null) {
					m_lastReports.put(product, report);
				}
				return report;
			}
		} else {
			throw new RuntimeException("internal error, this can't be reached.");
		}
	}

	private double[] mergerArray(double[] from, double[] to) {
		int fromLength = from.length;
		int toLength = to.length;
		double[] result = new double[fromLength + toLength];
		int index = 0;

		for (int i = 0; i < fromLength; i++) {
			result[i] = from[i];
			index++;
		}
		for (int i = 0; i < toLength; i++) {
			result[i + index] = to[i];
		}
		return result;
	}

	protected boolean needAlert(MetricItemConfig config) {
		return true;
	}

	private void processMetricItemConfig(MetricItemConfig config, int minute, ProductLine productLine) {
		if (needAlert(config)) {
			String product = productLine.getId();
			String metricKey = m_metricConfigManager.buildMetricKey(config.getDomain(), config.getType(), config.getMetricKey());

			Pair<Boolean, String> alert = null;
			if (config.isShowAvg()) {
				alert = computeAlertInfo(minute, product, metricKey, MetricType.AVG);
			}
			if (config.isShowCount()) {
				alert = computeAlertInfo(minute, product, metricKey, MetricType.COUNT);
			}
			if (config.isShowSum()) {
				alert = computeAlertInfo(minute, product, metricKey, MetricType.SUM);
			}

			if (alert != null && alert.getKey()) {
				m_alertInfo.addAlertInfo(config, new Date().getTime());

				sendAlertInfo(productLine, config.getTitle(), alert.getValue());
			}
		}
	}

	protected void processProductLine(ProductLine productLine) {
		List<String> domains = m_productLineConfigManager.queryDomainsByProductLine(productLine.getId());
		List<MetricItemConfig> configs = m_metricConfigManager.queryMetricItemConfigs(domains);
		long current = (System.currentTimeMillis()) / 1000 / 60;
		int minute = (int) (current % (60)) - DATA_AREADY_MINUTE;

		for (MetricItemConfig config : configs) {
			try {
				processMetricItemConfig(config, minute, productLine);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
	}

	private double[] queryBaseLine(int start, int end, String baseLineKey, Date date, MetricType type) {
		double[] baseline = m_baselineService.queryHourlyBaseline(MetricAnalyzer.ID, baseLineKey + ":" + type, date);
		int length = end - start + 1;
		double[] result = new double[length];
		System.arraycopy(baseline, start, result, 0, length);

		return result;
	}

	private int queryCheckMinute(List<Config> configs) {
		int maxMinute = 0;

		for (Config config : configs) {
			for (Condition con : config.getConditions()) {
				int tmpMinute = con.getMinute();

				if (tmpMinute > maxMinute) {
					maxMinute = tmpMinute;
				}
			}
		}
		return maxMinute;
	}

	private double[] queryRealData(int start, int end, String metricKey, MetricReport report, MetricType type) {
		double[] all = new double[60];
		Map<Integer, Segment> map = report.findOrCreateMetricItem(metricKey).getSegments();

		for (Entry<Integer, Segment> entry : map.entrySet()) {
			Integer minute = entry.getKey();
			Segment seg = entry.getValue();

			if (type == MetricType.AVG) {
				all[minute] = seg.getAvg();
			} else if (type == MetricType.COUNT) {
				all[minute] = (double) seg.getCount();
			} else if (type == MetricType.SUM) {
				all[minute] = seg.getSum();
			}
		}
		int length = end - start + 1;
		double[] result = new double[length];
		System.arraycopy(all, start, result, 0, length);

		return result;
	}

	protected abstract void sendAlertInfo(ProductLine productLine, String metricTitle, String content);

}
