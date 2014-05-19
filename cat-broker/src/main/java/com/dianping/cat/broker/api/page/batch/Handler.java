package com.dianping.cat.broker.api.page.batch;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.dianping.cat.broker.api.ApiPage;
import com.dianping.cat.broker.api.page.MonitorEntity;
import com.dianping.cat.broker.api.page.MonitorManager;
import com.dianping.cat.broker.api.page.RequestUtils;

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
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "batch")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "batch")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		HttpServletRequest request = ctx.getHttpServletRequest();
		String userIp = m_util.getRemoteIp(request);

		if (userIp != null) {
			String content = payload.getContent();
			String[] lines = content.split("\n");

			if (userIp != null) {
				for (String line : lines) {
					String[] tabs = line.split("\t");

					if (tabs.length == 5) {
						MonitorEntity entity = new MonitorEntity();

						entity.setTimestamp(Long.parseLong(tabs[0]));
						entity.setTargetUrl(tabs[1]);
						entity.setDuration(Double.parseDouble(tabs[1]));
						entity.setErrorCode(tabs[3]);
						entity.setHttpStatus(tabs[4]);
						entity.setIp(userIp);

						m_manager.offer(entity);
					}
				}
			}
		} else {
			m_logger.info("unknown http request, x-forwarded-for:" + request.getHeader("x-forwarded-for"));
		}

		model.setAction(Action.VIEW);
		model.setPage(ApiPage.BATCH);

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}
}
