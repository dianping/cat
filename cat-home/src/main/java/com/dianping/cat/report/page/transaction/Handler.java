package com.dianping.cat.report.page.transaction;

import java.io.IOException;

import javax.servlet.ServletException;

import com.dianping.cat.consumer.transaction.TransactionReportMessageAnalyzer;
import com.dianping.cat.message.spi.MessageAnalyzer;
import com.dianping.cat.report.ReportPage;
import com.site.lookup.annotation.Inject;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject(type = MessageAnalyzer.class, value = "transaction")
	private TransactionReportMessageAnalyzer m_analyzer;

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

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.TRANSACTION);
		model.setReport(m_analyzer.generate());

		m_jspViewer.view(ctx, model);
	}
}
