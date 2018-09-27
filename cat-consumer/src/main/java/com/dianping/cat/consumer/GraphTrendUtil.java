package com.dianping.cat.consumer;

import org.codehaus.plexus.util.StringUtils;

public class GraphTrendUtil {
	public static final String GRAPH_SPLITTER = ";";

	public static final char GRAPH_CHAR_SPLITTER = ';';

	public static Double[] parseToDouble(String str, int length) {
		Double[] result = new Double[length];

		if (StringUtils.isNotBlank(str)) {
			String[] strs = str.split(GraphTrendUtil.GRAPH_SPLITTER);

			for (int i = 0; i < length; i++) {
				try {
					result[i] = Double.parseDouble(strs[i]);
				} catch (Exception e) {
					result[i] = 0.0;
				}
			}
		} else {
			for (int i = 0; i < length; i++) {
				result[i] = 0.0;
			}
		}
		return result;
	}

	public static Long[] parseToLong(String str, int length) {
		Long[] result = new Long[length];

		if (StringUtils.isNotBlank(str)) {
			String[] strs = str.split(GraphTrendUtil.GRAPH_SPLITTER);

			for (int i = 0; i < length; i++) {
				try {
					result[i] = Long.parseLong(strs[i]);
				} catch (Exception e) {
					result[i] = 0L;
				}
			}
		} else {
			for (int i = 0; i < length; i++) {
				result[i] = 0L;
			}
		}
		return result;
	}
}
