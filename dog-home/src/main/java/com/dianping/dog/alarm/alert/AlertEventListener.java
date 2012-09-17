package com.dianping.dog.alarm.alert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.dog.alarm.entity.Duration;
import com.dianping.dog.alarm.problem.AlertEvent;
import com.dianping.dog.alarm.rule.AlarmType;
import com.dianping.dog.alarm.rule.message.Message;
import com.dianping.dog.alarm.rule.message.MessageCreater;
import com.dianping.dog.alarm.rule.message.MessageCreaterFactory;
import com.dianping.dog.alarm.strategy.AlarmStrategy;
import com.dianping.dog.alarm.strategy.AlarmStrategyFactory;
import com.dianping.dog.event.Event;
import com.dianping.dog.event.EventListener;
import com.dianping.dog.event.EventType;
import com.site.lookup.annotation.Inject;

public class AlertEventListener implements EventListener {
	
	@Inject
	private MessageCreaterFactory m_messageCreaterFactory;
	
	@Inject
	private AlarmStrategyFactory m_alarmStrategyFactory;
	
	private Map<Long,Map<Duration,Long>> lastAlertTime = new HashMap<Long,Map<Duration,Long>>();

	@Override
   public boolean isEligible(Event event) {
		if(EventType.ProblemAlarmEvent == event.getEventType()){
			return true;
		}
	   return false;
   }

	@Override
   public void onEvent(Event event) {
	   AlertEvent alertEvent = (AlertEvent) event;
	   if(!isAlert(alertEvent)){
	   	System.out.println("do no need to alert!");
	   	return;
	   }
	   MessageCreater messageCreater = m_messageCreaterFactory.getMessageCreater(alertEvent);
	   Message  message = messageCreater.create(alertEvent);
	   List<AlarmType> types = alertEvent.getDuration().getAlarmType();
	   for(AlarmType type:types){
	   	AlarmStrategy strategy = m_alarmStrategyFactory.getStrategy(type);
	   	strategy.doStrategy(message);
	   }
   }
	
	private boolean isAlert(AlertEvent alertEvent){
		long ruleId  = alertEvent.getEntity().getId();
		long currentTime = System.currentTimeMillis();
		Duration duration = alertEvent.getDuration();
		Map<Duration,Long> durationMap = lastAlertTime.get(ruleId);
		if(durationMap == null){
			synchronized(lastAlertTime){
				durationMap = new HashMap<Duration,Long>();
				durationMap.put(duration, currentTime);
				lastAlertTime.put(ruleId, durationMap);
			}
			return true;
		}
		boolean rusult = false;
		synchronized(durationMap){
			Long lastTime = durationMap.get(duration);
			if(lastTime == null){
				durationMap.put(duration, currentTime);
				return false;
			}
			if((currentTime - lastTime)>=duration.getInterval()){
				durationMap.put(duration, currentTime);
				rusult = true;
			}else{
				rusult = false;
			}
		}
		return rusult;
	}
}
