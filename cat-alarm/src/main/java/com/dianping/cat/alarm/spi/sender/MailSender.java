package com.dianping.cat.alarm.spi.sender;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.sender.entity.Sender;
import com.dianping.cat.alarm.spi.AlertChannel;

public class MailSender extends AbstractSender {

	public static final String ID = AlertChannel.MAIL.getName();

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public boolean send(SendMessageEntity message) {
		Sender sender = querySender();
		boolean batchSend = sender.isBatchSend();
		boolean result = false;

		if (batchSend) {
			String emails = message.getReceiverString();

			result = sendEmail(message, emails, sender);
		} else {
			List<String> emails = message.getReceivers();

			for (String email : emails) {
				boolean success = sendEmail(message, email, sender);
				result = result || success;
			}
		}
		return result;
	}

	private boolean sendEmail(SendMessageEntity message, String receiver, Sender sender) {
		String title = message.getTitle().replaceAll(",", " ");
		String content = message.getContent().replaceAll(",", " ");
		String urlPrefix = sender.getUrl();
		String urlPars = m_senderConfigManager.queryParString(sender);
		String time = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());

		try {
			urlPars = urlPars.replace("${receiver}", receiver).replace("${title}", URLEncoder.encode(title, "utf-8"))
			      .replace("${content}", URLEncoder.encode(content, "utf-8"))
			      .replace("${time}", URLEncoder.encode(time, "utf-8"));

		} catch (Exception e) {
			Cat.logError(e);
		}

		return httpSend(sender.getSuccessCode(), sender.getType(), urlPrefix, urlPars);
	}
}
