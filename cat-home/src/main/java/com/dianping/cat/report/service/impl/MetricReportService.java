package com.dianping.cat.report.service.impl;

import java.util.Date;
import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.advanced.dal.BusinessReport;
import com.dianping.cat.consumer.advanced.dal.BusinessReportDao;
import com.dianping.cat.consumer.advanced.dal.BusinessReportEntity;
import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.transform.DefaultNativeParser;
import com.dianping.cat.message.Event;
import com.dianping.cat.report.page.model.metric.MetricReportMerger;
import com.dianping.cat.report.service.AbstractReportService;

public class MetricReportService extends AbstractReportService<MetricReport> {

	@Inject
	private BusinessReportDao m_businessReportDao;

	@Override
	public MetricReport makeReport(String domain, Date start, Date end) {
		MetricReport report = new MetricReport(domain);

		report.setStartTime(start);
		report.setEndTime(end);
		return report;
	}

	@Override
	public MetricReport queryDailyReport(String domain, Date start, Date end) {
		throw new RuntimeException("Metric report don't support daily report");
	}

	@Override
	public MetricReport queryHourlyReport(String group, Date start, Date end) {
		MetricReportMerger merger = new MetricReportMerger(new MetricReport(group));

		try {
			List<BusinessReport> reports = m_businessReportDao.findAllByProductLineNameDuration(start, end, group,
			      MetricAnalyzer.ID, BusinessReportEntity.READSET_FULL);

			for (BusinessReport report : reports) {
				byte[] content = report.getContent();

				try {
					MetricReport reportModel = DefaultNativeParser.parse(content);
					reportModel.accept(merger);
				} catch (Exception e) {
					Cat.logError(e);
					Cat.getProducer().logEvent("ErrorXML", "metric", Event.SUCCESS,
					      report.getProductLine() + " " + report.getPeriod() + " " + report.getId());
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		MetricReport metricReport = merger.getMetricReport();

		metricReport.setStartTime(start);
		metricReport.setEndTime(new Date(end.getTime() - 1));
		return metricReport;
	}

	@Override
	public MetricReport queryMonthlyReport(String domain, Date start) {
		throw new RuntimeException("Metric report don't support monthly report");
	}

	@Override
	public MetricReport queryWeeklyReport(String domain, Date start) {
		throw new RuntimeException("Metric report don't support weekly report");
	}

}
