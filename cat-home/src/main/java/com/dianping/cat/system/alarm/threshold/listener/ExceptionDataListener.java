package com.dianping.cat.system.alarm.threshold.listener;

import java.util.List;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.system.alarm.alert.AlertInfo;
import com.dianping.cat.system.alarm.threshold.ThresholdDataEntity;
import com.dianping.cat.system.alarm.threshold.ThresholdRule;
import com.dianping.cat.system.alarm.threshold.ThresholdRuleManager;
import com.dianping.cat.system.alarm.threshold.event.ThresholdAlertEvent;
import com.dianping.cat.system.alarm.threshold.event.ExceptionDataEvent;
import com.dianping.cat.system.alarm.threshold.template.ThresholdAlarmMeta;
import com.dianping.cat.system.event.Event;
import com.dianping.cat.system.event.EventDispatcher;
import com.dianping.cat.system.event.EventListener;
import com.dianping.cat.system.event.EventType;
import com.site.lookup.annotation.Inject;

public class ExceptionDataListener implements EventListener {

	@Inject
	private EventDispatcher m_dispatcher;

	@Inject
	private ThresholdRuleManager m_manager;

	@Override
	public boolean isEligible(Event event) {
		if (event.getEventType() == EventType.ExceptionDataEvent) {
			return true;
		}
		return false;
	}

	@Override
	public void onEvent(Event event) {
		ExceptionDataEvent dataEvent = (ExceptionDataEvent) event;

		ThresholdDataEntity data = dataEvent.getData();
		List<ThresholdRule> rules = m_manager.getExceptionRuleByDomain(data.getDomain());

		for (ThresholdRule rule : rules) {
			ThresholdAlarmMeta alarmMeta = rule.addData(data,AlertInfo.EXCEPTION);

			if (alarmMeta != null) {
				Transaction t = Cat.newTransaction("SendAlarm", "Exception");
				t.addData(alarmMeta.toString());
	
				try {
	            ThresholdAlertEvent alertEvent = new ThresholdAlertEvent(alarmMeta);
	           
	            m_dispatcher.dispatch(alertEvent);
	            t.setStatus(Transaction.SUCCESS);
            } catch (Exception e) {
            	t.setStatus(e);
            }finally{
            	t.complete();
            }
			}
		}
	}

}
