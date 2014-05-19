package com.dianping.cat.broker;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Test;
import org.unidal.helper.Files;

public class PostTest {

	@Test
	public void test() throws Exception {
		String url = "http://localhost:2765/broker-server/monitor?op=batch";

		URLConnection conn = new URL(url).openConnection();

		conn.setDoOutput(true);
		conn.setDoInput(true);
		OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

		String content = "1400037748182\thttp\t300\t200\t300\n1400037748182\thttp\t300\t200\t300\n1400037748182\thttp\t300\t200\t300\n";
		writer.write( content);
		writer.flush();

		InputStream in = conn.getInputStream();
		String result = Files.forIO().readFrom(in, "utf-8");

		System.out.println(result);
	}

}
