package com.dianping.cat.broker.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Random;

import org.junit.Test;

public class AppSpeedTest {

	@Test
	public void test() throws Exception {
		while (true) {
			SendData("localhost:2765");
			Thread.sleep(5000);
		}
	}

	public void SendData(String host) throws Exception {
		String url = "http://" + host + "/broker-service/api/speed";
		long timestamp = System.currentTimeMillis();
		String urlStr = "";
		/*
		 * network version platform page step:responseTime ..... 1400037748182 1 6.9 1 shop.bin 1:30 2:40 3:50
		 */
		for (int i = 0; i < 4; i++) {
			int value1 = new Random().nextInt(300);
			int value2 = new Random().nextInt(300);
			urlStr += timestamp + "\t1\t2\t1\tindex.bin\t1-" + value1 + "\t2-" + value2 + "\n";
		}
		urlStr = url + "?v=1&c=" + URLEncoder.encode(urlStr, "utf-8");
		String ret = sendGet(urlStr);
		System.out.println(ret);
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
