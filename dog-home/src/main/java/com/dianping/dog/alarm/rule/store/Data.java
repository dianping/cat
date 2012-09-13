package com.dianping.dog.alarm.rule.store;

public interface Data {
	
	long getTimeStamp();
	
	Data merge(Data data);

}
