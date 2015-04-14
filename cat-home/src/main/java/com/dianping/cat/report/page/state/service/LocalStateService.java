package com.dianping.cat.report.page.state.service;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.ApiPayload;
import com.dianping.cat.report.ReportBucket;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;

public class LocalStateService extends LocalModelService<StateReport> {

	public static final String ID = StateAnalyzer.ID;

	@Inject
	private ReportBucketManager m_bucketManager;

	public LocalStateService() {
		super(StateAnalyzer.ID);
	}

	@Override
	public String buildReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload)
	      throws Exception {
		StateReport report = super.getReport(period, domain);

		if ((report == null || report.getMachines().isEmpty()) && period.isLast()) {
			long startTime = request.getStartTime();
			report = getReportFromLocalDisk(startTime, domain);

			if (report == null) {
				report = new StateReport(domain);
				report.setStartTime(new Date(startTime));
				report.setEndTime(new Date(startTime + TimeHelper.ONE_HOUR - 1));
			}
		}
		return new StateReportFilter().buildXml(report);
	}

	private StateReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		ReportBucket<String> bucket = null;
		try {
			bucket = m_bucketManager.getReportBucket(timestamp, StateAnalyzer.ID);
			String xml = bucket.findById(domain);

			return xml == null ? null : DefaultSaxParser.parse(xml);
		} finally {
			if (bucket != null) {
				m_bucketManager.closeBucket(bucket);
			}
		}
	}

	public static class StateReportFilter extends com.dianping.cat.consumer.state.model.transform.DefaultXmlBuilder {
		public StateReportFilter() {
			super(true, new StringBuilder(DEFAULT_SIZE));
		}
	}

}
