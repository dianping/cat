package com.dianping.cat.consumer.matrix;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.matrix.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.matrix.model.transform.DefaultNativeParser;
import com.dianping.cat.consumer.matrix.model.transform.DefaultSaxParser;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.task.TaskManager;
import com.dianping.cat.task.TaskManager.TaskProlicy;

public class MatrixDelegate implements ReportDelegate<MatrixReport> {

	@Inject
	private TaskManager m_taskManager;

	@Inject
	private ServerFilterConfigManager m_configManager;

	@Override
	public void afterLoad(Map<String, MatrixReport> reports) {
	}

	@Override
	public void beforeSave(Map<String, MatrixReport> reports) {
		for (MatrixReport report : reports.values()) {
			Set<String> domainNames = report.getDomainNames();

			domainNames.clear();
			domainNames.addAll(reports.keySet());

			new MatrixReportFilter().visitMatrixReport(report);
		}
	}

	@Override
	public byte[] buildBinary(MatrixReport report) {
		return DefaultNativeBuilder.build(report);
	}

	@Override
	public String buildXml(MatrixReport report) {
		return report.toString();
	}

	@Override
	public boolean createHourlyTask(MatrixReport report) {
		String domain = report.getDomain();

		if (m_configManager.validateDomain(domain)) {
			return m_taskManager.createTask(report.getStartTime(), domain, MatrixAnalyzer.ID,
			      TaskProlicy.ALL_EXCLUED_HOURLY);
		} else {
			return true;
		}
	}

	@Override
	public String getDomain(MatrixReport report) {
		return report.getDomain();
	}

	@Override
	public MatrixReport makeReport(String domain, long startTime, long duration) {
		MatrixReport report = new MatrixReport(domain);

		report.setStartTime(new Date(startTime));
		report.setEndTime(new Date(startTime + duration - 1));

		return report;
	}

	@Override
	public MatrixReport mergeReport(MatrixReport old, MatrixReport other) {
		MatrixReportMerger merger = new MatrixReportMerger(old);

		other.accept(merger);
		return old;
	}

	@Override
	public MatrixReport parseBinary(byte[] bytes) {
		return DefaultNativeParser.parse(bytes);
	}

	@Override
	public MatrixReport parseXml(String xml) throws Exception {
		MatrixReport report = DefaultSaxParser.parse(xml);

		return report;
	}
}
