package com.dianping.cat.broker.api.page.single;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.dianping.cat.Cat;
import com.dianping.cat.broker.api.ApiPage;
import com.dianping.cat.broker.api.page.MonitorEntity;
import com.dianping.cat.broker.api.page.MonitorManager;
import com.dianping.cat.broker.api.page.RequestUtils;
import com.dianping.cat.message.Event;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context>, LogEnabled {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private MonitorManager m_manager;

	@Inject
	private RequestUtils m_util;

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "single")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "single")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		HttpServletRequest request = ctx.getHttpServletRequest();

		model.setAction(Action.VIEW);
		model.setPage(ApiPage.SINGLE);

		MonitorEntity entity = new MonitorEntity();
		String userIp = m_util.getRemoteIp(request);

		if (userIp != null) {
			Cat.logEvent("Ip", "hit", Event.SUCCESS, userIp);

			entity.setDuration(payload.getDuration());
			entity.setErrorCode(payload.getErrorCode());
			entity.setHttpStatus(payload.getHttpStatus());
			entity.setIp(userIp);
			entity.setTargetUrl(payload.getTargetUrl());
			entity.setTimestamp(payload.getTimestamp());

			m_manager.offer(entity);
		} else {
			Cat.logEvent("Ip", "miss", Event.SUCCESS, request.getHeader("x-forwarded-for"));

			m_logger.info("unknown http request, x-forwarded-for:" + request.getHeader("x-forwarded-for"));
		}

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}
}
