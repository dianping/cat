package com.dianping.dog.alarm.merge;

import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.dog.alarm.problem.ProblemDataEvent;
import com.dianping.dog.alarm.problem.ProblemViolationEvent;
import com.dianping.dog.alarm.rule.Rule;
import com.dianping.dog.alarm.rule.RuleManager;
import com.dianping.dog.event.Event;
import com.dianping.dog.event.EventDispatcher;
import com.dianping.dog.event.EventListener;
import com.dianping.dog.event.EventType;
import com.site.helper.Threads;
import com.site.helper.Threads.Task;
import com.site.lookup.annotation.Inject;

public class ProblemMergeListener implements Initializable, EventListener, LogEnabled {

	@Inject
	protected EventDispatcher m_eventdispatcher;

	@Inject
	private RuleManager m_ruleManager;

	private DefaultEventQueue<ProblemDataEvent> m_defaultEventQueue;

	private Logger m_logger;

	private int m_queueOverflow;

	private static final long SLEEP_TIME = 1 * 1000;// sleep for 1 seconds

	@Override
	public boolean isEligible(Event event) {
		if (event.getEventType() == EventType.ProblemViolationEvent) {
			return true;
		}
		return false;
	}

	private void processEvent() {
		ProblemDataEvent event = m_defaultEventQueue.poll();
		if (event != null) {
			List<Rule> rules = m_ruleManager.getRules();
			for (Rule rule : rules) {
				try {
					if (rule.isEligible(event)) {
						rule.apply(event);
					}
				} catch (Exception e) {
					m_logger.error(String.format("data id :[%s] ,apply rule fail.   rule id[%s],rule name[%s], exception: %s",
					      event.getDataId(), rule.getRuleId(),rule.getName(), e.toString()));
				}
			}
		}
	}

	public void onEvent(Event event) {
		ProblemViolationEvent problemViolationEvent = (ProblemViolationEvent) event;
		ProblemDataEvent problemEvent = problemViolationEvent.getOrigin();
		boolean result = m_defaultEventQueue.offer(problemEvent);
		if (!result) { // trace queue overflow
			m_queueOverflow++;
			if (m_queueOverflow % 1000 == 0) {
				Cat.logError(new Exception("DataEvent overflow the queue size[:" + m_queueOverflow + "]"));
			}
		}
	}

	@Override
	public void initialize() throws InitializationException {
		m_defaultEventQueue = new DefaultEventQueue<ProblemDataEvent>();
		Threads.forGroup("Dog").start(new MergerEventPrcossor());
	}

	@Override
	public void enableLogging(Logger logger) {
		this.m_logger = logger;
	}

	private class MergerEventPrcossor implements Task {
		@Override
		public void run() {
			while (true) {
				try {
					processEvent();
				} catch (Exception e) {
					m_logger.error(e.getMessage());
					Cat.logError(e);
				}
				try {
					Thread.sleep(SLEEP_TIME);
				} catch (Exception e) {
					m_logger.error(String.format("exception current when sleep in:[%s]  :[%s]",
					      MergerEventPrcossor.class.getName(), e.getMessage()));
				}
			}
		}

		@Override
		public String getName() {
			return "ProblemMergeEventPrcossor";
		}

		@Override
		public void shutdown() {

		}

	}

}
