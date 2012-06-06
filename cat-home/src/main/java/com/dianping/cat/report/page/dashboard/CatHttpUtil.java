package com.dianping.cat.report.page.dashboard;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class CatHttpUtil {

	private static Gson gson = new Gson();

	public static Map<String, String> getCatInfo(String catUrl) {
		try {
			URL url = new URL(catUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			int nRc = conn.getResponseCode();
			if (nRc == HttpURLConnection.HTTP_OK) {
				InputStream input = conn.getInputStream();
				byte[] temp = new byte[1024];
				StringBuilder sb = new StringBuilder();

				while (input.read(temp) > 0) {
					sb.append(new String(temp, "utf-8"));
				}
				return gson.fromJson(sb.toString().trim(), new TypeToken<Map<String, String>>() {
				}.getType());
			}
		} catch (Exception e) {
			// ignore
		}
		return new HashMap<String, String>();
	}
}
