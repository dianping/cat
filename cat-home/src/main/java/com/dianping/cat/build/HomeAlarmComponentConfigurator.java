package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.alarm.spi.config.AlertConfigManager;
import com.dianping.cat.report.alert.AlarmManager;
import com.dianping.cat.report.alert.config.BaseRuleHelper;
import com.dianping.cat.report.alert.spi.config.UserDefinedRuleManager;

public class HomeAlarmComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {

		List<Component> all = new ArrayList<Component>();

		all.add(A(AlarmManager.class));
		all.add(A(AlertConfigManager.class));
		all.add(A(BaseRuleHelper.class));
		all.add(A(UserDefinedRuleManager.class));
		return all;
	}
}
