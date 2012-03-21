package com.dianping.cat.report.page.home;

import java.io.IOException;

import javax.servlet.ServletException;

import com.dianping.cat.consumer.RealtimeConsumer;
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
	private RealtimeConsumer m_realtimeConsumer;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "home")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		Payload payload = ctx.getPayload();

		if (payload.getAction() == Action.CHECKPOINT) {
			m_realtimeConsumer.doCheckpoint();
		}
	}

	@Override
	@OutboundActionMeta(name = "home")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);

		model.setAction(Action.VIEW);
		model.setPage(ReportPage.HOME);

		m_jspViewer.view(ctx, model);
	}
}
