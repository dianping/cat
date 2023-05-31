package com.dianping.cat.alarm.server;

import org.unidal.lookup.util.StringUtils;

public class TagSplitHelper {

	public static String queryByKey(String tags, String key) {
		if (StringUtils.isNotEmpty(tags)) {
			String[] fields = tags.split(";");

			for (int i = 0; i < fields.length; i++) {
				String field = fields[i];

				if (field.startsWith(key + "=")) {
					String[] kv = field.split("=");

					return kv[1].replace("'", "");
				}
			}
		}
		return tags;
	}

	public static String queryDomain(String tags) {
		return queryByKey(tags, "domain");
	}
}
