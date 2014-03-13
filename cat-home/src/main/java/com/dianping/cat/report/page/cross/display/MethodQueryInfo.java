package com.dianping.cat.report.page.cross.display;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.consumer.cross.model.entity.Name;

public class MethodQueryInfo {

	public Map<String, Item> m_items = new HashMap<String, Item>();

	public void add(String remoteIp, String currentRole, String domain, String method, Name name) {
		String key = remoteIp + ":" + currentRole + ":" + method;
		Item item = m_items.get(key);

		if (item == null) {
			item = new Item();
			item.setDomain(domain).setIp(remoteIp).setType(currentRole).setMethod(method);

			m_items.put(key, item);
		}
		item.mergeName(name);
	}

	public List<Item> getItems() {
		return new ArrayList<Item>(m_items.values());
	}

	public static class Item {

		private String m_ip;

		private String m_domain;

		private String m_method;

		private String m_type;

		private double m_avg;

		private long m_failureCount;

		private double m_failurePercent;

		private double m_sum;

		private long m_totalCount;

		private double m_tps;

		public double getAvg() {
			return m_avg;
		}

		public String getDomain() {
			return m_domain;
		}

		public long getFailureCount() {
			return m_failureCount;
		}

		public double getFailurePercent() {
			return m_failurePercent;
		}

		public String getIp() {
			return m_ip;
		}

		public String getMethod() {
			return m_method;
		}

		public double getSum() {
			return m_sum;
		}

		public long getTotalCount() {
			return m_totalCount;
		}

		public double getTps() {
			return m_tps;
		}

		public String getType() {
			return m_type;
		}

		public void mergeName(Name name) {
			m_totalCount += name.getTotalCount();
			m_failureCount += name.getFailCount();
			m_sum += name.getSum();

			if (m_totalCount > 0) {
				m_avg = m_sum / (double) m_totalCount;
				m_failurePercent = (double) m_failureCount / (double) m_totalCount;
			}
		}

		public Item setAvg(double avg) {
			m_avg = avg;
			return this;
		}

		public Item setDomain(String domain) {
			m_domain = domain;
			return this;
		}

		public Item setFailureCount(long failureCount) {
			m_failureCount = failureCount;
			return this;
		}

		public Item setFailurePercent(double failurePercent) {
			m_failurePercent = failurePercent;
			return this;
		}

		public Item setIp(String ip) {
			m_ip = ip;
			return this;
		}

		public Item setMethod(String method) {
			m_method = method;
			return this;
		}

		public Item setSum(double sum) {
			m_sum = sum;
			return this;
		}

		public Item setTotalCount(long totalCount) {
			m_totalCount = totalCount;
			return this;
		}

		public Item setTps(double tps) {
			m_tps = tps;
			return this;
		}

		public Item setType(String type) {
			m_type = type;
			return this;
		}
	}

}
