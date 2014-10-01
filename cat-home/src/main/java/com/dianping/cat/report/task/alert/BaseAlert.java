package com.dianping.cat.report.task.alert;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
import com.dianping.cat.consumer.metric.ProductLineConfigManager;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Segment;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.rule.entity.Condition;
import com.dianping.cat.home.rule.entity.Config;
import com.dianping.cat.report.baseline.BaselineService;
import com.dianping.cat.report.task.alert.sender.AlertEntity;
import com.dianping.cat.report.task.alert.sender.AlertManager;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.system.config.BaseRuleConfigManager;

public abstract class BaseAlert {

	@Inject
	protected BaseRuleConfigManager m_ruleConfigManager;

	@Inject
	protected AlertInfo m_alertInfo;

	@Inject
	protected DataChecker m_dataChecker;

	@Inject
	protected ProductLineConfigManager m_productLineConfigManager;

	@Inject
	protected BaselineService m_baselineService;

	@Inject
	protected RemoteMetricReportService m_service;

	@Inject
	protected AlertManager m_sendManager;

	protected static final int DATA_AREADY_MINUTE = 1;

	protected static final long DURATION = TimeUtil.ONE_MINUTE;

	protected Logger m_logger;

	protected Map<String, MetricReport> m_currentReports = new HashMap<String, MetricReport>();

	protected Map<String, MetricReport> m_lastReports = new HashMap<String, MetricReport>();

	protected String buildMetricName(String metricKey) {
		try {
			return metricKey.split(":")[2];
		} catch (Exception ex) {
			Cat.logError("get metric name error:" + metricKey, ex);
			return null;
		}
	}

	protected String extractDomain(String metricKey) {
		try {
			return metricKey.split(":")[0];
		} catch (Exception ex) {
			Cat.logError("extract domain error:" + metricKey, ex);
			return null;
		}
	}

	protected List<AlertResultEntity> computeAlertInfo(int minute, String product, String metricKey, MetricType type) {
		double[] value = null;
		double[] baseline = null;
		List<Config> configs = m_ruleConfigManager.queryConfigs(product, metricKey, type);
		Pair<Integer, List<Condition>> resultPair = queryCheckMinuteAndConditions(configs);
		int maxMinute = resultPair.getKey();
		List<Condition> conditions = resultPair.getValue();

		if (minute >= maxMinute - 1) {
			MetricReport report = fetchMetricReport(product, ModelPeriod.CURRENT);

			if (report != null) {
				int start = minute + 1 - maxMinute;
				int end = minute;

				value = queryRealData(start, end, metricKey, report, type);
				baseline = queryBaseLine(start, end, metricKey, new Date(ModelPeriod.CURRENT.getStartTime()), type);

				return m_dataChecker.checkData(value, baseline, conditions);
			}
		} else if (minute < 0) {
			MetricReport lastReport = fetchMetricReport(product, ModelPeriod.LAST);

			if (lastReport != null) {
				int start = 60 + minute + 1 - (maxMinute);
				int end = 60 + minute;

				value = queryRealData(start, end, metricKey, lastReport, type);
				baseline = queryBaseLine(start, end, metricKey, new Date(ModelPeriod.LAST.getStartTime()), type);
				return m_dataChecker.checkData(value, baseline, conditions);
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
				return m_dataChecker.checkData(value, baseline, conditions);
			}
		}
		return new ArrayList<AlertResultEntity>();
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

	private boolean judgeCurrentInConfigRange(Config config) {
		try {
			if (compareTime(config.getStarttime(), config.getEndtime())) {
				return true;
			} else {
				return false;
			}
		} catch (Exception ex) {
			Cat.logError("throw exception when judge time: " + config.toString(), ex);
			return false;
		}
	}

	private boolean compareTime(String start, String end) {
		String[] startTime = start.split(":");
		int hourStart = Integer.parseInt(startTime[0]);
		int minuteStart = Integer.parseInt(startTime[1]);
		int startMinute = hourStart * 60 + minuteStart;

		String[] endTime = end.split(":");
		int hourEnd = Integer.parseInt(endTime[0]);
		int minuteEnd = Integer.parseInt(endTime[1]);
		int endMinute = hourEnd * 60 + minuteEnd;

		Calendar cal = Calendar.getInstance();
		int current = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);

		return current >= startMinute && current <= endMinute;
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

	protected boolean needAlert(MetricItemConfig config) {
		return true;
	}

	private void processMetricItem(int minute, ProductLine productLine, String metricKey) {
		for (MetricType type : MetricType.values()) {
			String productlineName = productLine.getId();
			List<AlertResultEntity> alertResults = computeAlertInfo(minute, productlineName, metricKey, type);

			for (AlertResultEntity alertResult : alertResults) {
				m_alertInfo.addAlertInfo(productlineName, metricKey, new Date().getTime());

				String metricName = buildMetricName(metricKey);
				AlertEntity entity = new AlertEntity();

				entity.setDate(alertResult.getAlertTime()).setContent(alertResult.getContent())
				      .setLevel(alertResult.getAlertLevel());
				entity.setMetric(metricName).setType(getName()).setGroup(productlineName);

				m_sendManager.addAlert(entity);
			}
		}
	}

	protected void processProductLine(ProductLine productLine) {
		int minute = getAlreadyMinute();
		String product = productLine.getId();
		MetricReport report = fetchMetricReport(product, ModelPeriod.CURRENT);

		if (report != null) {
			for (Entry<String, MetricItem> entry : report.getMetricItems().entrySet()) {
				try {
					processMetricItem(minute, productLine, entry.getKey());
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		}
	}

	protected int getAlreadyMinute() {
		long current = (System.currentTimeMillis()) / 1000 / 60;
		int minute = (int) (current % (60)) - DATA_AREADY_MINUTE;

		return minute;
	}

	private double[] queryBaseLine(int start, int end, String baseLineKey, Date date, MetricType type) {
		double[] baseline = m_baselineService.queryHourlyBaseline(MetricAnalyzer.ID, baseLineKey + ":" + type, date);
		int length = end - start + 1;
		double[] result = new double[length];

		if (baseline != null) {
			System.arraycopy(baseline, start, result, 0, length);
		}

		return result;
	}

	protected Pair<Integer, List<Condition>> queryCheckMinuteAndConditions(List<Config> configs) {
		int maxMinute = 0;
		List<Condition> conditions = new ArrayList<Condition>();
		Iterator<Config> iterator = configs.iterator();

		while (iterator.hasNext()) {
			Config config = iterator.next();

			if (judgeCurrentInConfigRange(config)) {
				List<Condition> tmpConditions = config.getConditions();
				conditions.addAll(tmpConditions);

				for (Condition con : tmpConditions) {
					int tmpMinute = con.getMinute();

					if (tmpMinute > maxMinute) {
						maxMinute = tmpMinute;
					}
				}
			}
		}

		return new Pair<Integer, List<Condition>>(maxMinute, conditions);
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

	protected abstract String getName();
}
