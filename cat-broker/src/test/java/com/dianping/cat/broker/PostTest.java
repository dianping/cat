package com.dianping.cat.broker;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.helper.Urls;

public class PostTest {

	@Test
	public void testSingle() throws Exception {
		while (true) {

			long time = System.currentTimeMillis();
			String url = null;

			for (int i = 0; i < 100; i++) {
				url = "http://localhost:2765/broker-service/api?v=1.0&tu=http://www.dianping.com/movie&d=100&hs=200&ts="
				      + time;
				read(url);
			}

			for (int i = 0; i < 150; i++) {
				url = "http://localhost:2765/broker-service/api?v=1.0&tu=http://www.dianping.com/movie&d=100&hs=300&ts="
				      + time;
				read(url);
			}

			for (int i = 0; i < 200; i++) {
				url = "http://localhost:2765/broker-service/api?v=1.0&tu=http://www.dianping.com/movie&d=100&hs=400&ts="
				      + time;
				read(url);
			}
			for (int i = 0; i < 250; i++) {
				url = "http://localhost:2765/broker-service/api?v=1.0&tu=http://www.dianping.com/movie&d=100&ec=300&ts="
				      + time;
				read(url);
			}
			for (int i = 0; i < 300; i++) {
				url = "http://localhost:2765/broker-service/api?v=1.0&tu=http://www.dianping.com/movie&d=100&ec=400&ts="
				      + time;
				read(url);
			}
			Thread.sleep(20*1000);
		}
	}

	private void read(String url) throws Exception {
		InputStream input = Urls.forIO().connectTimeout(1000).openStream(url);
		Files.forIO().readFrom(input);

	}

	@Test
	public void test() throws Exception {
		String url = "http://localhost:2765/broker-service/api/batch?v=1.0";
		URLConnection conn = new URL(url).openConnection();

		conn.setDoOutput(true);
		conn.setDoInput(true);
		OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

		String content = "&c=1400037748182\\thttp\\t300\\t200\\t300\\n1400037748182\\thttp\\t300\\t200\\t300\\n1400037748182\\thttp\\t300\\t200\\t300\\n";
		writer.write(content);
		writer.flush();

		InputStream in = conn.getInputStream();
		String result = Files.forIO().readFrom(in, "utf-8");

		System.out.println(result);
	}

}
