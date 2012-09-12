package com.dianping.dog.alarm.merge;

import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.dog.alarm.problem.ProblemEvent;
import com.dianping.dog.alarm.rule.Rule;
import com.dianping.dog.alarm.rule.RuleManager;
import com.dianping.dog.event.AbstractReactorListener;
import com.site.lookup.annotation.Inject;

public class ProblemMergeListener extends AbstractReactorListener<ProblemEvent> implements Initializable, Runnable {

	@Inject
	private DefaultEventQueue m_defaultEventQueue;

	private int m_queueOverflow;

	private RuleManager m_ruleManager;

	private volatile boolean m_active = true;

	private Thread serviceTask = null;

	private static final long SLEEP_TIME = 1 * 1000;// sleep for 1 seconds

	@Override
	public void run() {
		while (isActive()) {
			try {
				ProblemEvent event = m_defaultEventQueue.poll();
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onEvent(ProblemEvent event) {
		boolean result = m_defaultEventQueue.offer(event);
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
