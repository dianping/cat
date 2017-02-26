package com.dianping.cat.report.task.notify;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;
import org.unidal.webres.helper.Splitters;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.alert.sender.AlertChannel;
import com.dianping.cat.report.alert.sender.AlertMessageEntity;
import com.dianping.cat.report.alert.sender.sender.SenderManager;
import com.dianping.cat.report.page.event.service.EventReportService;
import com.dianping.cat.report.page.problem.service.ProblemReportService;
import com.dianping.cat.report.page.transaction.service.TransactionReportService;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.service.ProjectService;

public class NotifyTaskBuilder implements TaskBuilder {

	public static final String ID = Constants.REPORT_NOTIFY;

	@Inject
	private TransactionReportService m_transactionReportService;
	
	@Inject
	private EventReportService m_eventReportService;
	
	@Inject
	private ProblemReportService m_problemReportService;

	@Inject
	private SenderManager m_sendManager;

	@Inject
	private ReportRender m_render;

	@Inject
	private ServerFilterConfigManager m_serverConfigManager;

	@Inject
	private ProjectService m_projectService;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd");

	private boolean m_active = false;

	private String renderContent(String domain, Date start) {
		Date end = new Date(start.getTime() + TimeHelper.ONE_DAY);
		TransactionReport transactionReport = m_transactionReportService.queryReport(domain, start, end);
		EventReport eventReport = m_eventReportService.queryReport(domain, start, end);
		ProblemReport problemReport = m_problemReportService.queryReport(domain, start, end);

		StringBuilder sb = new StringBuilder(10240);

		sb.append(m_sdf.format(start)).append("</br>");
		sb.append(m_render.renderReport(transactionReport));
		sb.append(m_render.renderReport(eventReport));
		sb.append(m_render.renderReport(problemReport));
		return sb.toString();
	}

	private String renderTitle(String domain) {
		return " CAT 日常报表 [ " + domain + " ]";
	}

	@Override
	public boolean buildDailyTask(String name, String domainName, Date period) {
		sendDailyReport(period);
		return true;
	}

	private void sendDailyReport(Date period) {
		Date start = TimeHelper.getCurrentDay();
		Date end = new Date(start.getTime() + TimeHelper.ONE_HOUR);
		Set<String> domains = m_transactionReportService.queryAllDomainNames(start, end, TransactionAnalyzer.ID);

		for (String domain : domains) {
			if (m_serverConfigManager.validateDomain(domain) && m_active) {
				Transaction t = Cat.newTransaction("ScheduledReport", domain);

				try {
					String content = renderContent(domain, period);
					String title = renderTitle(domain);
					String email = m_projectService.findByDomain(domain).getEmail();
					List<String> emails = Splitters.by(',').noEmptyItem().split(email);
					AlertMessageEntity message = new AlertMessageEntity(domain, title, "ScheduledJob", content, emails);

					m_sendManager.sendAlert(AlertChannel.MAIL, message);
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
