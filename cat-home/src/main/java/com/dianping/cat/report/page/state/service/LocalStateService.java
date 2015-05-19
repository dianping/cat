package com.dianping.cat.report.page.state.service;

import java.util.Date;
import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.StateReportMerger;
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
		List<StateReport> reports = super.getReport(period, domain);
		StateReport report = null;

		if (reports != null) {
			report = new StateReport(domain);
			StateReportMerger merger = new StateReportMerger(report);

			for (StateReport tmp : reports) {
				tmp.accept(merger);
			}
		}
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
		StateReport report = new StateReport(domain);
		StateReportMerger merger = new StateReportMerger(report);

		report.setStartTime(new Date(timestamp));
		report.setEndTime(new Date(timestamp + TimeHelper.ONE_HOUR - 1));

		for (int i = 0; i < ANALYZER_COUNT; i++) {
			ReportBucket bucket = null;
			try {
				bucket = m_bucketManager.getReportBucket(timestamp, StateAnalyzer.ID, i);
				String xml = bucket.findById(domain);

				if (xml != null) {
					StateReport tmp = DefaultSaxParser.parse(xml);

					tmp.accept(merger);
				}
			} finally {
				if (bucket != null) {
					m_bucketManager.closeBucket(bucket);
				}
			}
		}
		return report;
	}

	public static class StateReportFilter extends com.dianping.cat.consumer.state.model.transform.DefaultXmlBuilder {
		public StateReportFilter() {
			super(true, new StringBuilder(DEFAULT_SIZE));
		}
	}

}
