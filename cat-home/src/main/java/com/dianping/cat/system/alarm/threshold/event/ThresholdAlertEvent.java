package com.dianping.cat.system.alarm.threshold.event;

import com.dianping.cat.system.alarm.threshold.template.ThresholdAlarmMeta;
import com.dianping.cat.system.event.Event;
import com.dianping.cat.system.event.EventType;

public class ThresholdAlertEvent implements Event {

	private ThresholdAlarmMeta m_alarmMeta;

	public ThresholdAlertEvent(ThresholdAlarmMeta alarmMeta) {
		m_alarmMeta = alarmMeta;
	}

	public ThresholdAlarmMeta getAlarmMeta() {
		return m_alarmMeta;
	}

	@Override
	public EventType getEventType() {
		return EventType.ExceptionAlertEvent;
	}

}
