package com.dianping.cat.alarm.spi.sender;

import com.dianping.cat.alarm.spi.AlertChannel;

import java.util.List;

/**
 * 钉钉发送
 *
 * @author <a href="mailto:shiyindaxiaojie@gmail.com">gyl</a>
 * @since 2.4.13
 */
public class DingTalkSender extends AccessTokenSender {

	public static final String ID = AlertChannel.DINGTALK.getName();

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public boolean send(SendMessageEntity message) {
		com.dianping.cat.alarm.sender.entity.Sender sender = querySender();
		boolean result = false;

		List<String> tokens = message.getReceivers();

		for (String token : tokens) {
			boolean success = sendMessage(message, token, sender);
			result = result || success;
		}
		return result;
	}
}
