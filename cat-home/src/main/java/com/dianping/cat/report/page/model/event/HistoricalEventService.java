package com.dianping.cat.report.page.model.event;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.dainping.cat.consumer.dal.report.Report;
import com.dainping.cat.consumer.dal.report.ReportDao;
import com.dainping.cat.consumer.dal.report.ReportEntity;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseHistoricalModelService;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.annotation.Inject;

public class HistoricalEventService extends BaseHistoricalModelService<EventReport> {
	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportDao m_reportDao;

	public HistoricalEventService() {
		super("event");
	}

	@Override
	protected EventReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = Long.parseLong(request.getProperty("date"));
		EventReport report;

		if (isLocalMode()) {
			report = getReportFromLocalDisk(date, domain);
		} else {
			report = getReportFromDatabase(date, domain);
		}

		return report;
	}

	private EventReport getReportFromDatabase(long timestamp, String domain) throws Exception {
		List<Report> reports = m_reportDao.findAllByPeriodDomainTypeName(new Date(timestamp), domain, 1, getName(),
		      ReportEntity.READSET_FULL);
		EventReportMerger merger = new EventReportMerger(new EventReport(domain));

		for (Report report : reports) {
			String xml = report.getContent();
			EventReport model = DefaultSaxParser.parse(xml);
			model.accept(merger);
		}
		EventReport eventReport = merger.getEventReport();

		List<Report> historyReports = m_reportDao.findAllByDomainNameDuration(new Date(timestamp), new Date(
		      timestamp + 60 * 60 * 1000), null, "event", ReportEntity.READSET_DOMAIN_NAME);

		if (eventReport == null) {
			eventReport = new EventReport(domain);
		}
		Set<String> domainNames = eventReport.getDomainNames();
		for (Report report : historyReports) {
			domainNames.add(report.getDomain());
		}
		return eventReport;
	}

	private EventReport getReportFromLocalDisk(long timestamp, String domain) throws Exception {
		Bucket<String> bucket = m_bucketManager.getReportBucket(timestamp, "event");
		String xml = bucket.findById(domain);

		return xml == null ? null : DefaultSaxParser.parse(xml);
	}
}
