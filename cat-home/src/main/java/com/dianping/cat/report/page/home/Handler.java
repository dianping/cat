package com.dianping.cat.report.page.home;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.TreeMap;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.analysis.MessageConsumer;
import com.dianping.cat.analysis.TcpSocketReceiver;
import com.dianping.cat.report.ReportPage;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private TcpSocketReceiver m_receiver;

	@Inject
	private MessageConsumer m_realtimeConsumer;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "home")
	public void handleInbound(Context ctx) throws ServletException, IOException {
	}

	@Override
	@OutboundActionMeta(name = "home")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		switch (payload.getAction()) {
		case THREAD_DUMP:
			showThreadDump(model, payload);
			break;
		case VIEW:
			break;
		case CHECKPOINT:
			m_receiver.destory();
			m_realtimeConsumer.doCheckpoint();
			break;
		default:
			break;
		}

		model.setAction(payload.getAction());
		model.setPage(ReportPage.HOME);
		model.setDomain(payload.getDomain());
		model.setDate(payload.getDate());
		m_jspViewer.view(ctx, model);
	}

	private void showThreadDump(Model model, Payload payload) {
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		ThreadInfo[] threads = bean.dumpAllThreads(true, true);
		StringBuilder sb = new StringBuilder(32768);
		int index = 1;

		TreeMap<String, ThreadInfo> sortedThreads = new TreeMap<String, ThreadInfo>();

		for (ThreadInfo thread : threads) {
			sortedThreads.put(thread.getThreadName(), thread);
		}

		sb.append("Threads: ").append(threads.length);
		sb.append("<pre>");

		for (ThreadInfo thread : sortedThreads.values()) {
			sb.append(index++).append(": <a href=\"#").append(thread.getThreadId()).append("\">")
			      .append(thread.getThreadName()).append("</a>\r\n");
		}

		sb.append("\r\n");
		sb.append("\r\n");

		index = 1;

		for (ThreadInfo thread : sortedThreads.values()) {
			sb.append("<a name=\"").append(thread.getThreadId()).append("\">").append(index++).append(": ").append(thread)
			      .append("\r\n");
		}

		sb.append("</pre>");

		model.setContent(sb.toString());
	}
}
