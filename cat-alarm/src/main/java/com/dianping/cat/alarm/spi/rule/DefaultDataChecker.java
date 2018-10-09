package com.dianping.cat.alarm.spi.rule;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Named;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.rule.entity.Condition;
import com.dianping.cat.alarm.rule.entity.SubCondition;

@Named(type = DataChecker.class)
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

	public List<DataCheckEntity> checkData(double[] value, double[] baseline, List<Condition> conditions) {
		List<DataCheckEntity> alertResults = new ArrayList<DataCheckEntity>();

		for (Condition condition : conditions) {
			int conditionMinute = condition.getMinute();
			double[] valueValid = buildLastMinutesDoubleArray(value, conditionMinute);
			double[] baselineValid = buildLastMinutesDoubleArray(baseline, conditionMinute);

			Pair<Boolean, String> condResult = checkDataByCondition(valueValid, baselineValid, condition);

			if (condResult.getKey() == true) {
				String alertType = condition.getAlertType();
				alertResults.add(new DataCheckEntity(condResult.getKey(), condResult.getValue(), alertType));
			}
		}

		return alertResults;
	}

	public List<DataCheckEntity> checkData(double[] value, List<Condition> conditions) {
		List<DataCheckEntity> alertResults = new ArrayList<DataCheckEntity>();

		for (Condition condition : conditions) {
			int conditionMinute = condition.getMinute();
			double[] valueValid = buildLastMinutesDoubleArray(value, conditionMinute);
			Pair<Boolean, String> condResult = checkDataByCondition(valueValid, valueValid, condition);

			if (condResult.getKey() == true) {
				String alertType = condition.getAlertType();

				alertResults.add(new DataCheckEntity(condResult.getKey(), condResult.getValue(), alertType));
			}
		}

		return alertResults;
	}

	private Pair<Boolean, String> checkDataByCondition(double[] value, double[] baseline, Condition condition) {
		StringBuilder builder = new StringBuilder();

		for (SubCondition subCondition : condition.getSubConditions()) {
			try {
				String ruleType = subCondition.getType();
				RuleType rule = RuleType.getByTypeId(ruleType);
				Pair<Boolean, String> subResult = rule.executeRule(value, baseline, subCondition.getText());

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

	public List<DataCheckEntity> checkDataForApp(double[] value, List<Condition> conditions) {
		List<DataCheckEntity> alertResults = new ArrayList<DataCheckEntity>();

		for (Condition condition : conditions) {
			Pair<Boolean, String> condResult = checkDataByCondition(value, null, condition);

			if (condResult.getKey() == true) {
				String alertType = condition.getAlertType();
				alertResults.add(new DataCheckEntity(condResult.getKey(), condResult.getValue(), alertType));
			}
		}

		return alertResults;
	}

}
