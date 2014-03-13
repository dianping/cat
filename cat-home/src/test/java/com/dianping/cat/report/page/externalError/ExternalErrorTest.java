package com.dianping.cat.report.page.externalError;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

public class ExternalErrorTest {

	@Test
	public void testSendError() {

		for (int i = 0; i < 10; i++) {
			try {
				String buildUrl = buildUrl(i);
				URL url = new URL(buildUrl);
				URLConnection URLconnection = url.openConnection();
				URLconnection.setRequestProperty("User-Agent",
				      "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Maxthon;)");
				URLconnection.setRequestProperty("referer", "http://www.dianping.com/shop/1");

				HttpURLConnection httpConnection = (HttpURLConnection) URLconnection;
				int responseCode = httpConnection.getResponseCode();

				if (responseCode == HttpURLConnection.HTTP_OK) {
				} else {
					System.err.println("Error");
				}
				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private String buildUrl(int i) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String ip = "192.168.7.70";
		String title = "ZabbixError";
		String database = "cat";
		String content = "ZabbixErrorContent";
		String time = sdf.format(new Date());
		int type = i % 3 + 1;

		StringBuilder sb = new StringBuilder(128);
		sb.append("http://localhost:2281/cat/r/externalError?");
		sb.append("ip=" + ip);
		sb.append("&title=" + title);
		sb.append("&database=" + database);
		sb.append("&content=" + content);
		sb.append("&link=" + "http://www.sina.com.cn");
		sb.append("&time=" + time);
		sb.append("&type=" + type);
		return sb.toString();
	}
}
