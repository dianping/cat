package com.dianping.cat.report.page.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;

public class CacheReport {
	private String m_domain;

	private Set<String> m_domains;

	private java.util.Date m_startTime;

	private java.util.Date m_endTime;

	private Set<String> m_domainNames = new LinkedHashSet<String>();

	private Set<String> m_ips = new LinkedHashSet<String>();

	private Map<String, CacheTypeItem> m_typeItems = new HashMap<String, CacheTypeItem>();

	private Map<String, CacheNameItem> m_nameItems = new HashMap<String, CacheNameItem>();

	private String m_sortBy;

	public void addNewTypeItem(TransactionType transactionType, EventType eventType) {
		String key = transactionType.getId();
		CacheTypeItem item = m_typeItems.get(key);
		if (item == null) {
			item = new CacheTypeItem();
			item.setType(transactionType);
			item.setMissed(eventType.getTotalCount());
			item.setHited((double) eventType.getTotalCount() / transactionType.getTotalCount());
			m_typeItems.put(key, item);
		}
	}

	public void addNewNameItem(TransactionName transactionName, EventName eventName) {
		String key = transactionName.getId();
		CacheNameItem item = m_nameItems.get(key);
		if (item == null) {
			item = new CacheNameItem();
			item.setName(transactionName);
			item.setMissed(eventName.getTotalCount());
			item.setHited((double) eventName.getTotalCount() / transactionName.getTotalCount());
			m_nameItems.put(key, item);
		}
	}

	public String getDomain() {
		return m_domain;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public Set<String> getDomains() {
		return m_domains;
	}

	public void setDomains(Set<String> domains) {
		m_domains = domains;
	}

	public java.util.Date getStartTime() {
		return m_startTime;
	}

	public void setStartTime(java.util.Date startTime) {
		m_startTime = startTime;
	}

	public java.util.Date getEndTime() {
		return m_endTime;
	}

	public void setEndTime(java.util.Date endTime) {
		m_endTime = endTime;
	}

	public Set<String> getDomainNames() {
		return m_domainNames;
	}

	public void setDomainNames(Set<String> domainNames) {
		m_domainNames = domainNames;
	}

	public Set<String> getIps() {
		return m_ips;
	}

	public void setIps(Set<String> ips) {
		m_ips = ips;
	}

	public List<CacheNameItem> getNameItems() {
		List<CacheNameItem> result = new ArrayList<CacheNameItem>(m_nameItems.values());
		Collections.sort(result, new CacheNameItemCompator(m_sortBy));
		return result;
	}

	public List<CacheTypeItem> getTypeItems() {
		List<CacheTypeItem> result = new ArrayList<CacheTypeItem>(m_typeItems.values());
		Collections.sort(result, new CacheTypeItemCompator(m_sortBy));
		return result;
	}

	public static class CacheNameItem {
		private TransactionName m_name;

		private long m_missed;

		private double m_hited;

		public TransactionName getName() {
			return m_name;
		}

		public void setName(TransactionName name) {
			m_name = name;
		}

		public long getMissed() {
			return m_missed;
		}

		public void setMissed(long missed) {
			m_missed = missed;
		}

		public double getHited() {
			return m_hited;
		}

		public void setHited(double hited) {
			m_hited = hited;
		}
	}

	public static class CacheTypeItem {
		private TransactionType m_type;

		private long m_missed;

		private double m_hited;

		public TransactionType getType() {
			return m_type;
		}

		public void setType(TransactionType type) {
			m_type = type;
		}

		public long getMissed() {
			return m_missed;
		}

		public void setMissed(long missed) {
			m_missed = missed;
		}

		public double getHited() {
			return m_hited;
		}

		public void setHited(double hited) {
			m_hited = hited;
		}
	}

	public void setSortBy(String sortBy) {
		m_sortBy = sortBy;
	}

	public static class CacheTypeItemCompator implements Comparator<CacheTypeItem> {

		private String m_sort;

		private CacheTypeItemCompator(String sort) {
			m_sort = sort;
		}

		@Override
		public int compare(CacheTypeItem o1, CacheTypeItem o2) {
			if (m_sort.equals("total")) {
				return (int) (o2.getType().getTotalCount() - o1.getType().getTotalCount());
			} else if (m_sort.equals("missed")) {
				return (int) (o2.getMissed() - o1.getMissed());
			} else if (m_sort.equals("hitPercent")) {
				return (int) (o2.getHited() * 100 - o1.getHited() * 100);
			} else if (m_sort.equals("avg")) {
				return (int) (o2.getType().getAvg() * 100 - o1.getType().getAvg() * 100);
			} else if (m_sort.equals("type")) {
				return o1.getType().getId().compareTo(o2.getType().getId());
			}
			return 0;
		}
	}

	public static class CacheNameItemCompator implements Comparator<CacheNameItem> {

		private String m_sort;

		private CacheNameItemCompator(String sort) {
			m_sort = sort;
		}

		@Override
		public int compare(CacheNameItem o1, CacheNameItem o2) {
			if (m_sort.equals("total")) {
				return (int) (o2.getName().getTotalCount() - o1.getName().getTotalCount());
			} else if (m_sort.equals("missed")) {
				return (int) (o2.getMissed() - o1.getMissed());
			} else if (m_sort.equals("hitPercent")) {
				return (int) (o2.getHited() * 100 - o1.getHited() * 100);
			} else if (m_sort.equals("avg")) {
				return (int) (o2.getName().getAvg() * 100 - o1.getName().getAvg() * 100);
			} else if (m_sort.equals("name")) {
				return o1.getName().getId().compareTo(o2.getName().getId());
			}
			return 0;
		}
	}
}
