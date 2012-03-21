package com.dianping.cat.report.page.model;

import java.io.IOException;

import javax.servlet.ServletException;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.model.logview.LocalLogViewService;
import com.dianping.cat.report.page.model.problem.LocalProblemService;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.report.page.model.transaction.LocalTransactionService;
import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler extends ContainerHolder implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject(type = ModelService.class, value = "transaction-local")
	private LocalTransactionService m_transactionService;
	
	@Inject(type = ModelService.class, value = "problem-local")
	private LocalProblemService m_problemService;
	
	@Inject(type = ModelService.class, value = "logview-local")
	private LocalLogViewService m_logviewService;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "model")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "model")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		model.setAction(Action.XML);
		model.setPage(ReportPage.MODEL);

		try {
			String report = payload.getReport();
			String domain = payload.getDomain();
			ModelRequest request = new ModelRequest(domain, payload.getPeriod());
			ModelResponse<?> response = null;

			if ("transaction".equals(report)) {
				response = m_transactionService.invoke(request);
			} else if ("problem".equals(report)) {
				response = m_problemService.invoke(request);
			} else if ("logview".equals(report)) {
				response = m_logviewService.invoke(request);
			} else {
				throw new RuntimeException("Unsupported report: " + report + "!");
			}

			Object dataModel = response.getModel();

			model.setModel(dataModel);
			model.setModelInXml(dataModel == null ? "" : String.valueOf(dataModel));
		} catch (Throwable e) {
			model.setException(e);
		}

		m_jspViewer.view(ctx, model);
	}
}
