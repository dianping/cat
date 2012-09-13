package com.dianping.dog.alarm.rule.message;

import com.dianping.dog.alarm.problem.AlertEvent;


public interface MessageCreater {
	
	Message create(AlertEvent event);

}
