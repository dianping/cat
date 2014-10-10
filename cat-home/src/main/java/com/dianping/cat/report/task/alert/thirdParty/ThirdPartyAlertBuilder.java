package com.dianping.cat.report.task.alert.thirdParty;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.alert.thirdparty.entity.Http;
import com.dianping.cat.home.alert.thirdparty.entity.Par;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.system.config.ThirdPartyConfigManager;

public class ThirdPartyAlertBuilder implements Task, LogEnabled {

	@Inject
	private HttpConnector m_httpConnector;

	@Inject
	private ThirdPartyAlert m_thirdPartyAlert;

	@Inject
	private ThirdPartyConfigManager m_configManager;

	private static final long DURATION = TimeHelper.ONE_MINUTE;

	private Logger m_logger;

	@Override
	public void run() {
		boolean active = true;

		while (active) {
			long current = System.currentTimeMillis();
			int minute = Calendar.getInstance().get(Calendar.MINUTE);
			String minuteStr = String.valueOf(minute);

			if (minute < 10) {
				minuteStr = '0' + minuteStr;
			}
			Transaction t = Cat.newTransaction("ThirdPartyAlertBuilder", "M" + minuteStr);

			try {
				buildAlertEntities(current);
				t.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				t.setStatus(e);
				m_logger.error(e.getMessage(), e);
			} finally {
				t.complete();
			}

			long duration = System.currentTimeMillis() - current;

			try {
				if (duration < DURATION) {
					Thread.sleep(DURATION - duration);
				}
			} catch (InterruptedException e) {
				active = false;
			}
		}
	}

	public void putAlertEnity(ThirdPartyAlertEntity entity) {
		m_thirdPartyAlert.put(entity);
	}

	private String buildPars(Map<String, Par> paras) {
		String[] s = new String[paras.size()];
		int i = 0;

		for (Entry<String, Par> entry : paras.entrySet()) {
			Par par = entry.getValue();
			s[i++] = par.getId() + "=" + entry.getValue().getValue();
		}
		return StringUtils.join(s, "&");
	}

	public boolean connectHttpUrl(Http http) {
		boolean result = false;
		String type = http.getType();
		String url = http.getUrl();
		Map<String, Par> paras = http.getPars();
		String joined = null;

		if (paras != null) {
			joined = buildPars(paras);
		}

		if ("get".equalsIgnoreCase(type)) {
			if (StringUtils.isNotEmpty(joined)) {
				url += "?" + joined;
			}
			result = m_httpConnector.readFromGet(url);
		} else if ("post".equalsIgnoreCase(type)) {
			result = m_httpConnector.readFromPost(url, joined);
		}
		return result;
	}

	private void buildAlertEntities(long current) {
		List<Http> https = m_configManager.queryHttps();

		for (Http http : https) {
			if (!connectHttpUrl(http)) {
				Threads.forGroup("cat").start(new HttpReconnector(this, http, current + DURATION));
			}
		}
	}

	@Override
	public String getName() {
		return "thirdParty-alert-task";
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void shutdown() {
	}

	public ThirdPartyAlertEntity buildAlertEntity(Http http) {
		ThirdPartyAlertEntity entity = new ThirdPartyAlertEntity();
		String url = http.getUrl();
		String type = http.getType();
		Map<String, Par> pars = http.getPars();
		String details = "HTTP URL[" + url + "?" + buildPars(pars) + "] " + type.toUpperCase() + "访问出现异常";

		entity.setDomain(http.getDomain()).setType(type).setDetails(details);
		return entity;
	}

	public class HttpReconnector implements Task {

		private ThirdPartyAlertBuilder m_alertTask;

		private int m_retryTimes = 2;

		private Http m_http;

		private long m_deadLine;

		public HttpReconnector(ThirdPartyAlertBuilder alertTask, Http http, long deadLine) {
			m_http = http;
			m_alertTask = alertTask;
			m_deadLine = deadLine;
		}

		@Override
		public void run() {
			while (true) {
				if (m_retryTimes > 0 && System.currentTimeMillis() < m_deadLine) {
					m_retryTimes--;
					if (m_alertTask.connectHttpUrl(m_http)) {
						break;
					} else {
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
						}
					}
				} else {
					m_alertTask.putAlertEnity(m_alertTask.buildAlertEntity(m_http));
					break;
				}
			}
		}

		@Override
		public String getName() {
			return "http-reconnector";
		}

		@Override
		public void shutdown() {
		}
	}
}
