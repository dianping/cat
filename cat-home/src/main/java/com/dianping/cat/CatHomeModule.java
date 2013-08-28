package com.dianping.cat;

import java.io.File;

import org.unidal.helper.Threads;
import org.unidal.initialization.AbstractModule;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;

import com.dianping.cat.consumer.CatConsumerAdvancedModule;
import com.dianping.cat.consumer.CatConsumerModule;
import com.dianping.cat.hadoop.hdfs.DumpUploader;
import com.dianping.cat.message.spi.core.MessageConsumer;
import com.dianping.cat.message.spi.core.TcpSocketReceiver;
import com.dianping.cat.report.service.CachedReportTask;
import com.dianping.cat.report.task.DefaultTaskConsumer;
import com.dianping.cat.report.task.metric.MetricAlert;
import com.dianping.cat.report.view.DomainNavManager;
import com.dianping.cat.system.config.ConfigReloadTask;
import com.dianping.cat.system.notify.ScheduledMailTask;

public class CatHomeModule extends AbstractModule {
	public static final String ID = "cat-home";

	@Override
	protected void execute(ModuleContext ctx) throws Exception {
		ServerConfigManager serverConfigManager = ctx.lookup(ServerConfigManager.class);
		
		ctx.lookup(MessageConsumer.class);
		if (!serverConfigManager.isLocalMode()) {
			ConfigReloadTask configReloadTask = ctx.lookup(ConfigReloadTask.class);
			DumpUploader uploader = ctx.lookup(DumpUploader.class);

			Threads.forGroup("Cat").start(configReloadTask);
			Threads.forGroup("Cat").start(uploader);
		}
		
		if (serverConfigManager.isJobMachine() && !serverConfigManager.isLocalMode()) {
			DefaultTaskConsumer taskConsumer = ctx.lookup(DefaultTaskConsumer.class);
			MetricAlert metricAlert = ctx.lookup(MetricAlert.class);
			DomainNavManager domainNavManager = ctx.lookup(DomainNavManager.class);
			CachedReportTask cachedReportTask = ctx.lookup(CachedReportTask.class);
		
			Threads.forGroup("Cat").start(cachedReportTask);
			Threads.forGroup("Cat").start(domainNavManager);
			Threads.forGroup("Cat").start(metricAlert);
			Threads.forGroup("Cat").start(taskConsumer);
		}
		executeAlarmModule(ctx);
	}

	private void executeAlarmModule(ModuleContext ctx) throws Exception {
		ServerConfigManager serverConfigManager = ctx.lookup(ServerConfigManager.class);
		ScheduledMailTask scheduledTask = ctx.lookup(ScheduledMailTask.class);

		if (serverConfigManager.isJobMachine() && !serverConfigManager.isLocalMode()) {
			Threads.forGroup("Cat").start(scheduledTask);
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

		serverConfigManager.initialize(serverConfigFile);

		TcpSocketReceiver server = ctx.lookup(TcpSocketReceiver.class);
		server.init();
	}

}
