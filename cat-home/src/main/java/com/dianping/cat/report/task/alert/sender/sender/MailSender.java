package com.dianping.cat.report.task.alert.sender.sender;

import java.net.URLEncoder;
import java.util.List;

import com.dianping.cat.Cat;
import com.dianping.cat.report.task.alert.sender.AlertChannel;
import com.dianping.cat.report.task.alert.sender.AlertMessageEntity;

public class MailSender extends AbstractSender {

	public static final String ID = AlertChannel.MAIL.getName();

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public boolean send(AlertMessageEntity message) {
		com.dianping.cat.home.sender.entity.Sender sender = querySender();
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

	private boolean sendEmail(AlertMessageEntity message, String receiver,
	      com.dianping.cat.home.sender.entity.Sender sender) {
		String title = message.getTitle().replaceAll(",", " ");
		String content = message.getContent().replaceAll(",", " ");
		String urlPrefix = sender.getUrl();
		String urlPars = m_senderConfigManager.queryParString(sender);

		try {
			urlPars = urlPars.replace("${receiver}", URLEncoder.encode(receiver, "utf-8"))
			      .replace("${title}", URLEncoder.encode(title, "utf-8"))
			      .replace("${content}", URLEncoder.encode(content, "utf-8"));

		} catch (Exception e) {
			Cat.logError(e);
		}

		return httpGetSend(sender.getSuccessCode(), urlPrefix, urlPars);
	}
}
