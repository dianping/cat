package com.dianping.cat.report.page.metric.service;

import com.dianping.cat.consumer.metric.model.entity.Abtest;
import com.dianping.cat.consumer.metric.model.entity.Group;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Point;
import com.dianping.cat.consumer.metric.model.entity.Segment;
import com.dianping.cat.consumer.metric.model.entity.StatisticsItem;
import com.dianping.cat.consumer.metric.model.transform.DefaultMerger;

public class MetricReportMerger extends DefaultMerger {

	public MetricReportMerger(MetricReport metricReport) {
		super(metricReport);
	}

	@Override
	protected void mergeAbtest(Abtest to, Abtest from) {
		super.mergeAbtest(to, from);
	}

	@Override
	protected void mergeGroup(Group old, Group group) {
		super.mergeGroup(old, group);
	}

	@Override
	protected void mergeMetricItem(MetricItem old, MetricItem metricItem) {
		old.setType(metricItem.getType());
		super.mergeMetricItem(old, metricItem);
	}

	@Override
	protected void mergeMetricReport(MetricReport old, MetricReport metricReport) {
		super.mergeMetricReport(old, metricReport);
	}

	@Override
	protected void mergePoint(Point old, Point point) {
		old.setCount(old.getCount() + point.getCount());
		old.setSum(old.getSum() + point.getSum());
		if (old.getCount() > 0) {
			old.setAvg(old.getSum() / old.getCount());
		}
	}

	@Override
	protected void mergeSegment(Segment old, Segment segment) {
		old.setCount(old.getCount() + segment.getCount());
		old.setSum(old.getSum() + segment.getSum());
		
		if (old.getCount() > 0) {
			old.setAvg(old.getSum() / old.getCount());
		}
	}

	@Override
	protected void mergeStatisticsItem(StatisticsItem to, StatisticsItem from) {
		to.setCount(to.getCount() + from.getCount());
	}

}
