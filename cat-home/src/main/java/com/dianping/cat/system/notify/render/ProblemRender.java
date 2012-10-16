package com.dianping.cat.system.notify.render;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.problem.ProblemStatistics;
import com.dianping.cat.report.page.problem.ProblemStatistics.TypeStatistics;

public class ProblemRender {

	private Date m_date;

	private String m_dateStr;

	private String m_domain;

	private Map<Object, Object> m_result = new HashMap<Object, Object>();

	private List<Type> m_types = new ArrayList<Type>();

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyyMMddHH");

	private String m_problemLink = "http://%s/cat/r/p?op=history&domain=%s&date=%s&reportType=day";

	private String m_typeGraphLink = "http://%s/cat/r/p?op=historyGraph&domain=%s&date=%s&ip=All&reportType=day&type=%s";

	private String m_host;

	public ProblemRender(Date date, String domain) {
		m_domain = domain;
		m_date = date;
		m_dateStr = m_sdf.format(date);

		String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
		if (ip.startsWith("10.")) {
			m_host = CatString.ONLINE;
		} else {
			m_host = CatString.OFFLINE;
		}
	}

	public void visitProblemReport(ProblemReport report) {
		ProblemStatistics problemStatistics = new ProblemStatistics();
		problemStatistics.setAllIp(true);

		problemStatistics.visitProblemReport(report);

		Collection<TypeStatistics> status = problemStatistics.getStatus().values();
		for (TypeStatistics statistic : status) {
			String type = statistic.getType();
			int count = statistic.getCount();
			String graphUrl = buildGraphUrl(type);
			Type temp = new Type();

			temp.setType(type).setCount(count).setUrl(graphUrl);
			m_types.add(temp);
		}
		Date lastDay = new Date(m_date.getTime() - TimeUtil.ONE_DAY);
		Date lastWeek = new Date(m_date.getTime() - 7 * TimeUtil.ONE_DAY);
		String currentUrl = buildProblemUrl(m_date);
		String lastDayUrl = buildProblemUrl(lastDay);
		String lastWeekUrl = buildProblemUrl(lastWeek);

		m_result.put("current", currentUrl);
		m_result.put("lastDay", lastDayUrl);
		m_result.put("lastWeek", lastWeekUrl);
		m_result.put("types", m_types);
	}

	private String buildProblemUrl(Date date) {
		String dateStr = m_sdf.format(m_date);

		return String.format(m_problemLink, m_host, m_domain, dateStr);
	}

	private String buildGraphUrl(String type) {
		return String.format(m_typeGraphLink, m_host, m_domain, m_dateStr, type);
	}

	public Map<Object, Object> getRenderResult() {
		return m_result;
	}

	public static class Type {
		private String m_type;

		private int m_count;

		private String m_url;

		public String getType() {
			return m_type;
		}

		public Type setType(String type) {
			m_type = type;
			return this;
		}

		public int getCount() {
			return m_count;
		}

		public Type setCount(int count) {
			m_count = count;
			return this;
		}

		public String getUrl() {
			return m_url;
		}

		public Type setUrl(String url) {
			m_url = url;
			return this;
		}

	}
}
