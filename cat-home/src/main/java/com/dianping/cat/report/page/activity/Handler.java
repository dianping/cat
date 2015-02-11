package com.dianping.cat.report.page.activity;

import java.io.IOException;

import javax.servlet.ServletException;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.home.activity.entity.Activity;
import com.dianping.cat.home.activity.entity.ActivityConfig;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.system.config.ActivityConfigManager;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;
	
	@Inject
	private ActivityConfigManager m_configManager;

	@Inject(type = ModelService.class, value = TransactionAnalyzer.ID)
	private ModelService<TransactionReport> m_service;

	private TransactionReport getTransactionGraphReport(Activity activity) {
//		String domain = activity.getDomain();
//		String type = activity.getType();
//		String name = activity.getName();
//
//		ModelRequest request = new ModelRequest(domain, payload.getDate()) //
//		      .setProperty("type", type) //
//		      .setProperty("name", name);
//
//		if (name == null || name.length() == 0) {
//			request.setProperty("name", "*");
//			request.setProperty("all", "true");
//			name = Constants.ALL;
//		}
//
//		ModelResponse<TransactionReport> response = m_service.invoke(request);
//		TransactionReport report = response.getModel();
//		return report;
		return null;
	}
	
	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "activity")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "activity")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.ACTIVITY);

		if (!ctx.isProcessStopped()) {
		   m_jspViewer.view(ctx, model);
		}
	}
}
