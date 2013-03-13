package com.dianping.cat.system.alarm.connector.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Map;

import org.unidal.helper.Files;

import com.dianping.cat.Cat;
import com.dianping.cat.system.alarm.connector.Connector;
import com.dianping.cat.system.alarm.threshold.ThresholdDataEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ThresholdConnector implements Connector {

	@Override
	public ThresholdDataEntity fetchAlarmData(String url) {
		try {
			return getContent(url);
		} catch (Exception e) {
			try {
				return getContent(url);
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		}
		return null;
	}

	private ThresholdDataEntity getContent(String url) throws MalformedURLException, IOException {
		URL data = new URL(url);
		String content = Files.forIO().readFrom(data.openStream(), "utf-8");

		return parseContent(content);
	}

	private ThresholdDataEntity parseContent(String content) {
		Gson gson = new Gson();
		Map<String, String> obj = gson.fromJson(content.trim(), new TypeToken<Map<String, String>>() {
		}.getType());

		ThresholdDataEntity data = new ThresholdDataEntity();
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
