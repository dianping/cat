package com.dianping.cat;

import java.io.File;

import org.unidal.helper.Threads;
import org.unidal.initialization.AbstractModule;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.CatConsumerModule;
import com.dianping.cat.job.CatJobModule;
import com.dianping.cat.message.io.TcpSocketReceiver;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.report.task.thread.DefaultTaskConsumer;
import com.dianping.cat.report.task.thread.TaskProducer;
import com.dianping.cat.report.view.DomainNavManager;
import com.dianping.cat.system.alarm.AlarmRuleCreator;
import com.dianping.cat.system.alarm.AlarmTask;
import com.dianping.cat.system.alarm.threshold.listener.ExceptionDataListener;
import com.dianping.cat.system.alarm.threshold.listener.ServiceDataListener;
import com.dianping.cat.system.alarm.threshold.listener.ThresholdAlertListener;
import com.dianping.cat.system.event.EventListenerRegistry;
import com.dianping.cat.system.notify.ScheduledMailTask;

public class CatHomeModule extends AbstractModule {
	public static final String ID = "cat-home";

	@Override
	protected void execute(ModuleContext ctx) throws Exception {
		// warm up IP seeker
		// IPSeekerManager.initailize(new File(serverConfigManager.getStorageLocalBaseDir()));
		ServerConfigManager serverConfigManager = ctx.lookup(ServerConfigManager.class);

		ctx.lookup(MessageConsumer.class, "realtime");
		ctx.lookup(DomainNavManager.class);

		DefaultTaskConsumer taskConsumer = ctx.lookup(DefaultTaskConsumer.class);
		TaskProducer dailyTaskProducer = ctx.lookup(TaskProducer.class);

		if (serverConfigManager.isJobMachine() && !serverConfigManager.isLocalMode()) {
			Threads.forGroup("Cat").start(taskConsumer);
			Threads.forGroup("Cat").start(dailyTaskProducer);
		}

		executeAlarmModule(ctx);
	}

	private void executeAlarmModule(ModuleContext ctx) throws Exception {
		ServerConfigManager serverConfigManager = ctx.lookup(ServerConfigManager.class);

		EventListenerRegistry registry = ctx.lookup(EventListenerRegistry.class);
		ExceptionDataListener exceptionDataListener = ctx.lookup(ExceptionDataListener.class);
		ServiceDataListener serviceDataListener = ctx.lookup(ServiceDataListener.class);
		ThresholdAlertListener thresholdAlertListener = ctx.lookup(ThresholdAlertListener.class);

		registry.register(exceptionDataListener);
		registry.register(serviceDataListener);
		registry.register(thresholdAlertListener);

		AlarmTask exceptionAlarmTask = ctx.lookup(AlarmTask.class);
		AlarmRuleCreator alarmCreatorTask = ctx.lookup(AlarmRuleCreator.class);
		ScheduledMailTask scheduledTask = ctx.lookup(ScheduledMailTask.class);

		if (serverConfigManager.isJobMachine() && !serverConfigManager.isLocalMode()) {
			Threads.forGroup("Cat").start(exceptionAlarmTask);
			Threads.forGroup("Cat").start(alarmCreatorTask);
			Threads.forGroup("Cat").start(scheduledTask);
		}
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

		TcpSocketReceiver server = ctx.lookup(TcpSocketReceiver.class);
		server.init();
	}

}
