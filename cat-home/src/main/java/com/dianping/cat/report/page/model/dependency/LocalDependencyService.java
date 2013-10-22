package com.dianping.cat.report.page.model.dependency;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;

public class LocalDependencyService extends BaseLocalModelService<DependencyReport> {

	@Inject
	private ReportService m_reportService;

	public LocalDependencyService() {
		super(DependencyAnalyzer.ID);
	}

	@Override
	protected DependencyReport getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		DependencyReport report = super.getReport(request, period, domain);

		if (report == null && period.isLast()) {
			long startTime = request.getStartTime();
			Date start = new Date(startTime);
			Date end = new Date(startTime + TimeUtil.ONE_HOUR);

			report = m_reportService.queryDependencyReport(domain, start, end);
		}
		return report;
	}
}
