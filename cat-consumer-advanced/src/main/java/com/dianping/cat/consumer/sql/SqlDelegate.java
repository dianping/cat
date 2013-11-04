package com.dianping.cat.consumer.sql;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.consumer.sql.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.sql.model.transform.DefaultNativeParser;
import com.dianping.cat.consumer.sql.model.transform.DefaultSaxParser;
import com.dianping.cat.service.ReportDelegate;
import com.dianping.cat.task.TaskManager;
import com.dianping.cat.task.TaskManager.TaskProlicy;

public class SqlDelegate implements ReportDelegate<SqlReport> {

	@Inject
	private TaskManager m_taskManager;

	@Override
	public void afterLoad(Map<String, SqlReport> reports) {
	}

	@Override
	public void beforeSave(Map<String, SqlReport> reports) {
		for (SqlReport report : reports.values()) {
			Set<String> domainNames = report.getDomainNames();

			domainNames.clear();
			domainNames.addAll(reports.keySet());
		}
	}

	@Override
	public String buildXml(SqlReport report) {
		return report.toString();
	}

	@Override
	public boolean createHourlyTask(SqlReport report) {
		return m_taskManager.createTask(report.getStartTime(), report.getDomain(), SqlAnalyzer.ID,
		      TaskProlicy.ALL_EXCLUED_HOURLY);
	}

	@Override
	public String getDomain(SqlReport report) {
		return report.getDomain();
	}

	@Override
	public SqlReport makeReport(String domain, long startTime, long duration) {
		SqlReport report = new SqlReport(domain);

		report.setStartTime(new Date(startTime));
		report.setEndTime(new Date(startTime + duration - 1));

		return report;
	}

	@Override
	public SqlReport mergeReport(SqlReport old, SqlReport other) {
		SqlReportMerger merger = new SqlReportMerger(old);

		other.accept(merger);
		return old;
	}

	@Override
	public SqlReport parseXml(String xml) throws Exception {
		SqlReport report = DefaultSaxParser.parse(xml);

		return report;
	}

	@Override
	public byte[] buildBinary(SqlReport report) {
		return DefaultNativeBuilder.build(report);
	}

	@Override
	public SqlReport parseBinary(byte[] bytes) {
		return DefaultNativeParser.parse(bytes);
	}
}
