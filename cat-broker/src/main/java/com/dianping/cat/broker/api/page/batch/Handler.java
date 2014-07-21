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

import com.dianping.cat.broker.api.app.AppData;
import com.dianping.cat.broker.api.app.AppDataConsumer;
import com.dianping.cat.broker.api.page.Constrants;
import com.dianping.cat.broker.api.page.IpService;
import com.dianping.cat.broker.api.page.IpService.IpInfo;
import com.dianping.cat.broker.api.page.MonitorEntity;
import com.dianping.cat.broker.api.page.MonitorManager;
import com.dianping.cat.broker.api.page.RequestUtils;
import com.dianping.cat.config.app.AppConfigManager;

public class Handler implements PageHandler<Context>, LogEnabled {

	@Inject
	private AppDataConsumer m_appDataConsumer;

	@Inject
	private IpService m_ipService;

	@Inject
	private AppConfigManager m_appConfigManager;

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
		String version = payload.getVersion();

		if (userIp != null) {
			if (version.equals("1")) {
				processVersion1(payload, request, userIp);
			} else if (version.equals("2")) {
				processVersion2(payload, request, userIp);
			}
		} else {
			m_logger.info("unknown http request, x-forwarded-for:" + request.getHeader("x-forwarded-for"));
		}

		response.getWriter().write("OK");
	}

	private void processVersion1(Payload payload, HttpServletRequest request, String userIp) {
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
	}

	private void processVersion2(Payload payload, HttpServletRequest request, String userIp) {
		String content = payload.getContent();
		String records[] = content.split("\n");
		IpInfo ipInfo = m_ipService.findIpInfoByString(userIp);

		if (ipInfo != null) {
			String province = ipInfo.getProvince();
			String operatorStr = ipInfo.getChannel();
			Integer cityId = m_appConfigManager.getCities().get(province);
			Integer operatorId = m_appConfigManager.getOperators().get(operatorStr);

			if (cityId != null && operatorId != null) {
				for (String record : records) {
					processOneRecord(cityId, operatorId, record);
				}
			}
		}
	}

	private void processOneRecord(int cityId, int operatorId, String record) {
		String items[] = record.split("\t");

		if (items.length == 10) {
			AppData appData = new AppData();

			try {
				appData.setTimestamp(Long.parseLong(items[0]));
				Integer command = m_appConfigManager.getCommands().get(items[1]);

				if (command != null) {
					appData.setCommand(command);
					appData.setNetwork(Integer.parseInt(items[2]));
					appData.setVersion(Integer.parseInt(items[3]));
					appData.setConnectType(Integer.parseInt(items[4]));
					appData.setCode(Integer.parseInt(items[5]));
					appData.setPlatform(Integer.parseInt(items[6]));
					appData.setRequestByte(Integer.parseInt(items[7]));
					appData.setResponseByte(Integer.parseInt(items[8]));
					appData.setResponseTime(Integer.parseInt(items[9]));
					appData.setCity(cityId);
					appData.setOperator(operatorId);
					appData.setCount(1);

					m_appDataConsumer.enqueue(appData);
				}
			} catch (Exception e) {
				m_logger.error(e.getMessage(), e);
			}
		}
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
