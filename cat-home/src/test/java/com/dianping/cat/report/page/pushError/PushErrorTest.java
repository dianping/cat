package com.dianping.cat.report.page.pushError;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Test;

public class PushErrorTest {

	@Test
	public void test() throws Exception {
		for (int i = 0; i < 1000; i++) {
			URL url = new URL(buildUrl());
			URLConnection URLconnection = url.openConnection();
			HttpURLConnection httpConnection = (HttpURLConnection) URLconnection;
			int responseCode = httpConnection.getResponseCode();

			if (responseCode == HttpURLConnection.HTTP_OK) {
			} else {
			}
			Thread.sleep(100);
		}
	}

	private String buildUrl() {
		StringBuilder sb = new StringBuilder(128);
		sb.append("http://127.0.0.1:2281/cat/r/pushError?");
		sb.append("error=testError1");
		sb.append("&host=t.dianping.com");
		sb.append("&file=testFile");
		sb.append("&timestamp" + System.currentTimeMillis());
		sb.append("&url=test.url");
		sb.append("&line=line98");

		return sb.toString();
	}
}
