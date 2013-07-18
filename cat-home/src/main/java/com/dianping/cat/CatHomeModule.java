package com.dianping.cat;

import java.io.File;

import org.unidal.helper.Threads;
import org.unidal.initialization.AbstractModule;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;

import com.dianping.cat.consumer.CatConsumerAdvancedModule;
import com.dianping.cat.consumer.CatConsumerModule;
import com.dianping.cat.consumer.problem.aggregation.AggregationConfigManager;
import com.dianping.cat.hadoop.hdfs.DumpUploader;
import com.dianping.cat.message.spi.core.MessageConsumer;
import com.dianping.cat.message.spi.core.TcpSocketReceiver;
import com.dianping.cat.report.task.DefaultTaskConsumer;
import com.dianping.cat.report.task.metric.MetricAlert;
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
		ServerConfigManager serverConfigManager = ctx.lookup(ServerConfigManager.class);
		ctx.lookup(DumpUploader.class);
		ctx.lookup(MessageConsumer.class);
		ctx.lookup(DomainNavManager.class);
		ctx.lookup(AggregationConfigManager.class);

		if (serverConfigManager.isJobMachine() && !serverConfigManager.isLocalMode()) {
			// MetricAlert metricAlert = ctx.lookup(MetricAlert.class);
			// Threads.forGroup("Cat").start(metricAlert);
			DefaultTaskConsumer taskConsumer = ctx.lookup(DefaultTaskConsumer.class);
			MetricAlert metricAlert = ctx.lookup(MetricAlert.class);
			Threads.forGroup("Cat").start(metricAlert);
			Threads.forGroup("Cat").start(taskConsumer);
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
