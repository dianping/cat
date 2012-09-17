package com.dianping.dog.alarm.rule.message;

import com.dianping.dog.alarm.problem.AlertEvent;
import com.dianping.dog.alarm.rule.RuleType;

public class MessageCreaterFactory {

	public MessageCreater getMessageCreater(AlertEvent event) {
		RuleType type = event.getEntity().getRuleType();
		if (type == RuleType.Exception) {
			return new ExceptionMessageCreater();
		} else if (type == RuleType.Service) {
			return new ServiceMessageCreater();
		}
		return null;
	}

}
