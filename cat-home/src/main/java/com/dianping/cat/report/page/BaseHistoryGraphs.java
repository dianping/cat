package com.dianping.cat.report.page;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.dianping.cat.helper.TimeUtil;

public class BaseHistoryGraphs {

	protected List<String> buildSubTitle(Date date, int size, long step, String queryType) {
		List<String> result = new ArrayList<String>();

		if (queryType.equals("day")) {
			result.add(buildSingalTitle(date, size, step));
			result.add(buildSingalTitle(new Date(date.getTime() - TimeUtil.ONE_DAY), size, step));
			result.add(buildSingalTitle(new Date(date.getTime() - 7 * TimeUtil.ONE_DAY), size, step));
		} else if (queryType.equals("week")) {
			result.add(buildSingalTitle(date, size, step));
			result.add(buildSingalTitle(new Date(date.getTime() - 7*TimeUtil.ONE_DAY), size, step));
		} else if (queryType.equals("month")) {
			result.add(buildSingalTitle(date, size, step));
		}
		return result;
	}

	protected String buildSingalTitle(Date date, int size, long step) {
		SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd");
		StringBuilder sb = new StringBuilder();

		sb.append("From ").append(m_sdf.format(date)).append(" To ")
		      .append(m_sdf.format(new Date(date.getTime() + step * size)));
		return sb.toString();
	}
}
