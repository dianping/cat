package com.dianping.cat.system.alarm;

import java.util.List;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.system.alarm.connector.Connector;
import com.dianping.cat.system.alarm.threshold.ThresholdDataEntity;
import com.dianping.cat.system.alarm.threshold.ThresholdRule;
import com.dianping.cat.system.alarm.threshold.ThresholdRuleManager;
import com.dianping.cat.system.alarm.threshold.event.ExceptionDataEvent;
import com.dianping.cat.system.alarm.threshold.event.ServiceDataEvent;
import com.dianping.cat.system.event.EventDispatcher;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

public class AlarmTask implements Task {

	@Inject
	private Connector m_connector;

	@Inject
	private EventDispatcher m_dispatcher;

	@Inject
	private ThresholdRuleManager m_manager;

	@Override
	public String getName() {
		return "Exception-Service-Alarm";
	}

	@Override
	public void run() {
		boolean active = true;

		while (active) {
			long time = System.currentTimeMillis();

			try {
				processExceptionRule();
				processServiceRule();
			} catch (Exception e) {
				Cat.logError(e);
			}

			long duration = System.currentTimeMillis() - time;

			if (duration < 29 * 1000) {
				try {
					Thread.sleep(29 * 1000 - duration);
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

		for (ThresholdRule rule : rules) {
			try {
				String connectUrl = rule.getConnectUrl();
				ThresholdDataEntity entity = m_connector.fetchAlarmData(connectUrl);

				if (entity != null) {
					String domain = rule.getDomain();

					entity.setDomain(domain);
					Cat.getProducer().logEvent("AlarmRule", domain + "[" + rule.getRuleId() + "]", Event.SUCCESS,
					      entity.toString());

					ServiceDataEvent event = new ServiceDataEvent(entity);
					m_dispatcher.dispatch(event);
				}
			} catch (Exception e) {
				t.setStatus(e);
				Cat.logError(e);
			}
		}
		t.setStatus(Transaction.SUCCESS);
		t.complete();
	}

	private void processExceptionRule() {
		List<ThresholdRule> rules = m_manager.getAllExceptionRules();
		Transaction t = Cat.newTransaction("Alarm", "ProcessExceptionRule");

		for (ThresholdRule rule : rules) {
			try {
				String connectUrl = rule.getConnectUrl();
				ThresholdDataEntity entity = m_connector.fetchAlarmData(connectUrl);

				if (entity != null) {
					entity.setDomain(rule.getDomain());
					Cat.getProducer().logEvent("AlarmUrl", connectUrl, Event.SUCCESS, entity.toString());

					ExceptionDataEvent event = new ExceptionDataEvent(entity);

					m_dispatcher.dispatch(event);
				}
			} catch (Exception e) {
				t.setStatus(e);
				Cat.logError(e);
			}
		}
		t.setStatus(Transaction.SUCCESS);
		t.complete();
	}

	@Override
	public void shutdown() {
	}

}
