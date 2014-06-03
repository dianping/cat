package com.dianping.cat;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class test {
	public static void main(String[] args) {
		List<String> urls = new ArrayList<String>(
				Arrays.asList(
						"http://localhost:2281/cat/r/monitor?timestamp=1401431719416&group=f5-2400-1-dianping-com&domain=2400-1-dianping-com&key=cpu-usage-core1-core1&op=avg&avg=52",
							"http://localhost:2281/cat/r/monitor?timestamp=1401431719416&group=f5-2400-1-dianping-com&domain=2400-1-dianping-com&key=cpu-usage-core2-core2&op=avg&avg=51",
							"http://localhost:2281/cat/r/monitor?timestamp=1401431719416&group=f5-2400-1-dianping-com&domain=2400-1-dianping-com&key=cpu-usage-core3-core3&op=avg&avg=45",
							"http://localhost:2281/cat/r/monitor?timestamp=1401431719416&group=f5-2400-1-dianping-com&domain=2400-1-dianping-com&key=cpu-usage-core4-core4&op=avg&avg=46",
							"http://localhost:2281/cat/r/monitor?timestamp=1401431719416&group=f5-2400-1-dianping-com&domain=2400-1-dianping-com&key=cpu-usage-core5-core5&op=avg&avg=48",
							"http://localhost:2281/cat/r/monitor?timestamp=1401431719416&group=f5-2400-1-dianping-com&domain=2400-1-dianping-com&key=cpu-usage-core6-core6&op=avg&avg=49",
							"http://localhost:2281/cat/r/monitor?timestamp=1401431719416&group=f5-2400-1-dianping-com&domain=2400-1-dianping-com&key=cpu-usage-core7-core7&op=avg&avg=45",
							"http://localhost:2281/cat/r/monitor?timestamp=1401431719416&group=f5-2400-1-dianping-com&domain=2400-1-dianping-com&key=cpu-usage-core8-core8&op=avg&avg=46",
							"http://localhost:2281/cat/r/monitor?timestamp=1401431719593&group=f5-2400-2-dianping-com&domain=2400-2-dianping-com&key=cpu-usage-core1-core1&op=avg&avg=14",
							"http://localhost:2281/cat/r/monitor?timestamp=1401431719593&group=f5-2400-2-dianping-com&domain=2400-2-dianping-com&key=cpu-usage-core2-core2&op=avg&avg=12",
							"http://localhost:2281/cat/r/monitor?timestamp=1401431719593&group=f5-2400-2-dianping-com&domain=2400-2-dianping-com&key=cpu-usage-core3-core3&op=avg&avg=15",
							"http://localhost:2281/cat/r/monitor?timestamp=1401431719593&group=f5-2400-2-dianping-com&domain=2400-2-dianping-com&key=cpu-usage-core4-core4&op=avg&avg=14",
							"http://localhost:2281/cat/r/monitor?timestamp=1401431719593&group=f5-2400-2-dianping-com&domain=2400-2-dianping-com&key=cpu-usage-core5-core5&op=avg&avg=12",
							"http://localhost:2281/cat/r/monitor?timestamp=1401431719593&group=f5-2400-2-dianping-com&domain=2400-2-dianping-com&key=cpu-usage-core6-core6&op=avg&avg=11",
							"http://localhost:2281/cat/r/monitor?timestamp=1401431719593&group=f5-2400-2-dianping-com&domain=2400-2-dianping-com&key=cpu-usage-core7-core7&op=avg&avg=10",
							"http://localhost:2281/cat/r/monitor?timestamp=1401431719593&group=f5-2400-2-dianping-com&domain=2400-2-dianping-com&key=cpu-usage-core8-core8&op=avg&avg=11"));

		while(true){
			for(String url : urls){
				sendGet(url);
			}
			try {
	         Thread.sleep(10 * 1000);
         } catch (InterruptedException e) {
	         e.printStackTrace();
         }
		}

	}

	public static String sendGet(String url) {
		String result = "";
		BufferedReader in = null;
		try {
			URL realUrl = new URL(url);
			URLConnection connection = realUrl.openConnection();
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			connection.connect();
			Map<String, List<String>> map = connection.getHeaderFields();
			System.out.println(url);
			for (String key : map.keySet()) {
				System.out.println(key + "--->" + map.get(key));
			}
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
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
