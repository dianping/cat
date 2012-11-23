package com.dianping.cat.report.task.month;

import java.util.Date;
import java.util.Set;

import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.service.DailyReportService;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

public class MonthReportBuilder implements Task {

	private boolean m_active = true;

	@Inject
	private DailyReportService m_dailyReportService;

	private boolean checkReportNeedCreat(String domain, Date date, String reportName) {
		return false;
	}

	@Override
	public String getName() {
		return null;
	}

	private boolean insert() {
		return false;
	}

	private Set<String> queryAllDomains(String reportName, Date date) {
		return null;
	}

	private Set<String> queryAllReportNames(Date date) {
		return null;
	}

	@Override
	public void run() {
		while (m_active) {

			Date date = TimeUtil.getCurrentMonth();

			Set<String> m_reports = queryAllReportNames(date);
			for (String reportName : m_reports) {
				Set<String> domains = queryAllDomains(reportName, date);

				for (String domain : domains) {
					// delete and insert
					System.out.println(domain);
				}
			}

			Date lastMonth = TimeUtil.getLastMonth();

			for (String reportName : m_reports) {
				Set<String> domains = queryAllDomains(reportName, lastMonth);

				for (String domain : domains) {
					if (checkReportNeedCreat(domain, lastMonth, reportName)) {
						insert();
					}
				}
			}

			try {
				Thread.sleep(60 * 60 * 1000);
			} catch (InterruptedException e) {
				m_active = false;
			}
		}
	}

	@Override
	public void shutdown() {
	}

}
