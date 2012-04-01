package com.dianping.cat.report.page.model.event;

import java.util.Date;
import java.util.List;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultXmlParser;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.hadoop.dal.ReportEntity;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseHistoricalModelService;
import com.site.lookup.annotation.Inject;

public class HistoricalEventService extends BaseHistoricalModelService<EventReport> {
	@Inject
	private ReportDao m_reportDao;

	public HistoricalEventService() {
		super("event");
	}

	@Override
	protected EventReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = Long.parseLong(request.getProperty("date"));
		List<Report> reports = m_reportDao.findAllByPeriodDomainTypeName(new Date(date), domain, 1, getName(),
		      ReportEntity.READSET_FULL);
		EventReportMerger merger = null;

		for (Report report : reports) {
			String xml = report.getContent();
			EventReport model = new DefaultXmlParser().parse(xml);

			if (merger == null) {
				merger = new EventReportMerger(model);
			} else {
				model.accept(merger);
			}
		}

		return merger.getEventReport();
	}
}
