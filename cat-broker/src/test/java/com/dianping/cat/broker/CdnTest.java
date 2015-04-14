package com.dianping.cat.broker;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.junit.Test;
import org.unidal.helper.Files;

public class CdnTest {

	public static void main(String[] args) {
		while (true) {
			try {
				sendCdnRequest();
				Thread.sleep(5000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void test() throws UnsupportedEncodingException{
		String url = "http://localhost:2765/broker-service/api/cdn?v=1&tt=";
		String url2 = url+URLEncoder.encode("http://sfsd/cat?sdf=tt&gg=yy","utf-8");
		
		System.out.println(URLDecoder.decode(url,"utf-8"));
		System.out.println(URLDecoder.decode(url2,"utf-8"));
	}
	
	public static void sendCdnRequest() throws Exception {
		String url = "http://localhost:2765/broker-service/api/cdn?v=1";
		URLConnection conn = new URL(url).openConnection();

		conn.setDoOutput(true);
		conn.setDoInput(true);

		OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

		String content = "&c=1400037748182\thttp://j1.s2.dpfile.com/lib/1.0/cdn-perf/res/cdn_middle.css\t300\t200\t300\t300\n1400037748182\thttp://j1.s2.dpfile.com/lib/1.0/cdn-perf/res/cdn_middle.css\t300\t200\t300\t300\n1400037748182\thttp://j1.s2.dpfile.com/lib/1.0/cdn-perf/res/cdn_middle.css\t300\t200\t300\t300\n";

		String[] tabs = content.split("\n");
		for (int i = 0; i < tabs.length; i++) {
			System.out.println(tabs[i]);
		}
		writer.write(content);
		writer.flush();

		InputStream in = conn.getInputStream();
		String result = Files.forIO().readFrom(in, "utf-8");

		System.out.println(result);
	}
}
