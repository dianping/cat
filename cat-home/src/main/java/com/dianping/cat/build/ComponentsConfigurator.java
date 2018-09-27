package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.configuration.AbstractJdbcResourceConfigurator;
import org.unidal.initialization.DefaultModuleManager;
import org.unidal.initialization.ModuleManager;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.CatHomeModule;
import com.dianping.cat.build.report.AppComponentConfigurator;
import com.dianping.cat.build.report.DependencyComponentConfigurator;
import com.dianping.cat.build.report.EventComponentConfigurator;
import com.dianping.cat.build.report.HeartbeatComponentConfigurator;
import com.dianping.cat.build.report.MetricComponentConfigurator;
import com.dianping.cat.build.report.OfflineComponentConfigurator;
import com.dianping.cat.build.report.ProblemComponentConfigurator;
import com.dianping.cat.build.report.ReportComponentConfigurator;
import com.dianping.cat.build.report.StorageComponentConfigurator;
import com.dianping.cat.build.report.TransactionComponentConfigurator;
import com.dianping.cat.config.app.AppCommandTableProvider;
import com.dianping.cat.config.app.AppConnectionTableProvider;
import com.dianping.cat.config.app.AppSpeedTableProvider;
import com.dianping.cat.config.app.CrashLogContentTableProvider;
import com.dianping.cat.config.app.CrashLogTableProvider;
import com.dianping.cat.config.web.AjaxDataTableProvider;
import com.dianping.cat.config.web.WebSpeedDataTableProvider;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.HourlyReportContentTableProvider;
import com.dianping.cat.report.HourlyReportTableProvider;
import com.dianping.cat.report.graph.svg.DefaultGraphBuilder;
import com.dianping.cat.report.graph.svg.DefaultValueTranslater;
import com.dianping.cat.report.page.DomainGroupConfigManager;
import com.dianping.cat.report.page.network.config.NetGraphConfigManager;
import com.dianping.cat.report.page.server.config.ServerMetricConfigManager;
import com.dianping.cat.report.server.RemoteServersManager;
import com.dianping.cat.report.task.DefaultRemoteServersUpdater;
import com.dianping.cat.report.task.DefaultTaskConsumer;
import com.dianping.cat.report.task.ReportFacade;
import com.dianping.cat.report.task.cmdb.ProjectUpdateTask;
import com.dianping.cat.report.task.reload.ReportReloadTask;
import com.dianping.cat.report.task.reload.impl.BusinessReportReloader;
import com.dianping.cat.report.task.reload.impl.CrossReportReloader;
import com.dianping.cat.report.task.reload.impl.DependencyReportReloader;
import com.dianping.cat.report.task.reload.impl.EventReportReloader;
import com.dianping.cat.report.task.reload.impl.HeartbeatReportReloader;
import com.dianping.cat.report.task.reload.impl.MatrixReportReloader;
import com.dianping.cat.report.task.reload.impl.ProblemReportReloader;
import com.dianping.cat.report.task.reload.impl.StateReportReloader;
import com.dianping.cat.report.task.reload.impl.StorageReportReloader;
import com.dianping.cat.report.task.reload.impl.TopReportReloader;
import com.dianping.cat.report.task.reload.impl.TransactionReportReloader;
import com.dianping.cat.system.page.permission.ResourceConfigManager;
import com.dianping.cat.system.page.permission.UserConfigManager;

public class ComponentsConfigurator extends AbstractJdbcResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	private List<Component> defineCommonComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(JsonBuilder.class));

		all.add(A(RemoteServersManager.class));

		all.add(A(DefaultRemoteServersUpdater.class));

		all.add(A(DefaultValueTranslater.class));

		all.add(A(DefaultGraphBuilder.class));

		all.add(A(PayloadNormalizer.class));

		all.add(A(ProjectUpdateTask.class));

		all.add(A(ReportFacade.class));

		all.add(A(DefaultTaskConsumer.class));

		return all;
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.addAll(defineCommonComponents());

		all.addAll(defineConfigComponents());

		// must define in home module instead of core
		all.addAll(defineTableProviderComponents());

		all.add(A(CatHomeModule.class));

		all.add(A(UserConfigManager.class));

		all.add(A(ResourceConfigManager.class));

		all.add(C(ModuleManager.class, DefaultModuleManager.class) //
		      .config(E("topLevelModules").value(CatHomeModule.ID)));

		all.addAll(new AppComponentConfigurator().defineComponents());

		all.addAll(new TransactionComponentConfigurator().defineComponents());

		all.addAll(new EventComponentConfigurator().defineComponents());

		all.addAll(new MetricComponentConfigurator().defineComponents());

		all.addAll(new HeartbeatComponentConfigurator().defineComponents());

		all.addAll(new ProblemComponentConfigurator().defineComponents());

		all.addAll(new StorageComponentConfigurator().defineComponents());

		all.addAll(new DependencyComponentConfigurator().defineComponents());

		all.addAll(new ReportComponentConfigurator().defineComponents());

		all.addAll(new OfflineComponentConfigurator().defineComponents());

		all.add(defineJdbcDataSourceConfigurationManagerComponent("/data/appdatas/cat/datasources.xml"));

		all.addAll(new CatDatabaseConfigurator().defineComponents());

		// for alarm module
		all.addAll(new HomeAlarmComponentConfigurator().defineComponents());

		// web, please keep it last
		all.addAll(new WebComponentConfigurator().defineComponents());

		return all;
	}

	private List<Component> defineConfigComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(DomainGroupConfigManager.class));
		all.add(A(NetGraphConfigManager.class));
		all.add(A(ServerMetricConfigManager.class));

		all.add(A(ReportReloadTask.class));
		all.add(A(BusinessReportReloader.class));
		all.add(A(CrossReportReloader.class));
		all.add(A(DependencyReportReloader.class));
		all.add(A(EventReportReloader.class));
		all.add(A(HeartbeatReportReloader.class));
		all.add(A(MatrixReportReloader.class));
		all.add(A(ProblemReportReloader.class));
		all.add(A(StateReportReloader.class));
		all.add(A(StorageReportReloader.class));
		all.add(A(TopReportReloader.class));
		all.add(A(TransactionReportReloader.class));

		return all;
	}

	private List<Component> defineTableProviderComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(AppCommandTableProvider.class));
		all.add(A(AppConnectionTableProvider.class));
		all.add(A(AppSpeedTableProvider.class));
		all.add(A(CrashLogTableProvider.class));
		all.add(A(CrashLogContentTableProvider.class));
		all.add(A(AjaxDataTableProvider.class));
		all.add(A(WebSpeedDataTableProvider.class));
		all.add(A(HourlyReportTableProvider.class));
		all.add(A(HourlyReportContentTableProvider.class));

		return all;
	}
}
