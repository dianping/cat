package com.dianping.cat.status;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public abstract class AbstractCollector implements StatusExtension {

	protected Map<String, String> convert(Map<String, Number> map) {
		Map<String, String> result = new LinkedHashMap<String, String>();

		for (Entry<String, Number> entry : map.entrySet()) {
			result.put(entry.getKey(), entry.getValue().toString());
		}
		return result;
	}
	
	@Override
	public String getDescription() {
		return getId();
	}

}
