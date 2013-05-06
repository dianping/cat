package com.dianping.cat.report.page.metric;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import com.dianping.cat.consumer.metric.model.entity.Metric;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Point;
import com.dianping.cat.consumer.metric.model.transform.BaseVisitor;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.metric.MetricConfig.MetricFlag;
import com.google.gson.Gson;

public class MetricDisplay extends BaseVisitor {

	private Map<String, GraphItem> m_metrics = new LinkedHashMap<String, GraphItem>();

	private String m_key;

	private String m_channel;

	private Date m_start;

	private MetricConfig m_config;

	private String prefix = "channel=";

	private Set<String> m_allChannel = new TreeSet<String>();

	public MetricDisplay(MetricConfig metricConfig, String channel, Date start) {
		m_config = metricConfig;
		m_start = start;
		m_channel = channel;

		for (MetricFlag flag : m_config.getFlags()) {
			String title = flag.getTitle();
			if (flag.isShowSum()) {
				String key = flag.getKey() + ":sum";
				m_metrics.put(key, new GraphItem(m_start, title, flag.getKey()));
			}
			if (flag.isShowCount()) {
				String key = flag.getKey() + ":count";
				m_metrics.put(key, new GraphItem(m_start, title, flag.getKey()));
			}
			if (flag.isShowAvg()) {
				String key = flag.getKey() + ":avg";
				m_metrics.put(key, new GraphItem(m_start, title, flag.getKey()));
			}
		}
	}

	private void buildGraphItem(Collection<Point> points) {
		for (Point point : points) {

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
	}

	public Set<String> getAllChannel() {
		return m_allChannel;
	}

	public List<GraphItem> getGroups() {
		return new ArrayList<GraphItem>(m_metrics.values());
	}

	@Override
	public void visitMetric(Metric metric) {
		m_key = metric.getId();

		Map<String, Metric> metrics = metric.getMetrics();
		if (metrics != null) {
			Set<String> keySet = metrics.keySet();
			for (String temp : keySet) {
				if (temp.startsWith(prefix)) {
					m_allChannel.add(temp.substring(prefix.length()));
				}
			}
		}
		if (StringUtils.isEmpty(m_channel)) {
			buildGraphItem(metric.getPoints().values());
		} else {
			Metric m = metrics.get(prefix + m_channel);

			if (m != null) {
				buildGraphItem(m.getPoints().values());
			}
		}
	}

	@Override
	public void visitMetricReport(MetricReport metricReport) {
		super.visitMetricReport(metricReport);
	}

	public static class GraphItem {
		private transient SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");

		private int size = 60;

		private long step = TimeUtil.ONE_MINUTE;

		private String start;

		private String title;

		private String key;

		private static final int SIZE = 60;

		private double[] values = new double[SIZE];

		public GraphItem(Date start, String title, String key) {
			this.start = sdf.format(start);
			this.title = title;
			this.key = key;

			for (int i = 0; i < SIZE; i++) {
				values[i] = -1;
			}
		}

		public GraphItem addSubTitle(String title) {
			return this;
		}

		public String getJsonString() {
			Gson gson = new Gson();
			return gson.toJson(this);
		}

		public String getKey() {
			return key;
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

		public double[] getValues() {
			return values;
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

		public void setValues(double[] values) {
			this.values = values;
		}
	}

}
