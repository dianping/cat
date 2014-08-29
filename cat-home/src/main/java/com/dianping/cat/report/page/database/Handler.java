package com.dianping.cat.report.page.database;

import java.io.IOException;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.home.OverloadReport.entity.OverloadReport;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.task.monitor.database.TableCapacityService;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private TableCapacityService m_tableCapacityService;

	private OverloadReport generateReport(Payload payload) {
		OverloadReport report = new OverloadReport();
		String domain = payload.getDomain();
		String ip = payload.getIp();
		String name = payload.getName();

		if (domain != null) {
			report.setDomain(domain);
		}
		if (ip != null) {
			report.setIp(ip);
		}
		if (name != null) {
			report.setName(name);
		}

		return report;
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "database")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "database")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		switch (action) {
		case VIEW:
			OverloadReport compareReport = generateReport(payload);
			model.setReports(m_tableCapacityService.queryOverloadReports(compareReport, payload.getStartTime(),
			      payload.getEndTime()));
			break;
		}

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.DATABASE);

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}
}
