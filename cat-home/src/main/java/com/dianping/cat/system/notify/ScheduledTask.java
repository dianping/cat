package com.dianping.cat.system.notify;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.health.model.entity.HealthReport;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.alarm.ScheduledReport;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.service.DailyReportService;
import com.dianping.cat.system.page.alarm.ScheduledManager;
import com.dianping.cat.system.tool.MailSMS;
import com.site.dal.jdbc.DalException;
import com.site.helper.Threads.Task;
import com.site.lookup.annotation.Inject;

public class ScheduledTask implements Task {

	@Inject
	private MailSMS m_mailSms;

	@Inject
	private ReportRender m_render;

	@Inject
	private DailyReportService m_dailyReportService;

	@Inject
	private ScheduledManager m_scheduledManager;

	@Override
	public String getName() {
		return "ScheduledDailyReport";
	}

	private long getSleepTime() {
		Calendar cal = Calendar.getInstance();

		cal.add(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR, 1);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis() - System.currentTimeMillis();
	}

	private String renderContent(String names, String domain) {
		int healthFlag = names.indexOf("health");
		int transactionFlag = names.indexOf("transaction");
		int eventFlag = names.indexOf("event");
		int problemFlag = names.indexOf("problem");
		Date end = TimeUtil.getCurrentDay();
		Date start = new Date(end.getTime() - TimeUtil.ONE_DAY);

		TransactionReport transactionReport = m_dailyReportService.queryTransactionReport(domain, start, end);
		EventReport eventReport = m_dailyReportService.queryEventReport(domain, start, end);
		ProblemReport problemReport = m_dailyReportService.queryProblemReport(domain, start, end);
		HealthReport heathReport = m_dailyReportService.queryHealthReport(domain, start, end);

		StringBuilder sb = new StringBuilder(10240);
		if (healthFlag > -1) {
			sb.append(m_render.renderReport(heathReport)).append("</br>");
		}
		if (transactionFlag > -1) {
			sb.append(m_render.renderReport(transactionReport)).append("</br>");
		}
		if (eventFlag > -1) {
			sb.append(m_render.renderReport(eventReport)).append("</br>");
		}
		if (problemFlag > -1) {
			sb.append(m_render.renderReport(problemReport)).append("</br>");
		}
		return sb.toString();
	}

	private String renderTitle(String names, String domain) {
		return "CAT Daily Report[ " + domain + " ]";
	}

	@Override
	public void run() {
		while (true) {
			try {
				List<ScheduledReport> reports = m_scheduledManager.queryScheduledReports();

				for (ScheduledReport report : reports) {
					String domain = report.getDomain();
					Transaction t = Cat.newTransaction("ScheduledReport", domain);
					try {
						String names = report.getNames();
						String content = renderContent(names, domain);
						String title = renderTitle(names, domain);
						List<String> emails = m_scheduledManager.queryEmailsBySchReportId(report.getId());

						m_mailSms.sendEmail(title, content, emails);
						t.setStatus(Transaction.SUCCESS);
						Cat.getProducer().logEvent("ScheduledReport", "Email", Event.SUCCESS, emails.toString());
					} catch (DalException e) {
						Cat.logError(e);
						t.setStatus(e);
					}
					t.complete();
					break;
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
			try {
				Thread.sleep(getSleepTime());
			} catch (Exception e) {
				// ignore;
			}
		}

	}

	@Override
	public void shutdown() {
	}

}
