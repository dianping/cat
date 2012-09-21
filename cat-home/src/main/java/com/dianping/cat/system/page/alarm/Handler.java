package com.dianping.cat.system.page.alarm;

import java.io.IOException;

import javax.servlet.ServletException;

import com.dianping.cat.system.SystemPage;
import com.site.lookup.annotation.Inject;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;
import com.site.web.mvc.annotation.PreInboundActionMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Override
	@PreInboundActionMeta("login")
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "alarm")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "alarm")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);

		model.setAction(Action.VIEW);
		model.setPage(SystemPage.ALARM);
		m_jspViewer.view(ctx, model);
	}
}
