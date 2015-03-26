package com.dianping.cat.broker.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class AppConnectionTest {

	@Test
	public void test() throws Exception {
		while (true) {
			SendData("localhost:2765");
			Thread.sleep(5000);
		}
	}

	public void SendData(String host) throws Exception {
		List<String> urls = new ArrayList<String>();
		String url_pre = "http://" + host + "/broker-service/api/connection";
		long timestamp = System.currentTimeMillis();

		for (int i = 0; i < 10; i++) {
			urls.add(url_pre + "?v=1&c=" + timestamp
			      + URLEncoder.encode("\t1\t2\t3\tshop.bin\t4\t5\t10\t20\t30\n", "utf-8"));
			urls.add(url_pre + "?v=1&c=" + timestamp
			      + URLEncoder.encode("\t1\t2\t3\tsearchshop.api\t4\t5\t10\t20\t30\n", "utf-8"));
		}

		for (String url : urls) {
			String ret = sendGet(url);
			System.out.println(ret);
		}
	}

	public String sendGet(String url) {
		String result = "";
		BufferedReader in = null;
		try {
			URL realUrl = new URL(url);
			URLConnection connection = realUrl.openConnection();
			connection.connect();
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return result;
	}
}
