package com.dianping.cat.report.page.model.state;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;

public class LocalStateService extends BaseLocalModelService<StateReport> {
	@Inject
	private ReportService m_reportService;

	public LocalStateService() {
		super(StateAnalyzer.ID);
	}

	@Override
	protected StateReport getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		StateReport report = super.getReport(request, period, domain);

		if (report == null && period.isLast()) {
			long startTime = request.getStartTime();
			Date start = new Date(startTime);
			Date end = new Date(startTime + TimeUtil.ONE_HOUR);

			report = m_reportService.queryStateReport(domain, start, end);
		
		}
		return report;
	}
}
