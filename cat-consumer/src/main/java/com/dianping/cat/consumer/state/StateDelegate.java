package com.dianping.cat.consumer.state;

import java.util.Date;
import java.util.Map;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.DefaultSaxParser;
import com.dianping.cat.service.ReportDelegate;
import com.dianping.cat.task.TaskManager;
import com.dianping.cat.task.TaskManager.TaskProlicy;

public class StateDelegate implements ReportDelegate<StateReport> {

	@Inject
	private TaskManager m_taskManager;

	@Override
	public void afterLoad(Map<String, StateReport> reports) {
	}

	@Override
	public void beforeSave(Map<String, StateReport> reports) {
	}

	@Override
	public String buildXml(StateReport report) {
		return report.toString();
	}

	@Override
	public boolean createHourlyTask(StateReport report) {
		m_taskManager.createTask(report.getStartTime(), report.getDomain(), "service", TaskProlicy.ALL);
		m_taskManager.createTask(report.getStartTime(), report.getDomain(), "bug", TaskProlicy.ALL);
		return m_taskManager.createTask(report.getStartTime(), report.getDomain(), StateAnalyzer.ID, TaskProlicy.ALL_EXCLUED_HOURLY);
	}

	@Override
	public String getDomain(StateReport report) {
		return report.getDomain();
	}

	@Override
	public StateReport makeReport(String domain, long startTime, long duration) {
		StateReport report = new StateReport(domain);

		report.setStartTime(new Date(startTime));
		report.setEndTime(new Date(startTime + duration - 1));

		return report;
	}

	@Override
	public StateReport mergeReport(StateReport old, StateReport other) {
		StateReportMerger merger = new StateReportMerger(old);

		other.accept(merger);
		return old;
	}

	@Override
	public StateReport parseXml(String xml) throws Exception {
		StateReport report = DefaultSaxParser.parse(xml);

		return report;
	}
}
