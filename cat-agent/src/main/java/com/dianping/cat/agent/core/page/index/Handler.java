package com.dianping.cat.agent.core.page.index;

import java.io.IOException;

import javax.servlet.ServletException;

import org.unidal.helper.Threads;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.agent.core.CorePage;
import com.dianping.cat.agent.monitor.DataSender;
import com.dianping.cat.agent.monitor.TaskExecutors;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private DataSender m_dataSender;

	@Inject
	private TaskExecutors m_taskExecutors;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "index")
	public void handleInbound(Context ctx) throws ServletException, IOException {
	}

	@Override
	@OutboundActionMeta(name = "index")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);

		model.setAction(Action.VIEW);
		model.setPage(CorePage.INDEX);
		
		Threads.forGroup("Cat").start(m_dataSender);
		Threads.forGroup("Cat").start(m_taskExecutors);

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}
}
