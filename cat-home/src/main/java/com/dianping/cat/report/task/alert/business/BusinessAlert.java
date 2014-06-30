package com.dianping.cat.report.task.alert.business;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.task.alert.AlertResultEntity;
import com.dianping.cat.report.task.alert.BaseAlert;
import com.dianping.cat.report.task.alert.MetricType;

public class BusinessAlert extends BaseAlert implements Task, LogEnabled {

	@Inject
	private BusinessAlertConfig m_alertConfig;

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public String getName() {
		return "metric-alert";
	}

	public boolean needAlert(MetricItemConfig config) {
		if ((config.getAlarm() || config.isShowAvgDashboard() || config.isShowSumDashboard() || config
		      .isShowCountDashboard())) {
			return true;
		} else {
			return false;
		}
	}

	private void processMetricItemConfig(MetricItemConfig config, int minute, ProductLine productLine) {
		if (needAlert(config)) {
			String product = productLine.getId();
			String metricKey = m_metricConfigManager.buildMetricKey(config.getDomain(), config.getType(),
			      config.getMetricKey());

			AlertResultEntity alert = null;
			if (config.isShowAvg()) {
				alert = computeAlertInfo(minute, product, metricKey, MetricType.AVG);
			}
			if (config.isShowCount()) {
				alert = computeAlertInfo(minute, product, metricKey, MetricType.COUNT);
			}
			if (config.isShowSum()) {
				alert = computeAlertInfo(minute, product, metricKey, MetricType.SUM);
			}

			if (alert != null && alert.isTriggered()) {
				m_alertInfo.addAlertInfo(metricKey, new Date().getTime());

				sendAlertInfo(productLine, config.getTitle(), alert.getContent(), alert.getAlertType());
			}
		}
	}

	@Override
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

	@Override
	public void sendAlertInfo(ProductLine productLine, String metricTitle, String content, String alertType) {
		List<String> emails = m_alertConfig.buildMailReceivers(productLine);
		String title = m_alertConfig.buildMailTitle(productLine, metricTitle);

		m_logger.info(title + " " + content + " " + emails);
		m_mailSms.sendEmail(title, content, emails);

		if (alertType != null && alertType.equals("error")) {
			List<String> phones = m_alertConfig.buildSMSReceivers(productLine);
			m_mailSms.sendSms(title + " " + content, content, phones);
		}

		Cat.logEvent("MetricAlert", productLine.getId(), Event.SUCCESS, title + "  " + content);
	}

	@Override
	public void shutdown() {
	}
}