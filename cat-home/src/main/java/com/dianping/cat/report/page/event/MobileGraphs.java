package com.dianping.cat.report.page.event;

import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.Range;
import com.dianping.cat.report.graph.DefaultValueTranslater;
import com.dianping.cat.report.graph.MobileGraphItem;
import com.dianping.cat.report.graph.ValueTranslater;

public class MobileGraphs {

	private MobileGraphItem m_failure = new MobileGraphItem();

	private MobileGraphItem m_hit = new MobileGraphItem();

	private EventName m_name;

	private transient ValueTranslater m_tansalater = new DefaultValueTranslater();

	private transient String[] m_xlabel = { "0", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55", "60" };

	public MobileGraphs() {
	}

	private void creatFailureGraph() {
		double[] averageValues = loadFailureValues();
		m_failure.setTitle("Failures Over Time");
		m_failure.setValue(averageValues);
		double[] ylable = new double[6];
		m_failure.setXlabel(m_xlabel);
		m_failure.setYlable(ylable);
		double maxValue = m_tansalater.getMaxValue(averageValues);
		for (int i = 0; i < 6; i++) {
			if (i == 0) {
				ylable[0] = 0;
			} else {
				ylable[i] = maxValue / 5 * i;
			}
		}
	}

	private void creatHitGraph() {
		double[] averageValues = loadHitValues();
		m_hit.setTitle("Hits Over Time");
		m_hit.setValue(averageValues);
		double[] ylable = new double[6];
		m_hit.setXlabel(m_xlabel);
		m_hit.setYlable(ylable);
		double maxValue = m_tansalater.getMaxValue(averageValues);
		for (int i = 0; i < 6; i++) {
			if (i == 0) {
				ylable[0] = 0;
			} else {
				ylable[i] = maxValue / 5 * i;
			}
		}
	}

	public MobileGraphs display(EventName name) {
		m_name = name;
		creatFailureGraph();
		creatHitGraph();
		return this;
	}

	public MobileGraphItem getFailure() {
		return m_failure;
	}

	public MobileGraphItem getHit() {
		return m_hit;
	}

	protected double[] loadFailureValues() {
		double[] values = new double[12];

		for (Range range : m_name.getRanges().values()) {
			int value = range.getValue();
			int k = value / 5;

			values[k] += range.getFails();
		}

		return values;
	}

	protected double[] loadHitValues() {
		double[] values = new double[12];

		for (Range range : m_name.getRanges().values()) {
			int value = range.getValue();
			int k = value / 5;

			values[k] += range.getCount();
		}

		return values;
	}
}
