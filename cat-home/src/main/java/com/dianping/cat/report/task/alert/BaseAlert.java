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
import com.dianping.cat.home.monitorrules.entity.Condition;
import com.dianping.cat.home.monitorrules.entity.Config;
import com.dianping.cat.report.baseline.BaselineService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.system.config.BaseMetricRuleConfigManager;
import com.dianping.cat.system.tool.MailSMS;

public abstract class BaseAlert {

	@Inject
	private BaseMetricRuleConfigManager m_metricRuleConfigManager;

	@Inject
	protected MailSMS m_mailSms;

	@Inject
	protected AlertInfo m_alertInfo;

	@Inject
	private DataChecker m_dataChecker;

	protected static final long DURATION = TimeUtil.ONE_MINUTE;

	protected Logger m_logger;
	
	@Inject
	protected MetricConfigManager m_metricConfigManager;

	@Inject
	protected ProductLineConfigManager m_productLineConfigManager;

	@Inject
	protected BaselineService m_baselineService;

	@Inject
	protected RemoteMetricReportService m_service;

	protected static final int DATA_AREADY_MINUTE = 1;

	protected Map<String, MetricReport> m_currentReports = new HashMap<String, MetricReport>();

	protected Map<String, MetricReport> m_lastReports = new HashMap<String, MetricReport>();
	
	private Pair<Boolean, String> checkDataByAllTypes(int currentStart, int currentEnd, int lastStart, int lastEnd,
	      String metricKey, MetricReport currentReport, MetricReport lastReport, Map<MetricType, List<Config>> configMap) {
		for (Entry<MetricType, List<Config>> entry : configMap.entrySet()) {
			MetricType type = entry.getKey();
			List<Config> configs = entry.getValue();

			double[] currentValue = queryRealData(currentStart, currentEnd, metricKey, currentReport, type);
			double[] currentBaseline = queryBaseLine(currentStart, currentEnd, metricKey,
			      new Date(ModelPeriod.CURRENT.getStartTime()), type);
			double[] lastValue = queryRealData(lastStart, lastEnd, metricKey, lastReport, type);
			double[] lastBaseline = queryBaseLine(lastStart, lastEnd, metricKey,
			      new Date(ModelPeriod.LAST.getStartTime()), type);
			double[] value = mergerArray(lastValue, currentValue);
			double[] baseline = mergerArray(lastBaseline, currentBaseline);

			Pair<Boolean, String> tmpResult = m_dataChecker.checkData(value, baseline, configs);

			if (tmpResult.getKey()) {
				return tmpResult;
			}
		}

		return new Pair<Boolean, String>(false, "");
	}

	private Pair<Boolean, String> checkDataByAllTypes(int start, int end, String metricKey, MetricReport report,
	      ModelPeriod period, Map<MetricType, List<Config>> configMap) {
		for (Entry<MetricType, List<Config>> entry : configMap.entrySet()) {
			MetricType type = entry.getKey();
			List<Config> configs = entry.getValue();

			double[] value = queryRealData(start, end, metricKey, report, type);
			double[] baseline = queryBaseLine(start, end, metricKey, new Date(period.getStartTime()), type);
			Pair<Boolean, String> tmpResult = m_dataChecker.checkData(value, baseline, configs);

			if (tmpResult.getKey()) {
				return tmpResult;
			}
		}

		return new Pair<Boolean, String>(false, "");
	}

	private Pair<Boolean, String> computeAlertInfo(int minute, String product, MetricItemConfig config) {
		String domain = config.getDomain();
		String key = config.getMetricKey();
		String metricKey = m_metricConfigManager.buildMetricKey(domain, config.getType(), key);
		Map<MetricType, List<Config>> configMap = m_metricRuleConfigManager.queryConfigs(product, metricKey);
		int maxMinute = queryCheckMinute(configMap);

		if (minute >= maxMinute - 1) {
			MetricReport report = fetchMetricReport(product, ModelPeriod.CURRENT);

			if (report != null) {
				int start = minute + 1 - maxMinute;
				int end = minute;

				return checkDataByAllTypes(start, end, metricKey, report, ModelPeriod.CURRENT, configMap);
			}
		} else if (minute < 0) {
			MetricReport lastReport = fetchMetricReport(product, ModelPeriod.LAST);

			if (lastReport != null) {
				int start = 60 + minute + 1 - (maxMinute);
				int end = 60 + minute;

				return checkDataByAllTypes(start, end, metricKey, lastReport, ModelPeriod.LAST, configMap);
			}
		} else {
			MetricReport currentReport = fetchMetricReport(product, ModelPeriod.CURRENT);
			MetricReport lastReport = fetchMetricReport(product, ModelPeriod.LAST);

			if (currentReport != null && lastReport != null) {
				int currentStart = 0, currentEnd = minute;
				int lastStart = 60 + 1 - (maxMinute - minute);
				int lastEnd = 59;

				return checkDataByAllTypes(currentStart, currentEnd, lastStart, lastEnd, metricKey, currentReport,
				      lastReport, configMap);
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
	
	protected double[] mergerArray(double[] from, double[] to) {
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
	
	protected void processMetricItemConfig(MetricItemConfig config, int minute, ProductLine productLine) {
		if (!config.getAlarm()) {
			return;
		}
		
		String product = productLine.getId();
		Pair<Boolean, String> alert = computeAlertInfo(minute, product, config);

		if (alert != null && alert.getKey()) {
			m_alertInfo.addAlertInfo(config, new Date().getTime());
			sendAlertInfo(productLine, config, alert.getValue());
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
	
	protected double[] queryBaseLine(int start, int end, String baseLineKey, Date date, MetricType type) {
		double[] baseline = m_baselineService.queryHourlyBaseline(MetricAnalyzer.ID, baseLineKey + ":" + type, date);
		int length = end - start + 1;
		double[] result = new double[length];
		System.arraycopy(baseline, start, result, 0, length);

		return result;
	}

	private int queryCheckMinute(Map<MetricType, List<Config>> configsMap) {
		int maxMinute = 0;

		for (Entry<MetricType, List<Config>> entry : configsMap.entrySet()) {
			List<Config> configs = entry.getValue();

			for (Config config : configs) {
				for (Condition con : config.getConditions()) {
					int tmpMinute = con.getMinute();

					if (tmpMinute > maxMinute) {
						maxMinute = tmpMinute;
					}
				}
			}
		}

		return maxMinute;
	}

	protected double[] queryRealData(int start, int end, String metricKey, MetricReport report, MetricType type) {
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

	protected abstract void sendAlertInfo(ProductLine productLine, MetricItemConfig config, String content);

}
