package com.dianping.cat.report.page.metric.service;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.service.BaseHistoricalModelService;
import com.dianping.cat.report.service.ModelRequest;

public class HistoricalMetricService extends BaseHistoricalModelService<MetricReport> {

	@Inject
	private MetricReportService m_reportService;

	public HistoricalMetricService() {
		super(MetricAnalyzer.ID);
	}

	@Override
	protected MetricReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = request.getStartTime();
		MetricReport report = getReportFromDatabase(date, domain);

		return report;
	}

	private MetricReport getReportFromDatabase(long timestamp, String domain) throws Exception {
		return m_reportService.queryReport(domain, new Date(timestamp), new Date(timestamp + TimeHelper.ONE_HOUR));
	}

}
