package com.dianping.cat.report.page.failure;

import java.io.IOException;

import javax.servlet.ServletException;

import com.dianping.cat.consumer.RealtimeConsumer;
import com.dianping.cat.consumer.failure.FailureReportAnalyzer;
import com.dianping.cat.consumer.failure.model.entity.FailureReport;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.report.ReportPage;
import com.site.lookup.annotation.Inject;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject(type = MessageConsumer.class, value = "realtime")
	private RealtimeConsumer m_consumer;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "f")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "f")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.FAILURE);

		FailureReportAnalyzer analyzer = (FailureReportAnalyzer) m_consumer.getCurrentAnalyzer("failure");

		if (analyzer != null) {
			model.setReport(analyzer.generate());
		} else {
			model.setReport(new FailureReport());
		}

		m_jspViewer.view(ctx, model);
	}
}
