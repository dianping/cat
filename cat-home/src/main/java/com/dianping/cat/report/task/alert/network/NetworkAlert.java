package com.dianping.cat.report.task.alert.network;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.task.alert.BaseAlert;
import com.dianping.cat.report.task.alert.MetricType;
import com.dianping.cat.service.ModelPeriod;

public class NetworkAlert extends BaseAlert implements Task, LogEnabled {

	@Inject
	private NetworkAlertConfig m_alertConfig;

	private String buildMetricTitle(String metricKey) {
		try {
			return metricKey.split(":")[2];
		} catch (Exception ex) {
			Cat.logError("get metric title error:" + metricKey, ex);
			return null;
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public String getName() {
		return "network-alert";
	}

	private void processMetricItem(int minute, ProductLine productLine, String metricKey) {
		for (MetricType type : MetricType.values()) {
			Pair<Boolean, String> alert = computeAlertInfo(minute, productLine.getId(), metricKey, type);

			if (alert != null && alert.getKey()) {
				String metricTitle = buildMetricTitle(metricKey);
				m_alertInfo.addAlertInfo(metricKey, new Date().getTime());

				sendAlertInfo(productLine, metricTitle, alert.getValue());
			}
		}
	}

	@Override
	protected void processProductLine(ProductLine productLine) {
		long current = (System.currentTimeMillis()) / 1000 / 60;
		int minute = (int) (current % (60)) - DATA_AREADY_MINUTE;
		String product = productLine.getId();
		MetricReport report = fetchMetricReport(product, ModelPeriod.CURRENT);

		for (Entry<String, MetricItem> entry : report.getMetricItems().entrySet()) {
			try {
				processMetricItem(minute, productLine, entry.getKey());
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
			Transaction t = Cat.newTransaction("NetworkAlert", "M" + minuteStr);
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

	@Override
	protected void sendAlertInfo(ProductLine productLine, String metricTitle, String content) {
		List<String> emails = m_alertConfig.buildMailReceivers(productLine);
		List<String> phones = m_alertConfig.buildSMSReceivers(productLine);
		String title = m_alertConfig.buildMailTitle(productLine, metricTitle);

		m_logger.info(title + " " + content + " " + emails);
		m_mailSms.sendEmail(title, content, emails);
		m_mailSms.sendSms(title + " " + content, content, phones);

		Cat.logEvent("NetworkAlert", productLine.getId(), Event.SUCCESS, title + "  " + content);
	}

	@Override
	public void shutdown() {
	}

}