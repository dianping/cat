package com.dianping.cat.report.page.transaction;

import java.io.IOException;

import javax.servlet.ServletException;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.AbstractGraphPayload;
import com.dianping.cat.report.graph.GraphBuilder;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.site.lookup.annotation.Inject;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

/**
 * @author sean.wang
 * @since Feb 6, 2012
 */
public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject(type = ModelService.class, value = "transaction")
	private ModelService<TransactionReport> m_service;

	@Inject
	private GraphBuilder m_builder;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "t")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "t")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		model.setAction(payload.getAction());
		model.setPage(ReportPage.TRANSACTION);

		switch (payload.getAction()) {
		case VIEW:
			showReport(model, payload);
			break;
		case GRAPHS:
			String graph = m_builder.build(new AbstractGraphPayload("Duration Chart", "Hour of day", "Average time(ms)") {
				@Override
				public double[] getValues() {
					double[] values = new double[12];

					for (int i = 0; i < values.length; i++) {
						values[i] = Math.random() * (i + 1) * 110;
					}

					return values;
				}
			});

			model.setGraph(graph);
			break;
		}

		m_jspViewer.view(ctx, model);
	}

	private void showReport(Model model, Payload payload) {
		try {
			String domain = payload.getDomain();
			ModelRequest request = new ModelRequest(domain, payload.getPeriod());
			ModelResponse<TransactionReport> response = m_service.invoke(request);
			TransactionReport report = response.getModel();

			model.setReport(report);
		} catch (Throwable e) {
			model.setException(e);
		}
	}
}
