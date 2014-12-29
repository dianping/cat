package com.dianping.cat.report.task.alert.sender.sender;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.dianping.cat.Cat;
import com.dianping.cat.report.task.alert.sender.AlertChannel;
import com.dianping.cat.report.task.alert.sender.AlertMessageEntity;

public class WeixinSender implements Sender {

	private static final String WEIXIN_URL = "http://dpoa.api.dianping.com/app/monitor/cat/push";

	private static final String SUCCESS_TEXT = "{\"success\":\"1\"}";

	public static final String ID = AlertChannel.WEIXIN.getName();

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public boolean send(AlertMessageEntity message) {
		if (!sendWeixin(message)) {
			return false;
		}

		return true;
	}

	private boolean sendWeixin(AlertMessageEntity message) {
		String domain = message.getGroup();
		String title = message.getTitle();
		String content = message.getContent();
		String type = message.getType();
		String weixins = message.getReceiverString();
		StringBuilder paraBuilder = new StringBuilder(300);

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

		paraBuilder.append("domain=").append(urlDomain);
		paraBuilder.append("&email=").append(urlWeixins);
		paraBuilder.append("&title=").append(urlTitle);
		paraBuilder.append("&content=").append(urlContent);
		paraBuilder.append("&type=").append(urlType);
		String urlParameters = paraBuilder.toString();

		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(WEIXIN_URL).openConnection();

			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.setConnectTimeout(2000);
			connection.setReadTimeout(3000);

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
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} catch (Exception e) {
			Cat.logError("send weixin error:" + domain + " " + title + " " + content + " " + weixins, e);
			return false;
		}
	}

}
