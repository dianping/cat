package com.dianping.cat.report.task.abtest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cat.consumer.metric.model.entity.Abtest;
import com.dianping.cat.consumer.metric.model.entity.Group;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Point;
import com.dianping.cat.consumer.metric.model.transform.BaseVisitor;
import com.dianping.cat.report.abtest.entity.AbtestReport;
import com.dianping.cat.report.abtest.entity.Goal;
import com.dianping.cat.report.abtest.entity.Variation;

public class MetricReportForABTestVisitor extends BaseVisitor {

	private Map<Integer, AbtestReport> m_reportMap;

	private Map<Integer, HashMap<String, String>> m_metrics;

	private String m_id;

	private String m_type;

	private int m_runId;

	private String m_variation;

	private Date m_startDate;

	private Date m_endDate;

	public MetricReportForABTestVisitor() {
		m_reportMap = new HashMap<Integer, AbtestReport>();
		m_metrics = new HashMap<Integer, HashMap<String, String>>();
	}

	private AbtestReport findOrCreateAbtestReport(int runId) {
		AbtestReport report = m_reportMap.get(runId);

		if (report == null) {
			report = new AbtestReport();

			report.setRunId(runId);
			report.setStartTime(m_startDate);
			report.setEndTime(m_endDate);
			m_reportMap.put(runId, report);
		}

		return report;
	}

	public Map<Integer, AbtestReport> getReportMap() {
		for (AbtestReport report : m_reportMap.values()) {
			HashMap<String, String> map = m_metrics.get(report.getRunId());

			for (Entry<String, String> entry : map.entrySet()) {
				String metric = entry.getKey();
				String value = entry.getValue();
				
				for (Variation variation : report.getVariations().values()) {
					Goal goal = variation.findOrCreateGoal(metric);

					goal.setType(value);
				}

				Goal goal = new Goal();

				goal.setName(metric);
				report.getGoals().add(goal);
			}
		}

		return m_reportMap;
	}

	@Override
	public void visitAbtest(Abtest abtest) {
		try {
			m_runId = Integer.parseInt(abtest.getRunId());
		} catch (Exception e) {
			m_runId = -1;
		}

		HashMap<String, String> map = m_metrics.get(m_runId);

		if (map == null) {
			map = new HashMap<String, String>();

			m_metrics.put(m_runId, map);
		}

		map.put(m_id, m_type);
		super.visitAbtest(abtest);
	}

	@Override
	public void visitGroup(Group group) {
		m_variation = group.getName();

		if (m_variation.length() == 0) {
			m_variation = "Control";
		}

		super.visitGroup(group);
	}

	@Override
	public void visitMetricItem(MetricItem metricItem) {
		m_id = metricItem.getId();
		m_type = metricItem.getType();

		super.visitMetricItem(metricItem);
	}

	@Override
	public void visitMetricReport(MetricReport metricReport) {
		m_startDate = metricReport.getStartTime();
		m_endDate = metricReport.getEndTime();

		super.visitMetricReport(metricReport);
	}

	@Override
	public void visitPoint(Point point) {
		AbtestReport report = findOrCreateAbtestReport(m_runId);

		Variation variation = report.findOrCreateVariation(m_variation);

		Goal goal = variation.findOrCreateGoal(m_id);

		int count = goal.getCount() + point.getCount();
		double sum = goal.getSum() + point.getSum();

		goal.setType(m_type);
		goal.setCount(count);
		goal.setSum(sum);
		// avg?
	}
}
