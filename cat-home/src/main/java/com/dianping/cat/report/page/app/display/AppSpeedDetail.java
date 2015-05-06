package com.dianping.cat.report.page.app.display;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.dianping.cat.helper.TimeHelper;

public class AppSpeedDetail {

	private Date m_period;

	private int m_minuteOrder;

	private long m_accessNumberSum;

	private double m_responseTimeAvg;

	private double m_slowRatio;

	public long getAccessNumberSum() {
		return m_accessNumberSum;
	}

	public String getDateTime() {
		long time = m_period.getTime() + m_minuteOrder * TimeHelper.ONE_MINUTE;

		return new SimpleDateFormat("HH:mm").format(new Date(time));
	}

	public String getDayTime() {
		return new SimpleDateFormat("yyyy-MM-dd").format(m_period);
	}

	public int getMinuteOrder() {
		return m_minuteOrder;
	}

	public Date getPeriod() {
		return m_period;
	}

	public double getResponseTimeAvg() {
		return m_responseTimeAvg;
	}

	public double getSlowRatio() {
		return m_slowRatio;
	}

	public AppSpeedDetail setAccessNumberSum(long accessNumberSum) {
		m_accessNumberSum = accessNumberSum;
		return this;
	}

	public void setMinuteOrder(int minuteOrder) {
		m_minuteOrder = minuteOrder;
	}

	public void setPeriod(Date period) {
		m_period = period;
	}

	public AppSpeedDetail setResponseTimeAvg(double responseTimeSum) {
		m_responseTimeAvg = responseTimeSum;
		return this;
	}

	public AppSpeedDetail setSlowRatio(double slowRatio) {
		m_slowRatio = slowRatio;
		return this;
	}

}
