package com.dianping.cat.report.task.alert.system;

import java.util.Calendar;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Threads.Task;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.task.alert.AlertConstants;
import com.dianping.cat.report.task.alert.BaseAlert;

public class SystemAlert extends BaseAlert implements Task, LogEnabled {

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public String getName() {
		return AlertConstants.SYSTEM;
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
			Transaction t = Cat.newTransaction("SystemAlert", "M" + minuteStr);
			long current = System.currentTimeMillis();

			try {
				Map<String, ProductLine> productLines = m_productLineConfigManager.getCompany().getProductLines();

				for (ProductLine productLine : productLines.values()) {
					try {
						if (productLine.isSystemMonitorDashboard()) {
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
	public void shutdown() {
	}

}