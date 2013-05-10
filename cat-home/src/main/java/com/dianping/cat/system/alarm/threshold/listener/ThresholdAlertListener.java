package com.dianping.cat.system.alarm.threshold.listener;

import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.system.alarm.AlarmContentBuilder;
import com.dianping.cat.system.alarm.alert.AlertInfo;
import com.dianping.cat.system.alarm.alert.AlertManager;
import com.dianping.cat.system.alarm.threshold.event.ThresholdAlertEvent;
import com.dianping.cat.system.alarm.threshold.template.ThresholdAlarmMeta;
import com.dianping.cat.system.event.Event;
import com.dianping.cat.system.event.EventListener;
import com.dianping.cat.system.event.EventType;
import com.dianping.cat.system.page.alarm.RuleManager;

public class ThresholdAlertListener implements EventListener {
	@Inject
	private AlertManager m_alertManager;

	@Inject
	private RuleManager m_ruleManager;
	
	@Inject
	private AlarmContentBuilder m_builder;


	private AlertInfo buildAlertInfo(ThresholdAlarmMeta meta, String title, String content, String ruleType,
	      int alertType) {
		AlertInfo info = new AlertInfo();

		info.setContent(content);
		info.setTitle(title);
		info.setRuleId(meta.getRuleId());
		info.setDate(meta.getDate());
		info.setRuleType(ruleType);
		info.setAlertType(alertType);
		return info;
	}



	@Override
	public boolean isEligible(Event event) {
		if (event.getEventType() == EventType.AlertEvent) {
			return true;
		}
		return false;
	}

	@Override
	public void onEvent(Event event) {
		ThresholdAlertEvent alertEvent = (ThresholdAlertEvent) event;
		ThresholdAlarmMeta metaInfo = alertEvent.getAlarmMeta();
		String title = m_builder.buildAlarmTitle(metaInfo);
		String content = m_builder.buildEmailAlarmContent(metaInfo);
		String alertType = metaInfo.getDuration().getAlarm().toLowerCase();
		String ruleType = metaInfo.getType();

		if (alertType != null && alertType.length() > 0) {
			String[] types = alertType.split(",");

			for (String type : types) {
				if (type.equalsIgnoreCase(AlertInfo.EMAIL)) {
					List<String> emailAddress = m_ruleManager.queryUserMailsByRuleId(metaInfo.getRuleId());
					AlertInfo info = buildAlertInfo(metaInfo, title, content, ruleType, AlertInfo.EMAIL_TYPE);

					info.setMails(emailAddress);
					m_alertManager.addAlarmInfo(info);
				}
				if (type.equalsIgnoreCase(AlertInfo.SMS)) {
					List<String> emailAddress = m_ruleManager.queryUserMailsByRuleId(metaInfo.getRuleId());
					AlertInfo info = buildAlertInfo(metaInfo, title + "[SMS]", content, ruleType, AlertInfo.EMAIL_TYPE);

					info.setMails(emailAddress);
					m_alertManager.addAlarmInfo(info);

					List<String> phoneAddress = m_ruleManager.queryUserPhonesByRuleId(metaInfo.getRuleId());
					AlertInfo smsInfo = buildAlertInfo(metaInfo, title, content, ruleType, AlertInfo.SMS_TYPE);

					smsInfo.setPhones(phoneAddress);
					m_alertManager.addAlarmInfo(smsInfo);
				}
			}
		}
	}

}
