package com.dianping.cat.report.heartbeat.heartbeat.page.heartbeat;

import java.io.IOException;

import javax.servlet.ServletException;

import com.dianping.cat.report.heartbeat.heartbeat.HeartbeatPage;
import com.site.lookup.annotation.Inject;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "heartbeat")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "heartbeat")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);

		model.setAction(Action.VIEW);
		model.setPage(HeartbeatPage.HEARTBEAT);
		m_jspViewer.view(ctx, model);
	}
}
