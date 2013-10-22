package com.dianping.cat.report.page.model.metric;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;

public class LocalMetricService extends BaseLocalModelService<MetricReport> {
	@Inject
	private ReportService m_reportService;

	public LocalMetricService() {
		super("metric");
	}

	@Override
	protected MetricReport getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		MetricReport report = super.getReport(request, period, domain);

		if (report == null && period.isLast()) {
			long startTime = request.getStartTime();
			Date start = new Date(startTime);
			Date end = new Date(startTime + TimeUtil.ONE_HOUR);

			report = m_reportService.queryMetricReport(domain, start, end);
		}
		return report;
	}
}
