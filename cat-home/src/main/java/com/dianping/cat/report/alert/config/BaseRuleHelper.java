package com.dianping.cat.report.alert.config;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.unidal.lookup.annotation.Named;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.rule.entity.Condition;
import com.dianping.cat.alarm.rule.entity.Config;

@Named
public class BaseRuleHelper {

	public Pair<Integer, List<Condition>> convertConditions(List<Config> configs) {
		int maxMinute = 0;
		List<Condition> conditions = new ArrayList<Condition>();
		Iterator<Config> iterator = configs.iterator();

		while (iterator.hasNext()) {
			Config config = iterator.next();

			if (checkTimeValidate(config)) {
				List<Condition> tmpConditions = config.getConditions();
				conditions.addAll(tmpConditions);

				for (Condition con : tmpConditions) {
					int tmpMinute = con.getMinute();

					if (tmpMinute > maxMinute) {
						maxMinute = tmpMinute;
					}
				}
			}
		}

		if (maxMinute > 0) {
			return new Pair<Integer, List<Condition>>(maxMinute, conditions);
		} else {
			return null;
		}
	}

	private boolean checkTimeValidate(Config config) {
		try {
			if (compareTime(config.getStarttime(), config.getEndtime())) {
				return true;
			} else {
				return false;
			}
		} catch (Exception ex) {
			Cat.logError("throw exception when judge time: " + config.toString(), ex);
			return false;
		}
	}

	private boolean compareTime(String start, String end) {
		String[] startTime = start.split(":");
		int hourStart = Integer.parseInt(startTime[0]);
		int minuteStart = Integer.parseInt(startTime[1]);
		int startMinute = hourStart * 60 + minuteStart;

		String[] endTime = end.split(":");
		int hourEnd = Integer.parseInt(endTime[0]);
		int minuteEnd = Integer.parseInt(endTime[1]);
		int endMinute = hourEnd * 60 + minuteEnd;

		Calendar cal = Calendar.getInstance();
		int current = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);

		return current >= startMinute && current <= endMinute;
	}
}
