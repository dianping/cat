package com.dianping.cat.report.page.home;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

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
		Payload payload = ctx.getPayload();

		model.setAction(payload.getAction());
		model.setPage(ReportPage.HOME);
		model.setDomain(payload.getDomain());
		model.setLongDate(payload.getDate());

		switch (payload.getAction()) {
		case THREAD_DUMP:
			showThreadDump(model, payload);
			break;
		case VIEW:
			break;
		}

		m_jspViewer.view(ctx, model);
	}

	private void showThreadDump(Model model, Payload payload) {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		ThreadInfo[] threads = bean.dumpAllThreads(true, true);
		StringBuilder sb = new StringBuilder(8096);
		int index = 1;
		
		sb.append("Threads: ").append(threads.length);
		sb.append("<pre>");
		
		for (ThreadInfo thread: threads) {
			sb.append(index++).append(": ").append(thread).append("\r\n");
		}
		
		sb.append("</pre>");
		
		model.setContent(sb.toString());
	}
}
