package com.dianping.cat.report.page.system.graph;

import java.util.Set;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Segment;
import com.dianping.cat.consumer.metric.model.transform.BaseVisitor;

public class SystemReportConvertor extends BaseVisitor {

	private String m_type;

	private Set<String> m_ipAddrs;

	private String m_chartKey;

	private MetricReport m_report;

	public static final String IP_LIST_KEY = "ipList";

	public SystemReportConvertor(String type, Set<String> ipAddrs) {
		m_type = type;
		m_ipAddrs = ipAddrs;
	}

	public MetricReport getReport() {
		return m_report;
	}

	@Override
	public void visitMetricItem(MetricItem metricItem) {
		try {
			if (validate(metricItem.getId())) {
				MetricItem item = m_report.findOrCreateMetricItem(m_chartKey);

				item.setType(metricItem.getType());
				mergeMetricItem(item, metricItem);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public void mergeMetricItem(MetricItem to, MetricItem from) {
		for (Segment temp : from.getSegments().values()) {
			Segment target = to.findOrCreateSegment(temp.getId());

			mergeSegment(target, temp);
		}
	}

	protected void mergeSegment(Segment to, Segment from) {
		to.setCount(from.getCount());
		to.setSum(from.getSum());
		to.setAvg(from.getAvg());
	}

	private boolean validate(String id) {
		try {
			int index = id.indexOf(":", id.indexOf(":") + 1);
			String realKey = id.substring(index + 1);
			int typeIndex = realKey.indexOf("_");
			String type = realKey.substring(0, typeIndex);
			int ipIndex = realKey.lastIndexOf("_");
			String chartKey = realKey.substring(typeIndex + 1, ipIndex);
			String ip = realKey.substring(ipIndex + 1);

			m_report.findOrCreateStatistic(IP_LIST_KEY).findOrCreateStatisticsItem(ip);

			if (m_type.equals(type)) {
				m_chartKey = chartKey + "_" + ip;

				if (m_ipAddrs != null && !m_ipAddrs.contains(ip)) {
					return false;
				} else {
					return true;
				}
			} else {
				return false;
			}
		} catch (Exception e) {
			Cat.logError(new RuntimeException("system agent send metric [" + id + "]  error"));
		}
		return false;
	}

	@Override
	public void visitMetricReport(MetricReport metricReport) {
		m_report = new MetricReport(metricReport.getProduct());

		m_report.setStartTime(metricReport.getStartTime());
		m_report.setEndTime(metricReport.getEndTime());
		super.visitMetricReport(metricReport);
	}

	@Override
	public void visitSegment(Segment segment) {
		super.visitSegment(segment);
	}

}
