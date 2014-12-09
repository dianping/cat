package com.dianping.cat.report.page.event;

public class DistributionDetail {

	private String m_ip;

	private long m_totalCount;

	private long m_failCount;

	private double m_failPercent;

	public long getFailCount() {
		return m_failCount;
	}

	public double getFailPercent() {
		return m_failPercent;
	}

	public String getIp() {
		return m_ip;
	}

	public long getTotalCount() {
		return m_totalCount;
	}

	public DistributionDetail setFailCount(long failCount) {
		m_failCount = failCount;
		return this;
	}

	public DistributionDetail setFailPercent(double failPercent) {
		m_failPercent = failPercent;
		return this;
	}

	public DistributionDetail setIp(String ip) {
		m_ip = ip;
		return this;
	}

	public DistributionDetail setTotalCount(long totalCount) {
		m_totalCount = totalCount;
		return this;
	}
}
