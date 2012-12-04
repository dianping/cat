package com.dianping.cat.report.page.cross.display;

import com.dianping.cat.consumer.cross.model.entity.Type;

public class TypeDetailInfo {

	private double m_avg;

	private long m_failureCount;

	private double m_failurePercent;

	private String m_projectName;

	private long m_seconds;

	private double m_sum;

	private long m_totalCount;

	private double m_tps;

	private String m_type;

	private String m_ip;

	public TypeDetailInfo(long seconds) {
		m_seconds = seconds;
	}

	public TypeDetailInfo(long seconds, String projectName) {
		m_seconds = seconds;
		m_projectName = projectName;
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

	public String getIp() {
		return m_ip;
	}

	public String getProjectName() {
		return m_projectName;
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

	public void mergeType(Type type) {
		m_type = type.getId();
		m_totalCount += type.getTotalCount();
		m_failureCount += type.getFailCount();
		m_sum += type.getSum();

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

	public void setProjectName(String projectName) {
		m_projectName = projectName;
	}

}
