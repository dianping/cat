package com.dianping.cat.report.graph;

import java.util.Arrays;

public class MobileGraphItem {

	private String m_title;

	private double[] m_value;

	private String[] m_xlabel;

	private double[] m_ylable;

	public MobileGraphItem() {
	}

	public String getTitle() {
		return m_title;
	}

	public double[] getValue() {
		return m_value;
	}

	public String[] getXlabel() {
		return m_xlabel;
	}

	public double[] getYlable() {
		return m_ylable;
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

	public void setTitle(String title) {
		m_title = title;
	}

	public MobileGraphItem setValue(double[] value) {
		if (value == null) {
			m_value = new double[0];
		} else {
			m_value = Arrays.copyOf(value, value.length);
		}
		return this;
	}

	public MobileGraphItem setXlabel(String[] xlabel) {
		if (xlabel == null) {
			m_xlabel = new String[0];
		} else {
			m_xlabel = Arrays.copyOf(xlabel, xlabel.length);
		}
		return this;
	}

	public MobileGraphItem setYlable(double[] ylable) {
		if (ylable == null) {
			m_ylable = new double[0];
		} else {
			m_ylable = Arrays.copyOf(ylable, ylable.length);
		}
		return this;
	}
}
