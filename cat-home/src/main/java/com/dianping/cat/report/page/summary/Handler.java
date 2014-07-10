package com.dianping.cat.report.page.summary;

import java.io.IOException;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.task.alert.summary.AlertSummaryExecutor;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

//	@Inject
//	AlertSummaryExecutor m_executor;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "summary")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "summary")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

//		switch (action) {
//		case VIEW:
//			String summaryContent = m_executor.execute(payload.getDomain(), payload.getTime(), payload.getEmails());
//			model.setSummaryContent(summaryContent);
//			break;
//		}

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.SUMMARY);

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}
}
