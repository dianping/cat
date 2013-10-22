package com.dianping.cat.report.page.model.matrix;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;

public class LocalMatrixService extends BaseLocalModelService<MatrixReport> {

	@Inject
	private ReportService m_reportService;

	public LocalMatrixService() {
		super(MatrixAnalyzer.ID);
	}

	@Override
	protected MatrixReport getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		MatrixReport report = super.getReport(request, period, domain);

		if (report == null && period.isLast()) {
			long startTime = request.getStartTime();
			Date start = new Date(startTime);
			Date end = new Date(startTime + TimeUtil.ONE_HOUR);

			report = m_reportService.queryMatrixReport(domain, start, end);
		}

		return report;
	}
}
