package com.dianping.cat.report.page.state.service;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.service.BaseHistoricalModelService;
import com.dianping.cat.report.service.ModelRequest;

public class HistoricalStateService extends BaseHistoricalModelService<StateReport> {

	@Inject
	private StateReportService m_reportService;

	public HistoricalStateService() {
		super(StateAnalyzer.ID);
	}

	@Override
	protected StateReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = request.getStartTime();
		StateReport report = getReportFromDatabase(date, domain);

		return report;
	}

	private StateReport getReportFromDatabase(long timestamp, String domain) throws Exception {
		return m_reportService.queryReport(domain, new Date(timestamp), new Date(timestamp + TimeHelper.ONE_HOUR));
	}

}
