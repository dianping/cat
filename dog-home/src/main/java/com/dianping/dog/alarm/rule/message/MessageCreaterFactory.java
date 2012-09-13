package com.dianping.dog.alarm.rule.message;

import com.dianping.dog.alarm.problem.AlertEvent;

public class MessageCreaterFactory {
	
	public MessageCreater getMessageCreater(AlertEvent event){
      return new ExceptionMessageCreater();		
	}

}
