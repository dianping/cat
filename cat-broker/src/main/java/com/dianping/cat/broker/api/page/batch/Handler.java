package com.dianping.cat.broker.api.page.batch;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.broker.api.page.MonitorEntity;
import com.dianping.cat.broker.api.page.MonitorManager;
import com.dianping.cat.broker.api.page.RequestUtils;

public class Handler implements PageHandler<Context>, LogEnabled {

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
		Payload payload = ctx.getPayload();
		HttpServletRequest request = ctx.getHttpServletRequest();
		HttpServletResponse response = ctx.getHttpServletResponse();
		String userIp = m_util.getRemoteIp(request);

		if (userIp != null) {
			try {
				String content = payload.getContent();
				String[] lines = content.split("\n");

				for (String line : lines) {
					String[] tabs = line.split("\t");

					if (tabs.length == 5) {
						MonitorEntity entity = new MonitorEntity();
						String errorCode = tabs[3];

						if (StringUtils.isEmpty(errorCode)) {
							errorCode = "not-set";
						}
						entity.setTimestamp(Long.parseLong(tabs[0]));
						entity.setTargetUrl(tabs[1]);
						entity.setDuration(Double.parseDouble(tabs[2]));
						entity.setErrorCode(errorCode);
						entity.setHttpStatus(tabs[4]);
						entity.setIp(userIp);

						if (payload.getVersion().equals("1")) {
							entity.setCount(10);
						}
						m_manager.offer(entity);
					}
				}
			} catch (Exception e) {
				m_logger.error(e.getMessage(), e);
			}
		} else {
			m_logger.info("unknown http request, x-forwarded-for:" + request.getHeader("x-forwarded-for"));
		}
		response.getWriter().write("OK");
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}
}
