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
 * 飞书发送
 *
 * @author <a href="mailto:shiyindaxiaojie@gmail.com">gyl</a>
 * @since 2.4.13
 */
public class FeishuSender extends AccessTokenSender {

	public static final String ID = AlertChannel.DINGTALK.getName();

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
			jsonMsg.put("msg_type", "interactive");

			JSONObject jsonBody = new JSONObject();

			// 标题
			String title = message.getTitle();
			JSONObject jsonTitle = new JSONObject();
			jsonTitle.put("tag", "plain_text");
			jsonTitle.put("content", title);
			JSONObject jsonHeader = new JSONObject();
			jsonHeader.put("title", jsonTitle);
			jsonBody.put("header", jsonHeader);

			List<JSONObject> jsonElements = new ArrayList<>();

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
			JSONObject jsonText = new JSONObject();
			jsonText.put("tag", "lark_md");
			jsonText.put("content", text);
			JSONObject jsonContent = new JSONObject();
			jsonContent.put("tag", "div");
			jsonContent.put("text", jsonText);
			jsonElements.add(jsonContent);

			// 按钮
			JSONObject jsonBtns = new JSONObject();
			jsonBtns.put("tag", "action");
			List<JSONObject> btns = new ArrayList<>();
			try {
				JSONObject jsonSettings = new JSONObject();
				jsonSettings.put("tag", "button");
				jsonSettings.put("url", PAGE_LINK + URLEncoder.encode(message.getSettingsLink(), Charsets.UTF_8.name()));
				jsonSettings.put("type", "default");
				JSONObject jsonSettingsBtn = new JSONObject();
				jsonSettingsBtn.put("content", "\uD83D\uDD27 告警规则");
				jsonSettingsBtn.put("tag", "lark_md");
				jsonSettings.put("text", jsonSettingsBtn);
				btns.add(jsonSettings);

				JSONObject jsonView = new JSONObject();
				jsonSettings.put("tag", "button");
				jsonSettings.put("url", PAGE_LINK + URLEncoder.encode(message.getViewLink(), Charsets.UTF_8.name()));
				jsonSettings.put("type", "default");
				JSONObject jsonViewBtn = new JSONObject();
				jsonViewBtn.put("content", "\uD83D\uDD14 查看告警");
				jsonViewBtn.put("tag", "lark_md");
				jsonSettings.put("text", jsonViewBtn);
				btns.add(jsonView);
			} catch (UnsupportedEncodingException e) {
				m_logger.error(e.getMessage(), e);
				continue;
			}
			jsonBtns.put("actions", btns);
			jsonElements.add(jsonBtns);
			jsonBody.put("elements", jsonElements);

			jsonMsg.put("card", jsonBody);

			String token = receiverArr.length > 1? receiverArr[0]: receiver;
			String url = webHookURL + token;
			m_logger.info("Feishu send to [" + url + "]");
			String response = httpPostSendByJson(url, jsonMsg.toString());
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
					m_logger.error("Feishu [" + url +  "] response errorcode: " + errcode + ", errmsg: " + errmsg);
					Cat.logError(url, new AccessTokenResponseError("errorcode: " + errcode + ", errmsg: " + errmsg));
				}
			}
		}
		return result;
	}
}
