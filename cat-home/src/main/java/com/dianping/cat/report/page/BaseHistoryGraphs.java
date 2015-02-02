package com.dianping.cat.report.page;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cat.helper.TimeHelper;

public class BaseHistoryGraphs {
	
	private String buildSingalTitle(Date date, int size, long step) {
		SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat to = new SimpleDateFormat("MM-dd");
		StringBuilder sb = new StringBuilder();

		sb.append(from.format(date)).append("~").append(to.format(new Date(date.getTime() + step * size)));
		return sb.toString();
	}

	protected List<String> buildSubTitle(Date date, int size, long step, String queryType) {
		List<String> result = new ArrayList<String>();

		if (queryType.equals("day")) {
			result.add(buildSingalTitle(date, size, step));
			result.add(buildSingalTitle(new Date(date.getTime() - TimeHelper.ONE_DAY), size, step));
			result.add(buildSingalTitle(new Date(date.getTime() - 7 * TimeHelper.ONE_DAY), size, step));
		} else if (queryType.equals("week")) {
			result.add(buildSingalTitle(date, size, step));
			result.add(buildSingalTitle(new Date(date.getTime() - 7 * TimeHelper.ONE_DAY), size, step));
		} else if (queryType.equals("month")) {
			result.add(buildSingalTitle(date, size, step));
		}
		return result;
	}

	protected void mergerList(List<Map<String, double[]>> src, List<Map<String, double[]>> des) {
		int length = src.size();

		for (int i = 0; i < length; i++) {
			Map<String, double[]> first = src.get(i);
			Map<String, double[]> next = des.get(i);

			for (Entry<String, double[]> entry : first.entrySet()) {
				String key = entry.getKey();
				double[] firstValue = entry.getValue();
				double[] nextValue = next.get(key);

				mergeValue(firstValue, nextValue);
			}
		}
	}

	protected void mergeValue(double[] src, double[] des) {
		int length = src.length;

		for (int i = 0; i < length; i++) {
			src[i] = src[i] + des[i];
		}
	}
}
