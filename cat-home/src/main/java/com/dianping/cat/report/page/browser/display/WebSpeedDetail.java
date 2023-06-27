package com.dianping.cat.report.page.browser.display;

import com.dianping.cat.helper.TimeHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WebSpeedDetail {

	private Date m_period;

	private int m_minuteOrder;

	private long m_accessNumberSum;

	private double m_responseTimeAvg;

	private String m_itemName;

	public long getAccessNumberSum() {
		return m_accessNumberSum;
	}

	public String getDateTime() {
		long time = m_period.getTime() + m_minuteOrder * TimeHelper.ONE_MINUTE;

		return new SimpleDateFormat("HH:mm").format(new Date(time));
	}

	public String getItemName() {
		return m_itemName;
	}

	public void setItemName(String itemName) {
		m_itemName = itemName;
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

	public WebSpeedDetail setAccessNumberSum(long accessNumberSum) {
		m_accessNumberSum = accessNumberSum;
		return this;
	}

	public void setMinuteOrder(int minuteOrder) {
		m_minuteOrder = minuteOrder;
	}

	public void setPeriod(Date period) {
		m_period = period;
	}

	public WebSpeedDetail setResponseTimeAvg(double responseTimeSum) {
		m_responseTimeAvg = responseTimeSum;
		return this;
	}

}
