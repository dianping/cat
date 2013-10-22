package com.dianping.cat.report.page.model.top;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;

public class LocalTopService extends BaseLocalModelService<TopReport> {
	@Inject
	private ReportService m_reportService;

	public LocalTopService() {
		super(TopAnalyzer.ID);
	}

	@Override
	protected TopReport getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		TopReport report = super.getReport(request, period, domain);

		if (report == null && period.isLast()) {
			long startTime = request.getStartTime();
			Date start = new Date(startTime);
			Date end = new Date(startTime + TimeUtil.ONE_HOUR);

			report = m_reportService.queryTopReport(domain, start, end);
		}
		return report;
	}
}
