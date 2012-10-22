package com.dianping.cat.system.alarm.connector.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Map;

import com.dianping.cat.Cat;
import com.dianping.cat.system.alarm.connector.Connector;
import com.dianping.cat.system.alarm.exception.ExceptionDataEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.site.helper.Files;

public class CatConnector implements Connector {

	@Override
	public ExceptionDataEntity fetchAlarmData(String url) {
		try {
			return getContent(url);
		} catch (Exception e) {
			try {
				return getContent(url);
			} catch (Exception e1) {
				Cat.logError(e1);
			}
		}
		return null;
	}

	private ExceptionDataEntity getContent(String url) throws MalformedURLException, IOException {
		URL data = new URL(url);
		String content = Files.forIO().readFrom(data.openStream(), "utf-8");

		return parseContent(content);
	}

	private ExceptionDataEntity parseContent(String content) {
		Gson gson = new Gson();
		Map<String, String> obj = gson.fromJson(content.trim(), new TypeToken<Map<String, String>>() {
		}.getType());

		ExceptionDataEntity data = new ExceptionDataEntity();
		String count = obj.get("Count");

		if (count != null) {
			data.setCount(Long.parseLong(count));
		}

		String timestamp = obj.get("timestamp");
		if (timestamp != null) {
			data.setDate(new Date(Long.parseLong(timestamp)));
		}
		return data;
	}
}
