package com.dianping.cat.system.alarm;

import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.system.alarm.connector.Connector;
import com.dianping.cat.system.alarm.threshold.ThresholdDataEntity;
import com.dianping.cat.system.alarm.threshold.ThresholdRule;
import com.dianping.cat.system.alarm.threshold.ThresholdRuleManager;
import com.dianping.cat.system.alarm.threshold.event.ExceptionDataEvent;
import com.dianping.cat.system.alarm.threshold.event.ServiceDataEvent;
import com.dianping.cat.system.event.EventDispatcher;

public class AlarmTask implements Task, LogEnabled {

	@Inject
	private Connector m_connector;

	@Inject
	private EventDispatcher m_dispatcher;

	@Inject
	private ThresholdRuleManager m_manager;

	private Logger m_logger;

	private static final int MAX_DURATION = 29 * 1000;

	@Override
	public String getName() {
		return "Exception-Service-Alarm";
	}

	@Override
	public void run() {
		boolean active = true;
		try {
			Thread.sleep(1000 * 10);
		} catch (InterruptedException e) {
			active = false;
		}

		while (active) {
			long time = System.currentTimeMillis();

			try {
				processExceptionRule();
				processServiceRule();
			} catch (Throwable e) {
				m_logger.error("Error in alarm task!", e);
				Cat.logError(e);
			}

			long duration = System.currentTimeMillis() - time;

			if (duration < MAX_DURATION) {
				try {
					Thread.sleep(MAX_DURATION - duration);
				} catch (InterruptedException e) {
					active = false;
				}
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				active = false;
			}
		}
	}

	private void processServiceRule() {
		List<ThresholdRule> rules = m_manager.getAllServiceRules();
		Transaction t = Cat.newTransaction("Alarm", "ProcessServiceRule");
		
		try {
			for (ThresholdRule rule : rules) {
				try {
					String connectUrl = rule.getConnectUrl();
					ThresholdDataEntity entity = m_connector.fetchAlarmData(connectUrl);

					if (entity != null) {
						entity.setDomain(rule.getDomain());
						m_dispatcher.dispatch(new ServiceDataEvent(entity));
					}
				} catch (Exception e) {
					t.setStatus(e);
					Cat.logError(e);
				}
			}
			t.setStatus(Transaction.SUCCESS);
		} catch (Exception e) {
			m_logger.error("Error in process service rule task!", e);
			t.setStatus(e);
		} finally {
			t.complete();
		}
	}

	private void processExceptionRule() {
		List<ThresholdRule> rules = m_manager.getAllExceptionRules();
		Transaction t = Cat.newTransaction("Alarm", "ProcessExceptionRule");
		
		try {
			for (ThresholdRule rule : rules) {
				try {
					String connectUrl = rule.getConnectUrl();
					ThresholdDataEntity entity = m_connector.fetchAlarmData(connectUrl);

					if (entity != null) {
						entity.setDomain(rule.getDomain());
						m_dispatcher.dispatch(new ExceptionDataEvent(entity));
					}
				} catch (Exception e) {
					t.setStatus(e);
					Cat.logError(e);
				}
			}
			t.setStatus(Transaction.SUCCESS);
		} catch (Exception e) {
			m_logger.error("Error in process exception rule task!", e);
			t.setStatus(e);
		} finally {
			t.complete();
		}
	}

	@Override
	public void shutdown() {
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

}
