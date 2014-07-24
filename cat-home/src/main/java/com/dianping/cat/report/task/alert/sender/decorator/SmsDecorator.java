package com.dianping.cat.report.task.alert.sender.decorator;

import com.dianping.cat.report.task.alert.sender.AlertChannel;
import com.dianping.cat.report.task.alert.sender.AlertEntity;

public class SmsDecorator extends Decorator {

	public static final String ID = AlertChannel.SMS.getName();
	
	@Override
	public String generateContent(AlertEntity alert) {
		String content;

		if ("exception".equals(alert.getType())) {
			content = buildExceptionContent(alert);
		} else {
			content = alert.getContent();
		}

		content = content + buildContactInfo(alert.getGroup());

		return content.replaceAll("<br/>", " ");
	}
	
}
