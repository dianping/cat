package com.dianping.cat.report.page.jsError;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import junit.framework.Assert;

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

	@Test
	public void testFormatePerformance() {
		long current = System.currentTimeMillis();
		Handler handler = new Handler();
		int size = 10000000;
		for (int i = 0; i < size; i++) {
			String url = "http://www.dianping.com/search/category/345/10/g251r6656p1";
			handler.formateFile(url);
		}
		System.out.println("time:" + (System.currentTimeMillis() - current) * 1.0 / size);
	}

	@Test
	public void formateUrl() {
		String url1 = "http://www.dianping.com/search/category/345/10/g251r6656p1";
		String url2 = "http://www.dianping.com/search/1";
		String url3 = "http://www.dianping.com/help";
		String url4 = "http://www.dianping.com/";

		Handler handler = new Handler();

		Assert.assertEquals("http://www.dianping.com/search/category", handler.formateFile(url1));
		Assert.assertEquals("http://www.dianping.com/search/", handler.formateFile(url2));
		Assert.assertEquals(url3, handler.formateFile(url3));
		Assert.assertEquals(url4, handler.formateFile(url4));
	}
}
