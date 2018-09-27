package com.dianping.cat.build.report;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.report.page.browser.task.WebDatabasePruner;
import com.dianping.cat.report.page.overload.task.CapacityUpdateStatusManager;
import com.dianping.cat.report.page.overload.task.CapacityUpdateTask;
import com.dianping.cat.report.page.overload.task.DailyCapacityUpdater;
import com.dianping.cat.report.page.overload.task.HourlyCapacityUpdater;
import com.dianping.cat.report.page.overload.task.MonthlyCapacityUpdater;
import com.dianping.cat.report.page.overload.task.TableCapacityService;
import com.dianping.cat.report.page.overload.task.WeeklyCapacityUpdater;
import com.dianping.cat.report.page.server.task.MetricGraphPruner;
import com.dianping.cat.report.task.cmdb.CmdbInfoReloadBuilder;
import com.dianping.cat.report.task.current.CurrentReportBuilder;

public class OfflineComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(CapacityUpdateStatusManager.class));
		all.add(A(HourlyCapacityUpdater.class));
		all.add(A(DailyCapacityUpdater.class));
		all.add(A(WeeklyCapacityUpdater.class));
		all.add(A(MonthlyCapacityUpdater.class));
		all.add(A(TableCapacityService.class));
		all.add(A(CapacityUpdateTask.class));

		all.add(A(CurrentReportBuilder.class));
		all.add(A(CmdbInfoReloadBuilder.class));

		all.add(A(WebDatabasePruner.class));
		all.add(A(MetricGraphPruner.class));

		return all;
	}
}
