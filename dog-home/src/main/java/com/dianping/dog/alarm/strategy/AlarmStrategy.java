package com.dianping.dog.alarm.strategy;

import com.dianping.dog.alarm.rule.message.Message;

public interface AlarmStrategy {
	boolean doStrategy(Message message);
}
