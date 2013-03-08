package com.dianping.cat.system.notify;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.health.model.entity.HealthReport;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.alarm.MailRecord;
import com.dianping.cat.home.dal.alarm.MailRecordDao;
import com.dianping.cat.home.dal.alarm.MailRecordEntity;
import com.dianping.cat.home.dal.alarm.ScheduledReport;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.service.DailyReportService;
import com.dianping.cat.system.page.alarm.ScheduledManager;
import com.dianping.cat.system.tool.MailSMS;

public class ScheduledMailTask implements Task {

	@Inject
	private DailyReportService m_dailyReportService;

	@Inject
	private MailRecordDao m_mailRecordDao;

	@Inject
	private MailSMS m_mailSms;

	@Inject
	private ReportRender m_render;

	@Inject
	private ScheduledManager m_scheduledManager;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public String getName() {
		return "ScheduledDailyReport";
	}

	private long getSleepTime() {
		Calendar cal = Calendar.getInstance();

		cal.add(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 1);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis() - System.currentTimeMillis();
	}

	private void insertMailLog(int reportId, String content, String title, boolean result, List<String> emails)
	      throws DalException {
		MailRecord entity = m_mailRecordDao.createLocal();

		entity.setTitle(title);
		entity.setContent(content);
		entity.setRuleId(reportId);
		entity.setType(1);
		entity.setReceivers(emails.toString());
		if (result) {
			entity.setStatus(0);
		} else {
			entity.setStatus(1);
		}
		m_mailRecordDao.insert(entity);
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
		sb.append(m_sdf.format(start)).append("</br>");
		if (healthFlag > -1) {
			sb.append(m_render.renderReport(heathReport));
		}
		if (transactionFlag > -1) {
			sb.append(m_render.renderReport(transactionReport));
		}
		if (eventFlag > -1) {
			sb.append(m_render.renderReport(eventReport));
		}
		if (problemFlag > -1) {
			sb.append(m_render.renderReport(problemReport));
		}
		return sb.toString();
	}

	private String renderTitle(String names, String domain) {
		return " CAT 日常报表 [ " + domain + " ]";
	}

	@Override
	public void run() {
		boolean active = true;

		while (active) {
			try {
				MailRecord mailRecord = null;
				try {
					mailRecord = m_mailRecordDao.findLastReportRecord(MailRecordEntity.READSET_FULL);
				} catch (DalNotFoundException e) {
				} catch (Exception e) {
					Cat.logError(e);
				}

				if (mailRecord == null || mailRecord.getCreationDate().getTime() < TimeUtil.getCurrentDay().getTime()) {
					List<ScheduledReport> reports = m_scheduledManager.queryScheduledReports();

					for (ScheduledReport report : reports) {
						String domain = report.getDomain();
						Transaction t = Cat.newTransaction("ScheduledReport", domain);

						try {
							String names = report.getNames();
							String content = renderContent(names, domain);
							String title = renderTitle(names, domain);
							List<String> emails = m_scheduledManager.queryEmailsBySchReportId(report.getId());

							boolean result = m_mailSms.sendEmail(title, content, emails);

							insertMailLog(report.getId(), content, title, result, emails);
							t.setStatus(Transaction.SUCCESS);
							Cat.getProducer().logEvent("ScheduledReport", "Email", Event.SUCCESS, emails.toString());
						} catch (DalException e) {
							Cat.logError(e);
							t.setStatus(e);
						}
						t.complete();
					}
				} else {
					Cat.getProducer().logEvent("ScheduledReport", "SendNot", Event.SUCCESS, null);
				}
			} catch (Exception e) {
				Cat.logError(e);
			}

			try {
				Thread.sleep(getSleepTime());
			} catch (Exception e) {
				active = false;
			}
		}
	}

	@Override
	public void shutdown() {
	}

}
