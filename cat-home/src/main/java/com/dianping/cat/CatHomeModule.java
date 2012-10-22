package com.dianping.cat;

import java.io.File;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.CatConsumerModule;
import com.dianping.cat.job.CatJobModule;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageHandler;
import com.dianping.cat.message.spi.internal.DefaultMessageHandler;
import com.dianping.cat.report.task.DailyTaskProducer;
import com.dianping.cat.report.task.DefaultTaskConsumer;
import com.dianping.cat.report.task.monthreport.MonthReportBuilderTask;
import com.dianping.cat.system.alarm.DefaultAlarmCreator;
import com.dianping.cat.system.alarm.ExceptionAlarmTask;
import com.dianping.cat.system.alarm.exception.listener.ExceptionAlertListener;
import com.dianping.cat.system.alarm.exception.listener.ExceptionDataListener;
import com.dianping.cat.system.event.EventListenerRegistry;
import com.dianping.cat.system.notify.ScheduledMailTask;
import com.site.helper.Threads;
import com.site.initialization.AbstractModule;
import com.site.initialization.Module;
import com.site.initialization.ModuleContext;

public class CatHomeModule extends AbstractModule {
	public static final String ID = "cat-home";

	@Override
	protected void execute(ModuleContext ctx) throws Exception {
		// warm up IP seeker
		// IPSeekerManager.initailize(new File(serverConfigManager.getStorageLocalBaseDir()));
		ServerConfigManager serverConfigManager = ctx.lookup(ServerConfigManager.class);

		ctx.lookup(MessageConsumer.class, "realtime");

		DefaultTaskConsumer taskConsumer = ctx.lookup(DefaultTaskConsumer.class);
		DailyTaskProducer dailyTaskProducer = ctx.lookup(DailyTaskProducer.class);
		MonthReportBuilderTask monthReportTask = ctx.lookup(MonthReportBuilderTask.class);

		if (serverConfigManager.isJobMachine() && !serverConfigManager.isLocalMode()) {
			Threads.forGroup("Cat").start(dailyTaskProducer);
			Threads.forGroup("Cat").start(taskConsumer);
			Threads.forGroup("Cat").start(monthReportTask);
		}

		//executeAlarmModule(ctx, serverConfigManager);
	}

	private void executeAlarmModule(ModuleContext ctx, ServerConfigManager serverConfigManager) throws Exception {
		EventListenerRegistry registry = ctx.lookup(EventListenerRegistry.class);
		ExceptionDataListener exceptionDataListener = ctx.lookup(ExceptionDataListener.class);
		ExceptionAlertListener exceptionAlertListener = ctx.lookup(ExceptionAlertListener.class);

		registry.register(exceptionDataListener);
		registry.register(exceptionAlertListener);

		ExceptionAlarmTask exceptionAlarmTask = ctx.lookup(ExceptionAlarmTask.class);
		DefaultAlarmCreator alarmCreatorTask = ctx.lookup(DefaultAlarmCreator.class);
		ScheduledMailTask scheduledTask = ctx.lookup(ScheduledMailTask.class);

		if (serverConfigManager.isJobMachine()) {
			Threads.forGroup("Cat-Alarm").start(exceptionAlarmTask);
			Threads.forGroup("Cat-Alarm").start(alarmCreatorTask);
			Threads.forGroup("Cat-Alarm").start(scheduledTask);
		}

		// OtherJobReport tuangouMonth = ctx.lookup(OtherJobReport.class);
		// Threads.forGroup("Cat").start(tuangouMonth);
	}

	@Override
	public Module[] getDependencies(ModuleContext ctx) {
		return ctx.getModules(CatConsumerModule.ID, CatJobModule.ID);
	}

	@Override
	protected void setup(ModuleContext ctx) throws Exception {
		File serverConfigFile = ctx.getAttribute("cat-server-config-file");
		ServerConfigManager serverConfigManager = ctx.lookup(ServerConfigManager.class);

		serverConfigManager.initialize(serverConfigFile);

		DefaultMessageHandler handler = (DefaultMessageHandler) ctx.lookup(MessageHandler.class);

		Threads.forGroup("Cat").start(handler);
	}
}
