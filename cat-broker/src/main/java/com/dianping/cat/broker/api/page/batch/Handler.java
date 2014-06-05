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

import com.dianping.cat.broker.api.page.Constrants;
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
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

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
					// timstampTABtargetUrlTABdurationTABhttpCodeTABerrorCodeENTER
					if (tabs.length == 5 && validate(tabs[3], tabs[4])) {
						MonitorEntity entity = new MonitorEntity();
						String httpStatus = tabs[3];
						String errorCode = tabs[4];

						if (StringUtils.isEmpty(errorCode)) {
							errorCode = Constrants.NOT_SET;
						}
						if (StringUtils.isEmpty(httpStatus)) {
							httpStatus = Constrants.NOT_SET;
						}
						entity.setTimestamp(Long.parseLong(tabs[0]));
						entity.setTargetUrl(tabs[1]);
						entity.setDuration(Double.parseDouble(tabs[2]));
						entity.setHttpStatus(httpStatus);
						entity.setErrorCode(errorCode);
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

	private boolean validate(String errorCode, String httpStatus) {
		try {
			if (StringUtils.isNotEmpty(errorCode) && !Constrants.NOT_SET.equals(errorCode)) {
				Double.parseDouble(errorCode);
			}
			if (StringUtils.isNotEmpty(httpStatus) && !Constrants.NOT_SET.equals(httpStatus)) {
				Double.parseDouble(httpStatus);
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
