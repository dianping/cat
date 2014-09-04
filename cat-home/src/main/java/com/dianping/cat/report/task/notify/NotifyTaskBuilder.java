package com.dianping.cat.report.task.notify;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.alarm.MailRecord;
import com.dianping.cat.home.dal.alarm.MailRecordDao;
import com.dianping.cat.home.dal.alarm.ScheduledReport;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.service.ReportServiceManager;
import com.dianping.cat.report.task.alert.sender.AlertChannel;
import com.dianping.cat.report.task.alert.sender.AlertMessageEntity;
import com.dianping.cat.report.task.alert.sender.sender.SenderManager;
import com.dianping.cat.report.task.spi.ReportTaskBuilder;
import com.dianping.cat.system.page.alarm.ScheduledManager;

public class NotifyTaskBuilder implements ReportTaskBuilder {

	public static final String ID = Constants.REPORT_NOTIFY;

	@Inject
	private ReportServiceManager m_reportService;

	@Inject
	private MailRecordDao m_mailRecordDao;

	@Inject
	private SenderManager m_sendManager;

	@Inject
	private ReportRender m_render;

	@Inject
	private ScheduledManager m_scheduledManager;

	@Inject
	private AppDataComparisonNotifier m_appDataInformer;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd");

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

	private String renderContent(String names, String domain, Date start) {
		int transactionFlag = names.indexOf(TransactionAnalyzer.ID);
		int eventFlag = names.indexOf(EventAnalyzer.ID);
		int problemFlag = names.indexOf(ProblemAnalyzer.ID);
		Date end = new Date(start.getTime() + TimeUtil.ONE_DAY);
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
	public boolean buildDailyTask(String name, String domainName, Date period) {
		sendDailyReport(period);
		sendVsMeiTuanReport(period);
		return true;
	}

	private void sendVsMeiTuanReport(Date period) {
		m_appDataInformer.doNotifying(period);
	}

	private void sendDailyReport(Date period) {
		List<ScheduledReport> reports = m_scheduledManager.queryScheduledReports();

		for (ScheduledReport report : reports) {
			String domain = report.getDomain();
			Transaction t = Cat.newTransaction("ScheduledReport", domain);

			try {
				String names = String.valueOf(report.getNames());
				String content = renderContent(names, domain, period);
				String title = renderTitle(names, domain);
				List<String> emails = m_scheduledManager.queryEmailsBySchReportId(report.getId());
				AlertMessageEntity message = new AlertMessageEntity(domain, title, "ScheduledJob", content, emails);
				boolean result = m_sendManager.sendAlert(AlertChannel.MAIL, message);

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
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		throw new RuntimeException("daily report builder don't support hourly task");
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		throw new RuntimeException("daily report builder don't support monthly task");
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		throw new RuntimeException("daily report builder don't support weekly task");
	}

}
