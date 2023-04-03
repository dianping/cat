package com.dianping.cat.alarm.spi.sender;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dianping.cat.Cat;
import com.dianping.cat.alarm.spi.AlertChannel;
import com.site.lookup.util.StringUtils;
import org.apache.commons.codec.Charsets;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 企业微信发送
 *
 * @author <a href="mailto:shiyindaxiaojie@gmail.com">gyl</a>
 * @since 2.4.13
 */
public class WeComSender extends AccessTokenSender {

	public static final String ID = AlertChannel.WECOM.getName();

	public static final String PAGE_LINK = "";

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
			jsonMsg.put("msgtype", "template_card");

			JSONObject jsonBody = new JSONObject();
			jsonBody.put("card_type", "text_notice");

			// 标题
			String title = message.getTitle();
			JSONObject jsonTtile = new JSONObject();
			jsonTtile.put("title", title);
			jsonBody.put("main_title", jsonTtile);

			// 内容
			String color = title.contains("已恢复")? DEFAULT_COLOR : message.getLevel().getColor();
			String text = "### <font color=\"" + color +"\">" + title + "</font>\n" +
				message.getContent().replaceAll("<br/>", "\n");

			String[] receiverArr = receiver.split(":");
			if (!message.getContent().contains("负责人员") && receiverArr.length > 1) {
				String owner = receiver.split(":")[1];
				if (StringUtils.isNotEmpty(owner)) {
					text += "\n负责人员：" + owner;
				}
			}
			if (!message.getContent().contains("联系号码") && receiverArr.length > 2) {
				String phone = receiver.split(":")[2];
				if (StringUtils.isNotEmpty(phone)) {
					text += "\n联系号码：" + phone;
				}
			}
			jsonBody.put("sub_title_text", text);

			List<JSONObject> btns = new ArrayList<>();
			try {
				JSONObject jsonSettings = new JSONObject();
				jsonSettings.put("type", "1");
				jsonSettings.put("title", "⚙️ 告警规则");
				jsonSettings.put("url", PAGE_LINK + URLEncoder.encode(message.getSettingsLink(), Charsets.UTF_8.name()));
				btns.add(jsonSettings);

				JSONObject jsonView = new JSONObject();
				jsonView.put("type", "2");
				jsonView.put("title", "\uD83D\uDD14 查看告警");
				jsonView.put("url", PAGE_LINK + URLEncoder.encode(message.getViewLink(), Charsets.UTF_8.name()));
				btns.add(jsonView);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}

//			JSONObject jsonSilent = new JSONObject();
//			jsonSilent.put("type", "3");
//			jsonSilent.put("title", "🔕 告警静默");
//			jsonSilent.put("actionURL", message.getViewLink());
//			btns.add(jsonSilent);
			jsonBody.put("jump_list", btns);

			jsonMsg.put("template_card", jsonBody);

			String token = receiver.contains(":")? receiver.split(":")[0]: receiver;
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
