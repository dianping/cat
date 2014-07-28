package com.dianping.cat.report.task.alert.sender.sender;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.report.task.alert.sender.AlertMessageEntity;

public class WeixinSender implements Sender, LogEnabled {

	private static final String WEIXIN_URL = "http://dpoa.api.dianping.com/app/monitor/cat/push";

	private static final String SUCCESS_TEXT = "{\"success\":\"1\"}";

	public static final String ID = "weixin";

	private Logger m_logger;

	@Override
	public boolean send(AlertMessageEntity message, String type) {
		try {
			String messageStr = message.toString();

			if (!sendWeixin(message, type)) {
				Cat.logEvent("AlertWeixinError", type, Event.SUCCESS, messageStr);
				m_logger.info("AlertWeixinError " + messageStr);
				return false;
			}

			Cat.logEvent("AlertWeiixin", type, Event.SUCCESS, messageStr);
			m_logger.info("AlertWeiixin " + messageStr);
			return true;
		} catch (Exception ex) {
			Cat.logError("send weixin error " + message.toString(), ex);
			return false;
		}
	}

	private boolean sendWeixin(AlertMessageEntity message, String type) {
		String domain = message.getGroup();
		String title = message.getTitle();
		String content = message.getContent();
		String weixins = message.getReceiverString();

		String urlDomain = null;
		String urlTitle = null;
		String urlContent = null;
		String urlWeixins = null;
		String urlType = null;

		try {
			urlDomain = URLEncoder.encode(domain, "UTF-8");
			urlTitle = URLEncoder.encode(title, "UTF-8");
			urlContent = URLEncoder.encode(content.replaceAll("<a href.*(?=</a>)</a>", ""), "UTF-8");
			urlWeixins = URLEncoder.encode(weixins, "UTF-8");
			urlType = URLEncoder.encode(type, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Cat.logError("transfer weixin content error:" + title + " " + content + " " + domain + " " + weixins, e);
			return false;
		}

		String urlParameters = "domain=" + urlDomain + "&email=" + urlWeixins + "&title=" + urlTitle + "&content="
		      + urlContent + "&type=" + urlType;

		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(WEIXIN_URL).openConnection();

			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);

			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();

			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine;
				StringBuilder builder = new StringBuilder();

				while ((inputLine = reader.readLine()) != null) {
					builder.append(inputLine);
				}
				reader.close();

				String responseText = builder.toString();

				if (responseText.equals(SUCCESS_TEXT)) {
					Cat.logEvent("WeiXinSend", "send_success", Event.SUCCESS, "send success:" + domain + " " + title + " "
					      + content + " " + weixins + " " + responseText);
					return true;
				} else {
					Cat.logEvent("WeiXinSend", "send_fail", Event.SUCCESS, "send fail:" + domain + " " + title + " "
					      + content + " " + weixins + " " + responseText);
					return false;
				}
			} else {
				Cat.logEvent("WeiXinSend", "network_fail", Event.SUCCESS, "network fail:" + domain + " " + title + " "
				      + content + " " + weixins);
				return false;
			}
		} catch (Exception ex) {
			Cat.logEvent("WeiXinSend", "error", Event.SUCCESS, "error:" + domain + " " + title + " " + content + " "
			      + weixins);
			Cat.logError("send weixin error:" + domain + " " + title + " " + content + " " + weixins, ex);
			return false;
		}
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

}
