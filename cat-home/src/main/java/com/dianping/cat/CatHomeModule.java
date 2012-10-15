package com.dianping.cat;

import java.io.File;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.CatConsumerModule;
import com.dianping.cat.job.CatJobModule;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageHandler;
import com.dianping.cat.message.spi.internal.DefaultMessageHandler;
import com.dianping.cat.report.page.ip.location.IPSeekerManager;
import com.dianping.cat.report.task.DailyTaskProducer;
import com.dianping.cat.report.task.TaskConsumer;
import com.dianping.cat.report.task.monthreport.MonthReportBuilderTask;
import com.dianping.cat.system.alarm.DefaultAlarmCreator;
import com.dianping.cat.system.notify.ScheduledTask;
import com.site.helper.Threads;
import com.site.initialization.AbstractModule;
import com.site.initialization.Module;
import com.site.initialization.ModuleContext;

public class CatHomeModule extends AbstractModule {
	public static final String ID = "cat-home";

	@Override
	protected void execute(ModuleContext ctx) throws Exception {
		ServerConfigManager serverConfigManager = ctx.lookup(ServerConfigManager.class);

		ctx.lookup(MessageConsumer.class, "realtime");

		// warm up IP seeker
		IPSeekerManager.initailize(new File(serverConfigManager.getStorageLocalBaseDir()));

		TaskConsumer taskConsumer = ctx.lookup(TaskConsumer.class);
		DailyTaskProducer dailyTaskProducer = ctx.lookup(DailyTaskProducer.class);
		MonthReportBuilderTask monthReportTask = ctx.lookup(MonthReportBuilderTask.class);

		if (serverConfigManager.isJobMachine() && !serverConfigManager.isLocalMode()) {
			Threads.forGroup("Cat").start(dailyTaskProducer);
			Threads.forGroup("Cat").start(taskConsumer);
			Threads.forGroup("Cat").start(monthReportTask);
		}

		DefaultAlarmCreator alarmCreator = ctx.lookup(DefaultAlarmCreator.class);
		ScheduledTask scheduledTask = ctx.lookup(ScheduledTask.class);

		if (serverConfigManager.isJobMachine()) {
			Threads.forGroup("Cat-Alarm").start(alarmCreator);
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
