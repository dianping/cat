package com.dianping.cat.report.page.nettopo;

import java.io.IOException;

import javax.servlet.ServletException;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.JsonBuilder;
import com.dianping.cat.report.page.nettopo.model.NetGraph;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private NetGraphManager m_netGraphManager = new NetGraphManager();

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "nettopo")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "nettopo")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);

		NetGraph netGraph = m_netGraphManager.getNetGraph();
		if (netGraph != null) {
			model.setNetData(netGraph.getJsonData());
		}

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.NETTOPO);

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}
}
