package com.dianping.cat.broker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

public class AppSpeedTest {

	@Test
	public void test() throws Exception {
		while (true) {
			SendData();
			Thread.sleep(5000);
		}
	}

	public void SendData() throws Exception {
		List<String> urls = new ArrayList<String>();
		String url_pre = "http://localhost:2765/broker-service/api/batch";
		long timestamp = System.currentTimeMillis();
		String urlStr = "";
		/*
		 * network version platform page step:responseTime ..... 1400037748182 1 6.9 1 shop.bin 1:30 2:40 3:50
		 */
		for (int i = 0; i < 4; i++) {
			int value = new Random().nextInt(300);
			urlStr += timestamp + "\t1\t2\t1\tindex.bin\t1-" + value + "\n";
			urls.add(url_pre + "?v=1&c=" + timestamp + URLEncoder.encode(urlStr, "utf-8"));
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
