package com.dianping.cat.report.graph;

public class MobileGraphItem {

	private String[] m_xlabel;

	private double[] m_ylable;

	private double[] m_value;

	private String m_title;

	public MobileGraphItem() {
	}

	public String[] getXlabel() {
		return m_xlabel;
	}

	public double[] getYlable() {
		return m_ylable;
	}

	public double[] getValue() {
		return m_value;
	}

	public MobileGraphItem setXlabel(String[] xlabel) {
		m_xlabel = xlabel;
		return this;
	}

	public MobileGraphItem setYlable(double[] ylable) {
		m_ylable = ylable;
		return this;
	}

	public MobileGraphItem setValue(double[] value) {
		m_value = value;
		return this;
	}

	public String getTitle() {
		return m_title;
	}

	public void setTitle(String title) {
		m_title = title;
	}

	public void setMaxValue(double maxValue) {
		m_ylable = new double[6];
		for (int i = 0; i < 6; i++) {
			if (i == 0) {
				m_ylable[0] = 0;
			} else {
				m_ylable[i] = maxValue / 5 * i;
			}
		}
	}
}
