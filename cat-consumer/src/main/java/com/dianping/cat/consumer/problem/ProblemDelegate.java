package com.dianping.cat.consumer.problem;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.consumer.problem.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.problem.model.transform.DefaultNativeParser;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;
import com.dianping.cat.service.ReportDelegate;
import com.dianping.cat.task.TaskManager;
import com.dianping.cat.task.TaskManager.TaskProlicy;

public class ProblemDelegate implements ReportDelegate<ProblemReport> {

	@Inject
	private ProblemReportAggregation m_problemReportAggregation;

	@Inject
	private TaskManager m_taskManager;

	@Override
	public void afterLoad(Map<String, ProblemReport> reports) {
	}

	@Override
	public void beforeSave(Map<String, ProblemReport> reports) {
		for (ProblemReport report : reports.values()) {
			Set<String> domainNames = report.getDomainNames();

			domainNames.clear();
			domainNames.addAll(reports.keySet());
		}

		if (reports.size() > 0) {
			ProblemReport all = createAggregatedReport(reports);

			reports.put(all.getDomain(), all);
		}

		ProblemReport frontEnd = reports.get(Constants.FRONT_END);

		if (frontEnd != null) {
			reports.put(Constants.FRONT_END, rebuildFrontEndReport(frontEnd));
		}
	}

	public ProblemReport rebuildFrontEndReport(ProblemReport report) {
		m_problemReportAggregation.refreshRule();
		report.accept(m_problemReportAggregation);

		return m_problemReportAggregation.getReport();
	}

	private boolean validateDomain(String domain) {
		return !domain.equals(Constants.FRONT_END);
	}

	public ProblemReport createAggregatedReport(Map<String, ProblemReport> reports) {
		ProblemReport report = new ProblemReport(Constants.ALL);
		ProblemReportAllBuilder visitor = new ProblemReportAllBuilder(report);

		try {
			for (ProblemReport temp : reports.values()) {
				if (validateDomain(temp.getDomain())) {
					report.getIps().add(temp.getDomain());
					report.getDomainNames().add(temp.getDomain());
					visitor.visitProblemReport(temp);
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return report;
	}

	@Override
	public String buildXml(ProblemReport report) {
		return report.toString();
	}

	@Override
	public boolean createHourlyTask(ProblemReport report) {
		return m_taskManager.createTask(report.getStartTime(), report.getDomain(), ProblemAnalyzer.ID, TaskProlicy.ALL);
	}

	@Override
	public String getDomain(ProblemReport report) {
		return report.getDomain();
	}

	@Override
	public ProblemReport makeReport(String domain, long startTime, long duration) {
		ProblemReport report = new ProblemReport(domain);

		report.setStartTime(new Date(startTime));
		report.setEndTime(new Date(startTime + duration - 1));

		return report;
	}

	@Override
	public ProblemReport mergeReport(ProblemReport old, ProblemReport other) {
		ProblemReportMerger merger = new ProblemReportMerger(old);

		other.accept(merger);
		return old;
	}

	@Override
	public ProblemReport parseXml(String xml) throws Exception {
		ProblemReport report = DefaultSaxParser.parse(xml);

		return report;
	}

	@Override
	public byte[] buildBinary(ProblemReport report) {
		return DefaultNativeBuilder.build(report);
	}

	@Override
	public ProblemReport parseBinary(byte[] bytes) {
		return DefaultNativeParser.parse(bytes);
	}
}
