package com.dianping.cat.report.page.overload;

import java.io.IOException;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.overload.task.TableCapacityService;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private TableCapacityService m_tableCapacityService;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "overload")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "overload")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		switch (action) {
		case VIEW:
			try {
				model.setReports(m_tableCapacityService.queryOverloadReports(payload.getStartTime(), payload.getEndTime()));
			} catch (Exception e) {
				Cat.logError(e);
			}
			break;
		}

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.OVERLOAD);

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}
}
