package com.dianping.cat.report.task.metric;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Threads.Task;
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
import com.dianping.cat.home.monitorrules.entity.Subcondition;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.baseline.BaselineService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.system.config.MetricRuleConfigManager;
import com.dianping.cat.system.tool.MailSMS;

public class MetricAlert implements Task, LogEnabled {

	@Inject
	private MetricConfigManager m_metricConfigManager;

	@Inject
	private ProductLineConfigManager m_productLineConfigManager;

	@Inject
	private MetricRuleConfigManager m_metricRuleConfigManager;

	@Inject
	private BaselineService m_baselineService;

	@Inject
	private MailSMS m_mailSms;

	@Inject
	private RemoteMetricReportService m_service;

	@Inject
	private AlertConfig m_alertConfig;

	@Inject
	private AlertInfo m_alertInfo;

	private static final long DURATION = TimeUtil.ONE_MINUTE;

	private static final int DATA_CHECK_MINUTE = 3;

	private static final int DATA_AREADY_MINUTE = 1;

	private Map<String, MetricReport> m_currentReports = new HashMap<String, MetricReport>();

	private Map<String, MetricReport> m_lastReports = new HashMap<String, MetricReport>();

	private Logger m_logger;

	private void addDescMetricIfNotExist(MetricItemConfig config, List<Config> configs) {
		double descPer = config.getDecreasePercentage();
		double descVal = config.getDecreaseValue();
		boolean isDescPerExist = false;
		boolean isDescValExist = false;
		String dayBeginTime = "00:00";
		String dayEndTime = "24:00";

		if (descPer == 0) {
			descPer = 50;
		}
		if (descVal == 0) {
			descVal = 100;
		}

		for (Config con : configs) {
			String startTime = con.getStarttime();
			String endTime = con.getEndtime();

			if (startTime == null || !startTime.equals(dayBeginTime) || endTime == null || !endTime.equals(dayEndTime)) {
				continue;
			}

			for (Condition c : con.getConditions()) {
				List<Subcondition> subCons = c.getSubconditions();

				if (subCons.size() != 2) {
					continue;
				}

				for (Subcondition sub : subCons) {
					RuleType type = RuleType.getByTypeId(sub.getType());

					switch (type) {
					case DecreasePercentage:
						isDescPerExist = true;
						break;
					case DecreaseValue:
						isDescValExist = true;
						break;
					default:
						break;
					}
				}

				if (isDescPerExist && isDescValExist) {
					break;
				} else {
					isDescPerExist = false;
					isDescValExist = false;
				}
			}
		}

		if (isDescPerExist && isDescValExist) {
			return;
		} else {
			addNewCondition(configs, "DescPer", descPer, "DescVal", descVal, dayBeginTime, dayEndTime);
		}

	}

	private void addNewCondition(List<Config> configs, String type, double val, String type2, double val2,
	      String dayBeginTime, String dayEndTime) {
		configs.add(new Config()
		      .setStarttime(dayBeginTime)
		      .setEndtime(dayEndTime)
		      .addCondition(
		            new Condition().setTitle("default rule")
		                  .addSubcondition(new Subcondition().setType(type).setText(String.valueOf(val)))
		                  .addSubcondition(new Subcondition().setType(type2).setText(String.valueOf(val2)))));
	}

	private Pair<Boolean, String> checkDataByJudge(MetricItemConfig config, double[] value, double[] baseline,
	      MetricType type, List<Config> configs) {
		Pair<Boolean, String> originResult = m_alertConfig.checkData(config, value, baseline, type);

		addDescMetricIfNotExist(config, configs);

		Pair<Boolean, String> ruleJudgeResult = m_alertConfig.checkData(config, value, baseline, type, configs);

		if (originResult.getKey() != ruleJudgeResult.getKey()) {
			Cat.logError("rule execute error!", new Exception());
		}

		return originResult;
	}

	private Pair<Boolean, String> computeAlertInfo(int minute, String product, MetricItemConfig config, MetricType type) {
		double[] value = null;
		double[] baseline = null;
		String domain = config.getDomain();
		String key = config.getMetricKey();
		String metricKey = m_metricConfigManager.buildMetricKey(domain, config.getType(), key);
		List<Config> configs = m_metricRuleConfigManager.getConfigs(product, domain, key, metricKey);
		int maxMinute = getMaxMinute(configs);

		if (minute >= maxMinute - 1) {
			MetricReport report = fetchMetricReport(product, ModelPeriod.CURRENT);

			if (report != null) {
				int start = minute + 1 - maxMinute;
				int end = minute;

				value = queryRealData(start, end, metricKey, report, type);
				baseline = queryBaseLine(start, end, metricKey, new Date(ModelPeriod.CURRENT.getStartTime()), type);

				return checkDataByJudge(config, value, baseline, type, configs);
			}
		} else if (minute < 0) {
			MetricReport lastReport = fetchMetricReport(product, ModelPeriod.LAST);

			if (lastReport != null) {
				int start = 60 + minute + 1 - (maxMinute);
				int end = 60 + minute;

				value = queryRealData(start, end, metricKey, lastReport, type);
				baseline = queryBaseLine(start, end, metricKey, new Date(ModelPeriod.LAST.getStartTime()), type);
				return checkDataByJudge(config, value, baseline, type, configs);
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
				return checkDataByJudge(config, value, baseline, type, configs);
			}
		}
		return null;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	private MetricReport fetchMetricReport(String product, ModelPeriod period) {
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

	private int getMaxMinute(List<Config> configs) {
		int maxMinute = DATA_CHECK_MINUTE;

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

	@Override
	public String getName() {
		return "metric-alert";
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

	private void processMetricItemConfig(MetricItemConfig config, int minute, String product, ProductLine productLine) {
		if ((!config.getAlarm() && !config.isShowAvgDashboard() && !config.isShowSumDashboard() && !config
		      .isShowCountDashboard())) {
			return;
		}

		Pair<Boolean, String> alert = null;
		if (config.isShowAvg()) {
			alert = computeAlertInfo(minute, product, config, MetricType.AVG);
		}
		if (config.isShowCount()) {
			alert = computeAlertInfo(minute, product, config, MetricType.COUNT);
		}
		if (config.isShowSum()) {
			alert = computeAlertInfo(minute, product, config, MetricType.SUM);
		}

		if (alert != null && alert.getKey()) {
			config.setId(m_metricConfigManager.buildMetricKey(config.getDomain(), config.getType(), config.getMetricKey()));

			m_alertInfo.addMetric(config, new Date().getTime());
			sendAlertInfo(productLine, config, alert.getValue());
		}
	}

	private void processProductLine(ProductLine productLine) {
		List<String> domains = m_productLineConfigManager.queryDomainsByProductLine(productLine.getId());
		List<MetricItemConfig> configs = m_metricConfigManager.queryMetricItemConfigs(new HashSet<String>(domains));
		long current = (System.currentTimeMillis()) / 1000 / 60;
		int minute = (int) (current % (60)) - DATA_AREADY_MINUTE;
		String product = productLine.getId();

		for (MetricItemConfig config : configs) {
			processMetricItemConfig(config, minute, product, productLine);
		}
	}

	private double[] queryBaseLine(int start, int end, String baseLineKey, Date date, MetricType type) {
		double[] baseline = m_baselineService.queryHourlyBaseline(MetricAnalyzer.ID, baseLineKey + ":" + type, date);
		int length = end - start + 1;
		double[] result = new double[length];
		System.arraycopy(baseline, start, result, 0, length);

		return result;
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

	@Override
	public void run() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		boolean active = true;
		while (active) {
			int minute = Calendar.getInstance().get(Calendar.MINUTE);
			String minuteStr = String.valueOf(minute);

			if (minute < 10) {
				minuteStr = '0' + minuteStr;
			}
			Transaction t = Cat.newTransaction("MetricAlert", "M" + minuteStr);
			long current = System.currentTimeMillis();

			m_currentReports.clear();
			m_lastReports.clear();
			try {
				Map<String, ProductLine> productLines = m_productLineConfigManager.getCompany().getProductLines();

				for (ProductLine productLine : productLines.values()) {
					try {
						processProductLine(productLine);
					} catch (Exception e) {
						Cat.logError(e);
					}
				}

				t.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				t.setStatus(e);
			} finally {
				t.complete();
			}
			long duration = System.currentTimeMillis() - current;

			try {
				if (duration < DURATION) {
					Thread.sleep(DURATION - duration);
				}
			} catch (InterruptedException e) {
				active = false;
			}
		}
	}

	private void sendAlertInfo(ProductLine productLine, MetricItemConfig config, String content) {
		List<String> emails = m_alertConfig.buildMailReceivers(productLine);
		List<String> phones = m_alertConfig.buildSMSReceivers(productLine);
		String title = m_alertConfig.buildMailTitle(productLine, config);

		m_logger.info(title + " " + content + " " + emails);
		m_mailSms.sendEmail(title, content, emails);
		m_mailSms.sendSms(title + " " + content, content, phones);

		Cat.logEvent("MetricAlert", productLine.getId(), Event.SUCCESS, title + "  " + content);
	}

	@Override
	public void shutdown() {
	}

}