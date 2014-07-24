package com.dianping.cat.report.task.alert.sender.decorator;

import com.dianping.cat.report.task.alert.sender.AlertEntity;

public class MailDecorator extends Decorator {

	@Override
	public String generateContent(AlertEntity alert) {
		String content;

		if ("exception".equals(alert.getType())) {
			content = buildExceptionContent(alert);
		} else {
			content = alert.getContent();
		}

		return content + buildContactInfo(alert.getGroup());
	}

}
