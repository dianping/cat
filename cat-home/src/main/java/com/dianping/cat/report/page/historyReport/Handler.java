package com.dianping.cat.report.page.historyReport;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.ServletException;

import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultDomParser;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.report.page.event.DisplayEventNameReport;
import com.dianping.cat.report.page.event.DisplayEventTypeReport;
import com.dianping.cat.report.page.problem.ProblemStatistics;
import com.dianping.cat.report.page.transaction.DisplayTransactionNameReport;
import com.dianping.cat.report.page.transaction.DisplayTransactionTypeReport;
import com.site.lookup.annotation.Inject;
import com.site.lookup.util.StringUtils;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "hr")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "hr")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Date start = new Date();
		Date end = new Date();
		String domain = payload.getDomain();

		model.setPage(payload.getPage());
		model.setAction(payload.getAction());

		try {
			start = sdf.parse(payload.getStartDate());
			end = sdf.parse(payload.getEndDate());
		} catch (Exception e) {
			m_jspViewer.view(ctx, model);
			return;
		}

		// TODO Use Dao get Daily Report
		model.setDomain(domain);
		switch (payload.getAction()) {
		case TRANSACTION:
			TransactionReport transactionReport = getTransactionReport(domain, start, end);
			showTransactionReport(transactionReport, payload, model);
			break;
		case EVENT:
			EventReport eventReport = getEventReport(domain, start, end);
			showEventReport(eventReport, payload, model);
			break;
		case PROBLEM:
			ProblemReport problemReport = getProblemReport(domain, start, end);
			showProblemReport(problemReport, payload, model);
			break;
		}
		m_jspViewer.view(ctx, model);
	}

	private void showTransactionReport(TransactionReport report, Payload payload, Model model) {
		String type = payload.getType();
		String sorted = payload.getSortBy();
		String ip = payload.getIp();
		if (ip == null) {
			ip = CatString.ALL_IP;
		}
		model.setIpAddress(ip);
		if (report == null) {
			return;
		}
		model.setIps(new ArrayList<String>(report.getIps()));
		if (!StringUtils.isEmpty(type)) {
			model.setTransactionNames(new DisplayTransactionNameReport().display(sorted, type, ip, report, ""));
		} else {
			model.setTransactionTypes(new DisplayTransactionTypeReport().display(sorted, ip, report));
		}
	}

	private void showEventReport(EventReport report, Payload payload, Model model) {
		String type = payload.getType();
		String sorted = payload.getSortBy();
		String ip = payload.getIp();
		if (ip == null) {
			ip = CatString.ALL_IP;
		}
		model.setIpAddress(ip);
		if (report == null) {
			return;
		}
		model.setIps(new ArrayList<String>(report.getIps()));
		if (!StringUtils.isEmpty(type)) {
			model.setEventNames(new DisplayEventNameReport().display(sorted, type, ip, report));
		} else {
			model.setEventTypes(new DisplayEventTypeReport().display(sorted, ip, report));
		}
	}

	private void showProblemReport(ProblemReport report, Payload payload, Model model) {
		String ip = payload.getIp();
		if (ip == null) {
			ip = CatString.ALL_IP;
		}
		int threashold = payload.getLongTime();
		if (threashold == 0) {
			threashold = 1000;
		}
		model.setThreshold(threashold);
		model.setIpAddress(ip);
		if (report == null) {
			return;
		}
		model.setIps(new ArrayList<String>(report.getIps()));
		model.setProblemStatistics(new ProblemStatistics().displayByIp(report, ip, payload.getLongTime(), 60));
	}

	private TransactionReport getTransactionReport(String domain, Date start, Date end) {
		String oldXml;
		try {
			oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReportOld.xml"), "utf-8");
			TransactionReport reportOld = new DefaultDomParser().parse(oldXml);
			return reportOld;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private EventReport getEventReport(String domain, Date start, Date end) {
		String oldXml;
		try {
			oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("event.xml"), "utf-8");
			EventReport reportOld = new com.dianping.cat.consumer.event.model.transform.DefaultDomParser().parse(oldXml);
			return reportOld;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private ProblemReport getProblemReport(String domain, Date start, Date end) {
		String oldXml;
		try {
			oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("problem.xml"), "utf-8");
			ProblemReport reportOld = new com.dianping.cat.consumer.problem.model.transform.DefaultDomParser()
			      .parse(oldXml);
			return reportOld;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
