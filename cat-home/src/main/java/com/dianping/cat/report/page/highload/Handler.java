package com.dianping.cat.report.page.highload;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.highload.entity.HighloadReport;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.service.ReportServiceManager;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ReportServiceManager m_manager;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "highload")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "highload")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		switch (action) {
		case VIEW:
			Date startDate = payload.getDate();
			Date endDate = TimeHelper.addDays(startDate, 1);
			HighloadReport report = m_manager.queryHighloadReport("", startDate, endDate);

			model.setReport(new DisplayTypes().display(payload.getSortBy(), report));
			break;
		}

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.HIGHLOAD);

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}
}
