package com.dianping.cat.broker.api.page.cdn;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.broker.api.page.MonitorEntity;
import com.dianping.cat.broker.api.page.MonitorManager;
import com.dianping.cat.broker.api.page.RequestUtils;
import com.dianping.cat.message.Event;
import org.unidal.lookup.util.StringUtils;

public class Handler implements PageHandler<Context>, LogEnabled {

	@Inject
	private MonitorManager m_manager;

	@Inject
	private RequestUtils m_util;

	private Logger m_logger;

	private MonitorEntity createEntity(String url, long timestamp, double duration, String userIp) {
		MonitorEntity entity = new MonitorEntity();

		entity.setTimestamp(timestamp);
		entity.setTargetUrl(url);
		entity.setDuration(duration);
		entity.setHttpStatus("200");
		entity.setErrorCode("200");
		entity.setIp(userIp);
		return entity;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "cdn")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "cdn")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Payload payload = ctx.getPayload();
		HttpServletRequest request = ctx.getHttpServletRequest();
		HttpServletResponse response = ctx.getHttpServletResponse();
		String userIp = m_util.getRemoteIp(request);

		if (userIp != null) {
			try {
				String content = payload.getContent();

				if (StringUtils.isNotEmpty(content)) {
					String[] lines = content.split("\n");

					for (String line : lines) {
						if (StringUtils.isNotEmpty(line)) {
							String[] tabs = line.split("\t");
							// timstampTABtargetUrlTABdnslookupTABtcpconnectTABrequestTABresponseENTER
							if (tabs.length == 6) {
								// long timestamp = Long.parseLong(tabs[0]);
								long timestamp = System.currentTimeMillis();
								MonitorEntity entity = createEntity(tabs[1] + "/dnsLookup", timestamp,
								      Double.parseDouble(tabs[2]), userIp);

								m_manager.offer(entity);

								entity = createEntity(tabs[1] + "/tcpConnect", timestamp, Double.parseDouble(tabs[3]), userIp);
								m_manager.offer(entity);

								entity = createEntity(tabs[1] + "/request", timestamp, Double.parseDouble(tabs[4]), userIp);
								m_manager.offer(entity);

								entity = createEntity(tabs[1] + "/response", timestamp, Double.parseDouble(tabs[5]), userIp);
								m_manager.offer(entity);
								Cat.logEvent("Cdn.Hit", tabs[1], Event.SUCCESS, userIp);
							} else {
								Cat.logEvent("InvalidRecord", "cdn", Event.SUCCESS, null);
							}
						}
					}
				} else {
					Cat.logEvent("contentEmpty", "cdn", Event.SUCCESS, null);
				}
			} catch (Exception e) {
				Cat.logError(e);
				m_logger.error(e.getMessage(), e);
			}
		} else {
			Cat.logEvent("UnknownIp", "cdn", Event.SUCCESS, null);
			m_logger.info("unknown http request, x-forwarded-for:" + request.getHeader("x-forwarded-for"));
		}
		response.getWriter().write("OK");
	}
}
