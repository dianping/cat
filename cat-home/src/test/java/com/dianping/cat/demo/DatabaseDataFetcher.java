package com.dianping.cat.demo;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.helper.Urls;
import org.unidal.webres.json.JsonArray;
import org.unidal.webres.json.JsonObject;

public class DatabaseDataFetcher {

	public String url = "http://cat.dp/cat/r/database?op=view&date=2015060%s23&domain=cat&product=db-mysql-rcpt01.nh[10.1.101.131]&timeRange=24&forceDownload=json";

	@Test
	public void test() {
		for (int n = 2; n < 7; n++) {
			try {
				InputStream stream = Urls.forIO().connectTimeout(5000).readTimeout(10000).openStream(String.format(url, n));
				String result = Files.forIO().readFrom(stream, "utf-8");
				JsonObject jo = new JsonObject(result);
				JsonArray array = jo.getJSONArray("lineCharts");
				Map<Long, Double> datas = new LinkedHashMap<Long, Double>();

				for (int i = 0; i < array.length(); i++) {
					JsonObject o = array.getJSONObject(i);
					String title = o.getString("title");
					
					if (title.contains("THREADS_RUNNING")) {
						JsonArray arys = o.getJSONArray("datas");
						JsonObject object = arys.getJSONObject(0);
						JsonArray names = object.names();

						List<Long> sortedNames = new ArrayList<Long>();

						for (int k = 0; k < names.length(); k++) {
							String key = names.getString(k);
							sortedNames.add(Long.parseLong(key));
						}

						Collections.sort(sortedNames);

						for (int j = 0; j < sortedNames.size(); j++) {
							Long key = sortedNames.get(j);
							datas.put(key, Double.parseDouble(object.getString(String.valueOf(key))));
						}
					}
				}
				System.out.println(datas);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}
}
