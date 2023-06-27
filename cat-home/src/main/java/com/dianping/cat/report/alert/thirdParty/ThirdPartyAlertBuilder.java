package com.dianping.cat.report.alert.thirdParty;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.alert.thirdparty.entity.Http;
import com.dianping.cat.home.alert.thirdparty.entity.Par;
import com.dianping.cat.message.Transaction;

@Named
public class ThirdPartyAlertBuilder implements Task, LogEnabled {

	@Inject
	private HttpConnector m_httpConnector;

	@Inject
	private ThirdPartyAlert m_thirdPartyAlert;

	@Inject
	private ThirdPartyConfigManager m_configManager;

	private static final long DURATION = TimeHelper.ONE_MINUTE;

	private Logger m_logger;

	private static final int MAX_THREADS = 3;

	private static ThreadPoolExecutor s_threadPool = new ThreadPoolExecutor(MAX_THREADS, MAX_THREADS, 10,
	      TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10), new ThreadPoolExecutor.CallerRunsPolicy());

	private void buildAlertEntities(long current) {
		List<Http> https = m_configManager.queryHttps();

		for (Http http : https) {
			if (!connectHttpUrl(http)) {
				s_threadPool.submit(new HttpReconnector(this, http, current + DURATION));
			}
		}
	}

	public ThirdPartyAlertEntity buildAlertEntity(Http http) {
		ThirdPartyAlertEntity entity = new ThirdPartyAlertEntity();
		String url = http.getUrl();
		String type = http.getType();
		List<Par> pars = http.getPars();
		String details = "HTTP URL[" + url + "?" + buildPars(pars) + "] " + type.toUpperCase() + "访问出现异常";

		entity.setDomain(http.getDomain()).setType(type).setDetails(details);
		return entity;
	}

	private String buildPars(List<Par> paras) {
		String[] s = new String[paras.size()];
		int i = 0;

		for (Par entry : paras) {
			s[i++] = entry.getId();
		}
		return StringUtils.join(s, "&");
	}

	public boolean connectHttpUrl(Http http) {
		boolean result = false;
		String type = http.getType();
		String url = http.getUrl();
		List<Par> paras = http.getPars();
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

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public String getName() {
		return "thirdParty-alert-task";
	}

	public void putAlertEnity(ThirdPartyAlertEntity entity) {
		m_thirdPartyAlert.put(entity);
	}

	@Override
	public void run() {
		boolean active = true;

		while (active) {
			long current = System.currentTimeMillis();
			Transaction t = Cat.newTransaction("ReloadTask", "AlertThirdPartyBuilder");

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

	@Override
	public void shutdown() {
	}

	public class HttpReconnector implements Task {

		private ThirdPartyAlertBuilder m_alertBuilder;

		private int m_retryTimes = 2;

		private Http m_http;

		private long m_deadLine;

		public HttpReconnector(ThirdPartyAlertBuilder alertBuilder, Http http, long deadLine) {
			m_http = http;
			m_alertBuilder = alertBuilder;
			m_deadLine = deadLine;
		}

		@Override
		public String getName() {
			return "http-reconnector";
		}

		@Override
		public void run() {
			while (true) {
				if (m_retryTimes > 0 && System.currentTimeMillis() < m_deadLine) {
					m_retryTimes--;
					if (m_alertBuilder.connectHttpUrl(m_http)) {
						break;
					} else {
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
						}
					}
				} else {
					m_alertBuilder.putAlertEnity(m_alertBuilder.buildAlertEntity(m_http));
					break;
				}
			}
		}

		@Override
		public void shutdown() {
		}
	}
}
