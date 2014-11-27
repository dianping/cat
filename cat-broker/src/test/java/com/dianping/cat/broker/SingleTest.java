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
				URL url = new URL(buildUrl(i));
				URLConnection URLconnection = url.openConnection();
				URLconnection.setRequestProperty("User-Agent",
				      "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Maxthon;)");

				HttpURLConnection httpConnection = (HttpURLConnection) URLconnection;
				int responseCode = httpConnection.getResponseCode();

				if (responseCode == HttpURLConnection.HTTP_OK) {
					System.out.println("ok");
				} else {
					System.out.println(responseCode);
				}
			}
			Thread.sleep(1000);
		}

	}

	private String buildUrl(int i) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("http://localhost:2765/broker-service/api/single?v=1&ts=123456&tu=http://j1.s1.dpfile.com/lib/1.0/cdn-perf/res/cdn_small.png/dnsLookup&d=1000&hs=200&ec=100");

		return sb.toString();
	}

	// @Test
	// public void test2() throws Exception{
	// for (int i = 0; i < 1000; i++) {
	// URL url = new URL("http://localhost:2281/cat/r/jsError?error=Script%20error.&file=&line=0&timestamp=1371196520045");
	// URLConnection URLconnection = url.openConnection();
	// URLconnection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Maxthon;)");
	// URLconnection.setRequestProperty("referer", "http://www.dianping.com/shop/2340226");
	//
	// HttpURLConnection httpConnection = (HttpURLConnection) URLconnection;
	// int responseCode = httpConnection.getResponseCode();
	//
	// if (responseCode == HttpURLConnection.HTTP_OK) {
	// } else {
	// }
	// Thread.sleep(100);
	// }

	// }

}
