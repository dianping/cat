package com.dianping.cat.report.page.cross.display;

import com.dianping.cat.consumer.cross.model.entity.Name;

public class NameDetailInfo {

	private String m_type;

	private double m_avg;

	private long m_failureCount;

	private double m_failurePercent;

	private String m_id;

	private String m_ip;

	private long m_seconds;

	private double m_sum;

	private long m_totalCount;

	private double m_tps;

	public NameDetailInfo(long seconds, String id, String ip, String type) {
		m_seconds = seconds;
		m_id = id;
		m_ip = ip;
		m_type = type;
	}

	public double getAvg() {
		return m_avg;
	}

	public long getFailureCount() {
		return m_failureCount;
	}

	public double getFailurePercent() {
		return m_failurePercent;
	}

	public String getId() {
		return m_id;
	}

	public String getIp() {
		return m_ip;
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
		if (m_seconds > 0) {
			m_tps = m_totalCount / (double) m_seconds;
		}
	}

	public void setAvg(double avg) {
		m_avg = avg;
	}

	public void setFailureCount(long failureCount) {
		m_failureCount = failureCount;
	}

	public void setFailurePercent(double failrePercent) {
		m_failurePercent = failrePercent;
	}

	public void setId(String id) {
		m_id = id;
	}

	public void setIp(String ip) {
		m_ip = ip;
	}

	public void setSum(double sum) {
		m_sum = sum;
	}

	public void setTotalCount(long totalCount) {
		m_totalCount = totalCount;
	}

	public void setType(String type) {
		m_type = type;
	}
}
