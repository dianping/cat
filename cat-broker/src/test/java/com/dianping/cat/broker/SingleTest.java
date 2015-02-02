package com.dianping.cat.broker;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Test;

public class SingleTest {

	@Test
	public void test() throws Exception {
		while (true) {
			for (int i = 0; i < 10; i++) {
				try {
					URL url = new URL(buildUrl(i));
					URLConnection URLconnection = url.openConnection();

					URLconnection.setConnectTimeout(500);
					URLconnection.setReadTimeout(500);
					URLconnection.setRequestProperty("User-Agent",
					      "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Maxthon;)");

					HttpURLConnection httpConnection = (HttpURLConnection) URLconnection;
					int responseCode = httpConnection.getResponseCode();

					if (responseCode == HttpURLConnection.HTTP_OK) {
						System.out.println("ok");
					} else {
						System.out.println(responseCode);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			Thread.sleep(1000);
		}

	}

	private String buildUrl(int i) {
		StringBuilder sb = new StringBuilder(128);
		int hs = 200;
		int ec = 100;

		if (i % 2 == 1) {
			hs = 300;
			ec = 200;
		}
		sb.append(
		      "http://localhost:2765/broker-service/api/single?v=1&ts=123456&tu=http://j1.s1.dpfile.com/lib/1.0/cdn-perf/res/cdn_small.png/dnsLookup&d=1000&hs=")
		      .append(hs).append("&ec=").append(ec);

		return sb.toString();
	}

}
