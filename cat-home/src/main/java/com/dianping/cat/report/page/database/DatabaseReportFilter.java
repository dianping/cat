package com.dianping.cat.report.page.database;

import java.util.List;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Segment;
import com.dianping.cat.consumer.metric.model.transform.BaseVisitor;

public class DatabaseReportFilter extends BaseVisitor {

	private MetricReport m_report;

	private List<String> m_keys;

	public DatabaseReportFilter(List<String> keys) {
		m_keys = keys;
	}

	public MetricReport getReport() {
		return m_report;
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

	public void setReport(MetricReport report) {
		m_report = report;
	}

	private boolean validate(String id) {
		try {
			if (m_keys != null && !m_keys.isEmpty()) {
				int index = id.indexOf(":", id.indexOf(":") + 1);
				String realKey = id.substring(index + 1);

				return m_keys.contains(realKey);
			} else {
				return true;
			}
		} catch (Exception e) {
			Cat.logError(e);
			return true;
		}
	}

	@Override
	public void visitMetricItem(MetricItem metricItem) {
		String id = metricItem.getId();

		if (validate(id)) {
			MetricItem item = m_report.findOrCreateMetricItem(id);

			item.setType(metricItem.getType());
			mergeMetricItem(item, metricItem);
		}
	}

	@Override
	public void visitMetricReport(MetricReport metricReport) {
		m_report = new MetricReport(metricReport.getProduct());

		m_report.setStartTime(metricReport.getStartTime());
		m_report.setEndTime(metricReport.getEndTime());
		super.visitMetricReport(metricReport);
	}

}
