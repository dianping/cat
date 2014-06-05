package com.dianping.cat.report.task.alert.metric;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.monitorrules.entity.Condition;
import com.dianping.cat.home.monitorrules.entity.Config;
import com.dianping.cat.home.monitorrules.entity.Subcondition;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.task.alert.AlertInfo;
import com.dianping.cat.report.task.alert.BaseAlert;
import com.dianping.cat.report.task.alert.DataChecker;
import com.dianping.cat.report.task.alert.MetricType;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.system.tool.MailSMS;

public class MetricAlert extends BaseAlert implements Task, LogEnabled {

	@Inject
	private MailSMS m_mailSms;

	@Inject
	private MetricAlertConfig m_alertConfig;

	@Inject
	private AlertInfo m_alertInfo;
	
	@Inject
	private DataChecker m_dataChecker;

	private static final long DURATION = TimeUtil.ONE_MINUTE;

	private static final int DATA_CHECK_MINUTE = 3;

	private Logger m_logger;

	private Pair<Boolean, String> checkDataByJudge(MetricItemConfig config, double[] value, double[] baseline,
	      MetricType type) {
		Pair<Boolean, String> originResult = m_alertConfig.checkData(config, value, baseline, type);

		try {
			List<Config> configs = convert(config);
			Pair<Boolean, String> ruleJudgeResult = m_dataChecker.checkData(value, baseline,  configs);

			if (originResult.getKey() != ruleJudgeResult.getKey()) {
				String metricKey = m_metricConfigManager.buildMetricKey(config.getDomain(), config.getType(),
				      config.getMetricKey());

				m_logger.error(String.format("Error judge result, config: %s, value: %s, baseline: %s", metricKey,
				      printArray(value), printArray(baseline)));
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return originResult;
	}

	private Pair<Boolean, String> computeAlertInfo(int minute, String product, MetricItemConfig config, MetricType type) {
		double[] value = null;
		double[] baseline = null;
		String domain = config.getDomain();
		String key = config.getMetricKey();
		String metricKey = m_metricConfigManager.buildMetricKey(domain, config.getType(), key);

		if (minute >= DATA_CHECK_MINUTE - 1) {
			MetricReport report = fetchMetricReport(product, ModelPeriod.CURRENT);

			if (report != null) {
				int start = minute + 1 - DATA_CHECK_MINUTE;
				int end = minute;

				value = queryRealData(start, end, metricKey, report, type);
				baseline = queryBaseLine(start, end, metricKey, new Date(ModelPeriod.CURRENT.getStartTime()), type);

				return checkDataByJudge(config, value, baseline, type);
			}
		} else if (minute < 0) {
			MetricReport lastReport = fetchMetricReport(product, ModelPeriod.LAST);

			if (lastReport != null) {
				int start = 60 + minute + 1 - (DATA_CHECK_MINUTE);
				int end = 60 + minute;

				value = queryRealData(start, end, metricKey, lastReport, type);
				baseline = queryBaseLine(start, end, metricKey, new Date(ModelPeriod.LAST.getStartTime()), type);
				return checkDataByJudge(config, value, baseline, type);
			}
		} else {
			MetricReport currentReport = fetchMetricReport(product, ModelPeriod.CURRENT);
			MetricReport lastReport = fetchMetricReport(product, ModelPeriod.LAST);

			if (currentReport != null && lastReport != null) {
				int currentStart = 0, currentEnd = minute;
				double[] currentValue = queryRealData(currentStart, currentEnd, metricKey, currentReport, type);
				double[] currentBaseline = queryBaseLine(currentStart, currentEnd, metricKey,
				      new Date(ModelPeriod.CURRENT.getStartTime()), type);

				int lastStart = 60 + 1 - (DATA_CHECK_MINUTE - minute);
				int lastEnd = 59;
				double[] lastValue = queryRealData(lastStart, lastEnd, metricKey, lastReport, type);
				double[] lastBaseline = queryBaseLine(lastStart, lastEnd, metricKey,
				      new Date(ModelPeriod.LAST.getStartTime()), type);

				value = mergerArray(lastValue, currentValue);
				baseline = mergerArray(lastBaseline, currentBaseline);
				return checkDataByJudge(config, value, baseline, type);
			}
		}
		return null;
	}

	private List<Config> convert(MetricItemConfig metricItemConfig) {
		List<Config> configs = new ArrayList<Config>();
		Config config = new Config();
		Condition condition = new Condition();
		Subcondition descPerSubcon = new Subcondition();
		Subcondition descValSubcon = new Subcondition();

		double decreasePercent = metricItemConfig.getDecreasePercentage();
		double decreaseValue = metricItemConfig.getDecreaseValue();

		if (decreasePercent == 0) {
			decreasePercent = 50;
		}
		if (decreaseValue == 0) {
			decreaseValue = 100;
		}

		descPerSubcon.setType("DescPer").setText(String.valueOf(decreasePercent));
		descValSubcon.setType("DescVal").setText(String.valueOf(decreaseValue));

		condition.addSubcondition(descPerSubcon).addSubcondition(descValSubcon);
		config.addCondition(condition);
		configs.add(config);
		return configs;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public String getName() {
		return "metric-alert";
	}

	private String printArray(double[] value) {
		StringBuilder sb = new StringBuilder();

		for (double d : value) {
			sb.append(d).append(" ");
		}
		return sb.toString();
	}

	protected void processMetricItemConfig(MetricItemConfig config, int minute, ProductLine productLine) {
		if ((!config.getAlarm() && !config.isShowAvgDashboard() && !config.isShowSumDashboard() && !config
		      .isShowCountDashboard())) {
			return;
		}
		String product = productLine.getId();

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
			m_alertInfo.addAlertInfo(config, new Date().getTime());
			sendAlertInfo(productLine, config, alert.getValue());
		}
	}

	@Override
	public void run() {
		boolean active = true;
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			active = false;
		}
		while (active) {
			int minute = Calendar.getInstance().get(Calendar.MINUTE);
			String minuteStr = String.valueOf(minute);

			if (minute < 10) {
				minuteStr = '0' + minuteStr;
			}
			Transaction t = Cat.newTransaction("MetricAlert", "M" + minuteStr);
			long current = System.currentTimeMillis();

			try {
				Map<String, ProductLine> productLines = m_productLineConfigManager.getCompany().getProductLines();

				for (ProductLine productLine : productLines.values()) {
					try {
						if (productLine.isMetricDashboard()) {
							processProductLine(productLine);
						}
					} catch (Exception e) {
						Cat.logError(e);
					}
				}

				t.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				t.setStatus(e);
			} finally {
				m_currentReports.clear();
				m_lastReports.clear();
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
		String title = m_alertConfig.buildMailTitle(productLine, config.getTitle());

		m_logger.info(title + " " + content + " " + emails);
		m_mailSms.sendEmail(title, content, emails);
		m_mailSms.sendSms(title + " " + content, content, phones);

		Cat.logEvent("MetricAlert", productLine.getId(), Event.SUCCESS, title + "  " + content);
	}

	@Override
	public void shutdown() {
	}

}