package com.dianping.cat.report.page.metric;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.consumer.metric.model.entity.Metric;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Point;
import com.dianping.cat.consumer.metric.model.transform.BaseVisitor;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.metric.MetricConfig.MetricFlag;
import com.google.gson.Gson;

public class MetricDisplay extends BaseVisitor {

	private Map<String, GraphItem> m_metrics = new LinkedHashMap<String, GraphItem>();

	private Map<String, GraphItem> m_conversionRates = new LinkedHashMap<String, GraphItem>();

	private String m_key;

	private Date m_start;

	private MetricConfig m_config;

	private static final String COUNT = ":count";

	public MetricDisplay(MetricConfig metricConfig, Date start) {
		m_config = metricConfig;
		m_start = start;

		for (MetricFlag flag : m_config.getFlags()) {
			if (flag.isShowSum()) {
				String title = flag.getKey() + ":sum";
				m_metrics.put(title, new GraphItem(m_start, title, flag.getKey()));
			}
			if (flag.isShowCount()) {
				String title = flag.getKey() + COUNT;
				m_metrics.put(title, new GraphItem(m_start, title, flag.getKey()));
			}
			if (flag.isShowAvg()) {
				String title = flag.getKey() + ":avg";
				m_metrics.put(title, new GraphItem(m_start, title, flag.getKey()));
			}
		}
	}

	public MetricDisplay buildConvertRate(String key1, String key2) {
		GraphItem item1 = m_metrics.get(key1 + COUNT);
		GraphItem item2 = m_metrics.get(key2 + COUNT);

		if (item1 != null && item2 != null) {
			String key = key1 + ":" + key2;
			GraphItem item = new GraphItem(m_start, key1 + " to " + key2 + " Conversion Rate", key);
			double[] value1 = item1.getValues();
			double[] value2 = item2.getValues();
			int size = item.getSize();
			double[] value = new double[size];

			for (int i = 0; i < size; i++) {
				if (value1[i] > 0) {
					value[i] = value2[i] / value1[i];
				}
			}
			item.setValues(value);
			m_conversionRates.put(key, item);
		}
		return this;
	}

	public List<GraphItem> getConversionRates() {
		return new ArrayList<GraphItem>(m_conversionRates.values());
	}

	public List<GraphItem> getGroups() {
		return new ArrayList<GraphItem>(m_metrics.values());
	}

	@Override
	public void visitMetric(Metric metric) {
		m_key = metric.getId();
		super.visitMetric(metric);
	}

	@Override
	public void visitMetricReport(MetricReport metricReport) {
		super.visitMetricReport(metricReport);
	}

	@Override
	public void visitPoint(Point point) {
		int min = point.getId();
		long count = point.getCount();
		double sum = point.getSum();
		double avg = point.getAvg();

		GraphItem graphItem = m_metrics.get(m_key + ":sum");
		if (graphItem != null) {
			graphItem.setValue(min, sum);
		}
		graphItem = m_metrics.get(m_key + ":count");
		if (graphItem != null) {
			graphItem.setValue(min, count);
		}
		graphItem = m_metrics.get(m_key + ":avg");
		if (graphItem != null) {
			graphItem.setValue(min, avg);
		}
	}

	public static class GraphItem {
		private transient SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");

		private int size = 60;

		private long step = TimeUtil.ONE_MINUTE;

		private String start;

		private String title;

		private String key;

		private double[] values = new double[60];

		public GraphItem(Date start, String title, String key) {
			this.start = sdf.format(start);
			this.title = title;
			this.key = key;
		}

		public GraphItem addSubTitle(String title) {
			return this;
		}

		public String getJsonString() {
			Gson gson = new Gson();
			return gson.toJson(this);
		}

		public int getSize() {
			return this.size;
		}

		public String getStart() {
			return this.start;
		}

		public long getStep() {
			return step;
		}

		public String getTitle() {
			return this.title;
		}

		public GraphItem setSize(int size) {
			this.size = size;
			return this;
		}

		public GraphItem setStart(Date start) {
			this.start = sdf.format(start);
			return this;
		}

		public void setStep(long step) {
			this.step = step;
		}

		public GraphItem setTitle(String titles) {
			this.title = titles;
			return this;
		}

		public GraphItem setValue(int minute, double value) {
			values[minute] = value;
			return this;
		}

		public String getKey() {
			return key;
		}

		public double[] getValues() {
			return values;
		}

		public void setValues(double[] values) {
			this.values = values;
		}

	}

}
