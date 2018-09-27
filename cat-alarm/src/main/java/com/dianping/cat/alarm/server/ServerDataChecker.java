package com.dianping.cat.alarm.server;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.server.entity.Condition;
import com.dianping.cat.alarm.server.entity.SubCondition;
import com.dianping.cat.alarm.spi.rule.DataCheckEntity;
import com.dianping.cat.alarm.spi.rule.RuleType;
import com.dianping.cat.server.MetricService;

@Named
public class ServerDataChecker {

	@Inject
	private MetricService m_metricService;

	public List<DataCheckEntity> checkData(double[] value, List<Condition> conditions) {
		List<DataCheckEntity> alertResults = new ArrayList<DataCheckEntity>();

		for (Condition condition : conditions) {
			Pair<Boolean, String> condResult = checkDataByCondition(value, condition);

			if (condResult.getKey()) {
				String alertType = condition.getAlertType();

				alertResults.add(new DataCheckEntity(condResult.getKey(), condResult.getValue(), alertType));
			}
		}

		return alertResults;
	}

	private Pair<Boolean, String> checkDataByCondition(double[] value, Condition condition) {
		StringBuilder builder = new StringBuilder();

		for (SubCondition subCondition : condition.getSubConditions()) {
			try {
				String ruleType = subCondition.getType();
				RuleType rule = RuleType.getByTypeId(ruleType);
				Pair<Boolean, String> subResult = rule.executeRule(value, value, String.valueOf(subCondition.getValue()));

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

}
