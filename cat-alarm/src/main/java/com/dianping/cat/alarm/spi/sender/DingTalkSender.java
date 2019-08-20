package com.dianping.cat.alarm.spi.sender;

import com.alibaba.fastjson.JSONObject;
import com.dianping.cat.Cat;
import com.dianping.cat.alarm.sender.entity.Sender;
import com.dianping.cat.alarm.spi.AlertChannel;
import com.dianping.cat.alarm.spi.sender.AbstractSender;
import com.dianping.cat.alarm.spi.sender.SendMessageEntity;

import java.util.List;

/**
 * Created by kaixi.xu on 2019/8/3 3:52 PM
 **/
public class DingTalkSender extends AbstractSender {
	public static final String ID = AlertChannel.DINGTALK.getName();

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
			String dingTalks = message.getReceiverString();

			result = sendDingTalk(message, dingTalks, sender);
		} else {
			List<String> dingTalks = message.getReceivers();

			for (String dingTalk : dingTalks) {
				boolean success = sendDingTalk(message, dingTalk, sender);
				result = result || success;
			}
		}
		return result;
	}

	private boolean sendDingTalk(SendMessageEntity message, String receiver, Sender sender) {
		String domain = message.getGroup();
		String title = message.getTitle().replaceAll(",", " ");
		String content = message.getContent().replaceAll(",", " ").replaceAll("<a href.*(?=</a>)</a>", "");
		String urlPrefix = sender.getUrl();
		String sendContent = "";

		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("token", receiver);
			jsonObject.put("message", content);
			sendContent = jsonObject.toJSONString();

		} catch (Exception e) {
			Cat.logError(e);
		}

		return httpSend(sender.getSuccessCode(), sender.getType(), urlPrefix, sendContent);
	}
}