package com.dianping.cat;

import java.io.File;

import org.unidal.helper.Threads;
import org.unidal.initialization.AbstractModule;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;

import com.dianping.cat.consumer.CatConsumerAdvancedModule;
import com.dianping.cat.consumer.CatConsumerModule;
import com.dianping.cat.hadoop.hdfs.UploaderAndCleaner;
import com.dianping.cat.message.spi.core.MessageConsumer;
import com.dianping.cat.message.spi.core.TcpSocketReceiver;
import com.dianping.cat.report.service.CachedReportTask;
import com.dianping.cat.report.task.DefaultTaskConsumer;
import com.dianping.cat.report.task.alert.app.AppAlert;
import com.dianping.cat.report.task.alert.business.BusinessAlert;
import com.dianping.cat.report.task.alert.exception.ExceptionAlert;
import com.dianping.cat.report.task.alert.heartbeat.HeartbeatAlert;
import com.dianping.cat.report.task.alert.network.NetworkAlert;
import com.dianping.cat.report.task.alert.system.SystemAlert;
import com.dianping.cat.report.task.alert.thirdParty.ThirdPartyAlert;
import com.dianping.cat.report.task.alert.thirdParty.ThirdPartyAlertBuilder;
import com.dianping.cat.report.task.alert.web.WebAlert;
import com.dianping.cat.report.task.product.ProjectUpdateTask;
import com.dianping.cat.report.view.DomainNavManager;
import com.dianping.cat.system.config.ConfigReloadTask;

public class CatHomeModule extends AbstractModule {
	public static final String ID = "cat-home";

	@Override
	protected void execute(ModuleContext ctx) throws Exception {
		ServerConfigManager serverConfigManager = ctx.lookup(ServerConfigManager.class);

		ctx.lookup(MessageConsumer.class);
		if (!serverConfigManager.isLocalMode() && !serverConfigManager.isLocalMode()) {
			ConfigReloadTask configReloadTask = ctx.lookup(ConfigReloadTask.class);
			UploaderAndCleaner uploader = ctx.lookup(UploaderAndCleaner.class);

			Threads.forGroup("cat").start(configReloadTask);
			Threads.forGroup("cat").start(uploader);
		}

		if (serverConfigManager.isJobMachine()) {
			DefaultTaskConsumer taskConsumer = ctx.lookup(DefaultTaskConsumer.class);
			DomainNavManager domainNavManager = ctx.lookup(DomainNavManager.class);
			CachedReportTask cachedReportTask = ctx.lookup(CachedReportTask.class);

			Threads.forGroup("cat").start(cachedReportTask);
			Threads.forGroup("cat").start(domainNavManager);
			Threads.forGroup("cat").start(taskConsumer);
		}

		if (serverConfigManager.isAlertMachine() && !serverConfigManager.isLocalMode()) {
			BusinessAlert metricAlert = ctx.lookup(BusinessAlert.class);
			NetworkAlert networkAlert = ctx.lookup(NetworkAlert.class);
			SystemAlert systemAlert = ctx.lookup(SystemAlert.class);
			ExceptionAlert exceptionAlert = ctx.lookup(ExceptionAlert.class);
			HeartbeatAlert heartbeatAlert = ctx.lookup(HeartbeatAlert.class);
			ProjectUpdateTask productUpdateTask = ctx.lookup(ProjectUpdateTask.class);
			ThirdPartyAlert thirdPartyAlert = ctx.lookup(ThirdPartyAlert.class);
			ThirdPartyAlertBuilder alertBuildingTask = ctx.lookup(ThirdPartyAlertBuilder.class);
			AppAlert appAlert = ctx.lookup(AppAlert.class);
			WebAlert webAlert = ctx.lookup(WebAlert.class);

			Threads.forGroup("cat").start(networkAlert);
			Threads.forGroup("cat").start(systemAlert);
			Threads.forGroup("cat").start(metricAlert);
			Threads.forGroup("cat").start(exceptionAlert);
			Threads.forGroup("cat").start(heartbeatAlert);
			Threads.forGroup("cat").start(productUpdateTask);
			Threads.forGroup("cat").start(thirdPartyAlert);
			Threads.forGroup("cat").start(alertBuildingTask);
			Threads.forGroup("cat").start(appAlert);
			Threads.forGroup("cat").start(webAlert);
		}
	}

	@Override
	public Module[] getDependencies(ModuleContext ctx) {
		return ctx.getModules(CatConsumerModule.ID, CatConsumerAdvancedModule.ID);
	}

	@Override
	protected void setup(ModuleContext ctx) throws Exception {
		File serverConfigFile = ctx.getAttribute("cat-server-config-file");
		ServerConfigManager serverConfigManager = ctx.lookup(ServerConfigManager.class);
		TcpSocketReceiver messageReceiver = ctx.lookup(TcpSocketReceiver.class);

		serverConfigManager.initialize(serverConfigFile);
		messageReceiver.init();
	}

}
