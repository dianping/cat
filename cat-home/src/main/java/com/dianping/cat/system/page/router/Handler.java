package com.dianping.cat.system.page.router;

import java.io.IOException;

import javax.servlet.ServletException;

import com.dianping.cat.report.service.ReportServiceManager;
import com.dianping.cat.system.SystemPage;
import com.dianping.cat.system.config.RouterConfigManager;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;
	
	@Inject
	private ReportServiceManager m_reportService;
	
	@Inject
	private RouterConfigManager m_configManager;
	
	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "router")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "router")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();
		
		switch(action){
		case API:
			break;
		case MODEL:
			break;
		}
		model.setAction(Action.API);
		model.setPage(SystemPage.ROUTER);
		

		if (!ctx.isProcessStopped()) {
		   m_jspViewer.view(ctx, model);
		}
	}
}
