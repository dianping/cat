package com.dianping.dog.notify.job;

import java.util.Hashtable;
import java.util.Map;

public class JobContext {

	Map<String, Object> data;

	public void addData(String key, Object value) {
		if (data == null) {
			data = new Hashtable<String, Object>();
		}
		data.put(key, value);
	}

	public Object getData(String key) {
		return data.get(key);
	}

}
