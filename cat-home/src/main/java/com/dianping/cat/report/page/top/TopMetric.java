package com.dianping.cat.report.page.top;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cat.consumer.top.model.entity.Domain;
import com.dianping.cat.consumer.top.model.entity.Error;
import com.dianping.cat.consumer.top.model.entity.Segment;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.consumer.top.model.transform.BaseVisitor;
import com.dianping.cat.helper.TimeUtil;

public class TopMetric extends BaseVisitor {

	private String m_currentDomain;

	private Date m_currentStart;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("HH:mm");

	private MetricItem m_error;

	private MetricItem m_url;

	private MetricItem m_service;

	private MetricItem m_call;

	private MetricItem m_sql;

	private MetricItem m_cache;

	private long m_currentTime = System.currentTimeMillis();

	private Integer m_currentMinute;

	private Date m_end;

	private Date m_start;

	public TopMetric setStart(Date start) {
		m_start = start;
		return this;
	}

	public TopMetric setEnd(Date end) {
		m_end = end;
		return this;
	}

	public TopMetric(int count, int tops) {
		m_error = new MetricItem(count, tops);
		m_url = new MetricItem(count, tops);
		m_service = new MetricItem(count, tops);
		m_call = new MetricItem(count, tops);
		m_sql = new MetricItem(count, tops);
		m_cache = new MetricItem(count, tops);
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
	public void visitError(Error error) {
		String exception = error.getId();
		long count = error.getCount();
		Date minute = new Date(m_currentStart.getTime() + m_currentMinute * TimeUtil.ONE_MINUTE);
		String minuteStr = m_sdf.format(minute);

		m_error.addError(minuteStr, m_currentDomain, exception, count);
		super.visitError(error);
	}

	@Override
	public void visitSegment(Segment segment) {
		m_currentMinute = segment.getId();
		long time = m_currentStart.getTime() + m_currentMinute * TimeUtil.ONE_MINUTE;

		if (m_start != null && m_end != null) {
			if (time > m_end.getTime() || time < m_start.getTime()) {
				return;
			}
		}
		if (time <= m_currentTime + TimeUtil.ONE_MINUTE) {
			Date minute = new Date(m_currentStart.getTime() + m_currentMinute * TimeUtil.ONE_MINUTE);
			String minuteStr = m_sdf.format(minute);

			m_error.addIndex(minuteStr, m_currentDomain, segment.getError());
			m_url.addIndex(minuteStr, m_currentDomain, segment.getUrlDuration());
			m_service.addIndex(minuteStr, m_currentDomain, segment.getServiceDuration());
			m_call.addIndex(minuteStr, m_currentDomain, segment.getCallDuration());
			m_sql.addIndex(minuteStr, m_currentDomain, segment.getSqlDuration());
			m_cache.addIndex(minuteStr, m_currentDomain, segment.getCacheDuration());
		}
		super.visitSegment(segment);
	}

	@Override
	public void visitTopReport(TopReport topReport) {
		m_currentStart = topReport.getStartTime();
		super.visitTopReport(topReport);

		m_error.buildDisplayResult();
		m_url.buildDisplayResult();
		m_service.buildDisplayResult();
		m_call.buildDisplayResult();
		m_sql.buildDisplayResult();
		m_cache.buildDisplayResult();
	}

	public static class Item {

		private String m_domain;

		private double m_value;

		private Map<String, Double> m_exceptions = new HashMap<String, Double>();

		public Item(String domain, double value) {
			m_domain = domain;
			m_value = value;
		}

		public String getDomain() {
			return m_domain;
		}

		private String buildErrorText(String str) {
			StringBuilder sb = new StringBuilder();
			sb.append("<span style='color:red'>").append("<strong>");
			sb.append(str).append("</strong>").append("</span>");

			return sb.toString();
		}

		public String getErrorInfo() {
			StringBuilder sb = new StringBuilder();

			for (Entry<String, Double> entry : m_exceptions.entrySet()) {

				double value = entry.getValue().doubleValue();
				if (value > 200) {
					sb.append(buildErrorText(entry.getKey() + " " + value)).append("<br/>");
				} else {
					sb.append(entry.getKey()).append(" ");
					sb.append(value).append("<br/>");
				}
			}
			return sb.toString();
		}

		public Map<String, Double> getException() {
			return m_exceptions;
		}

		public double getValue() {
			return m_value;
		}

		public void setDomain(String domain) {
			m_domain = domain;
		}

		public void setExceptions(Map<String, Double> exceptions) {
			m_exceptions = exceptions;
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

		private int m_itemSize;

		private Map<String, Map<String, Item>> m_items = new LinkedHashMap<String, Map<String, Item>>();

		private Map<String, List<Item>> m_result;

		public MetricItem(int minuteCount, int itemSize) {
			m_minuteCount = minuteCount;
			m_itemSize = itemSize;
		}

		private Item findOrCreateItem(String minute, String domain) {
			Map<String, Item> temp = m_items.get(minute);

			if (temp == null) {
				temp = new HashMap<String, Item>();
				m_items.put(minute, temp);
			}
			Item item = temp.get(domain);

			if (item == null) {
				item = new Item(domain, 0);
				temp.put(domain, item);
			}

			return item;
		}

		public void addIndex(String minute, String domain, double value) {
			Item item = findOrCreateItem(minute, domain);
			item.setValue(item.getValue() + value);
		}

		public void addError(String minute, String domain, String exception, long count) {
			Item item = findOrCreateItem(minute, domain);
			Double d = item.getException().get(exception);

			if (d == null) {
				d = new Double(count);
			} else {
				d = d + count;
			}
			item.getException().put(exception, d);
		}

		public Map<String, List<Item>> getResult() {
			return m_result;
		}

		public void buildDisplayResult() {
			Map<String, List<Item>> temp = new LinkedHashMap<String, List<Item>>();
			List<String> keyList = new ArrayList<String>(m_items.keySet());
			Collections.sort(keyList, new StringCompartor());

			if (keyList.size() > m_minuteCount) {
				keyList = keyList.subList(0, m_minuteCount);
			}

			for (String key : keyList) {
				List<Item> valule = new ArrayList<Item>(m_items.get(key).values());

				Collections.sort(valule, new ItemCompartor());

				if (valule.size() > m_itemSize) {
					valule = valule.subList(0, m_itemSize);
				}

				if (keyList.contains(key)) {
					temp.put(key, valule);
				}
			}
			m_result = temp;
		}
	}

	public static class StringCompartor implements Comparator<String> {

		@Override
		public int compare(String o1, String o2) {
			String hour1 = o1.substring(0, 2);
			String hour2 = o2.substring(0, 2);

			if (!hour1.equals(hour2)) {
				return Integer.parseInt(hour2) - Integer.parseInt(hour1);
			} else {
				String first = o1.substring(3, 5);
				String end = o2.substring(3, 5);

				return Integer.parseInt(end) - Integer.parseInt(first);
			}
		}
	}
}
