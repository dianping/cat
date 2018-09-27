package com.dianping.cat.config.business;

public class ConfigItem {
	private int m_count;

	private double m_value;

	private boolean m_showCount = false;

	private boolean m_showAvg = false;

	private boolean m_showSum = false;

	private String m_title;
	
	private double viewOrder = 1;
	
	public int getCount() {
		return m_count;
	}

	public String getTitle() {
		return m_title;
	}

	public double getValue() {
		return m_value;
	}

	public boolean isShowAvg() {
		return m_showAvg;
	}

	public boolean isShowCount() {
		return m_showCount;
	}

	public boolean isShowSum() {
		return m_showSum;
	}

	public double getViewOrder() {
		return viewOrder;
	}

	public void setViewOrder(double viewOrder) {
		this.viewOrder = viewOrder;
	}

	public ConfigItem setCount(int count) {
		m_count = count;
		return this;
	}

	public ConfigItem setShowAvg(boolean showAvg) {
		m_showAvg = showAvg;
		return this;
	}

	public ConfigItem setShowCount(boolean showCount) {
		m_showCount = showCount;
		return this;
	}

	public ConfigItem setShowSum(boolean showSum) {
		m_showSum = showSum;
		return this;
	}

	public ConfigItem setTitle(String title) {
		m_title = title;
		return this;
	}

	public ConfigItem setValue(double value) {
		m_value = value;
		return this;
	}

}
