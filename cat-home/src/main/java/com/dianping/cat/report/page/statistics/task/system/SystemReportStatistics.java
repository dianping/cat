package com.dianping.cat.report.page.statistics.task.system;

import java.util.Date;
import java.util.List;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Segment;
import com.dianping.cat.consumer.metric.model.transform.BaseVisitor;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.system.entity.Day;
import com.dianping.cat.home.system.entity.Domain;
import com.dianping.cat.home.system.entity.Entity;
import com.dianping.cat.home.system.entity.Rush;
import com.dianping.cat.home.system.entity.SystemReport;

public class SystemReportStatistics extends BaseVisitor {

	private SystemReport m_systemReport;

	private List<String> m_keys;

	private String m_key;

	private String m_ip;

	private String m_productLine;

	private boolean m_rushHour;

	private long m_start;

	public SystemReportStatistics(long start, SystemReport systemReport, List<String> keys) {
		m_start = start;
		m_systemReport = systemReport;
		m_keys = keys;
	}

	public SystemReport getSystemReport() {
		return m_systemReport;
	}

	@Override
	public void visitMetricItem(MetricItem metricItem) {
		String suffix = metricItem.getId().split(":")[2];
		String[] fields = suffix.split("_");
		m_key = fields[1];
		m_ip = fields[2];

		if (m_keys.contains(m_key)) {
			updateMachine(Constants.ALL);
			updateMachine(m_productLine);
			super.visitMetricItem(metricItem);
		}
	}

	private void updateMachine(String product) {
		Entity entity = m_systemReport.findOrCreateDomain(product).findOrCreateEntity(m_key);

		entity.findOrCreateMachine(m_ip);
	}

	@Override
	public void visitSegment(Segment segment) {
		updateEntity(segment, Constants.ALL);
		updateEntity(segment, m_productLine);

		super.visitSegment(segment);
	}

	private void updateEntity(Segment segment, String product) {
		Domain domain = m_systemReport.findOrCreateDomain(product);
		Entity entity = domain.findOrCreateEntity(m_key);

		if (m_rushHour) {
			Rush rush = entity.getRush();

			if (rush == null) {
				rush = new Rush();
				entity.setRush(rush);
			}

			rush.incCount(segment.getCount());
			rush.incSum(segment.getSum());
			rush.setAvg(rush.getSum() / rush.getCount());
		}

		Day day = entity.getDay();

		if (day == null) {
			day = new Day();
			entity.setDay(day);
		}
		day.incCount(segment.getCount());
		day.incSum(segment.getSum());
		day.setAvg(day.getSum() / day.getCount());
	}

	@Override
	public void visitMetricReport(MetricReport metricReport) {
		m_productLine = metricReport.getProduct();
		m_rushHour = false;

		if (isRushHour(metricReport.getStartTime(), metricReport.getEndTime())) {
			m_rushHour = true;
		}
		super.visitMetricReport(metricReport);
	}

	private boolean isRushHour(Date start, Date end) {
		return start.getTime() >= m_start + TimeHelper.ONE_HOUR * 16
		      && end.getTime() <= m_start + TimeHelper.ONE_HOUR * 18;
	}

}
