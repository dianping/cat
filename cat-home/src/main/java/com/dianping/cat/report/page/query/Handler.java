package com.dianping.cat.report.page.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.query.display.EventQueryItem;
import com.dianping.cat.report.page.query.display.EventReportVisitor;
import com.dianping.cat.report.page.query.display.ProblemQueryItem;
import com.dianping.cat.report.page.query.display.ProblemReportVisitor;
import com.dianping.cat.report.page.query.display.TransactionQueryItem;
import com.dianping.cat.report.page.query.display.TransactionReportVisitor;
import com.dianping.cat.report.service.ReportService;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ReportService m_reportService;

	private static final String HOUR = "hour";

	private static final String DAY = "day";

	private List<EventQueryItem> buildEventItems(Date start, Date end, String domain, String type, String name,
	      String reportLevel) {
		List<EventQueryItem> items = new ArrayList<EventQueryItem>();

		if (HOUR.equalsIgnoreCase(reportLevel)) {
			for (long i = start.getTime(); i <= end.getTime(); i = i + TimeUtil.ONE_HOUR) {
				EventReport report = m_reportService.queryEventReport(domain, new Date(i), new Date(i + TimeUtil.ONE_HOUR));

				items.add(convert(report, type, name));
			}
		} else if (DAY.equalsIgnoreCase(reportLevel)) {
			for (long i = start.getTime(); i <= end.getTime(); i = i + TimeUtil.ONE_DAY) {
				EventReport report = m_reportService.queryEventReport(domain, new Date(i), new Date(i + TimeUtil.ONE_DAY));

				items.add(convert(report, type, name));
			}
		}
		return items;
	}

	private List<ProblemQueryItem> buildProblemItems(Date start, Date end, String domain, String type, String name,
	      String reportLevel) {
		List<ProblemQueryItem> items = new ArrayList<ProblemQueryItem>();

		if (HOUR.equalsIgnoreCase(reportLevel)) {
			for (long i = start.getTime(); i <= end.getTime(); i = i + TimeUtil.ONE_HOUR) {
				ProblemReport report = m_reportService.queryProblemReport(domain, new Date(i), new Date(i
				      + TimeUtil.ONE_HOUR));

				items.add(convert(report, type, name));
			}
		} else if (DAY.equalsIgnoreCase(reportLevel)) {
			for (long i = start.getTime(); i <= end.getTime(); i = i + TimeUtil.ONE_DAY) {
				ProblemReport report = m_reportService.queryProblemReport(domain, new Date(i), new Date(i
				      + TimeUtil.ONE_DAY));

				items.add(convert(report, type, name));
			}
		}
		return items;
	}

	private List<TransactionQueryItem> buildTransactionItems(Date start, Date end, String domain, String type,
	      String name, String reportLevel) {
		List<TransactionQueryItem> items = new ArrayList<TransactionQueryItem>();

		if (HOUR.equalsIgnoreCase(reportLevel)) {
			for (long i = start.getTime(); i <= end.getTime(); i = i + TimeUtil.ONE_HOUR) {
				TransactionReport report = m_reportService.queryTransactionReport(domain, new Date(i), new Date(i
				      + TimeUtil.ONE_HOUR));

				items.add(convert(report, type, name));
			}
		} else if (DAY.equalsIgnoreCase(reportLevel)) {
			for (long i = start.getTime(); i <= end.getTime(); i = i + TimeUtil.ONE_DAY) {
				TransactionReport report = m_reportService.queryTransactionReport(domain, new Date(i), new Date(i
				      + TimeUtil.ONE_DAY));

				items.add(convert(report, type, name));
			}
		}
		return items;
	}

	private EventQueryItem convert(EventReport report, String type, String name) {
		EventReportVisitor vistitor = new EventReportVisitor(type, name);
		vistitor.visitEventReport(report);

		return vistitor.getItem();
	}

	private ProblemQueryItem convert(ProblemReport report, String type, String name) {
		ProblemReportVisitor vistitor = new ProblemReportVisitor(type, name);
		vistitor.visitProblemReport(report);

		return vistitor.getItem();
	}

	private TransactionQueryItem convert(TransactionReport report, String type, String name) {
		TransactionReportVisitor vistitor = new TransactionReportVisitor(type, name);
		vistitor.visitTransactionReport(report);

		return vistitor.getItem();
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "query")
	public void handleInbound(Context ctx) throws ServletException, IOException {
	}

	@Override
	@OutboundActionMeta(name = "query")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.QUERY);
		model.setIpAddress(payload.getIpAddress());

		Date start = payload.getStart();
		Date end = payload.getEnd();
		String domain = payload.getQueryDomain();
		String type = payload.getType();
		String name = payload.getName();
		String reportLevel = payload.getReportLevel();
		String queryType = payload.getQueryType();

		if ("transaction".equals(queryType)) {
			List<TransactionQueryItem> items = buildTransactionItems(start, end, domain, type, name, reportLevel);

			model.setTransactionItems(items);
		} else if ("event".equals(queryType)) {
			List<EventQueryItem> items = buildEventItems(start, end, domain, type, name, reportLevel);

			model.setEventItems(items);
		} else if ("problem".equals(queryType)) {
			List<ProblemQueryItem> items = buildProblemItems(start, end, domain, type, name, reportLevel);

			model.setProblemItems(items);
		}
		m_jspViewer.view(ctx, model);
	}

}
