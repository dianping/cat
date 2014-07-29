package com.dianping.cat.broker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;

public class AppTest {
	
	@Test
	public void test() throws Exception {
		long time = System.currentTimeMillis();
		
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);

		int minute = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
		minute = minute - minute % 5;
		
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		
		Date period = new Date(cal.getTimeInMillis());
		
		
		System.out.println(minute);
		System.out.println(period);
	}
	
	public void SendData() throws Exception {
		List<String> urls = new ArrayList<String>();
		String url_pre = "http://localhost:2765/broker-service/api/batch";
		long timestamp = System.currentTimeMillis();
		
		for (int i = 0; i < 10; i++) {
			urls.add(url_pre + "?v=2&c=" + timestamp + URLEncoder.encode("\thttp://www.dianping.com/\t1\t1\t1\t1\t1\t1\t1\t1\n", "utf-8"));
		}

		for (String url : urls) {
			System.out.println(url);
			sendGet(url);
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
