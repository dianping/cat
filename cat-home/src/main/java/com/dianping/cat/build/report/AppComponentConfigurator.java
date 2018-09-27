package com.dianping.cat.build.report;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.alarm.spi.config.AlertConfigManager;
import com.dianping.cat.alarm.spi.decorator.Decorator;
import com.dianping.cat.alarm.spi.receiver.Contactor;
import com.dianping.cat.config.app.AppCommandConfigManager;
import com.dianping.cat.config.app.MobileConfigManager;
import com.dianping.cat.config.app.SdkConfigManager;
import com.dianping.cat.report.alert.app.AppAlert;
import com.dianping.cat.report.alert.app.AppContactor;
import com.dianping.cat.report.alert.app.AppDecorator;
import com.dianping.cat.report.page.app.service.AppConnectionService;
import com.dianping.cat.report.page.app.service.AppDataService;
import com.dianping.cat.report.page.app.service.AppSpeedDataBuilder;
import com.dianping.cat.report.page.app.service.AppSpeedService;
import com.dianping.cat.report.page.app.task.AppDatabasePruner;
import com.dianping.cat.report.page.app.task.AppReportBuilder;
import com.dianping.cat.report.page.applog.service.AppLogService;
import com.dianping.cat.report.page.appstats.service.AppStatisticBuilder;
import com.dianping.cat.report.page.appstats.service.AppStatisticReportService;
import com.dianping.cat.report.page.crash.service.CrashLogService;
import com.dianping.cat.report.page.crash.service.CrashStatisticReportService;
import com.dianping.cat.report.page.crash.task.CrashReportBuilder;
import com.dianping.cat.service.ProjectService;

public class AppComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(AppSpeedDataBuilder.class));
		all.add(A(AppSpeedService.class));

		all.add(A(AppDataService.class));

		all.add(A(AppConnectionService.class));

		all.add(A(AppStatisticReportService.class));

		all.add(A(AppDatabasePruner.class));

		all.add(A(AppReportBuilder.class));

		all.add(A(AppStatisticBuilder.class));

		all.add(A(CrashReportBuilder.class));

		all.add(A(CrashLogService.class));
		
		all.add(A(CrashStatisticReportService.class));
		
		all.add(A(AppLogService.class));

		all.add(C(Contactor.class, AppContactor.ID, AppContactor.class).req(AlertConfigManager.class,
		      AppCommandConfigManager.class, ProjectService.class));
		all.add(C(Decorator.class, AppDecorator.ID, AppDecorator.class).req(AppCommandConfigManager.class,
		      MobileConfigManager.class));

		all.add(A(AppAlert.class));

		all.add(A(SdkConfigManager.class));

		return all;
	}
}
