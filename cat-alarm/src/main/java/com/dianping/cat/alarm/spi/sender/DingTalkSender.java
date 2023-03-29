package com.dianping.cat.alarm.spi.sender;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dianping.cat.Cat;
import com.dianping.cat.alarm.spi.AlertChannel;

import java.util.Collections;
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
		String webHookURL = sender.getUrl();
		List<String> receivers = message.getReceivers();
		for (String receiver : receivers) {
			if (receiver == null) {
				continue;
			}

			JSONObject jsonMsg = new JSONObject();
			jsonMsg.put("msgtype", "actionCard");

			JSONObject jsonBody = new JSONObject();
			jsonBody.put("title", message.getTitle());
			jsonBody.put("text", message.getContent());
			jsonBody.put("btnOrientation", "0");

			JSONObject jsonBtn = new JSONObject();
			jsonBtn.put("title", "查看告警");
			jsonBtn.put("actionURL", "http://cat-web-server/cat/r/t?domain=${domain}&type=${type}&name=${name}&date=${linkDate}");
			jsonBody.put("btns", Collections.singletonList(jsonBtn));

			jsonMsg.put("actionCard", jsonBody);

			String token = receiver.contains(":")? receiver.split(":")[1]: receiver;
			String response = httpPostSendByJson(webHookURL + token, jsonMsg.toString());
			if (response == null) {
				// 跳过，不要影响下一个接收对象
				continue;
			}

			JSONObject jsonResponse = JSON.parseObject(response);
			if (jsonResponse.containsKey("errcode") && jsonResponse.getIntValue("errcode") == 0) {
				// 只要有一个成功就设置为 true
				result = true;
			} else if (jsonResponse.containsKey("errmsg") && jsonResponse.getString("errmsg").length() > 0) {
				int errcode = jsonResponse.getIntValue("errcode");
				String errmsg = jsonResponse.getString("errmsg");
				if (errmsg.length() > 0) {
					Cat.logError(webHookURL + ":" + jsonBody,
						new AccessTokenResponseError(webHookURL + " response errorcode: " + errcode + ", errmsg: " + errmsg));
				}
			}
		}
		return result;
	}
}
