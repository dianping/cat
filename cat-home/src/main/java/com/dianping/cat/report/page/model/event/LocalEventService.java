package com.dianping.cat.report.page.model.event;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;

public class LocalEventService extends BaseLocalModelService<EventReport> {

	@Inject
	private ReportService m_reportService;
	
	public LocalEventService() {
		super(EventAnalyzer.ID);
	}

	@Override
	protected EventReport getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		EventReport report = super.getReport(request, period, domain);

		if (report == null && period.isLast()) {
			long startTime = request.getStartTime();
			Date start = new Date(startTime);
			Date end = new Date(startTime + TimeUtil.ONE_HOUR);

			report = m_reportService.queryEventReport(domain, start, end);
		}

		return report;
	}
}
