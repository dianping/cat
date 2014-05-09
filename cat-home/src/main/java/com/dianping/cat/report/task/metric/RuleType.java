package com.dianping.cat.report.task.metric;

import java.util.LinkedHashMap;
import java.util.Map;

public enum RuleType {

	DecreasePercentage{
		@Override
      public boolean executeRule(double value, double baseline, double ruleValue) {
			return value / baseline <= (1 - ruleValue / 100);
      }

		@Override
      public int getId() {
	      return 1;
      }
	},
	
	DecreaseValue{
		@Override
      public boolean executeRule(double value, double baseline, double ruleValue) {
			return baseline - value >= ruleValue;
      }

		@Override
      public int getId() {
	      return 2;
      }
	},
	
	IncreasePercentage{
		@Override
      public boolean executeRule(double value, double baseline, double ruleValue) {
			return value / baseline >= (1 + ruleValue / 100);
      }

		@Override
      public int getId() {
	      return 3;
      }
	},
	
	IncreaseValue{
		@Override
      public boolean executeRule(double value, double baseline, double ruleValue) {
			return value - baseline >= ruleValue;
      }

		@Override
      public int getId() {
	      return 4;
      }
	},
	
	absoluteMaxValue{
		@Override
      public boolean executeRule(double value, double baseline, double ruleValue) {
			return value >= ruleValue;
      }

		@Override
      public int getId() {
	      return 5;
      }
	},
	
	absoluteMinValue{
		@Override
      public boolean executeRule(double value, double baseline, double ruleValue) {
			return value <= ruleValue;
      }

		@Override
      public int getId() {
	      return 6;
      }
	};
	
	static Map<Integer,RuleType> s_map = new LinkedHashMap<Integer,RuleType>();
	
	static {
		for(RuleType type : RuleType.values()){
			s_map.put(type.getId(), type);
		}
	}
	
	public abstract boolean executeRule(double value, double baseline, double ruleValue);
	
	public abstract int getId();
	
	public static RuleType getByTypeId(int typeId){
		return s_map.get(typeId);
	}
	
}
