package com.dianping.cat.report.task.alert.sender.decorator;

import com.dianping.cat.report.task.alert.sender.AlertEntity;

public class SmsDecorator extends DefaultDecorator {

	public static final String ID = "sms";

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

	@Override
	public String getId() {
		return ID;
	}

}
