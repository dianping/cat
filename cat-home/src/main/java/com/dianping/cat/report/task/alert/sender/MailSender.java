package com.dianping.cat.report.task.alert.sender;

import java.util.List;

import com.dianping.cat.Cat;

public class MailSender extends BaseSender {

	@Override
	protected void sendLog(String title, String content, List<String> receivers) {
		StringBuilder builder = new StringBuilder();

		builder.append(title).append(",").append(content).append(",");
		for (String receiver : receivers) {
			builder.append(receiver).append(" ");
		}

		Cat.logEvent("SendMail", builder.toString());
		m_logger.info("SendMail" + builder.toString());
	}

	@Override
	public boolean sendAlert(List<String> receivers, String domain, String title, String content, String alertType) {
		try {
			m_mailSms.sendEmail(title, content, receivers);
			sendLog(title, content, receivers);
			return true;
		} catch (Exception ex) {
			Cat.logError("send mail error" + " " + title + " " + content, ex);
			return false;
		}
	}
}
