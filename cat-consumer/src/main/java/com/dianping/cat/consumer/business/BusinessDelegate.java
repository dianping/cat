package com.dianping.cat.consumer.business;

import java.util.Date;
import java.util.Map;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.business.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.business.model.transform.DefaultNativeParser;
import com.dianping.cat.consumer.business.model.transform.DefaultSaxParser;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.task.TaskManager;
import com.dianping.cat.task.TaskManager.TaskProlicy;

@Named(type = ReportDelegate.class, value = BusinessAnalyzer.ID)
public class BusinessDelegate implements ReportDelegate<BusinessReport> {

	@Inject
	private TaskManager m_taskManager;

	@Override
	public void afterLoad(Map<String, BusinessReport> reports) {
	}

	@Override
	public void beforeSave(Map<String, BusinessReport> reports) {
	}

	@Override
	public byte[] buildBinary(BusinessReport report) {
		return DefaultNativeBuilder.build(report);
	}

	@Override
	public BusinessReport parseBinary(byte[] bytes) {
		return DefaultNativeParser.parse(bytes);
	}

	@Override
	public String buildXml(BusinessReport report) {
		return report.toString();
	}

	@Override
	public String getDomain(BusinessReport report) {
		return report.getDomain();
	}

	@Override
	public BusinessReport makeReport(String domain, long startTime, long duration) {
		BusinessReport report = new BusinessReport(domain);

		report.setStartTime(new Date(startTime));
		report.setEndTime(new Date(startTime + duration - 1));

		return report;
	}

	@Override
	public BusinessReport mergeReport(BusinessReport old, BusinessReport other) {
		BusinessReportMerger merger = new BusinessReportMerger(old);

		other.accept(merger);
		return old;
	}

	@Override
	public BusinessReport parseXml(String xml) throws Exception {
		return DefaultSaxParser.parse(xml);
	}

	@Override
	public boolean createHourlyTask(BusinessReport report) {
		return m_taskManager
		      .createTask(report.getStartTime(), report.getDomain(), BusinessAnalyzer.ID, TaskProlicy.DAILY);
	}

}
