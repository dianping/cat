package com.dianping.cat.report.page.transaction;

public class MobileGraphItem {
	private static final int X_SIZE = 13;

	private static final int Y_SIZE = 6;

	private String[] m_xlabel = new String[X_SIZE];

	private double[] m_ylable = new double[Y_SIZE];

	private double[] m_value = new double[X_SIZE];

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
		if (value.length == X_SIZE) {
			m_value = value;
		} else if (value.length == X_SIZE - 1) {
			m_value[0] = value[0];
			m_value[X_SIZE - 1] = value[X_SIZE - 2];
			for (int i = 1; i < X_SIZE - 1; i++) {
				m_value[i] = (value[i] + value[i - 1]) / 2;
			}
		}
		return this;
	}

	public String getTitle() {
		return m_title;
	}

	public void setTitle(String title) {
		m_title = title;
	}

}
