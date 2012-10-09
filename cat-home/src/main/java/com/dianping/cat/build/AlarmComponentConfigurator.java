package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import com.dainping.cat.consumer.dal.report.ReportDao;
import com.dianping.cat.home.dal.alarm.AlarmRuleDao;
import com.dianping.cat.home.dal.alarm.AlarmTemplateDao;
import com.dianping.cat.home.dal.alarm.ScheduledReportDao;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.system.alarm.DefaultAlarmCreator;
import com.site.lookup.configuration.AbstractResourceConfigurator;
import com.site.lookup.configuration.Component;

public class AlarmComponentConfigurator extends AbstractResourceConfigurator {

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(DefaultAlarmCreator.class)//
		      .req(AlarmRuleDao.class, AlarmTemplateDao.class, ReportDao.class, ScheduledReportDao.class)//
		      .req(ModelService.class, "event"));
		return all;
	}
}
