package com.dianping.cat.report.task.metric;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.unidal.tuple.Pair;

import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.home.monitorrules.entity.Condition;
import com.dianping.cat.home.monitorrules.entity.Config;
import com.dianping.cat.home.monitorrules.entity.Subcondition;

public abstract class BaseAlertConfig {
	protected DecimalFormat m_df = new DecimalFormat("0.0");

	protected static final Long ONE_MINUTE_MILLSEC = 60000L;

	public abstract List<String> buildSMSReceivers(ProductLine productLine);
	
	public abstract List<String> buildMailReceivers(ProductLine productLine);

	public abstract List<String> buildMailReceivers(Project project);
	
	public String buildMailTitle(ProductLine productLine, MetricItemConfig config) {
		StringBuilder sb = new StringBuilder();

		sb.append("[业务告警] [产品线 ").append(productLine.getTitle()).append("]");
		sb.append("[业务指标 ").append(config.getTitle()).append("]");
		return sb.toString();
	}
	
	public Pair<Boolean, String> checkData(double[] value, double[] baseline, MetricType type, List<Config> configs) {
		for (Config con : configs) {
			Pair<Boolean, String> tmpResult = checkDataByConfig(value, baseline, type, con);

			if (tmpResult.getKey() == true) {
				return tmpResult;
			}
		}
		return new Pair<Boolean, String>(false, "");
	}
	
	protected Pair<Boolean, String> checkDataByConfig(double[] value, double[] baseline, MetricType type, Config config) {
		if(judgeCurrentNotInConfigRange(config)){
			return new Pair<Boolean, String>(false, "");
		}

		for (Condition condition : config.getConditions()) {
			int conditionMinute = condition.getMinute();
			double[] valueValid = buildLastMinutesDoubleArray(value, conditionMinute);
			double[] baselineValid = buildLastMinutesDoubleArray(baseline, conditionMinute);

			Pair<Boolean, String> condResult = checkDataByCondition(valueValid, baselineValid, condition);

			if (condResult.getKey() == true) {
				return condResult;
			}
		}

		return new Pair<Boolean, String>(false, "");
	}

	protected Pair<Boolean, String> checkDataByCondition(double[] value, double[] baseline, Condition condition) {
		int length = value.length;
		StringBuilder baselines = new StringBuilder();
		StringBuilder values = new StringBuilder();
		double valueSum = 0;
		double baselineSum = 0;

		for (int i = 0; i < length; i++) {
			baselines.append(m_df.format(baseline[i])).append(" ");
			values.append(m_df.format(value[i])).append(" ");
			valueSum = valueSum + value[i];
			baselineSum = baselineSum + baseline[i];

			if (baseline[i] <= 0) {
				baseline[i] = 100;
			}

			if (!checkDataByMinute(condition, value[i], baseline[i])) {
				return new Pair<Boolean, String>(false, "");
			}
		}

		double percent = (1 - valueSum / baselineSum) * 100;
		StringBuilder sb = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		sb.append("[基线值:").append(baselines.toString()).append("] ");
		sb.append("[实际值:").append(values.toString()).append("] ");
		sb.append("[下降:").append(m_df.format(percent)).append("%").append("]");
		sb.append("[告警时间:").append(sdf.format(new Date()) + "]");
		return new Pair<Boolean, String>(true, sb.toString());
	}

	private boolean checkDataByMinute(Condition condition, double value, double baseline) {
		for (Subcondition subCondition : condition.getSubconditions()) {
			String ruleType = subCondition.getType();
			double ruleValue = Double.parseDouble(subCondition.getText());
			RuleType rule = RuleType.getByTypeId(ruleType);

			if (rule != null) {
				boolean isSubRuleTriggered = rule.executeRule(value, baseline, ruleValue);

				if (!isSubRuleTriggered) {
					return false;
				}
			}
		}
		return true;
	}
	
	protected double[] buildLastMinutesDoubleArray(double[] doubleList, int remainCount) {
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

	protected Long buildMillsByString(String time) throws Exception {
		String[] times = time.split(":");
		int hour = Integer.parseInt(times[0]);
		int minute = Integer.parseInt(times[1]);
		long result = hour * 60 * 60 * 1000 + minute * 60 * 1000;

		return result;
	}
	
	private boolean judgeCurrentNotInConfigRange(Config config) {
		long ruleStartTime;
		long ruleEndTime;
		long nowTime = (System.currentTimeMillis() + 8 * 60 * 60 * 1000) % (24 * 60 * 60 * 1000);

		try {
			ruleStartTime = buildMillsByString(config.getStarttime());
			ruleEndTime = buildMillsByString(config.getEndtime()) + ONE_MINUTE_MILLSEC;
		} catch (Exception ex) {
			ruleStartTime = 0L;
			ruleEndTime = 86400000L;
		}

		if (nowTime < ruleStartTime || nowTime > ruleEndTime) {
			return true;
		}
		
		return false;
   }

}
