package com.dianping.cat.alarm.spi.sender;

import java.net.URLEncoder;
import java.util.List;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.sender.entity.Sender;
import com.dianping.cat.alarm.spi.AlertChannel;

public class WeixinSender extends AbstractSender {

	public static final String ID = AlertChannel.WEIXIN.getName();

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
			String weixins = message.getReceiverString();

			result = sendWeixin(message, weixins, sender);
		} else {
			List<String> weixins = message.getReceivers();

			for (String weixin : weixins) {
				boolean success = sendWeixin(message, weixin, sender);
				result = result || success;
			}
		}
		return result;
	}

	private boolean sendWeixin(SendMessageEntity message, String receiver, Sender sender) {
		String domain = message.getGroup();
		String title = message.getTitle().replaceAll(",", " ");
		String content = message.getContent().replaceAll(",", " ").replaceAll("<a href.*(?=</a>)</a>", "");
		String urlPrefix = sender.getUrl();
		String urlPars = m_senderConfigManager.queryParString(sender);

		try {
			urlPars = urlPars.replace("${domain}", URLEncoder.encode(domain, "utf-8"))
			      .replace("${receiver}", URLEncoder.encode(receiver, "utf-8"))
			      .replace("${title}", URLEncoder.encode(title, "utf-8"))
			      .replace("${content}", URLEncoder.encode(content, "utf-8"))
			      .replace("${type}", URLEncoder.encode(message.getType(), "utf-8"));
		} catch (Exception e) {
			Cat.logError(e);
		}

		return httpSend(sender.getSuccessCode(), sender.getType(), urlPrefix, urlPars);
	}
}
