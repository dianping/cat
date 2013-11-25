package com.dianping.cat.system.notify;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.alarm.MailRecord;
import com.dianping.cat.home.dal.alarm.MailRecordDao;
import com.dianping.cat.home.dal.alarm.MailRecordEntity;
import com.dianping.cat.home.dal.alarm.ScheduledReport;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.system.page.alarm.ScheduledManager;
import com.dianping.cat.system.tool.MailSMS;

public class ScheduledMailTask implements Task, LogEnabled {

	@Inject
	private ReportService m_reportService;

	@Inject
	private MailRecordDao m_mailRecordDao;

	@Inject
	private MailSMS m_mailSms;

	@Inject
	private ReportRender m_render;

	@Inject
	private ScheduledManager m_scheduledManager;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd");

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public String getName() {
		return "ScheduledDailyReport";
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
		int transactionFlag = names.indexOf(TransactionAnalyzer.ID);
		int eventFlag = names.indexOf(EventAnalyzer.ID);
		int problemFlag = names.indexOf(ProblemAnalyzer.ID);
		Date end = TimeUtil.getCurrentDay();
		Date start = new Date(end.getTime() - TimeUtil.ONE_DAY);

		TransactionReport transactionReport = m_reportService.queryTransactionReport(domain, start, end);
		EventReport eventReport = m_reportService.queryEventReport(domain, start, end);
		ProblemReport problemReport = m_reportService.queryProblemReport(domain, start, end);

		StringBuilder sb = new StringBuilder(10240);
		sb.append(m_sdf.format(start)).append("</br>");
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

				long lastSendMailTime = mailRecord.getCreationDate().getTime();
				long currentDay = TimeUtil.getCurrentDay().getTime();
				Calendar cal = Calendar.getInstance();

				if (lastSendMailTime < currentDay && cal.get(Calendar.HOUR_OF_DAY) >= 2) {
					List<ScheduledReport> reports = m_scheduledManager.queryScheduledReports();

					m_logger.info("Send daily report starting! size :" + reports.size());
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
							t.addData(emails.toString());
							t.setStatus(Transaction.SUCCESS);
						} catch (Exception e) {
							Cat.logError(e);
							t.setStatus(e);
						} finally {
							t.complete();
						}
					}
					m_logger.info("Send daily report finnshed!");
				} else {
					Cat.getProducer().logEvent("ScheduledReport", "SendNot", Event.SUCCESS, null);
				}
			} catch (Throwable e) {
				Cat.logError(e);
			}
			try {
				Thread.sleep(TimeUtil.ONE_HOUR);
			} catch (Exception e) {
				active = false;
			}
		}
	}

	@Override
	public void shutdown() {
	}

}
