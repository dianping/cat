package com.dianping.cat.broker;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.helper.Urls;

public class PostTest {

	public String m_localhost = "localhost:2765";

	public String m_online = "114.80.165.63";

	@Test
	public void test1() throws UnsupportedEncodingException {
		System.err.println(System.currentTimeMillis() - 60 * 1000 * 2);
		String url = "v=1&c=1400650097	http://m.api.dianping.com/searchshop.api	0	200	0";

		String url2 = "1400656368280\thttp://m.api.dianping.com/searchshop.api\t300\t200\t300\n";

		System.out.println("http://114.80.165.63/broker-service/api/batch?v=1&c=" + URLEncoder.encode(url2, "utf-8"));

		System.out.println("http://114.80.165.63/broker-service/api/batch?" + URLEncoder.encode(url, "utf-8"));
	}

	@Test
	public void testJs() throws Exception {
		String url = "http://114.80.165.63/broker-service/api/js?";
		URLConnection conn = new URL(url).openConnection();

		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.addRequestProperty("Referer", "http://www.dianping.com/");

		OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

		String content = "v=1.0&error=Script error.&file=http://www.dianping.com/tt/ttt/tttt/001/002&line=0&timestamp="
		      + System.currentTimeMillis();
		writer.write(content);
		writer.flush();

		InputStream in = conn.getInputStream();
		String result = Files.forIO().readFrom(in, "utf-8");

		System.out.println(result);
	}

	@Test
	public void testSingle() throws Exception {
		String host = m_online;

		while (true) {

			long time = System.currentTimeMillis() - 60 * 1000 * 5;
			String url = null;

			for (int i = 0; i < 100; i++) {
				url = "http://" + host
				      + "/broker-service/api/singel?v=1.0&tu=http://www.dianping.com/test&d=100&hs=200&ts=" + time;
				System.out.println(url);
				read(url);
			}

			for (int i = 0; i < 150; i++) {
				url = "http://" + host
				      + "/broker-service/api/singel?v=1.0&tu=http://www.dianping.com/test&d=100&hs=300&ts=" + time;
				read(url);
			}

			for (int i = 0; i < 200; i++) {
				url = "http://" + host
				      + "/broker-service/api/singel?v=1.0&tu=http://www.dianping.com/test&d=100&hs=400&ts=" + time;
				read(url);
			}
			for (int i = 0; i < 250; i++) {
				url = "http://" + host
				      + "/broker-service/api/singel?v=1.0&tu=http://www.dianping.com/test&d=100&ec=300&ts=" + time;
				read(url);
			}
			for (int i = 0; i < 300; i++) {
				url = "http://" + host
				      + "/broker-service/api/singel?v=1.0&tu=http://www.dianping.com/test&d=100&ec=400&ts=" + time;
				read(url);
			}
			Thread.sleep(20 * 1000);
		}
	}

	private void read(String url) throws Exception {
		InputStream input = Urls.forIO().connectTimeout(1000).openStream(url);
		Files.forIO().readFrom(input);

	}

	@Test
	public void test() throws Exception {
		String url = "http://localhost:2765/broker-service/api/singel?v=1.0";
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
