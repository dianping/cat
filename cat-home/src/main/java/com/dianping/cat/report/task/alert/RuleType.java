package com.dianping.cat.report.task.alert;

import java.util.LinkedHashMap;
import java.util.Map;

public enum RuleType {

	DecreasePercentage {
		@Override
		public boolean executeRule(double value, double baseline, double ruleValue) {
			if (baseline > 0) {
				return value / baseline <= (1 - ruleValue / 100);
			} else {
				return false;
			}
		}

		@Override
		public String getId() {
			return "DescPer";
		}
	},

	DecreaseValue {
		@Override
		public boolean executeRule(double value, double baseline, double ruleValue) {
			return baseline - value >= ruleValue;
		}

		@Override
		public String getId() {
			return "DescVal";
		}
	},

	IncreasePercentage {
		@Override
		public boolean executeRule(double value, double baseline, double ruleValue) {
			if (baseline > 0) {
				return value / baseline >= (1 + ruleValue / 100);
			} else {
				return false;
			}
		}

		@Override
		public String getId() {
			return "AscPer";
		}
	},

	IncreaseValue {
		@Override
		public boolean executeRule(double value, double baseline, double ruleValue) {
			return value - baseline >= ruleValue;
		}

		@Override
		public String getId() {
			return "AscVal";
		}
	},

	absoluteMaxValue {
		@Override
		public boolean executeRule(double value, double baseline, double ruleValue) {
			return value >= ruleValue;
		}

		@Override
		public String getId() {
			return "MaxVal";
		}
	},

	absoluteMinValue {
		@Override
		public boolean executeRule(double value, double baseline, double ruleValue) {
			return value <= ruleValue;
		}

		@Override
		public String getId() {
			return "MinVal";
		}
	};

	static Map<String, RuleType> s_map = new LinkedHashMap<String, RuleType>();

	static {
		for (RuleType type : RuleType.values()) {
			s_map.put(type.getId(), type);
		}
	}

	public static RuleType getByTypeId(String typeId) {
		return s_map.get(typeId);
	}

	public abstract boolean executeRule(double value, double baseline, double ruleValue);

	public abstract String getId();

}
