package com.dianping.cat.report.page.top;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.consumer.top.model.entity.Domain;
import com.dianping.cat.consumer.top.model.entity.Segment;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.consumer.top.model.transform.BaseVisitor;
import com.dianping.cat.helper.TimeUtil;

public class Metric extends BaseVisitor {

	private String m_currentDomain;

	private Date m_start;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("HH:mm");

	private int m_count;

	private MetricItem m_error;

	private MetricItem m_url;

	private MetricItem m_service;

	private MetricItem m_call;

	private MetricItem m_sql;

	private MetricItem m_cache;
	
	private static final int DEFAULT = 10;

	private long m_currentTime = System.currentTimeMillis();

	public Metric() {
		this(DEFAULT);
	}

	public Metric(int count) {
		m_count = count;
		m_error = new MetricItem(m_count);
		m_url = new MetricItem(m_count);
		m_service = new MetricItem(m_count);
		m_call = new MetricItem(m_count);
		m_sql = new MetricItem(m_count);
		m_cache = new MetricItem(m_count);
	}

	public MetricItem getCache() {
		return m_cache;
	}

	public MetricItem getCall() {
		return m_call;
	}

	public MetricItem getError() {
		return m_error;
	}

	public MetricItem getService() {
		return m_service;
	}

	public MetricItem getSql() {
		return m_sql;
	}

	public MetricItem getUrl() {
		return m_url;
	}

	public void setError(MetricItem error) {
		m_error = error;
	}

	@Override
	public void visitDomain(Domain domain) {
		m_currentDomain = domain.getName();
		super.visitDomain(domain);
	}

	@Override
	public void visitSegment(Segment segment) {
		int minute = segment.getId();
		long time = m_start.getTime() + minute * TimeUtil.ONE_MINUTE;
		if (time <= m_currentTime + TimeUtil.ONE_MINUTE) {
			m_error.add(m_sdf.format(new Date(m_start.getTime() + minute * TimeUtil.ONE_MINUTE)), m_currentDomain,
			      segment.getError());
			m_url.add(m_sdf.format(new Date(m_start.getTime() + minute * TimeUtil.ONE_MINUTE)), m_currentDomain,
			      segment.getUrlDuration());
			m_service.add(m_sdf.format(new Date(m_start.getTime() + minute * TimeUtil.ONE_MINUTE)), m_currentDomain,
			      segment.getServiceDuration());
			m_call.add(m_sdf.format(new Date(m_start.getTime() + minute * TimeUtil.ONE_MINUTE)), m_currentDomain,
			      segment.getCallDuration());
			m_sql.add(m_sdf.format(new Date(m_start.getTime() + minute * TimeUtil.ONE_MINUTE)), m_currentDomain,
			      segment.getSqlDuration());
			m_cache.add(m_sdf.format(new Date(m_start.getTime() + minute * TimeUtil.ONE_MINUTE)), m_currentDomain,
			      segment.getCacheDuration());
		}
	}

	@Override
	public void visitTopReport(TopReport topReport) {
		m_start = topReport.getStartTime();
		super.visitTopReport(topReport);
	}

	public static class Item {

		private String m_domain;

		private double m_value;

		public Item(String domain, double value) {
			m_domain = domain;
			m_value = value;
		}

		@Override
		public Item clone() {
			return new Item(m_domain, m_value);
		}

		public String getDomain() {
			return m_domain;
		}

		public double getValue() {
			return m_value;
		}

		public void setDomain(String domain) {
			m_domain = domain;
		}

		public void setValue(double value) {
			m_value = value;
		}
	}

	public static class ItemCompartor implements Comparator<Item> {

		@Override
		public int compare(Item o1, Item o2) {
			return (int) (o2.getValue() - o1.getValue());
		}
	}

	public static class MetricItem {
		private int m_minuteCount;

		private int m_itemSize = 10;

		private Map<String, ArrayList<Item>> m_result = new LinkedHashMap<String, ArrayList<Item>>();

		public MetricItem(int minuteCount) {
			m_minuteCount = minuteCount;
		}

		public void add(String minute, String domain, double value) {
			ArrayList<Item> temp = m_result.get(minute);

			if (temp == null) {
				temp = new ArrayList<Item>();
				m_result.put(minute, temp);
			}
			temp.add(new Item(domain, value));
		}

		public Map<String, List<Item>> getResult() {
			Map<String, List<Item>> temp = new LinkedHashMap<String, List<Item>>();
			List<String> keyList = new ArrayList<String>(m_result.keySet());
			Collections.sort(keyList, new StringCompartor());

			if (keyList.size() > m_minuteCount) {
				keyList = keyList.subList(0, m_minuteCount);
			}

			synchronized (m_result) {
				for (String key : keyList) {
					ArrayList<Item> value = m_result.get(key);
					List<Item> other = new ArrayList<Item>();

					for (Item item : value) {
						other.add(item.clone());
					}

					Collections.sort(other, new ItemCompartor());

					if (other.size() > m_itemSize) {
						other = other.subList(0, m_itemSize);
					}

					if (keyList.contains(key)) {
						temp.put(key, other);
					}
				}
			}

			return temp;
		}
	}

	public static class StringCompartor implements Comparator<String> {

		@Override
		public int compare(String o1, String o2) {
			String first = o1.substring(3, 5);
			String end = o2.substring(3, 5);

			return Integer.parseInt(end) - Integer.parseInt(first);
		}
	}

}
