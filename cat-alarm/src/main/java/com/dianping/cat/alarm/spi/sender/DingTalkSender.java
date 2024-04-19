package com.dianping.cat.alarm.spi.sender;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dianping.cat.Cat;
import com.dianping.cat.alarm.spi.AlertChannel;
import com.site.lookup.util.StringUtils;
import org.apache.commons.codec.Charsets;
import org.glassfish.jersey.internal.guava.Lists;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 钉钉发送
 *
 * @author <a href="mailto:shiyindaxiaojie@gmail.com">gyl</a>
 * @since 2.4.13
 */
public class DingTalkSender extends AccessTokenSender {

	public static final String ID = AlertChannel.DINGTALK.getName();

	public static final String PAGE_LINK = "dingtalk://dingtalkclient/page/link?pc_slide=false&url=";

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
		for (String token : receivers) {
			if (token == null) {
				continue;
			}

			JSONObject jsonMsg = new JSONObject();
			jsonMsg.put("msgtype", "actionCard");

			JSONObject jsonBody = new JSONObject();

			// 标题
			String title = message.getTitle();
			jsonBody.put("title", title);

			// 内容，提示：钉钉App 目前仅支持 \n\n 换行，字体颜色必须用 \" 表示
			String color = title.contains("已恢复")? DEFAULT_COLOR : message.getLevel().getColor();
			String text = "### <font color=\"" + color +"\">" + title + "</font>\n\n" +
					message.getContent().replaceAll("<br/>", "\n\n");

			jsonBody.put("text", text);

			// 按钮
			jsonBody.put("btnOrientation", "1"); // 横放
			List<JSONObject> btns = new ArrayList<>();
			try {
				JSONObject jsonSettings = new JSONObject();
				jsonSettings.put("title", "\uD83D\uDD27 告警规则");
				jsonSettings.put("actionURL", PAGE_LINK + URLEncoder.encode(message.getSettingsLink(), Charsets.UTF_8.name()));
				btns.add(jsonSettings);

				JSONObject jsonView = new JSONObject();
				jsonView.put("title", "\uD83D\uDD14 查看告警");
				jsonView.put("actionURL", PAGE_LINK + URLEncoder.encode(message.getViewLink(), Charsets.UTF_8.name()));
				btns.add(jsonView);
			} catch (UnsupportedEncodingException e) {
				m_logger.error(e.getMessage(), e);
				continue;
			}

//			JSONObject jsonSilent = new JSONObject();
//			jsonSilent.put("title", "🔕 告警静默");
//			jsonSilent.put("actionURL", message.getViewLink());
//			btns.add(jsonSilent);
			jsonBody.put("btns", btns);

			jsonMsg.put("actionCard", jsonBody);

			String url = webHookURL + token;
			m_logger.info("Dingtalk send to [" + url + "]");
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
					m_logger.error("Dingtalk [" + url +  "] response errorcode: " + errcode + ", errmsg: " + errmsg);
					Cat.logError(url, new AccessTokenResponseError("errorcode: " + errcode + ", errmsg: " + errmsg));
				}
			}
		}
		return result;
	}
}
