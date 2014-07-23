package com.dianping.cat.report.task.alert.thirdParty;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.alert.thirdParty.entity.Http;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.system.config.ThirdPartyConfigManager;

public class ThirdPartyAlertTask implements Task, LogEnabled {

	@Inject
	private HttpMonitor m_httpMonitor;

	@Inject
	private ThirdPartyAlert m_thirdPartyAlert;

	@Inject
	private ThirdPartyConfigManager m_configManager;

	private static final long DURATION = TimeUtil.ONE_MINUTE;

	private Logger m_logger;

	@Override
	public void run() {
		boolean active = true;

		while (active) {
			long current = System.currentTimeMillis();
			Transaction t = Cat.newTransaction("Task", "ThirdPartyAlert");

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

	private String connectHttpUrl(Http http) {
		String type = http.getType();
		String result = null;
		String paras = http.getPars();

		if (StringUtils.isNotEmpty(paras)) {
			paras = paras.replaceAll("[;]", "&");
			http.setPars(paras);
		}

		try {
			if ("get".equalsIgnoreCase(type)) {
				String url = http.getUrl();

				if (StringUtils.isNotEmpty(paras)) {
					url += "?" + paras;
				}
				result = m_httpMonitor.readFromGet(url);
			} else if ("post".equalsIgnoreCase(type)) {
				String url = http.getUrl();
				result = m_httpMonitor.readFromPost(url, paras);
			}
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
		}
		return result;
	}

	private void buildAlertEntities(long current) {
		List<Http> https = m_configManager.queryHttps();

		for (Http http : https) {
			String result = connectHttpUrl(http);

			if (StringUtils.isEmpty(result)) {
				new HttpReconnector(this, http, current + DURATION);
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
		String pars = http.getPars();
		String details = "HTTP URL[" + url + "?" + pars + "] " + type.toUpperCase() + "访问出现异常";

		entity.setDomains(http.getDomains()).setType(type).setDetails(details);
		return entity;
	}

	public class HttpReconnector {
		private Timer m_timer;

		public HttpReconnector(ThirdPartyAlertTask alertTask, Http http, long deadLine) {
			m_timer = new Timer();
			m_timer.schedule(new RemindTask(alertTask, http, deadLine), 0, 5 * 1000);
		}

		class RemindTask extends TimerTask {
			private int m_reconncetNum = 2;

			private ThirdPartyAlertTask m_alertTask;

			private Http m_http;

			private long m_deadLine;

			public RemindTask(ThirdPartyAlertTask alertTask, Http http, long deadLine) {
				m_alertTask = alertTask;
				m_http = http;
				m_deadLine = deadLine;
			}

			public void run() {
				if (m_reconncetNum > 0 && System.currentTimeMillis() < m_deadLine) {
					m_reconncetNum--;
					String result = m_alertTask.connectHttpUrl(m_http);

					if (StringUtils.isNotEmpty(result)) {
						m_timer.cancel();
					}
				} else {
					m_timer.cancel();
					m_alertTask.putAlertEnity(m_alertTask.buildAlertEntity(m_http));
				}
			}
		}
	}

}
