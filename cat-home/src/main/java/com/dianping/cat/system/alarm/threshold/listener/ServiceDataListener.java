package com.dianping.cat.system.alarm.threshold.listener;

import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.system.alarm.alert.AlertInfo;
import com.dianping.cat.system.alarm.threshold.ThresholdDataEntity;
import com.dianping.cat.system.alarm.threshold.ThresholdRule;
import com.dianping.cat.system.alarm.threshold.ThresholdRuleManager;
import com.dianping.cat.system.alarm.threshold.event.ServiceDataEvent;
import com.dianping.cat.system.alarm.threshold.event.ThresholdAlertEvent;
import com.dianping.cat.system.alarm.threshold.template.ThresholdAlarmMeta;
import com.dianping.cat.system.event.Event;
import com.dianping.cat.system.event.EventDispatcher;
import com.dianping.cat.system.event.EventListener;
import com.dianping.cat.system.event.EventType;

public class ServiceDataListener implements EventListener {

	@Inject
	private EventDispatcher m_dispatcher;

	@Inject
	private ThresholdRuleManager m_manager;

	@Override
	public boolean isEligible(Event event) {
		if (event.getEventType() == EventType.ServiceDataEvent) {
			return true;
		}
		return false;
	}

	@Override
	public void onEvent(Event event) {
		ServiceDataEvent dataEvent = (ServiceDataEvent) event;

		ThresholdDataEntity data = dataEvent.getData();
		List<ThresholdRule> rules = m_manager.getServiceRuleByDomain(data.getDomain());

		for (ThresholdRule rule : rules) {
			ThresholdAlarmMeta alarmMeta = rule.addData(data, AlertInfo.SERVICE);

			if (alarmMeta != null) {
				Transaction t = Cat.newTransaction("SendAlarm", "Service");
				t.addData(alarmMeta.toString());

				try {
					ThresholdAlertEvent alertEvent = new ThresholdAlertEvent(alarmMeta);

					Cat.getProducer().logEvent("ServiceAlarm", "Domain", Message.SUCCESS, alarmMeta.getRuleId() + "");
					m_dispatcher.dispatch(alertEvent);
					t.setStatus(Transaction.SUCCESS);
				} catch (Exception e) {
					t.setStatus(e);
				} finally {
					t.complete();
				}
			}
		}
	}

}
