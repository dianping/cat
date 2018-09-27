package com.dianping.cat.report.page.eslog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.webres.json.JsonArray;
import org.unidal.webres.json.JsonObject;

import com.dianping.cat.Cat;

public class HttpTest {

	private static String s_pars ="{ \"query\": { \"filtered\": {  \"query\": { \"match\": {\"dpid\": \"6397090225317379358\"}}, \"filter\": { \"range\": {\"request_time\": { \"gte\": \"2015-11-17T00:21:21.000Z\", \"lte\": \"2015-11-18T08:21:21.000Z\"}}}}},\"sort\": [{ \"request_time\":   { \"order\": \"asc\" }}],\"size\": 100}";
	
	
	@Test
	public void testFormat(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		
		System.err.println(sdf.format(new Date()));
	}
	
	@Test
	public void test() {
		String pars = s_pars.replace("${dpId}", "8008015065141028025").replace("${start}", "2015-11-03T00:42:00.000Z")
		      .replace("${end}", "2015-11-03T11:42:00.000Z");
		String url = "http://logcenter-es-service01.gq:9200/dpods_log_applog-web/docs/_search?";

		if (url != null) {
			try {
				String content = httpPostSend(url, pars);

				System.err.println("==content=="+content);
				JsonObject object = new JsonObject(content);
				JsonArray hits = object.getJSONObject("hits").getJSONArray("hits");
				int length = hits.length();
				List<String> logs = new ArrayList<String>();

				for (int i = 0; i < length; i++) {
					JsonObject log = hits.getJSONObject(i).getJSONObject("_source");

					logs.add(log.toString());
				}
				System.out.println(logs);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
	}

	private static String httpPostSend(String urlPrefix, String pars) {
		URL url = null;
		InputStream in = null;
		OutputStreamWriter writer = null;
		URLConnection conn = null;

		try {
			url = new URL(urlPrefix);
			
			System.out.println(urlPrefix);
			System.out.println(pars);
			conn = url.openConnection();

			conn.setConnectTimeout(2000);
			conn.setReadTimeout(3000);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestProperty("content-type", "application/json");
			writer = new OutputStreamWriter(conn.getOutputStream());

			writer.write(pars);
			writer.flush();

			in = conn.getInputStream();
			StringBuilder sb = new StringBuilder();

			sb.append(Files.forIO().readFrom(in, "utf-8")).append("");

			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			Cat.logError(e);
			return null;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
			}
		}
	}
}
