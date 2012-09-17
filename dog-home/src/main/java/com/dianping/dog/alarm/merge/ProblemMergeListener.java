package com.dianping.dog.alarm.merge;

import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.dog.alarm.problem.ProblemDataEvent;
import com.dianping.dog.alarm.problem.ProblemViolationEvent;
import com.dianping.dog.alarm.rule.Rule;
import com.dianping.dog.alarm.rule.RuleManager;
import com.dianping.dog.event.Event;
import com.dianping.dog.event.EventDispatcher;
import com.dianping.dog.event.EventListener;
import com.dianping.dog.event.EventType;
import com.site.lookup.annotation.Inject;

public class ProblemMergeListener implements Initializable,EventListener, Runnable {
	
	@Inject
	protected EventDispatcher m_eventdispatcher;

	@Inject
	private DefaultEventQueue m_defaultEventQueue;

	@Inject
	private RuleManager m_ruleManager;
	
	private int m_queueOverflow;

	private volatile boolean m_active = true;

	private Thread serviceTask = null;

	private static final long SLEEP_TIME = 1 * 1000;// sleep for 1 seconds

	@Override
   public boolean isEligible(Event event) {
		if(event.getEventType() == EventType.ProblemViolationEvent){
			return true;
		}
	   return false;
   }

	
	@Override
	public void run() {
		while (isActive()) {
			try {
				ProblemDataEvent event = m_defaultEventQueue.poll();
				if (event != null) {
					List<Rule> rules = m_ruleManager.getRules();
					for (Rule rule : rules) {
						if (rule.isEligible(event)) {
							rule.apply(event);
						}
					}
				}
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void onEvent(Event event) {
		ProblemViolationEvent problemViolationEvent = (ProblemViolationEvent)event;
		ProblemDataEvent problemEvent = problemViolationEvent.getOrigin();
		boolean result = m_defaultEventQueue.offer(problemEvent);
		if (!result) { // trace queue overflow
			m_queueOverflow++;
			if (m_queueOverflow % 1000 == 0) {
			}
		}
	}

	protected boolean isActive() {
		synchronized (this) {
			return m_active;
		}
	}

	public void shutdown() {
		synchronized (this) {
			m_active = false;
		}
	}

	@Override
	public void initialize() throws InitializationException {
		m_defaultEventQueue = new DefaultEventQueue();
		serviceTask = new Thread(this, "ProblemMerge-task");
		serviceTask.start();
	}
	
}
