package com.dianping.cat.report.page.transaction;

public class DistributionDetail {

	private String m_ip;

	private long m_totalCount;

	private long m_failCount;

	private double m_failPercent;

	private double m_min;

	private double m_max;

	private double m_avg;

	private double m_std;

	public double getAvg() {
		return m_avg;
	}

	public long getFailCount() {
		return m_failCount;
	}

	public double getFailPercent() {
		return m_failPercent;
	}

	public String getIp() {
		return m_ip;
	}

	public double getMax() {
		return m_max;
	}

	public double getMin() {
		return m_min;
	}

	public double getStd() {
		return m_std;
	}

	public long getTotalCount() {
		return m_totalCount;
	}

	public DistributionDetail setAvg(double avg) {
		m_avg = avg;
		return this;
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

	public DistributionDetail setMax(double max) {
		m_max = max;
		return this;
	}

	public DistributionDetail setMin(double min) {
		m_min = min;
		return this;
	}

	public DistributionDetail setStd(double std) {
		m_std = std;
		return this;
	}

	public DistributionDetail setTotalCount(long totalCount) {
		m_totalCount = totalCount;
		return this;
	}

}
