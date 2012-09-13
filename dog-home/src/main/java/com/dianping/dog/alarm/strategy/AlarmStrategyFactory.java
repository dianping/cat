package com.dianping.dog.alarm.strategy;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.dog.alarm.rule.AlarmType;

public class AlarmStrategyFactory implements Initializable {

	private  Map<AlarmType, AlarmStrategy> strategyMap = null;

	public AlarmStrategy getStrategy(AlarmType type) {
		return strategyMap.get(type);
	}

	@Override
	public void initialize() throws InitializationException {
			this.strategyMap = new HashMap<AlarmType, AlarmStrategy>();
			strategyMap.put(AlarmType.EMAIL, new EmailAlarmStrategy());
			strategyMap.put(AlarmType.SMS, new SmsAlarmStrategy());
	}
}
