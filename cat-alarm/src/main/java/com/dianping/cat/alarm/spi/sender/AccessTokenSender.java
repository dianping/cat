package com.dianping.cat.alarm.spi.sender;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dianping.cat.Cat;
import com.dianping.cat.util.json.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 基于访问令牌发送
 *
 * @author <a href="mailto:shiyindaxiaojie@gmail.com">gyl</a>
 * @since 2.4.13
 */
public abstract class AccessTokenSender extends AbstractSender {

	protected boolean sendMessage(SendMessageEntity message, String token,
								  com.dianping.cat.alarm.sender.entity.Sender sender) {
		String title = message.getTitle().replaceAll(",", " ");
		String content = message.getContent();

		String webHookURL = sender.getUrl();

		JsonObject jsonBody = new JsonObject();
		jsonBody.put("msgtype", "markdown");

		JsonObject jsonMarkdown = new JsonObject();
		jsonMarkdown.put("title", title);
		jsonMarkdown.put("text", content);
		jsonBody.put("markdown", jsonMarkdown);

		if (token.contains(":")) {
			token = token.split(":")[1];
		}

		return httpPostSendByJson(webHookURL + token, jsonBody.toString());
	}

	private boolean httpPostSendByJson(String urlStr, String contentJsonStr) {
		URL url;
		HttpURLConnection conn = null;
		InputStream in = null;
		try {
			url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(3000);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Charset", "UTF-8");
			//转换为字节数组
			byte[] data = contentJsonStr.getBytes(StandardCharsets.UTF_8);
			// 设置文件长度
			conn.setRequestProperty("Content-Length", String.valueOf(data.length));
			conn.setRequestProperty("Content-type", "application/json;charset=UTF-8");
			conn.connect();
			OutputStream out = conn.getOutputStream();
			// 写入请求的字符串
			out.write(data);
			out.flush();
			out.close();

			int responseCode = conn.getResponseCode();
			if (responseCode == 200) {
				in = conn.getInputStream();
				String responseText = readBytes(in);
				JSONObject jsonResponse = JSON.parseObject(responseText);
				if (jsonResponse.containsKey("errcode") && jsonResponse.getIntValue("errcode") == 0) {
					return true;
				} else {
					String errmsg = jsonResponse.containsKey("errmsg") && jsonResponse.getString("errmsg").length() > 0 ? jsonResponse.getString("errmsg") : "ERROR";
					throw new AccessTokenResponseError(errmsg);
				}
			} else {
				return false;
			}
		} catch (Exception e) {
			m_logger.error("AccessToken send error: " + e.getMessage(), e);
			Cat.logError(urlStr + "---" + contentJsonStr, e);
			return false;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception ignored) {
				}
			}
			close(conn);
		}
	}

	private static void close(HttpURLConnection closeable) {
		if (closeable != null) {
			try {
				closeable.disconnect();
			} catch (Exception ignored) {
			}
		}
	}

	private static String readBytes(InputStream inputStream) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int readCount = 0;
		while ((readCount = inputStream.read(buffer)) > -1) {
			out.write(buffer, 0, readCount);
		}
		return out.toString("UTF-8");
	}

	private static class AccessTokenResponseError extends Exception {
		public AccessTokenResponseError(String errMsg) {
			super(errMsg);
		}
	}

	public static void main(String[] args) {
		String title = "标题";
		String content = "内容";

		JsonObject jsonBody = new JsonObject();
		jsonBody.put("msgtype", "text");

		JsonObject textJson = new JsonObject();
		content = "CAT报警：" + " - " + title + " \n " + content;
		textJson.put("content", content);

		jsonBody.put("text", textJson);

		System.out.println(jsonBody);
	}
}
