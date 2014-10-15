package com.dianping.cat.report.task.alert;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.unidal.tuple.Pair;

public enum RuleType {

	DecreasePercentage {
		private double[] buildDescPers(double[] values, double[] baselines) {
			int length = values.length;
			double[] descPers = new double[length];

			for (int i = 0; i < length; i++) {
				descPers[i] = (1 - values[i] / baselines[i]) * 100;
			}

			return descPers;
		}

		@Override
		protected String buildRuleMessage(double[] values, double[] baselines, double ruleValue) {
			StringBuilder sb = new StringBuilder();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			sb.append("[基线值:").append(convertDoublesToString(baselines)).append("] ");
			sb.append("[实际值:").append(convertDoublesToString(values)).append("] ");
			sb.append("[下降比:").append(convertPercentsToString(buildDescPers(values, baselines))).append("]");
			sb.append("[下降百分比阈值: " + m_df.format(ruleValue) + "% ]");
			sb.append("[告警时间:").append(sdf.format(new Date()) + "]");

			return sb.toString();
		}

		@Override
		public Pair<Boolean, String> executeRule(double[] values, double[] baselines, double ruleValue) {
			int length = values.length;

			for (int i = 0; i < length; i++) {
				if (baselines[i] <= 0 || values[i] / baselines[i] > (1 - ruleValue / 100)) {
					return new Pair<Boolean, String>(false, "");
				}
			}

			return new Pair<Boolean, String>(true, buildRuleMessage(values, baselines, ruleValue));
		}

		@Override
		public String getId() {
			return "DescPer";
		}

	},

	DecreaseValue {
		private double[] buildDescVals(double[] values, double[] baselines) {
			int length = values.length;
			double[] descVals = new double[length];

			for (int i = 0; i < length; i++) {
				descVals[i] = baselines[i] - values[i];
			}

			return descVals;
		}

		@Override
		protected String buildRuleMessage(double[] values, double[] baselines, double ruleValue) {
			StringBuilder sb = new StringBuilder();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			sb.append("[基线值:").append(convertDoublesToString(baselines)).append("] ");
			sb.append("[实际值:").append(convertDoublesToString(values)).append("] ");
			sb.append("[下降值:").append(convertDoublesToString(buildDescVals(values, baselines))).append("]");
			sb.append("[下降阈值: " + convertDoubleToString(ruleValue) + " ]");
			sb.append("[告警时间:").append(sdf.format(new Date()) + "]");

			return sb.toString();
		}

		@Override
		public Pair<Boolean, String> executeRule(double[] values, double[] baselines, double ruleValue) {
			int length = values.length;

			for (int i = 0; i < length; i++) {
				if (baselines[i] - values[i] < ruleValue) {
					return new Pair<Boolean, String>(false, "");
				}
			}

			return new Pair<Boolean, String>(true, buildRuleMessage(values, baselines, ruleValue));
		}

		@Override
		public String getId() {
			return "DescVal";
		}
	},

	IncreasePercentage {
		private double[] buildAscPers(double[] values, double[] baselines) {
			int length = values.length;
			double[] ascPers = new double[length];

			for (int i = 0; i < length; i++) {
				ascPers[i] = (values[i] / baselines[i] - 1) * 100;
			}

			return ascPers;
		}

		@Override
		protected String buildRuleMessage(double[] values, double[] baselines, double ruleValue) {
			StringBuilder sb = new StringBuilder();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			sb.append("[基线值:").append(convertDoublesToString(baselines)).append("] ");
			sb.append("[实际值:").append(convertDoublesToString(values)).append("] ");
			sb.append("[上升比:").append(convertPercentsToString(buildAscPers(values, baselines))).append("]");
			sb.append("[上升百分比阈值: " + m_df.format(ruleValue) + "% ]");
			sb.append("[告警时间:").append(sdf.format(new Date()) + "]");

			return sb.toString();
		}

		@Override
		public Pair<Boolean, String> executeRule(double[] values, double[] baselines, double ruleValue) {
			int length = values.length;

			for (int i = 0; i < length; i++) {
				if (baselines[i] <= 0 || values[i] / baselines[i] < (1 + ruleValue / 100)) {
					return new Pair<Boolean, String>(false, "");
				}
			}

			return new Pair<Boolean, String>(true, buildRuleMessage(values, baselines, ruleValue));
		}

		@Override
		public String getId() {
			return "AscPer";
		}
	},

	IncreaseValue {
		private double[] buildAscVals(double[] values, double[] baselines) {
			int length = values.length;
			double[] ascVals = new double[length];

			for (int i = 0; i < length; i++) {
				ascVals[i] = values[i] - baselines[i];
			}

			return ascVals;
		}

		@Override
		protected String buildRuleMessage(double[] values, double[] baselines, double ruleValue) {
			StringBuilder sb = new StringBuilder();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			sb.append("[基线值:").append(convertDoublesToString(baselines)).append("] ");
			sb.append("[实际值:").append(convertDoublesToString(values)).append("] ");
			sb.append("[上升值:").append(convertDoublesToString(buildAscVals(values, baselines))).append("]");
			sb.append("[上升阈值: " + convertDoubleToString(ruleValue) + " ]");
			sb.append("[告警时间:").append(sdf.format(new Date()) + "]");

			return sb.toString();
		}

		@Override
		public Pair<Boolean, String> executeRule(double[] values, double[] baselines, double ruleValue) {
			int length = values.length;

			for (int i = 0; i < length; i++) {
				if (values[i] - baselines[i] < ruleValue) {
					return new Pair<Boolean, String>(false, "");
				}
			}

			return new Pair<Boolean, String>(true, buildRuleMessage(values, baselines, ruleValue));
		}

		@Override
		public String getId() {
			return "AscVal";
		}
	},

	AbsoluteMaxValue {
		@Override
		protected String buildRuleMessage(double[] values, double[] baselines, double ruleValue) {
			StringBuilder sb = new StringBuilder();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			sb.append("[实际值:").append(convertDoublesToString(values)).append("] ");
			sb.append("[最大阈值: " + convertDoubleToString(ruleValue) + " ]");
			sb.append("[告警时间:").append(sdf.format(new Date()) + "]");

			return sb.toString();
		}

		@Override
		public Pair<Boolean, String> executeRule(double[] values, double[] baselines, double ruleValue) {
			int length = values.length;

			for (int i = 0; i < length; i++) {
				if (values[i] < ruleValue) {
					return new Pair<Boolean, String>(false, "");
				}
			}

			return new Pair<Boolean, String>(true, buildRuleMessage(values, baselines, ruleValue));
		}

		@Override
		public String getId() {
			return "MaxVal";
		}
	},

	AbsoluteMinValue {
		@Override
		protected String buildRuleMessage(double[] values, double[] baselines, double ruleValue) {
			StringBuilder sb = new StringBuilder();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			sb.append("[实际值:").append(convertDoublesToString(values)).append("] ");
			sb.append("[最小阈值: " + convertDoubleToString(ruleValue) + " ]");
			sb.append("[告警时间:").append(sdf.format(new Date()) + "]");

			return sb.toString();
		}

		@Override
		public Pair<Boolean, String> executeRule(double[] values, double[] baselines, double ruleValue) {
			int length = values.length;

			for (int i = 0; i < length; i++) {
				if (values[i] > ruleValue) {
					return new Pair<Boolean, String>(false, "");
				}
			}

			return new Pair<Boolean, String>(true, buildRuleMessage(values, baselines, ruleValue));
		}

		@Override
		public String getId() {
			return "MinVal";
		}
	},

	FluctuateIncreasePercentage {
		private double[] buildFlucAscPers(double[] values) {
			int length = values.length;
			double[] ascPers = new double[length - 1];
			double baseVal = values[length - 1];

			for (int i = 0; i <= length - 2; i++) {
				ascPers[i] = (baseVal / values[i] - 1) * 100;
			}

			return ascPers;
		}

		@Override
		protected String buildRuleMessage(double[] values, double[] baselines, double ruleValue) {
			StringBuilder sb = new StringBuilder();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			sb.append("[实际值:").append(convertDoublesToString(values)).append("] ");
			sb.append("[波动上升百分比:").append(convertPercentsToString(buildFlucAscPers(values))).append("] ");
			sb.append("[波动上升百分比阈值: " + m_df.format(ruleValue) + "% ]");
			sb.append("[告警时间:").append(sdf.format(new Date()) + "]");

			return sb.toString();
		}

		@Override
		public Pair<Boolean, String> executeRule(double[] values, double[] baselines, double ruleValue) {
			int length = values.length;

			if (length <= 1) {
				return new Pair<Boolean, String>(false, "");
			}

			double baseVal = values[length - 1];

			for (int i = 0; i <= length - 2; i++) {
				if (baseVal / values[i] - 1 < ruleValue / 100) {
					return new Pair<Boolean, String>(false, "");
				}
			}

			return new Pair<Boolean, String>(true, buildRuleMessage(values, baselines, ruleValue));
		}

		@Override
		public String getId() {
			return "FluAscPer";
		}
	},

	FluctuateDecreasePercentage {
		private double[] buildFlucDescPers(double[] values) {
			int length = values.length;
			double[] descPers = new double[length - 1];
			double baseVal = values[length - 1];

			for (int i = 0; i <= length - 2; i++) {
				descPers[i] = (1 - baseVal / values[i]) * 100;
			}

			return descPers;
		}

		@Override
		protected String buildRuleMessage(double[] values, double[] baselines, double ruleValue) {
			StringBuilder sb = new StringBuilder();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			sb.append("[实际值:").append(convertDoublesToString(values)).append("] ");
			sb.append("[波动下降百分比:").append(convertPercentsToString(buildFlucDescPers(values))).append("] ");
			sb.append("[波动下降百分比阈值: " + m_df.format(ruleValue) + "% ]");
			sb.append("[告警时间:").append(sdf.format(new Date()) + "]");

			return sb.toString();
		}

		@Override
		public Pair<Boolean, String> executeRule(double[] values, double[] baselines, double ruleValue) {
			int length = values.length;

			if (length <= 1) {
				return new Pair<Boolean, String>(false, "");
			}

			double baseVal = values[length - 1];

			for (int i = 0; i <= length - 2; i++) {
				if (1 - baseVal / values[i] < ruleValue / 100) {
					return new Pair<Boolean, String>(false, "");
				}
			}

			return new Pair<Boolean, String>(true, buildRuleMessage(values, baselines, ruleValue));
		}

		@Override
		public String getId() {
			return "FluDescPer";
		}
	},

	SumMaxValue {
		@Override
		protected String buildRuleMessage(double[] values, double[] baselines, double ruleValue) {
			StringBuilder sb = new StringBuilder();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			sb.append("[实际值:").append(convertDoublesToString(values)).append("] ");
			sb.append("[实际值总和:").append(convertDoubleToString(calSum(values))).append("] ");
			sb.append("[总和最大阈值: " + convertDoubleToString(ruleValue) + " ]");
			sb.append("[告警时间:").append(sdf.format(new Date()) + "]");

			return sb.toString();
		}

		@Override
		public Pair<Boolean, String> executeRule(double[] values, double[] baselines, double ruleValue) {
			double totalVal = calSum(values);

			if (totalVal < ruleValue) {
				return new Pair<Boolean, String>(false, "");
			}

			return new Pair<Boolean, String>(true, buildRuleMessage(values, baselines, ruleValue));
		}

		@Override
		public String getId() {
			return "SumMaxVal";
		}
	},

	SumMinValue {
		@Override
		protected String buildRuleMessage(double[] values, double[] baselines, double ruleValue) {
			StringBuilder sb = new StringBuilder();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			sb.append("[实际值:").append(convertDoublesToString(values)).append("] ");
			sb.append("[实际值总和:").append(convertDoubleToString(calSum(values))).append("] ");
			sb.append("[总和最小阈值: " + convertDoubleToString(ruleValue) + " ]");
			sb.append("[告警时间:").append(sdf.format(new Date()) + "]");

			return sb.toString();
		}

		@Override
		public Pair<Boolean, String> executeRule(double[] values, double[] baselines, double ruleValue) {
			double totalVal = calSum(values);

			if (totalVal > ruleValue) {
				return new Pair<Boolean, String>(false, "");
			}

			return new Pair<Boolean, String>(true, buildRuleMessage(values, baselines, ruleValue));
		}

		@Override
		public String getId() {
			return "SumMinVal";
		}
	};

	static Map<String, RuleType> s_map = new LinkedHashMap<String, RuleType>();

	static {
		for (RuleType type : RuleType.values()) {
			s_map.put(type.getId(), type);
		}
	}

	protected static final long MbS = 1 * 60 * 1024 * 1024 / 8;

	protected static final long GbS = MbS * 1024;

	protected DecimalFormat m_df = new DecimalFormat("0.0");

	protected abstract String buildRuleMessage(double[] values, double[] baselines, double ruleValue);

	protected String convertDoublesToString(double[] values) {
		StringBuilder builder = new StringBuilder();

		for (double value : values) {
			builder.append(convertDoubleToString(value)).append(" ");
		}

		return builder.toString();
	}

	protected String convertDoubleToString(double value) {
		if (value < MbS) {
			return m_df.format(value);
		} else if (value < GbS) {
			return m_df.format(value / MbS) + "Mb/s";
		} else {
			return m_df.format(value / GbS) + "Gb/s";
		}
	}

	protected String convertPercentsToString(double[] values) {
		StringBuilder builder = new StringBuilder();

		for (double value : values) {
			builder.append(m_df.format(value)).append("% ");
		}

		return builder.toString();
	}

	protected double calSum(double[] values) {
		double totalVal = 0;

		for (double value : values) {
			totalVal += value;
		}
		return totalVal;
	}

	public abstract Pair<Boolean, String> executeRule(double[] values, double[] baselines, double ruleValue);

	public abstract String getId();

	public static RuleType getByTypeId(String typeId) {
		return s_map.get(typeId);
	}

}
