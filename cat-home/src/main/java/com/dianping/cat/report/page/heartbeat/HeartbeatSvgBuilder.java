package com.dianping.cat.report.page.heartbeat;

import java.util.Arrays;

import com.dianping.cat.report.graph.svg.AbstractGraphPayload;

public class HeartbeatSvgBuilder extends AbstractGraphPayload {
	private String m_idPrefix;

	private int m_index;

	private String[] m_labels;

	private double[] m_values;

	public HeartbeatSvgBuilder(int index, String title, String axisXLabel, String axisYLabel, double[] values) {
		super(title, axisXLabel, axisYLabel);

		m_idPrefix = title;
		m_index = index;
		m_labels = new String[61];

		for (int i = 0; i <= 60; i++) {
			m_labels[i] = String.valueOf(i);
		}

		if (values == null) {
			m_values = new double[0];
		} else {
			m_values = Arrays.copyOf(values, values.length);
		}
	}

	@Override
	public String getAxisXLabel(int index) {
		if (index % 5 == 0 && index < m_labels.length) {
			return m_labels[index];
		} else {
			return "";
		}
	}

	@Override
	public int getDisplayHeight() {
		return (int) (super.getDisplayHeight() * 0.7);
	}

	@Override
	public int getDisplayWidth() {
		return (int) (super.getDisplayWidth() * 0.66);
	}

	@Override
	public String getIdPrefix() {
		return m_idPrefix;
	}

	@Override
	public int getOffsetX() {
		return m_index % 3 * getDisplayWidth();
	}

	@Override
	public int getOffsetY() {
		return m_index / 3 * (getDisplayHeight() + 20);
	}

	@Override
	public int getWidth() {
		return super.getWidth() + 120;
	}

	@Override
	public boolean isStandalone() {
		return false;
	}

	@Override
	protected double[] loadValues() {
		return m_values;
	}
}