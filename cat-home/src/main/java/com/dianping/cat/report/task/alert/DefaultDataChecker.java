package com.dianping.cat.report.task.alert;

import java.util.List;

import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.home.rule.entity.Condition;
import com.dianping.cat.home.rule.entity.SubCondition;

public class DefaultDataChecker implements DataChecker {

	private double[] buildLastMinutesDoubleArray(double[] doubleList, int remainCount) {
		if (doubleList.length <= remainCount) {
			return doubleList;
		}

		double[] result = new double[remainCount];
		int startIndex = doubleList.length - remainCount;

		for (int i = 0; i < remainCount; i++) {
			result[i] = doubleList[startIndex + i];
		}

		return result;
	}

	public AlertEntity checkData(double[] value, double[] baseline, List<Condition> conditions) {
		AlertEntity result = new AlertEntity();

		for (Condition condition : conditions) {
			int conditionMinute = condition.getMinute();
			double[] valueValid = buildLastMinutesDoubleArray(value, conditionMinute);
			double[] baselineValid = buildLastMinutesDoubleArray(baseline, conditionMinute);

			Pair<Boolean, String> condResult = checkDataByCondition(valueValid, baselineValid, condition);

			if (condResult.getKey() == true) {
				String alertType = condition.getAlertType();
				if (alertType != null && alertType.equals("error")) {
					return new AlertEntity(condResult.getKey(), condResult.getValue(), alertType);
				} else {
					result = new AlertEntity(condResult.getKey(), condResult.getValue(), alertType);
				}
			}
		}

		return result;
	}

	private Pair<Boolean, String> checkDataByCondition(double[] value, double[] baseline, Condition condition) {
		StringBuilder builder = new StringBuilder();

		for (SubCondition subCondition : condition.getSubConditions()) {
			try {
				String ruleType = subCondition.getType();
				double ruleValue = parseSubConditionText(subCondition.getText());
				RuleType rule = RuleType.getByTypeId(ruleType);

				Pair<Boolean, String> subResult = rule.executeRule(value, baseline, ruleValue);

				if (!subResult.getKey()) {
					return new Pair<Boolean, String>(false, "");
				}
				builder.append(subResult.getValue()).append("<br/>");
			} catch (Exception ex) {
				Cat.logError(condition.toString(), ex);
				return new Pair<Boolean, String>(false, "");
			}
		}

		return new Pair<Boolean, String>(true, builder.toString());
	}

	private double parseSubConditionText(String text) {
		if (text.endsWith("Mb/s")) {
			double value = Double.parseDouble(text.replaceAll("Mb/s", ""));
			return value * 60 * 1024 * 1024 / 8;
		} else if (text.endsWith("Gb/s")) {
			double value = Double.parseDouble(text.replaceAll("Gb/s", ""));
			return value * 60 * 1024 * 1024 * 1024 / 8;
		} else if (text.endsWith("MB/s")) {
			double value = Double.parseDouble(text.replaceAll("MB/s", ""));
			return value * 60 * 1024 * 1024;
		} else if (text.endsWith("GB/s")) {
			double value = Double.parseDouble(text.replaceAll("GB/s", ""));
			return value * 60 * 1024 * 1024 * 1024;
		} else if (text.endsWith("Mb")) {
			double value = Double.parseDouble(text.replaceAll("Mb", ""));
			return value * 1024 * 1024 / 8;
		} else if (text.endsWith("Gb")) {
			double value = Double.parseDouble(text.replaceAll("Gb", ""));
			return value * 1024 * 1024 * 1024 / 8;
		} else if (text.endsWith("MB")) {
			double value = Double.parseDouble(text.replaceAll("MB", ""));
			return value * 1024 * 1024;
		} else if (text.endsWith("GB")) {
			double value = Double.parseDouble(text.replaceAll("GB", ""));
			return value * 1024 * 1024 * 1024;
		}

		return Double.parseDouble(text);
	}

}
