package com.dianping.cat.report.task.alert.sender.decorator;

import com.dianping.cat.report.task.alert.sender.AlertEntity;

public class WeixinDecorator extends DefaultDecorator {

	public static final String ID = "weixin";

	@Override
	public String generateContent(AlertEntity alert) {
		String content;

		if ("exception".equals(alert.getType())) {
			content = buildExceptionContent(alert);
		} else if ("thirdparty".equals(alert.getType())) {
			content = buildThirdPartyContent(alert);
		} else {
			content = alert.getContent();
		}

		content = content + buildContactInfo(alert.getGroup());

		return content.replaceAll("<br/>", "\n");
	}

	@Override
	public String getId() {
		return ID;
	}
}
