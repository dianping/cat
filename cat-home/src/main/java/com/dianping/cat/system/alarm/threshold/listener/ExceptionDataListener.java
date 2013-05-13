package com.dianping.cat.system.alarm.threshold.listener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.page.externalError.EventCollectManager;
import com.dianping.cat.system.alarm.AlarmContentBuilder;
import com.dianping.cat.system.alarm.alert.AlertInfo;
import com.dianping.cat.system.alarm.threshold.ThresholdDataEntity;
import com.dianping.cat.system.alarm.threshold.ThresholdRule;
import com.dianping.cat.system.alarm.threshold.ThresholdRuleManager;
import com.dianping.cat.system.alarm.threshold.event.ExceptionDataEvent;
import com.dianping.cat.system.alarm.threshold.event.ThresholdAlertEvent;
import com.dianping.cat.system.alarm.threshold.template.ThresholdAlarmMeta;
import com.dianping.cat.system.event.Event;
import com.dianping.cat.system.event.EventDispatcher;
import com.dianping.cat.system.event.EventListener;
import com.dianping.cat.system.event.EventType;

public class ExceptionDataListener implements EventListener {

	@Inject
	private EventDispatcher m_dispatcher;

	@Inject
	private ThresholdRuleManager m_manager;

	@Inject
	private EventCollectManager m_eventCollectManager;

	@Inject
	private AlarmContentBuilder m_builder;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyyMMddHH");

	private void buildAlarmEvent(ThresholdAlarmMeta meta) {
		com.dianping.cat.home.dal.report.Event alertEvent = new com.dianping.cat.home.dal.report.Event();

		alertEvent.setType(EventCollectManager.CAT_ERROR);
		alertEvent.setDate(new Date());
		alertEvent.setDomain(meta.getDomain());
		alertEvent.setIp(CatString.ALL);
		alertEvent.setSubject(CatString.EXCEPTION_MANY + "[" + meta.getDomain() + "]");
		alertEvent.setContent(m_builder.buildEmailAlarmContent(meta));
		alertEvent.setLink("/cat/r/p?domain=" + meta.getDomain() + "&date="
		      + m_sdf.format(getCurrentHour(meta.getDate())));
		m_eventCollectManager.addEvent(alertEvent);
	}

	private Date getCurrentHour(Date date) {
		long time = date.getTime();
		time = time - time % TimeUtil.ONE_HOUR;

		return new Date(time);
	}

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
			Pair<Boolean, ThresholdAlarmMeta> alarmMeta = rule.addData(data, AlertInfo.EXCEPTION);

			if (alarmMeta != null) {
				ThresholdAlarmMeta value = alarmMeta.getValue();
				// need send email or sms
				if (alarmMeta.getKey().booleanValue()) {
					Transaction t = Cat.newTransaction("SendAlarm", "Exception");

					t.addData(alarmMeta.toString());
					try {
						ThresholdAlertEvent alertEvent = new ThresholdAlertEvent(value);
						Cat.getProducer().logEvent("ExceptionAlarm", "Domain", Message.SUCCESS,
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

				buildAlarmEvent(value);
			}
		}
	}

}
