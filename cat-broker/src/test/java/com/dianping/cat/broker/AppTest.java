package com.dianping.cat.broker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class AppTest {
	
	public static void main(String[] args) throws UnsupportedEncodingException {
		List<String> urls = new ArrayList<String>();
		String url_pre = "http://localhost:2765/broker-service/api/batch";
		long timestamp = System.currentTimeMillis();
		
		for (int i = 0; i < 10; i++) {
			urls.add(url_pre + "?v=2&c=" + timestamp + URLEncoder.encode("\tshop.bin\t1\t1\t1\t1\t1\t1\t1\t1\n", "utf-8"));
		}

		for (String url : urls) {
			System.out.println(url);
			sendGet(url);
		}
	}

	public static String sendGet(String url) {
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
