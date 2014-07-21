package com.dianping.cat.report.task.alert.sender;

import java.util.List;

import com.dianping.cat.Cat;

public class SmsSender extends BaseSender {
	
	@Override
	protected void sendLog(String title, String content, List<String> receivers) {
		StringBuilder builder = new StringBuilder();

		builder.append(title).append(" ").append(content).append(" ");
		for (String receiver : receivers) {
			builder.append(receiver).append(" ");
		}

		Cat.logEvent("SendSms", builder.toString());
		m_logger.info("SendSms" + builder.toString());
	}

	@Override
	public boolean sendAlert(List<String> receivers, String domain, String title, String content, String alertType) {
		if (alertType == null || !alertType.equals("error")) {
			return true;
		}

		try {
			m_mailSms.sendSms(title, content, receivers);
			sendLog(title, content, receivers);
			return true;
		} catch (Exception ex) {
			Cat.logError("send sms error" + " " + title + " " + content, ex);
			return false;
		}
	}
}
