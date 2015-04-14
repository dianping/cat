package com.dianping.cat.consumer.dependency;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.dependency.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.dependency.model.transform.DefaultNativeParser;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.transform.DefaultSaxParser;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.task.TaskManager;
import com.dianping.cat.task.TaskManager.TaskProlicy;

public class DependencyDelegate implements ReportDelegate<DependencyReport> {

	@Inject
	private TaskManager m_taskManager;

	@Override
	public void afterLoad(Map<String, DependencyReport> reports) {
	}

	@Override
	public void beforeSave(Map<String, DependencyReport> reports) {
		for (DependencyReport report : reports.values()) {
			Set<String> domainNames = report.getDomainNames();

			domainNames.clear();
			domainNames.addAll(reports.keySet());
		}
	}

	@Override
	public byte[] buildBinary(DependencyReport report) {
		return DefaultNativeBuilder.build(report);
	}

	@Override
	public String buildXml(DependencyReport report) {
		return report.toString();
	}

	@Override
	public boolean createHourlyTask(DependencyReport report) {
		return m_taskManager.createTask(report.getStartTime(), Constants.CAT, DependencyAnalyzer.ID, TaskProlicy.HOULY);
	}

	@Override
	public String getDomain(DependencyReport report) {
		return report.getDomain();
	}

	@Override
	public DependencyReport makeReport(String domain, long startTime, long duration) {
		DependencyReport report = new DependencyReport(domain);

		report.setStartTime(new Date(startTime));
		report.setEndTime(new Date(startTime + duration - 1));

		return report;
	}

	@Override
	public DependencyReport mergeReport(DependencyReport old, DependencyReport other) {
		DependencyReportMerger merger = new DependencyReportMerger(old);

		other.accept(merger);
		return old;
	}

	@Override
	public DependencyReport parseBinary(byte[] bytes) {
		return DefaultNativeParser.parse(bytes);
	}

	@Override
	public DependencyReport parseXml(String xml) throws Exception {
		DependencyReport report = DefaultSaxParser.parse(xml);

		return report;
	}
}
