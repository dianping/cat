package com.dianping.cat.report.page.model.metric;

import com.dianping.cat.consumer.metric.model.entity.Group;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Point;
import com.dianping.cat.consumer.metric.model.transform.DefaultMerger;

public class MetricReportMerger extends DefaultMerger {

	public MetricReportMerger(MetricReport metricReport) {
		super(metricReport);
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

}
