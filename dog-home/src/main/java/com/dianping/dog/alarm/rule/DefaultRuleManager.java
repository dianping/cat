package com.dianping.dog.alarm.rule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;


public class DefaultRuleManager implements RuleManager, Runnable, Initializable {

	private volatile boolean stop = false;

	private List<Rule> rules = new ArrayList<Rule>();

	@Override
	public List<Rule> getRules() {
		return rules;
	}

	private void reloadAllRules() {

	}

	@Override
	public void run() {
		while (!stop) {
			Date awakeTime = nextPeriod();
			LockSupport.parkUntil(awakeTime.getTime());
			reloadAllRules();
		}
	}

	@Override
	public void initialize() throws InitializationException {
		reloadAllRules();
	}
	
	private  Date nextPeriod() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		if (cal.get(Calendar.MINUTE) >0) {
			cal.add(Calendar.HOUR, 1);// timeout, waiting for next hour
		}
		return cal.getTime();
	}

}
