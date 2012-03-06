package com.dianping.cat.report.page.problem;

import java.io.IOException;

import javax.servlet.ServletException;

import com.dianping.cat.consumer.failure.model.entity.FailureReport;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.site.lookup.annotation.Inject;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject(type = ModelService.class, value = "failure")
	private ModelService<FailureReport> m_service;

	private FailureReport getReport(Payload payload) {
		String domain = payload.getDomain();
		String date = String.valueOf(payload.getDate());
		ModelRequest request = new ModelRequest(domain, payload.getPeriod()) //
		      .setProperty("domain", domain) //
		      .setProperty("date", date) //
		      .setProperty("ipAddress", payload.getIpAddress());

		if (m_service.isEligable(request)) {
			ModelResponse<FailureReport> response = m_service.invoke(request);
			FailureReport report = response.getModel();

			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable service registered for " + request + "!");
		}
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "p")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "p")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.PROBLEM);

		switch (payload.getAction()) {
		case VIEW:
			try {
				FailureReport report = getReport(payload);

				if (payload.getPeriod().isFuture()) {
					model.setDate(payload.getCurrentDate());
				} else {
					model.setDate(payload.getDate());
				}

				model.setReport(report);
			} catch (Throwable e) {
				model.setException(e);
			}

			break;
		}

		m_jspViewer.view(ctx, model);
	}
}
