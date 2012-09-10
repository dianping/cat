package com.dianping.dog.alarm.merge;

import java.util.List;

import com.dianping.dog.alarm.problem.ProblemEvent;
import com.dianping.dog.alarm.rule.Rule;
import com.dianping.dog.alarm.rule.RuleManager;
import com.dianping.dog.event.AbstractReactorListener;
import com.dianping.dog.event.EventQueue;

public class ProblemMergeListener extends AbstractReactorListener<ProblemEvent> implements Runnable {
	private EventQueue<ProblemEvent> m_queue;

	private int m_queueOverflow;
	
	private RuleManager m_ruleManager;
	
	private volatile boolean m_active = true;
	
	RuleExecutorThreadPool executorPool;

	@Override
	public void run() {
		while(isActive()){
			ProblemEvent event = m_queue.poll();
			if(event != null){
				List<Rule> rules = m_ruleManager.getRules();
				for(Rule rule : rules){
					if(rule.isEligible(event)){
						rule.apply(event);
					}
				}
			}
		}
	}

	@Override
	public void onEvent(ProblemEvent event) {
		boolean result = m_queue.offer(event);
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

}
