package com.dianping.cat.report.alert.sender.sender;

import java.net.URLEncoder;
import java.util.List;

import com.dianping.cat.Cat;
import com.dianping.cat.report.alert.sender.AlertChannel;
import com.dianping.cat.report.alert.sender.AlertMessageEntity;

public class SmsSender extends AbstractSender {

	public static final String ID = AlertChannel.SMS.getName();

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public boolean send(AlertMessageEntity message) {
		com.dianping.cat.home.sender.entity.Sender sender = querySender();
		boolean batchSend = sender.getBatchSend();
		boolean result = false;

		if (batchSend) {
			String phones = message.getReceiverString();

			result = sendSms(message, phones, sender);
		} else {
			List<String> phones = message.getReceivers();

			for (String phone : phones) {
				boolean success = sendSms(message, phone, sender);
				result = result || success;
			}
		}
		return result;
	}

	private boolean sendSms(AlertMessageEntity message, String receiver,
	      com.dianping.cat.home.sender.entity.Sender sender) {
		String filterContent = message.getContent().replaceAll("(<a href.*(?=</a>)</a>)|(\n)", "");
		String content = message.getTitle() + " " + filterContent;
		String urlPrefix = sender.getUrl();
		String urlPars = m_senderConfigManager.queryParString(sender);

		try {
			urlPars = urlPars.replace("${receiver}", URLEncoder.encode(receiver, "utf-8")).replace("${content}",
			      URLEncoder.encode(content, "utf-8"));
		} catch (Exception e) {
			Cat.logError(e);
		}

		return httpSend(sender.getSuccessCode(), sender.getType(), urlPrefix, urlPars);
	}
}
