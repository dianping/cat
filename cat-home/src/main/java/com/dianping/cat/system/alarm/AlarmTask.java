package com.dianping.cat.system.alarm;

import java.util.List;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.system.alarm.connector.Connector;
import com.dianping.cat.system.alarm.threshold.ThresholdDataEntity;
import com.dianping.cat.system.alarm.threshold.ThresholdRule;
import com.dianping.cat.system.alarm.threshold.ThresholdRuleManager;
import com.dianping.cat.system.alarm.threshold.event.ExceptionDataEvent;
import com.dianping.cat.system.alarm.threshold.event.ServiceDataEvent;
import com.dianping.cat.system.event.EventDispatcher;
import com.site.helper.Threads.Task;
import com.site.lookup.annotation.Inject;

public class AlarmTask implements Task {

	@Inject
	private Connector m_connector;

	@Inject
	private EventDispatcher m_dispatcher;

	@Inject
	private ThresholdRuleManager m_manager;

	@Override
	public String getName() {
		return "Exception-Alarm";
	}

	@Override
	public void run() {
		boolean active = true;

		while (active) {
			Transaction t = Cat.newTransaction("System", "Alarm");
			long time = System.currentTimeMillis();

			try {
				getExceptionRule();
				getServiceRule();
				t.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				t.setStatus(e);
			}
			t.complete();

			long duration = System.currentTimeMillis() - time;
			try {
				Thread.sleep(20 * 1000 - duration);
			} catch (InterruptedException e) {
				active = false;
			}
		}
	}

	private void getServiceRule() {
		List<ThresholdRule> rules = m_manager.getAllServiceRules();
		
		for (ThresholdRule rule : rules) {
			Transaction t = Cat.newTransaction("ServiceAlarm", rule.getDomain());
			
			try {
				ThresholdDataEntity entity = m_connector.fetchAlarmData(rule.getConnectUrl());
				
				entity.setDomain(rule.getDomain());
				
				ServiceDataEvent event = new ServiceDataEvent(entity);

				m_dispatcher.dispatch(event);
				t.addData(event.toString());
				t.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				t.setStatus(e);
				Cat.logError(e);
			} finally {
				t.complete();
			}
		}
	}

	private void getExceptionRule() {
		List<ThresholdRule> rules = m_manager.getAllExceptionRules();
		
		for (ThresholdRule rule : rules) {
			Transaction t = Cat.newTransaction("ExceptionAlarm", rule.getDomain());
			
			try {
				ThresholdDataEntity entity = m_connector.fetchAlarmData(rule.getConnectUrl());
				
				entity.setDomain(rule.getDomain());
				ExceptionDataEvent event = new ExceptionDataEvent(entity);

				m_dispatcher.dispatch(event);
				t.addData(event.toString());
				t.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				t.setStatus(e);
				Cat.logError(e);
			} finally {
				t.complete();
			}
		}
	}

	@Override
	public void shutdown() {
	}

}
