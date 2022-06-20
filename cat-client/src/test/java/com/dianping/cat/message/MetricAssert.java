package com.dianping.cat.message;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;

import com.dianping.cat.message.Metric.Kind;

public class MetricAssert {
	private static Map<String, KindAssert> s_metrics = new HashMap<>();

	public static KindAssert name(String name) {
		KindAssert ma = s_metrics.get(name);

		if (ma == null) {
			Assert.fail("No metric found!");
		}

		return ma;
	}

	public static void newMetric(Metric metric) {
		String name = metric.getName();
		KindAssert ma = s_metrics.get(name);

		if (ma == null) {
			ma = new KindAssert();
			s_metrics.put(name, ma);
		}

		ma.add(metric);
	}

	public static void reset() {
		s_metrics.clear();
	}

	public static class KindAssert {
		private Metric m_metric;

		private void add(Metric metric) {
			if (m_metric == null) {
				m_metric = metric;
			} else if (m_metric.getKind() == metric.getKind()) {
				m_metric.add(metric);
			} else {
				Assert.fail(String.format("Mismatch kind! expected: %s but was: %s", m_metric.getKind(), metric.getKind()));
			}
		}

		public KindAssert count(int count) {
			Assert.assertEquals("count mismatched!", count, m_metric.getCount());
			return this;
		}

		public KindAssert duration(long duration) {
			Assert.assertEquals("duration mismatched!", duration, m_metric.getDuration(), 1e-6);
			return this;
		}

		public KindAssert kind(Kind kind) {
			Assert.assertEquals("kind mismatched!", kind, m_metric.getKind());
			return this;
		}

		public KindAssert sum(double sum) {
			Assert.assertEquals("sum mismatched!", sum, m_metric.getSum(), 1e-6);
			return this;
		}
	}
}
