package com.dianping.cat.report.page.sql;

import java.util.List;

import org.apache.commons.lang.math.NumberUtils;

import com.dianping.cat.report.graph.AbstractGraphPayload;
import com.site.helper.Splitters;

public class SqlGraphPayload extends AbstractGraphPayload {
	private int m_index;

	private String[] m_labels;

	private int m_size;

	private double[] m_values;

	public SqlGraphPayload(int index, String title, String axisXLabel, String axisYLabel, String metaData) {
		super(title, axisXLabel, axisYLabel);

		m_index = index;

		List<String> data = Splitters.by(",").noEmptyItem().split(metaData);
		if (data != null) {
			m_size = data.size();
			m_labels = new String[m_size];
			m_values = new double[m_size - 1];

			for (int i = 0; i < m_size; i++) {
				String temp = data.get(i);
				String[] s = temp.split(":");
				if (i == m_size - 1) {
					m_labels[i] = s[0];
				} else {
					m_labels[i] = s[0];
					m_values[i] = NumberUtils.toDouble(s[1], 0d);
				}
			}

		} else {
			m_labels = new String[1];
			m_values = new double[1];
		}
	}

	@Override
	public String getAxisXLabel(int index) {
		if (index < m_labels.length) {
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
		return (int) (super.getDisplayWidth() * 0.7);
	}

	@Override
	public int getOffsetX() {
		if (m_index > 0 && m_index % 2 == 1) {
			return getDisplayWidth();
		} else {
			return 0;
		}
	}

	@Override
	public int getOffsetY() {
		if (m_index / 2 == 1) {
			return getDisplayHeight() + 20;
		} else {
			return 0;
		}
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

	public double sum() {
		double result = 0;
		for (int i = 0; i < m_size - 1; i++) {
			result += m_values[i];
		}
		return result;
	}
}
