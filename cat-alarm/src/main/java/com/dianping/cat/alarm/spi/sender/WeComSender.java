package com.dianping.cat.alarm.spi.sender;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dianping.cat.Cat;
import com.dianping.cat.alarm.spi.AlertChannel;
import com.dianping.cat.util.json.JsonObject;

import java.util.List;

/**
 * 企业微信发送
 *
 * @author <a href="mailto:shiyindaxiaojie@gmail.com">gyl</a>
 * @since 2.4.13
 */
public class WeComSender extends AccessTokenSender {

	public static final String ID = AlertChannel.WECOM.getName();

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

//			String title = message.getTitle().replaceAll(",", " ");
			String content = message.getContent();

			JsonObject jsonBody = new JsonObject();
			jsonBody.put("msgtype", "markdown");
			JsonObject jsonMarkdown = new JsonObject();
			jsonMarkdown.put("content", content);
			jsonBody.put("markdown", jsonMarkdown);

			String token = receiver.contains(":")? receiver.split(":")[1]: receiver;
			String response = httpPostSendByJson(webHookURL + token, jsonBody.toString());
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
