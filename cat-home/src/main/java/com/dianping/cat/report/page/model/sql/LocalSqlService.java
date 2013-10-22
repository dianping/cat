package com.dianping.cat.report.page.model.sql;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.sql.SqlAnalyzer;
import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;

public class LocalSqlService extends BaseLocalModelService<SqlReport> {
	@Inject
	private ReportService m_reportService;

	public LocalSqlService() {
		super(SqlAnalyzer.ID);
	}

	@Override
	protected SqlReport getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		SqlReport report = super.getReport(request, period, domain);

		if (report == null && period.isLast()) {
			long startTime = request.getStartTime();
			Date start = new Date(startTime);
			Date end = new Date(startTime + TimeUtil.ONE_HOUR);

			report = m_reportService.querySqlReport(domain, start, end);
		}
		return report;
	}
}
