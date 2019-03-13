/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.exception.entity.ExceptionLimit;
import com.dianping.cat.report.alert.exception.ExceptionRuleConfigManager;

public class TopMetric extends BaseVisitor {

	private transient ExceptionRuleConfigManager m_configManager;

	private transient List<String> m_excludedDomains;

	private transient String m_currentDomain;

	private transient Date m_currentStart;

	private transient SimpleDateFormat m_sdf = new SimpleDateFormat("HH:mm");

	private MetricItem m_error;

	private transient long m_currentTime = System.currentTimeMillis();

	private Integer m_currentMinute;

	private Date m_end;

	private Date m_start;

	public TopMetric(int count, int tops, ExceptionRuleConfigManager configManager) {
		m_configManager = configManager;
		m_error = new MetricItem(count, tops);
	}

	public TopMetric(int count, int tops, ExceptionRuleConfigManager configManager, List<String> excludedDomains) {
		this(count, tops, configManager);
		m_excludedDomains = excludedDomains;
	}

	public MetricItem getError() {
		return m_error;
	}

	public void setError(MetricItem error) {
		m_error = error;
	}

	public TopMetric setEnd(Date end) {
		m_end = end;
		return this;
	}

	public TopMetric setStart(Date start) {
		m_start = start;
		return this;
	}

	@Override
	public void visitDomain(Domain domain) {
		m_currentDomain = domain.getName();

		if (m_excludedDomains == null || !m_excludedDomains.contains(m_currentDomain)) {
			super.visitDomain(domain);
		}
	}

	@Override
	public void visitError(Error error) {
		String exception = error.getId();
		long count = error.getCount();
		Date minute = new Date(m_currentStart.getTime() + m_currentMinute * TimeHelper.ONE_MINUTE);
		String minuteStr = m_sdf.format(minute);

		m_error.addError(minuteStr, m_currentDomain, exception, count);
		super.visitError(error);
	}

	@Override
	public void visitSegment(Segment segment) {
		m_currentMinute = segment.getId();
		long time = m_currentStart.getTime() + m_currentMinute * TimeHelper.ONE_MINUTE;

		if (m_start != null && m_end != null) {
			if (time > m_end.getTime() || time < m_start.getTime()) {
				return;
			}
		}
		if (time <= m_currentTime + TimeHelper.ONE_MINUTE) {
			Date minute = new Date(m_currentStart.getTime() + m_currentMinute * TimeHelper.ONE_MINUTE);
			String minuteStr = m_sdf.format(minute);

			m_error.addIndex(minuteStr, m_currentDomain, segment.getError());
			super.visitSegment(segment);
		}
	}

	@Override
	public void visitTopReport(TopReport topReport) {
		m_currentStart = topReport.getStartTime();
		super.visitTopReport(topReport);

		m_error.buildDisplayResult();
	}

	public static class ItemCompartor implements Comparator<Item> {

		@Override
		public int compare(Item o1, Item o2) {
			int alert = 0;

			if (o2.getAlert() > o1.getAlert()) {
				alert = 1;
			} else if (o2.getAlert() < o1.getAlert()) {
				alert = -1;
			}
			int value = Double.compare(o2.getValue(), o1.getValue());

			return alert == 0 ? value : alert;
		}
	}

	public static class StringCompartor implements Comparator<String> {

		@Override
		public int compare(String o1, String o2) {
			String hour1 = o1.substring(0, 2);
			String hour2 = o2.substring(0, 2);

			if (!hour1.equals(hour2)) {
				int hour1Value = Integer.parseInt(hour1);
				int hour2Value = Integer.parseInt(hour2);

				if (hour1Value == 0 && hour2Value == 23) {
					return -1;
				} else if (hour1Value == 23 && hour2Value == 0) {
					return 1;
				} else {
					return hour2Value - hour1Value;
				}
			} else {
				String first = o1.substring(3, 5);
				String end = o2.substring(3, 5);

				return Integer.parseInt(end) - Integer.parseInt(first);
			}
		}
	}

	public class Item {

		private static final String ERROR_COLOR = "red";

		private static final String WARN_COLOR = "#bfa22f";

		private String m_domain;

		private double m_value;

		private int m_alert;

		private Map<String, Double> m_exceptions = new HashMap<String, Double>();

		public Item(String domain, double value) {
			m_domain = domain;
			m_value = value;
		}

		private String buildErrorText(String str, String color) {
			StringBuilder sb = new StringBuilder();
			sb.append("<span style='color:" + color + "'>").append("<strong>");
			sb.append(str).append("</strong>").append("</span>");

			return sb.toString();
		}

		public int getAlert() {
			return m_alert;
		}

		public String getDomain() {
			return m_domain;
		}

		public void setDomain(String domain) {
			m_domain = domain;
		}

		public String getErrorInfo() {
			StringBuilder sb = new StringBuilder();

			for (Entry<String, Double> entry : m_exceptions.entrySet()) {

				double value = entry.getValue().doubleValue();
				double warnLimit = -1;
				double errorLimit = -1;

				if (m_configManager != null) {
					ExceptionLimit exceptionLimit = m_configManager.queryExceptionLimit(m_domain, entry.getKey());
					if (exceptionLimit != null) {
						warnLimit = exceptionLimit.getWarning();
						errorLimit = exceptionLimit.getError();
					}
				}
				if (errorLimit > 0 && value >= errorLimit) {
					sb.append(buildErrorText(entry.getKey() + " " + value, ERROR_COLOR)).append("<br/>");
				} else if (warnLimit > 0 && value >= warnLimit) {
					sb.append(buildErrorText(entry.getKey() + " " + value, WARN_COLOR)).append("<br/>");
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

		public void setValue(double value) {
			m_value = value;
			double warningLimit = -1;
			double errorLimit = -1;

			if (m_configManager != null) {
				ExceptionLimit totalLimit = m_configManager.queryTotalLimitByDomain(m_domain);

				if (totalLimit != null) {
					warningLimit = totalLimit.getWarning();
					errorLimit = totalLimit.getError();
				}
			}
			if (errorLimit > 0 && value > errorLimit) {
				m_alert = 2;
			} else if (warningLimit > 0 && value > warningLimit) {
				m_alert = 1;
			}
		}

		public void setExceptions(Map<String, Double> exceptions) {
			m_exceptions = exceptions;
		}
	}

	public class MetricItem {
		private int m_minuteCount;

		private int m_itemSize;

		private transient Map<String, Map<String, Item>> m_items = new LinkedHashMap<String, Map<String, Item>>();

		private Map<String, List<Item>> m_result;

		public MetricItem(int minuteCount, int itemSize) {
			m_minuteCount = minuteCount;
			m_itemSize = itemSize;
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

		public void addIndex(String minute, String domain, double value) {
			Item item = findOrCreateItem(minute, domain);
			item.setValue(item.getValue() + value);
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

		private Item findOrCreateItem(String minute, String domain) {
			Map<String, Item> temp = m_items.get(minute);

			if (temp == null) {
				temp = new LinkedHashMap<String, Item>();
				m_items.put(minute, temp);
			}
			Item item = temp.get(domain);

			if (item == null) {
				item = new Item(domain, 0);
				temp.put(domain, item);
			}

			return item;
		}

		public Map<String, List<Item>> getResult() {
			return m_result;
		}
	}
}
