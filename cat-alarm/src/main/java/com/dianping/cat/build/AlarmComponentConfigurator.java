package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.alarm.app.AppAlarmRuleParamBuilder;
import com.dianping.cat.alarm.app.crash.CrashAlert;
import com.dianping.cat.alarm.app.crash.CrashContactor;
import com.dianping.cat.alarm.app.crash.CrashDecorator;
import com.dianping.cat.alarm.app.crash.CrashRuleConfigManager;
import com.dianping.cat.alarm.server.ServerAlarm;
import com.dianping.cat.alarm.server.database.ServerDatabaseAlarm;
import com.dianping.cat.alarm.server.database.ServerDatabaseContactor;
import com.dianping.cat.alarm.server.database.ServerDatabaseDecorator;
import com.dianping.cat.alarm.server.network.ServerNetworkAlarm;
import com.dianping.cat.alarm.server.network.ServerNetworkContactor;
import com.dianping.cat.alarm.server.network.ServerNetworkDecorator;
import com.dianping.cat.alarm.server.system.ServerSystemAlarm;
import com.dianping.cat.alarm.server.system.ServerSystemContactor;
import com.dianping.cat.alarm.server.system.ServerSystemDecorator;
import com.dianping.cat.alarm.service.ServerAlarmRuleService;
import com.dianping.cat.alarm.service.impl.AppAlarmRuleServiceImpl;
import com.dianping.cat.alarm.service.impl.ServerAlarmRuleServiceImpl;
import com.dianping.cat.alarm.spi.config.AlertConfigManager;
import com.dianping.cat.alarm.spi.decorator.Decorator;
import com.dianping.cat.alarm.spi.receiver.Contactor;
import com.dianping.cat.influxdb.InfluxDB;
import com.dianping.cat.server.MetricService;
import com.dianping.cat.service.ProjectService;

public class AlarmComponentConfigurator extends AbstractResourceConfigurator {

	@Override
	public List<Component> defineComponents() {

		List<Component> all = new ArrayList<Component>();

		all.add(A(ServerAlarmRuleServiceImpl.class));
		all.add(A(AppAlarmRuleServiceImpl.class));
		all.add(A(AppAlarmRuleParamBuilder.class));
		all.add(A(CrashAlert.class));
		all.add(A(CrashRuleConfigManager.class));

		all.add(C(ServerAlarm.class, ServerSystemAlarm.ID, ServerSystemAlarm.class).req(ServerAlarmRuleService.class)
		      .req(MetricService.class, InfluxDB.ID));
		all.add(C(ServerAlarm.class, ServerNetworkAlarm.ID, ServerNetworkAlarm.class).req(ServerAlarmRuleService.class)
		      .req(MetricService.class, InfluxDB.ID));
		all.add(C(ServerAlarm.class, ServerDatabaseAlarm.ID, ServerDatabaseAlarm.class).req(ServerAlarmRuleService.class)
		      .req(MetricService.class, InfluxDB.ID));

		all.add(C(Contactor.class, ServerDatabaseContactor.ID, ServerDatabaseContactor.class).req(
		      AlertConfigManager.class));
		all.add(C(Contactor.class, ServerNetworkContactor.ID, ServerNetworkContactor.class).req(AlertConfigManager.class));
		all.add(C(Contactor.class, ServerSystemContactor.ID, ServerSystemContactor.class).req(AlertConfigManager.class,
		      ProjectService.class));
		all.add(C(Contactor.class, CrashContactor.ID, CrashContactor.class).req(AlertConfigManager.class,
		      CrashRuleConfigManager.class));

		all.add(C(Decorator.class, ServerNetworkDecorator.ID, ServerNetworkDecorator.class));
		all.add(C(Decorator.class, ServerDatabaseDecorator.ID, ServerDatabaseDecorator.class));
		all.add(C(Decorator.class, ServerSystemDecorator.ID, ServerSystemDecorator.class));
		all.add(C(Decorator.class, CrashDecorator.ID, CrashDecorator.class));

		return all;
	}
}
