package com.dianping.dog.alarm.strategy;

import com.dianping.dog.alarm.rule.message.Message;

public class SmsAlarmStrategy implements AlarmStrategy {

	@Override
	public boolean doStrategy(Message message) {
		 System.out.println("begin to send SMS message "+ message.getContent());		
		 return false;
	}

}
