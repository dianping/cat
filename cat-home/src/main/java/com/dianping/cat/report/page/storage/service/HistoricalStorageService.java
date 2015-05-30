package com.dianping.cat.report.page.storage.service;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.storage.StorageAnalyzer;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.storage.task.StorageReportService;
import com.dianping.cat.report.service.BaseHistoricalModelService;
import com.dianping.cat.report.service.ModelRequest;

public class HistoricalStorageService extends BaseHistoricalModelService<StorageReport> {

	@Inject
	private StorageReportService m_reportService;

	public HistoricalStorageService() {
		super(StorageAnalyzer.ID);
	}

	@Override
	protected StorageReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = request.getStartTime();
		StorageReport report = getReportFromDatabase(date, domain);

		return report;
	}

	private StorageReport getReportFromDatabase(long timestamp, String id) throws Exception {
		return m_reportService.queryReport(id, new Date(timestamp), new Date(timestamp + TimeHelper.ONE_HOUR));
	}

}
