package com.dianping.cat.report.task.system;

import java.util.List;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Segment;
import com.dianping.cat.consumer.metric.model.transform.BaseVisitor;
import com.dianping.cat.home.system.entity.Day;
import com.dianping.cat.home.system.entity.Domain;
import com.dianping.cat.home.system.entity.Entity;
import com.dianping.cat.home.system.entity.Rush;
import com.dianping.cat.home.system.entity.SystemReport;

public class SystemReportStatistics extends BaseVisitor {

	private SystemReport m_systemReport;

	private List<String> m_keys;

	private String m_key;

	private String m_productLine;

	private boolean m_dayReport;

	public SystemReportStatistics(SystemReport systemReport, boolean dayReport, List<String> keys) {
		m_systemReport = systemReport;
		m_dayReport = dayReport;
		m_keys = keys;
	}

	public SystemReport getSystemReport() {
		return m_systemReport;
	}

	@Override
	public void visitMetricItem(MetricItem metricItem) {
		m_key = metricItem.getId().split("_")[0];

		if (m_keys == null || m_keys.isEmpty() || m_keys.contains(m_key)) {
			super.visitMetricItem(metricItem);
		}
	}

	@Override
	public void visitSegment(Segment segment) {
		updateReport(segment, Constants.ALL);
		updateReport(segment, m_productLine);

		super.visitSegment(segment);
	}

	private void updateReport(Segment segment, String product) {
		Domain domain = m_systemReport.findOrCreateDomain(product);
		Entity entity = domain.findOrCreateEntity(m_key);

		if (m_dayReport) {
			Day day = entity.getDay();

			if (day == null) {
				day = new Day();
				entity.setDay(day);
			}

			day.incCount(segment.getCount());
			day.incSum(segment.getSum());
			day.setAvg(day.getSum() / day.getCount());
		} else {
			Rush rush = entity.getRush();

			if (rush == null) {
				rush = new Rush();
				entity.setRush(rush);
			}

			rush.incCount(segment.getCount());
			rush.incSum(segment.getSum());
			rush.setAvg(rush.getSum() / rush.getCount());
		}
	}

	@Override
	public void visitMetricReport(MetricReport metricReport) {
		m_productLine = metricReport.getProduct();

		super.visitMetricReport(metricReport);
	}
}
