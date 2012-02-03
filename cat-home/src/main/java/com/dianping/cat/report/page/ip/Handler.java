package com.dianping.cat.report.page.ip;

import java.io.IOException;

import javax.servlet.ServletException;

import com.dianping.cat.consumer.RealtimeConsumer;
import com.dianping.cat.consumer.ip.IpAnalyzer;
import com.dianping.cat.consumer.ip.model.entity.IpReport;
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
	@InboundActionMeta(name = "ip")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "ip")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.IP);

		IpAnalyzer analyzer = (IpAnalyzer) m_consumer.getCurrentAnalyzer("ip");

		if (analyzer != null) {
			Payload payload = ctx.getPayload();
			String domain = payload.getDomain();

			model.setReport(analyzer.generate(domain));
		} else {
			model.setReport(new IpReport());
		}

		m_jspViewer.view(ctx, model);
	}
}
