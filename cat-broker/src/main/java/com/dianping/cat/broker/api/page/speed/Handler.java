package com.dianping.cat.broker.api.page.speed;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.broker.api.app.AppConsumer;
import com.dianping.cat.broker.api.app.proto.AppSpeedProto;
import com.dianping.cat.broker.api.app.proto.ProtoData;
import com.dianping.cat.broker.api.page.RequestUtils;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.config.app.AppSpeedConfigManager;
import com.dianping.cat.message.Event;
import com.dianping.cat.service.IpService;
import com.dianping.cat.service.IpService.IpInfo;

import org.unidal.helper.Splitters;
import org.unidal.lookup.util.StringUtils;

public class Handler implements PageHandler<Context>, LogEnabled {

	@Inject
	private AppConsumer m_appDataConsumer;

	@Inject
	private IpService m_ipService;

	@Inject
	private AppConfigManager m_appConfigManager;

	@Inject
	private AppSpeedConfigManager m_appSpeedConfigManager;

	@Inject
	private RequestUtils m_util;

	private Logger m_logger;

	private volatile int m_error;

	private static final String VERSION_ONE = "1";

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "speed")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "speed")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Payload payload = ctx.getPayload();
		HttpServletRequest request = ctx.getHttpServletRequest();
		HttpServletResponse response = ctx.getHttpServletResponse();
		String userIp = m_util.getRemoteIp(request);
		String version = payload.getVersion();
		boolean success = true;

		if (userIp != null) {
			success = processVersions(payload, request, userIp, version);
		} else {
			success = false;
			Cat.logEvent("UnknownIp", "speed", Event.SUCCESS, null);
			m_logger.info("unknown http request, x-forwarded-for:" + request.getHeader("x-forwarded-for"));
		}

		if (success) {
			response.getWriter().write("OK");
		} else {
			response.getWriter().write("ERROR");
		}
	}

	private boolean processVersions(Payload payload, HttpServletRequest request, String userIp, String version) {
		boolean success = false;

		if (VERSION_ONE.equals(version)) {
			Pair<Integer, Integer> infoPair = queryNetworkInfo(request, userIp);

			if (infoPair != null) {
				int cityId = infoPair.getKey();
				int operatorId = infoPair.getValue();
				String content = payload.getContent();

				processVersion1Content(cityId, operatorId, content, version);
				success = true;
			}
		} else {
			Cat.logEvent("InvalidVersion", "speed:" + version, Event.SUCCESS, version);
		}
		return success;
	}

	private void offerQueue(ProtoData appData) {
		boolean success = m_appDataConsumer.enqueue(appData);

		if (!success) {
			m_error++;

			if (m_error % 1000 == 0) {
				Cat.logEvent("Discard", "Speed", Event.SUCCESS, null);
				m_logger.error("Error when offer appData to queue , discard number " + m_error);
			}
		}
	}

	private void processVersion1Record(Integer cityId, Integer operatorId, String record) {
		String[] items = record.split("\t");
		int length = items.length;

		if (length == 6) {
			try {
				String page = URLDecoder.decode(items[4], "utf-8").toLowerCase();

				// appData.setTimestamp(Long.parseLong(items[0]));
				long current = System.currentTimeMillis();
				int network = Integer.parseInt(items[1]);
				int version = Integer.parseInt(items[2]);
				int platform = Integer.parseInt(items[3]);

				for (int i = 5; i < length; i++) {
					AppSpeedProto appData = new AppSpeedProto();

					appData.setTimestamp(current);
					appData.setNetwork(network);
					appData.setVersion(version);
					appData.setPlatform(platform);
					appData.setCity(cityId);
					appData.setOperator(operatorId);
					offerAppSpeedData(appData, page, items, i);
				}
			} catch (Exception e) {
				Cat.logError(e);
				m_logger.error(e.getMessage(), e);
			}
		} else {
			Cat.logEvent("InvalidRecord", "speed", Event.SUCCESS, null);
		}
	}

	private void offerAppSpeedData(AppSpeedProto appData, String page, String[] items, int i) {
		List<String> fields = Splitters.by("-").split(items[i]);
		String step = fields.get(0);
		long responseTime = Long.parseLong(fields.get(1));
		int id = m_appSpeedConfigManager.querySpeedId(page, step);

		if (id > 0) {
			boolean slow = responseTime > m_appSpeedConfigManager.querSpeedThreshold(page, step);
			appData.setSpeedId(id);

			if (slow) {
				appData.setSlowCount(1);
				appData.setSlowResponseTime(responseTime);
			} else {
				appData.setCount(1);
				appData.setResponseTime(responseTime);
			}

			if (responseTime >= 0) {
				offerQueue(appData);
				Cat.logEvent("Page", page, Event.SUCCESS, null);
			} else {
				Cat.logEvent("Speed.ResponseTimeError", page, Event.SUCCESS, String.valueOf(responseTime));
			}
		} else {
			Cat.logEvent("UnknownPage", page, Event.SUCCESS, page);
		}
	}

	private Pair<Integer, Integer> queryNetworkInfo(HttpServletRequest request, String userIp) {
		IpInfo ipInfo = m_ipService.findIpInfoByString(userIp);

		if (ipInfo != null) {
			String province = ipInfo.getProvince();
			String operatorStr = ipInfo.getChannel();
			Integer cityId = m_appConfigManager.getCities().get(province);
			Integer operatorId = m_appConfigManager.getOperators().get(operatorStr);

			if (cityId != null && operatorId != null) {
				return new Pair<Integer, Integer>(cityId, operatorId);
			}
		}
		return null;
	}

	private void processVersion1Content(Integer cityId, Integer operatorId, String content, String version) {
		if (StringUtils.isNotEmpty(content)) {
			String[] records = content.split("\n");

			for (String record : records) {
				try {
					if (StringUtils.isNotEmpty(record)) {
						processVersion1Record(cityId, operatorId, record);
					}
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		} else {
			Cat.logEvent("contentEmpty", "speed:1", Event.SUCCESS, null);
		}
	}

}
