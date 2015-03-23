package com.dianping.cat.report.page.metric.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.dal.BusinessReport;
import com.dianping.cat.consumer.dal.BusinessReportDao;
import com.dianping.cat.consumer.dal.BusinessReportEntity;
import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Point;
import com.dianping.cat.consumer.metric.model.entity.Segment;
import com.dianping.cat.consumer.metric.model.transform.DefaultNativeParser;
import com.dianping.cat.message.Event;
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
			List<BusinessReport> reports = m_businessReportDao.findAllByPeriodProductLineName(start, group,
			      BusinessReportEntity.READSET_FULL);

			for (BusinessReport report : reports) {
				byte[] content = report.getContent();

				try {
					MetricReport reportModel = DefaultNativeParser.parse(content);

					reportModel.accept(merger);
				} catch (Exception e) {
					Cat.logError(e);
					Cat.getProducer().logEvent("ErrorXML", MetricAnalyzer.ID, Event.SUCCESS,
					      report.getProductLine() + " " + report.getPeriod() + " " + report.getId());
				}
			}
		} catch (DalNotFoundException e) {
			m_logger.warn(this.getClass().getSimpleName() + " " + group + " " + start + " " + end);
		} catch (Exception e) {
			Cat.logError(e);
		}
		MetricReport metricReport = merger.getMetricReport();

		metricReport.setStartTime(start);
		metricReport.setEndTime(new Date(end.getTime() - 1));
		return transform(metricReport);
	}

	@Override
	public MetricReport queryMonthlyReport(String domain, Date start) {
		throw new RuntimeException("Metric report don't support monthly report");
	}

	@Override
	public MetricReport queryWeeklyReport(String domain, Date start) {
		throw new RuntimeException("Metric report don't support weekly report");
	}

	public MetricReport transform(MetricReport report) {
		Map<String, MetricItem> items = report.getMetricItems();

		for (Entry<String, MetricItem> item : items.entrySet()) {
			MetricItem metricItem = item.getValue();
			Map<Integer, Segment> segs = metricItem.getSegments();

			if (segs.size() == 0) {
				Map<Integer, Point> oldPoints = metricItem.findOrCreateAbtest("-1").findOrCreateGroup("").getPoints();

				for (Point point : oldPoints.values()) {
					Segment seg = new Segment();

					seg.setId(point.getId());
					seg.setCount(point.getCount());
					seg.setAvg(point.getAvg());
					seg.setSum(point.getSum());
					segs.put(seg.getId(), seg);
				}
			}
		}

		return report;
	}

}
