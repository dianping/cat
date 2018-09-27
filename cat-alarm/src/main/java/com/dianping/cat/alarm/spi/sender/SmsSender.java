package com.dianping.cat.alarm.spi.sender;

import java.net.URLEncoder;
import java.util.List;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.sender.entity.Sender;
import com.dianping.cat.alarm.spi.AlertChannel;

public class SmsSender extends AbstractSender {

	public static final String ID = AlertChannel.SMS.getName();

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public boolean send(SendMessageEntity message) {
		Sender sender = querySender();
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

	private boolean sendSms(SendMessageEntity message, String receiver, Sender sender) {
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
