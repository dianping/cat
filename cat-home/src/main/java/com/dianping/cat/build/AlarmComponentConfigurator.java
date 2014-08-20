package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.config.app.AppComparisonConfigManager;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.config.app.AppDataService;
import com.dianping.cat.home.dal.alarm.MailRecordDao;
import com.dianping.cat.report.service.ReportServiceManager;
import com.dianping.cat.report.task.alert.sender.sender.SenderManager;
import com.dianping.cat.system.notify.AppDataComparisonNotifier;
import com.dianping.cat.system.notify.ReportRender;
import com.dianping.cat.system.notify.ReportRenderImpl;
import com.dianping.cat.system.notify.ScheduledMailTask;
import com.dianping.cat.system.notify.render.AppDataComparisonRender;
import com.dianping.cat.system.page.alarm.ScheduledManager;

class AlarmComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(ReportRender.class, ReportRenderImpl.class));

		all.add(C(AppDataComparisonRender.class));

		all.add(C(AppDataComparisonNotifier.class).req(AppDataService.class)
		      .req(AppComparisonConfigManager.class, AppConfigManager.class)
		      .req(SenderManager.class, MailRecordDao.class, AppDataComparisonRender.class));

		all.add(C(ScheduledMailTask.class).//
		      req(ReportRender.class, SenderManager.class)//
		      .req(ReportServiceManager.class, ScheduledManager.class)//
		      .req(MailRecordDao.class, AppDataComparisonNotifier.class));

		return all;
	}
}
