package com.dianping.cat.report.page.transaction;

import java.util.HashMap;
import java.util.Map;

import com.dianping.cat.consumer.transaction.model.entity.Duration;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.report.graph.AbstractGraphPayload;

public class GraphPayload {
	abstract static class AbstractPayload extends AbstractGraphPayload {
		private final TransactionName m_name;

		public AbstractPayload(String title, String axisXLabel, String axisYLabel, TransactionName name) {
			super(title, axisXLabel, axisYLabel);

			m_name = name;
		}

		@Override
		public String getAxisXLabel(int index) {
			return String.valueOf(index * 5);
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
		public String getIdPrefix() {
			return m_name.getId() + "_" + super.getIdPrefix();
		}

		protected TransactionName getTransactionName() {
			return m_name;
		}

		@Override
		public int getWidth() {
			return super.getWidth() + 120;
		}

		@Override
		public boolean isStandalone() {
			return false;
		}
	}

	final static class AverageTimePayload extends AbstractPayload {
		public AverageTimePayload(String title, String axisXLabel, String axisYLabel, TransactionName name) {
			super(title, axisXLabel, axisYLabel, name);
		}

		@Override
		public int getOffsetY() {
			return getDisplayHeight() + 20;
		}

		@Override
		protected double[] loadValues() {
			double[] values = new double[12];

			for (Range range : getTransactionName().getRanges().values()) {
				int value = range.getValue();
				int k = value / 5;

				values[k] += range.getAvg();
			}

			return values;
		}
	}

	final static class DurationPayload extends AbstractPayload {

		private Map<Integer, Integer> m_map = new HashMap<Integer, Integer>();

		public DurationPayload(String title, String axisXLabel, String axisYLabel, TransactionName name) {
			super(title, axisXLabel, axisYLabel, name);
			int k = 1;

			m_map.put(0, 0);

			for (int i = 0; i < 17; i++) {
				m_map.put(k, i);
				k <<= 1;
			}
		}

		@Override
		public String getAxisXLabel(int index) {
			if (index == 0) {
				return "0";
			}

			int k = 1;

			for (int i = 1; i < index; i++) {
				k <<= 1;
			}

			return String.valueOf(k);
		}

		@Override
		public boolean isAxisXLabelRotated() {
			return true;
		}

		@Override
		public boolean isAxisXLabelSkipped() {
			return false;
		}

		@Override
		protected double[] loadValues() {
			double[] values = new double[17];

			for (Duration duration : getTransactionName().getDurations().values()) {
				int d = duration.getValue();
				Integer k = m_map.get(d);

				if (k != null) {
					values[k] += duration.getCount();
				}
			}

			return values;
		}
	}

	final static class FailurePayload extends AbstractPayload {
		public FailurePayload(String title, String axisXLabel, String axisYLabel, TransactionName name) {
			super(title, axisXLabel, axisYLabel, name);
		}

		@Override
		public int getOffsetX() {
			return getDisplayWidth();
		}

		@Override
		public int getOffsetY() {
			return getDisplayHeight() + 20;
		}

		@Override
		protected double[] loadValues() {
			double[] values = new double[12];

			for (Range range : getTransactionName().getRanges().values()) {
				int value = range.getValue();
				int k = value / 5;

				values[k] += range.getFails();
			}

			return values;
		}
	}

	final static class HitPayload extends AbstractPayload {
		public HitPayload(String title, String axisXLabel, String axisYLabel, TransactionName name) {
			super(title, axisXLabel, axisYLabel, name);
		}

		@Override
		public int getOffsetX() {
			return getDisplayWidth();
		}

		@Override
		protected double[] loadValues() {
			double[] values = new double[12];

			for (Range range : getTransactionName().getRanges().values()) {
				int value = range.getValue();
				int k = value / 5;

				values[k] += range.getCount();
			}

			return values;
		}
	}
}
