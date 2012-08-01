package com.dianping.cat.report.page.monthreport;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;

import org.xml.sax.SAXException;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.hadoop.dal.Dailyreport;
import com.dianping.cat.hadoop.dal.DailyreportDao;
import com.dianping.cat.hadoop.dal.DailyreportEntity;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.model.problem.ProblemReportMerger;
import com.dianping.cat.report.page.model.transaction.TransactionReportMerger;
import com.dianping.cat.report.page.problem.ProblemStatistics;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private DailyreportDao m_dailyreportDao;

	@Inject
	private ServerConfigManager m_manager;

	private static final long DAY = 24 * 60 * 60 * 1000L;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "monthreport")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "monthreport")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.MONTHREPORT);
		normalize(payload, model);

		String domain = payload.getDomain();
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		System.out.println(start);
		System.out.println(end);
		try {
			Set<String> domains = getAllDomains(start, end);
			TransactionReport transactionReport = getTransactionReport(start, end, domain);
			ProblemReport problemReport = getProblemReport(start, end, domain);
			ProjectReport report = new ProjectReport();
			report.visit(transactionReport);

			ProblemStatistics statistics = new ProblemStatistics();
			statistics.setAllIp(true);
			statistics.visitProblemReport(problemReport);

			report.visit(statistics);

			model.setDomains(domains);
			model.setReport(report);
		} catch (Exception e) {
			Cat.logError(e);
		}
		m_jspViewer.view(ctx, model);
	}

	private void normalize(Payload payload, Model model) {
		String domain = payload.getDomain();

		if (domain == null || domain.length() == 0) {
			payload.setDomain(m_manager.getConsoleDefaultDomain());
		}
		model.setDomain(payload.getDomain());
		payload.setReportType("month");
		payload.computeStartDate();
		model.setLongDate(payload.getDate());
	}

	private TransactionReport getTransactionReport(final Date start, final Date end, String domain) throws DalException,
	      SAXException, IOException {
		long startLong = Long.MAX_VALUE;
		long endLong = 0;
		long startTime = start.getTime();
		int days = (int) ((end.getTime() - startTime) / DAY);
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(domain));

		for (int i = 0; i < days; i++) {
			Dailyreport report = null;
			try {
				report = m_dailyreportDao.findByNameDomainPeriod(new Date(startTime + i * DAY), domain, "transaction",
				      DailyreportEntity.READSET_FULL);
			} catch (DalException e) {
			}
			if (report != null) {
				String xml = report.getContent();
				TransactionReport reportModel = DefaultSaxParser.parse(xml);
				startLong = Math.min(startLong, reportModel.getStartTime().getTime());
				endLong = Math.max(startLong, reportModel.getEndTime().getTime());
				reportModel.accept(merger);
			} else {
				Cat.getProducer().logEvent("MonthReport", "transaction", "NotFound",
				      domain + sdf.format(new Date(startTime + i * DAY)));
			}
		}
		TransactionReport transactionReport = merger.getTransactionReport();

		transactionReport.setStartTime(new Date(startLong));
		transactionReport.setEndTime(new Date(endLong));
		return transactionReport;
	}

	private ProblemReport getProblemReport(final Date start, final Date end, String domain) throws DalException,
	      SAXException, IOException {
		long startLong = Long.MAX_VALUE;
		long endLong = 0;
		long startTime = start.getTime();
		int days = (int) ((end.getTime() - startTime) / DAY);
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		
		ProblemReportMerger merger = new ProblemReportMerger(new ProblemReport(domain));

		for (int i = 0; i < days; i++) {
			Dailyreport report = null;
			try {
				report = m_dailyreportDao.findByNameDomainPeriod(new Date(startTime + i * DAY), domain, "problem",
				      DailyreportEntity.READSET_FULL);
			} catch (DalException e) {
			}
			if (report != null) {
				String xml = report.getContent();
				ProblemReport reportModel = com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser.parse(xml);
				startLong = Math.min(startLong, reportModel.getStartTime().getTime());
				endLong = Math.max(startLong, reportModel.getEndTime().getTime());
				reportModel.accept(merger);
			} else {
				Cat.getProducer().logEvent("MonthReport", "transaction", "NotFound",
				      domain + sdf.format(new Date(startTime + i * DAY)));
			}
		}
		ProblemReport problemReport = merger.getProblemReport();
		problemReport.setStartTime(new Date(startLong));
		problemReport.setEndTime(new Date(endLong));
		return problemReport;
	}

	private Set<String> getAllDomains(final Date start, final Date end) throws DalException {
		Set<String> domains = new HashSet<String>();
		List<Dailyreport> historyReports = m_dailyreportDao.findAllByPeriod(start, end,
		      DailyreportEntity.READSET_DOMAIN_NAME);

		for (Dailyreport report : historyReports) {
			domains.add(report.getDomain());
		}
		return domains;
	}
}
