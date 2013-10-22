package com.dianping.cat.report.page.model.cross;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;

public class LocalCrossService extends BaseLocalModelService<CrossReport> {

	@Inject
	private ReportService m_reportService;

	public LocalCrossService() {
		super(CrossAnalyzer.ID);
	}

	@Override
	protected CrossReport getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		CrossReport report = super.getReport(request, period, domain);

		if (report == null && period.isLast()) {
			long startTime = request.getStartTime();
			Date start = new Date(startTime);
			Date end = new Date(startTime + TimeUtil.ONE_HOUR);

			report = m_reportService.queryCrossReport(domain, start, end);
		}
		return report;
	}
}
