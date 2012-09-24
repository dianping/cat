package com.dianping.dog.alarm.alert;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.dog.alarm.entity.Duration;
import com.dianping.dog.alarm.merge.DefaultEventQueue;
import com.dianping.dog.alarm.problem.AlertEvent;
import com.dianping.dog.alarm.rule.AlarmType;
import com.dianping.dog.alarm.rule.Rule;
import com.dianping.dog.alarm.rule.RuleManager;
import com.dianping.dog.alarm.rule.message.Message;
import com.dianping.dog.alarm.rule.message.MessageCreater;
import com.dianping.dog.alarm.rule.message.MessageCreaterFactory;
import com.dianping.dog.alarm.strategy.AlarmStrategy;
import com.dianping.dog.alarm.strategy.AlarmStrategyFactory;
import com.dianping.dog.event.Event;
import com.dianping.dog.event.EventListener;
import com.dianping.dog.event.EventType;
import com.site.helper.Threads;
import com.site.helper.Threads.Task;
import com.site.lookup.annotation.Inject;

public class AlertEventListener implements Initializable, EventListener, LogEnabled {

	@Inject
	private MessageCreaterFactory m_messageCreaterFactory;

	@Inject
	private AlarmStrategyFactory m_alarmStrategyFactory;

	@Inject
	private RuleManager m_ruleMananger;

	private DefaultEventQueue<AlertEvent> m_defaultEventQueue;

	private Logger m_logger;

	private int m_queueOverflow;

	private static final long SLEEP_TIME = 2 * 1000;// sleep for 1 seconds

	private Map<Integer, Map<String, Long>> lastAlertTime = new HashMap<Integer, Map<String, Long>>();
	
	private Map<Integer,Long> ruleModifiedTimeCash = new HashMap<Integer,Long>();

	@Override
	public void initialize() throws InitializationException {
		m_defaultEventQueue = new DefaultEventQueue<AlertEvent>();
		Threads.forGroup("Dog").start(new AlertEventPrcossor());
	}
	
	@Override
	public boolean isEligible(Event event) {
		if (EventType.ProblemAlarmEvent == event.getEventType()) {
			return true;
		}
		return false;
	}

	@Override
	public void onEvent(Event event) {
		AlertEvent alertEvent = (AlertEvent) event;
		boolean result = m_defaultEventQueue.offer(alertEvent);
		if (!result) { // trace queue overflow
			m_queueOverflow++;
			if (m_queueOverflow % 1000 == 0) {
				Cat.logError(new Exception("DataEvent overflow the queue size[:" + m_queueOverflow + "]"));
			}
		}
	}

	private void processEvent() {
		try {
			doAlert();
		} catch (Exception e) {
			m_logger.error(String.format("fail to process event,excpiton:[%s]", e));
			Cat.logError(e);
		}
		try {
			refreshAlertTime();
		} catch (Exception e) {
			m_logger.error(String.format("fail to refresh alertTime,excpiton:[%s]", e));
			Cat.logError(e);
		}
	}

	private void doAlert() {
		AlertEvent alertEvent = m_defaultEventQueue.poll();
		if (alertEvent == null) {
			return;
		}
		if (!isAlert(alertEvent)) {
			m_logger.debug("do no need to alert!");
			return;
		}
		MessageCreater messageCreater = m_messageCreaterFactory.getMessageCreater(alertEvent);
		Message message = messageCreater.create(alertEvent);
		List<AlarmType> types = alertEvent.getDuration().getAlarmType();
		for (AlarmType type : types) {
			AlarmStrategy strategy = m_alarmStrategyFactory.getStrategy(type);
			strategy.doStrategy(message);
		}
	}

	public void refreshAlertTime() {
		List<Rule> rules = m_ruleMananger.getRules();
		Set<Integer> updatedRuleIds = new HashSet<Integer>();
		synchronized (lastAlertTime) {
			for (Rule rule : rules) {
				Long lastModifiedTime = ruleModifiedTimeCash.get(rule.getRuleId());
				if (lastModifiedTime == null) {
					lastModifiedTime = rule.getRuleEntity().getGmtModified().getTime();
					ruleModifiedTimeCash.put(rule.getRuleId(),lastModifiedTime);
					m_logger.info(String.format("init duration cash ruleId:[%s]",rule.getRuleId()));
					continue;
				}
				if (lastModifiedTime != rule.getRuleEntity().getGmtModified().getTime()) {
					lastAlertTime.remove(rule.getRuleId());
					ruleModifiedTimeCash.put(rule.getRuleId(),rule.getRuleEntity().getGmtModified().getTime());
					m_logger.info(String.format("remove duration cash duration:[%s]  rule[%s]",new Date(lastModifiedTime),rule.getRuleEntity().getGmtModified()));
					m_logger.info(String.format("remove duration cash ruleId:[%s]",rule.getRuleId()));
				}
				updatedRuleIds.add(rule.getRuleId());
			}
			Iterator<Integer> ruleIds = this.lastAlertTime.keySet().iterator();
			while (ruleIds.hasNext()) {
				Integer ruleId = ruleIds.next();
				if (!updatedRuleIds.contains(ruleId)) {
					ruleIds.remove();
					//TODO 如果队列中还缓存有多条老的RuleId事件，此处会被多次调用。需要优化
					m_logger.info(String.format("Remove duration cash ruleId:[%s]",ruleId));
				}
			}
		}
	}

	private boolean isAlert(AlertEvent alertEvent) {
		int ruleId = alertEvent.getEntity().getId();
		long currentTime = System.currentTimeMillis();
		Duration duration = alertEvent.getDuration();
		Map<String, Long> durationMap = lastAlertTime.get(ruleId);
		if (durationMap == null) {
			synchronized (lastAlertTime) {
				durationMap = new HashMap<String, Long>();
				durationMap.put(duration.getId(), currentTime);
				lastAlertTime.put(ruleId, durationMap);
			}
			return true;
		}
		boolean rusult = false;
		synchronized (durationMap) {
			Long lastTime = durationMap.get(duration);
			if (lastTime == null) {
				durationMap.put(duration.getId(), currentTime);
				return false;
			}
			if ((currentTime - lastTime) >= duration.getInterval()) {
				durationMap.put(duration.getId(), currentTime);
				rusult = true;
			} else {
				rusult = false;
			}
		}
		return rusult;
	}

	private class AlertEventPrcossor implements Task {

		@Override
		public void run() {
			while (true) {
				processEvent();
				try {
					Thread.sleep(SLEEP_TIME);
				} catch (Exception e) {
					m_logger.error(String.format("exception current when sleep in:[%s]  :[%s]", AlertEventPrcossor.class.getName(),e.getMessage()));
				}
			}
		}

		@Override
		public String getName() {
			return "AlertEventPrcossor";
		}

		@Override
		public void shutdown() {

		}

	}

	@Override
	public void enableLogging(Logger logger) {
		this.m_logger = logger;
	}

}
