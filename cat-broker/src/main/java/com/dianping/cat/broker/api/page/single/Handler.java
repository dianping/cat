package com.dianping.cat.broker.api.page.single;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.dianping.cat.broker.api.ApiPage;
import com.dianping.cat.broker.api.page.MonitorEntity;
import com.dianping.cat.broker.api.page.MonitorManager;
import com.dianping.cat.broker.api.page.RequestUtils;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private MonitorManager m_manager;

	@Inject
	private RequestUtils m_util;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "signal")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "signal")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		HttpServletRequest request = ctx.getHttpServletRequest();

		model.setAction(Action.VIEW);
		model.setPage(ApiPage.SINGLE);

		MonitorEntity entity = new MonitorEntity();
		String userIp = m_util.getRemoteIp(request);

		if (userIp != null) {
			entity.setDuration(payload.getDuration());
			entity.setErrorCode(payload.getErrorCode());
			entity.setHttpCode(payload.getHttpCode());
			entity.setIp(userIp);
			entity.setTargetUrl(payload.getTargetUrl());
			entity.setTimestamp(payload.getTimestamp());

			m_manager.offer(entity);
		}

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}
}
