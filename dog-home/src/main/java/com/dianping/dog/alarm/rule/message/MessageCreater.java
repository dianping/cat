package com.dianping.dog.alarm.rule.message;

import com.dianping.dog.alarm.problem.AlertEvent;


public interface MessageCreater {
	
	public final String ENTER_SPLITER = "\r\n";
	
	Message create(AlertEvent event);

}
