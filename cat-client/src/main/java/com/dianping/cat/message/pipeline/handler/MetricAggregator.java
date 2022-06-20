package com.dianping.cat.message.pipeline.handler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.dianping.cat.Cat;
import com.dianping.cat.component.ComponentContext;
import com.dianping.cat.component.lifecycle.Initializable;
import com.dianping.cat.configuration.ConfigureManager;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.context.MetricContext;
import com.dianping.cat.message.internal.DefaultMetricBag;
import com.dianping.cat.message.internal.MetricBag;
import com.dianping.cat.message.pipeline.MessageHandlerAdaptor;
import com.dianping.cat.message.pipeline.MessageHandlerContext;

// Component
public class MetricAggregator extends MessageHandlerAdaptor implements Initializable {
	public static String ID = "metric-aggregator";

	// Inject
	private ConfigureManager m_configureManager;

	private volatile ConcurrentMap<String, Metric> m_metrics = new ConcurrentHashMap<>();

	private MetricBag buildMetricBag(ConcurrentMap<String, Metric> metrics) {
		DefaultMetricBag bag = new DefaultMetricBag();

		bag.getMetrics().addAll(metrics.values());
		bag.setDomain(m_configureManager.getDomain());
		bag.setIpAddress(m_configureManager.getHost().getIp());
		bag.setHostName(m_configureManager.getHost().getName());

		return bag;
	}

	private synchronized ConcurrentMap<String, Metric> flip() {
		if (!m_metrics.isEmpty()) {
			ConcurrentMap<String, Metric> metrics = m_metrics;

			m_metrics = new ConcurrentHashMap<>();
			return metrics;
		} else {
			return null;
		}
	}

	@Override
	public int getOrder() {
		return 210;
	}

	@Override
	public void handleMessage(MessageHandlerContext ctx, Object msg) {
		if (MetricContext.TICK.equals(msg)) {
			ConcurrentMap<String, Metric> metrics = flip();

			if (metrics != null) {
				ctx.fireMessage(buildMetricBag(metrics));
			}
		}

		super.handleMessage(ctx, msg);
	}

	@Override
	protected synchronized void handleMetric(MessageHandlerContext ctx, Metric metric) {
		String name = metric.getName();
		Metric m = m_metrics.get(name);

		if (m == null) {
			if ((m = m_metrics.putIfAbsent(name, metric)) == null) {
				m = metric;
			}
		}

		if (m != metric) {
			if (m.getKind() == metric.getKind()) {
				m.add(metric);
			} else {
				Cat.logEvent("Metric.Kind.Conflicted", name, Message.SUCCESS, m.getKind() + "," + metric.getKind());
			}
		}
	}

	@Override
	public void initialize(ComponentContext ctx) {
		m_configureManager = ctx.lookup(ConfigureManager.class);
	}
}
