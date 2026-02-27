package com.dianping.cat.message.context;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import com.dianping.cat.Cat;
import com.dianping.cat.component.ComponentContext;
import com.dianping.cat.configuration.model.entity.ClientConfig;
import com.dianping.cat.message.pipeline.MessagePipeline;

public class MetricContextHelper {
	private static MetricContext s_context;

	private static AtomicBoolean s_initialized = new AtomicBoolean();

	private static Timer s_timer;

	public static MetricContext context() {
		if (!s_initialized.get()) {
			Cat.getBootstrap().initialize(new ClientConfig());

			ComponentContext context = Cat.getBootstrap().getComponentContext();
			final MessagePipeline pipeline = context.lookup(MessagePipeline.class);

			s_timer = new Timer(true);
			s_timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					// tick every second
					pipeline.headContext(MetricContext.TICK).fireMessage(MetricContext.TICK);
				}
			}, 1000, 1000);

			s_context = new DefaultMetricContext(pipeline);
			s_initialized.set(true);
		}

		return s_context;
	}

	public static void reset() {
		if (s_timer != null) {
			s_timer.cancel();
		}

		s_initialized.set(false);
	}
}
