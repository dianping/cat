package com.dianping.cat.report.page.model.matrix;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.matrix.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.model.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseHistoricalModelService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class HistoricalMatrixService extends BaseHistoricalModelService<MatrixReport> {
	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportService m_reportSerivce;

	public HistoricalMatrixService() {
		super("matrix");
	}

	@Override
	protected MatrixReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = Long.parseLong(request.getProperty("date"));
		MatrixReport report;

		if (isLocalMode()) {
			report = getReportFromLocalDisk(date, domain);
		} else {
			report = getReportFromDatabase(date, domain);
		}

		return report;
	}

	private MatrixReport getReportFromDatabase(long timestamp, String domain) throws Exception {
		return m_reportSerivce.queryMatrixReport(domain, new Date(timestamp), new Date(timestamp + TimeUtil.ONE_HOUR));
	}

	private MatrixReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		Bucket<String> bucket = m_bucketManager.getReportBucket(timestamp, "matrix");
		String xml = bucket.findById(domain);

		return xml == null ? null : DefaultSaxParser.parse(xml);
	}
}
