package com.dianping.cat.message.context;

import com.dianping.cat.message.Metric;
import com.dianping.cat.message.internal.DefaultMetric;
import com.dianping.cat.message.pipeline.MessagePipeline;

public class DefaultMetricContext implements MetricContext {
	private MessagePipeline m_pipeline;

	public DefaultMetricContext(MessagePipeline pipeline) {
		m_pipeline = pipeline;
	}

	@Override
	public Metric newMetric(String name) {
		return new DefaultMetric(this, name);
	}

	@Override
	public void add(Metric metric) {
		m_pipeline.headContext(metric).fireMessage(metric);
	}

	@Override
	public void tick() {
		m_pipeline.headContext(TICK).fireMessage(TICK);
	}
}
