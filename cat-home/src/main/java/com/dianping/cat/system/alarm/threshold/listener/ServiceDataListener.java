package com.dianping.cat.system.alarm.threshold.listener;

import java.util.Date;
import java.util.List;

import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.page.externalError.EventCollectManager;
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

	@Inject
	private EventCollectManager m_eventCollectManager;

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
			Pair<Boolean, ThresholdAlarmMeta> alarmMeta = rule.addData(data, AlertInfo.SERVICE);

			if (alarmMeta != null) {
				ThresholdAlarmMeta value = alarmMeta.getValue();
				// need send email or sms
				if (alarmMeta.getKey().booleanValue()) {
					Transaction t = Cat.newTransaction("SendAlarm", "Service");

					t.addData(alarmMeta.toString());
					try {
						ThresholdAlertEvent alertEvent = new ThresholdAlertEvent(value);
						Cat.getProducer().logEvent("ServiceAlarm", "Domain", Message.SUCCESS,
						      String.valueOf(value.getRuleId()));

						m_dispatcher.dispatch(alertEvent);
						t.setStatus("Alarm");
					} catch (Exception e) {
						t.setStatus(e);
						Cat.logError(e);
					} finally {
						t.complete();
					}
				}

				com.dianping.cat.home.dal.report.Event alertEvent = new com.dianping.cat.home.dal.report.Event();

				alertEvent.setType(EventCollectManager.CAT_ERROR);
				alertEvent.setDate(new Date());
				alertEvent.setDomain(value.getDomain());
				alertEvent.setIp(CatString.ALL);
				alertEvent.setSubject(CatString.SERVICE+"[" + value.getDomain() + "]");
				alertEvent.setLink("/cat/p?domain="+value.getDomain());
				m_eventCollectManager.addEvent(alertEvent);
			}
		}
	}
	
}
