package com.dianping.cat.report.page.logview;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletException;

import com.dianping.cat.report.ReportPage;
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
	@InboundActionMeta(name = "m")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "m")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.LOGVIEW);
		
		Payload payload = ctx.getPayload();
		
		System.out.println(payload.getIdentifier());
		System.out.println(Arrays.asList(payload.getPath()));
		
		m_jspViewer.view(ctx, model);
	}
}
