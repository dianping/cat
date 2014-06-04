package com.dianping.cat.report.task.metric;

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
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.system.config.MetricRuleConfigManager;
import com.dianping.cat.system.tool.MailSMS;

public class NetworkAlert extends BaseAlert implements Task, LogEnabled {

	@Inject
	private MetricRuleConfigManager m_metricRuleConfigManager;

	@Inject
	protected MailSMS m_mailSms;

	@Inject
	private NetworkAlertConfig m_alertConfig;

	@Inject
	private AlertInfo m_alertInfo;

	private static final long DURATION = TimeUtil.ONE_MINUTE;

	private Logger m_logger;

	private Pair<Boolean, String> computeAlertInfo(int minute, String product, MetricItemConfig config, MetricType type) {
		double[] value = null;
		double[] baseline = null;
		String domain = config.getDomain();
		String key = config.getMetricKey();
		String metricKey = m_metricConfigManager.buildMetricKey(domain, config.getType(), key);
		List<Config> configs = m_metricRuleConfigManager.buildConfigs(product, domain, key, metricKey);
		int maxMinute = queryCheckMinute(configs);

		if (minute >= maxMinute - 1) {
			MetricReport report = fetchMetricReport(product, ModelPeriod.CURRENT);

			if (report != null) {
				int start = minute + 1 - maxMinute;
				int end = minute;

				value = queryRealData(start, end, metricKey, report, type);
				baseline = queryBaseLine(start, end, metricKey, new Date(ModelPeriod.CURRENT.getStartTime()), type);

				return DataChecker.checkData(value, baseline, type, configs);
			}
		} else if (minute < 0) {
			MetricReport lastReport = fetchMetricReport(product, ModelPeriod.LAST);

			if (lastReport != null) {
				int start = 60 + minute + 1 - (maxMinute);
				int end = 60 + minute;

				value = queryRealData(start, end, metricKey, lastReport, type);
				baseline = queryBaseLine(start, end, metricKey, new Date(ModelPeriod.LAST.getStartTime()), type);
				return DataChecker.checkData(value, baseline, type, configs);
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
				return DataChecker.checkData(value, baseline, type, configs);
			}
		}
		return null;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public String getName() {
		return "metric-alert";
	}

	protected void processMetricItemConfig(MetricItemConfig config, int minute, ProductLine productLine) {
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
			Transaction t = Cat.newTransaction("SwitchAlert", "M" + minuteStr);
			long current = System.currentTimeMillis();

			try {
				Map<String, ProductLine> productLines = m_productLineConfigManager.getCompany().getProductLines();

				for (ProductLine productLine : productLines.values()) {
					try {
						if (productLine.isNetworkDashboard()) {
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
		//List<String> phones = m_alertConfig.buildSMSReceivers(productLine);
		String title = m_alertConfig.buildMailTitle(productLine, config);

		m_logger.info(title + " " + content + " " + emails);
		//m_mailSms.sendEmail(title, content, emails);
		//m_mailSms.sendSms(title + " " + content, content, phones);

		Cat.logEvent("SwitchAlert", productLine.getId(), Event.SUCCESS, title + "  " + content);
	}

	@Override
	public void shutdown() {
	}

}