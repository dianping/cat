package com.dianping.cat.broker.api.page.batch;

import java.io.IOException;
import java.net.URLDecoder;

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

import com.dianping.cat.Cat;
import com.dianping.cat.broker.api.app.AppData;
import com.dianping.cat.broker.api.app.AppDataConsumer;
import com.dianping.cat.broker.api.page.Constrants;
import com.dianping.cat.broker.api.page.MonitorEntity;
import com.dianping.cat.broker.api.page.MonitorManager;
import com.dianping.cat.broker.api.page.RequestUtils;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.message.Event;
import com.dianping.cat.service.IpService;
import com.dianping.cat.service.IpService.IpInfo;

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

	private volatile int m_error;

	public static final String TOO_LONG = "toolongurl.bin";

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
		boolean success = true;

		if (userIp != null) {
			if ("1".equals(version)) {
				processVersion1(payload, request, userIp);
			} else if ("2".equals(version)) {
				processVersion2(payload, request, userIp);
			} else {
				success = false;
				Cat.logEvent("InvalidVersion", version, Event.SUCCESS, version);
			}
		} else {
			success = false;
			Cat.logEvent("unknownIp", "batch", Event.SUCCESS, null);
			m_logger.info("unknown http request, x-forwarded-for:" + request.getHeader("x-forwarded-for"));
		}

		if (success) {
			response.getWriter().write("OK");
		} else {
			response.getWriter().write("validate request!");
		}
	}

	private void processVersion1(Payload payload, HttpServletRequest request, String userIp) {
		try {
			String content = payload.getContent();
			String[] lines = content.split("\n");
			long time = System.currentTimeMillis();

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
					// entity.setTimestamp(Long.parseLong(tabs[0]));
					entity.setTimestamp(time);
					entity.setTargetUrl(tabs[1]);
					entity.setDuration(Double.parseDouble(tabs[2]));
					entity.setHttpStatus(httpStatus);
					entity.setErrorCode(errorCode);
					entity.setIp(userIp);

					if ("200".equals(httpStatus)) {
						entity.setCount(10);
					} else {
						entity.setCount(1);
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
					try {
						if (!StringUtils.isEmpty(record)) {
							processOneRecord(cityId, operatorId, record);
						}
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
			} else {
				Cat.logEvent("Unknown", province + ":" + operatorStr, Event.SUCCESS, null);
			}
		}
	}

	private void processOneRecord(int cityId, int operatorId, String record) {
		String items[] = record.split("\t");

		if (items.length == 10) {
			AppData appData = new AppData();

			try {
				String url = URLDecoder.decode(items[4], "utf-8");
				Integer command = m_appConfigManager.getCommands().get(url);

				if (command != null) {
					// appData.setTimestamp(Long.parseLong(items[0]));
					appData.setTimestamp(System.currentTimeMillis());
					appData.setCommand(command);
					appData.setNetwork(Integer.parseInt(items[1]));
					appData.setVersion(Integer.parseInt(items[2]));
					appData.setConnectType(Integer.parseInt(items[3]));
					appData.setCode(Integer.parseInt(items[5]));
					appData.setPlatform(Integer.parseInt(items[6]));
					appData.setRequestByte(Integer.parseInt(items[7]));
					appData.setResponseByte(Integer.parseInt(items[8]));
					appData.setResponseTime(Integer.parseInt(items[9]));
					appData.setCity(cityId);
					appData.setOperator(operatorId);
					appData.setCount(1);

					int responseTime = appData.getResponseTime();

					if (responseTime < 60 * 1000 && responseTime >= 0) {
						offerQueue(appData);

						Cat.logEvent("Command", url, Event.SUCCESS, null);
					} else if (responseTime > 0) {
						Integer tooLong = m_appConfigManager.getCommands().get(TOO_LONG);

						if (tooLong != null) {
							appData.setCommand(tooLong);
							offerQueue(appData);
						}
						Cat.logEvent("ResponseTooLong", url, Event.SUCCESS, String.valueOf(responseTime));
					} else {
						Cat.logEvent("ResponseTimeError", url, Event.SUCCESS, String.valueOf(responseTime));
					}
				} else {
					Cat.logEvent("CommandNotFound", url, Event.SUCCESS, items[4]);
				}
			} catch (Exception e) {
				m_logger.error(e.getMessage(), e);
			}
		} else {
			Cat.logEvent("InvalidPar", items[4], Event.SUCCESS, items[4]);
		}
	}

	private void offerQueue(AppData appData) {
		boolean success = m_appDataConsumer.enqueue(appData);

		if (!success) {
			m_error++;

			if (m_error % 1000 == 0) {
				Cat.logEvent("Discard", "AppDataConsumer", Event.SUCCESS, null);
				m_logger.error("Error when offer appData to queue , discard number " + m_error);
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
