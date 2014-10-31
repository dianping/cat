package com.dianping.cat.report.page.highload;

import java.io.IOException;

import javax.servlet.ServletException;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.task.highload.HighLoadService;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private HighLoadService m_service;

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
			try {
				model.setReports(m_service.queryHighLoadReports(payload.getDate()));
			} catch (DalException e) {
				Cat.logError(e);
			}
			break;
		}

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.HIGHLOAD);

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}
}
