package com.dianping.cat;

import java.io.File;

import org.unidal.helper.Threads;
import org.unidal.initialization.AbstractModule;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.CatConsumerModule;
import com.dianping.cat.message.spi.core.MessageConsumer;
import com.dianping.cat.message.spi.core.TcpSocketReceiver;
import com.dianping.cat.report.task.DefaultTaskConsumer;
import com.dianping.cat.report.alert.app.AppAlert;
import com.dianping.cat.report.alert.business.BusinessAlert;
import com.dianping.cat.report.alert.database.DatabaseAlert;
import com.dianping.cat.report.alert.exception.ExceptionAlert;
import com.dianping.cat.report.alert.exception.FrontEndExceptionAlert;
import com.dianping.cat.report.alert.heartbeat.HeartbeatAlert;
import com.dianping.cat.report.alert.network.NetworkAlert;
import com.dianping.cat.report.alert.storage.StorageDatabaseAlert;
import com.dianping.cat.report.alert.system.SystemAlert;
import com.dianping.cat.report.alert.thirdParty.ThirdPartyAlert;
import com.dianping.cat.report.alert.thirdParty.ThirdPartyAlertBuilder;
import com.dianping.cat.report.alert.transaction.TransactionAlert;
import com.dianping.cat.report.alert.web.WebAlert;
import com.dianping.cat.system.config.ConfigReloadTask;

public class CatHomeModule extends AbstractModule {
	public static final String ID = "cat-home";

	@Override
	protected void execute(ModuleContext ctx) throws Exception {
		ServerConfigManager serverConfigManager = ctx.lookup(ServerConfigManager.class);

		ctx.lookup(MessageConsumer.class);

		ConfigReloadTask configReloadTask = ctx.lookup(ConfigReloadTask.class);
		Threads.forGroup("cat").start(configReloadTask);

		if (serverConfigManager.isJobMachine()) {
			DefaultTaskConsumer taskConsumer = ctx.lookup(DefaultTaskConsumer.class);

			Threads.forGroup("cat").start(taskConsumer);
		}

		if (serverConfigManager.isAlertMachine()) {
			BusinessAlert metricAlert = ctx.lookup(BusinessAlert.class);
			NetworkAlert networkAlert = ctx.lookup(NetworkAlert.class);
			DatabaseAlert databaseAlert = ctx.lookup(DatabaseAlert.class);
			SystemAlert systemAlert = ctx.lookup(SystemAlert.class);
			ExceptionAlert exceptionAlert = ctx.lookup(ExceptionAlert.class);
			FrontEndExceptionAlert frontEndExceptionAlert = ctx.lookup(FrontEndExceptionAlert.class);
			HeartbeatAlert heartbeatAlert = ctx.lookup(HeartbeatAlert.class);
			ThirdPartyAlert thirdPartyAlert = ctx.lookup(ThirdPartyAlert.class);
			ThirdPartyAlertBuilder alertBuildingTask = ctx.lookup(ThirdPartyAlertBuilder.class);
			AppAlert appAlert = ctx.lookup(AppAlert.class);
			WebAlert webAlert = ctx.lookup(WebAlert.class);
			TransactionAlert transactionAlert = ctx.lookup(TransactionAlert.class);
			StorageDatabaseAlert storageDatabaseAlert = ctx.lookup(StorageDatabaseAlert.class);

			Threads.forGroup("cat").start(networkAlert);
			Threads.forGroup("cat").start(databaseAlert);
			Threads.forGroup("cat").start(systemAlert);
			Threads.forGroup("cat").start(metricAlert);
			Threads.forGroup("cat").start(exceptionAlert);
			Threads.forGroup("cat").start(frontEndExceptionAlert);
			Threads.forGroup("cat").start(heartbeatAlert);
			Threads.forGroup("cat").start(thirdPartyAlert);
			Threads.forGroup("cat").start(alertBuildingTask);
			Threads.forGroup("cat").start(appAlert);
			Threads.forGroup("cat").start(webAlert);
			Threads.forGroup("cat").start(transactionAlert);
			Threads.forGroup("cat").start(storageDatabaseAlert);
		}
	}

	@Override
	public Module[] getDependencies(ModuleContext ctx) {
		return ctx.getModules(CatConsumerModule.ID);
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
