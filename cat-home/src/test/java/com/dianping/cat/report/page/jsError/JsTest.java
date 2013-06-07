package com.dianping.cat.report.page.jsError;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Test;

public class JsTest {

	@Test
	public void test() throws Exception {
		for (int i = 0; i < 1000; i++) {
			URL url = new URL(buildUrl(i));
			URLConnection URLconnection = url.openConnection();
			URLconnection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Maxthon;)");
			URLconnection.setRequestProperty("referer", "http://www.dianping.com/shop/1");

			HttpURLConnection httpConnection = (HttpURLConnection) URLconnection;
			int responseCode = httpConnection.getResponseCode();

			if (responseCode == HttpURLConnection.HTTP_OK) {
			} else {
			}
			Thread.sleep(100);
		}
	}

	private String buildUrl(int i) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("http://cat.qa.dianpingoa.com/cat/r/jsError?");
		sb.append("error=testError1");
		if (i % 10 == 0) {
			sb.append("&file=");
		} else {
			sb.append("&file=testFile" + i % 5);
		}
		sb.append("&timestamp" + System.currentTimeMillis());
		sb.append("&line=line98");

		return sb.toString();
	}

}
