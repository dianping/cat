package com.dianping.cat.report.task.metric;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.advanced.MetricConfigManager;
import com.dianping.cat.consumer.core.ProductLineConfigManager;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Point;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.report.Baseline;
import com.dianping.cat.report.baseline.BaselineConfig;
import com.dianping.cat.report.baseline.BaselineConfigManager;
import com.dianping.cat.report.baseline.BaselineCreator;
import com.dianping.cat.report.baseline.BaselineService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.spi.AbstractReportBuilder;
import com.dianping.cat.report.task.spi.ReportBuilder;

public class MetricBaselineReportBuilder extends AbstractReportBuilder implements ReportBuilder {
	@Inject
	protected ReportService m_reportService;
	
	@Inject
	protected MetricConfigManager m_configManager;

	@Inject
	protected ProductLineConfigManager m_productLineConfigManager;

	@Inject
	protected BaselineCreator m_baselineCreator;

	@Inject
	protected BaselineConfigManager m_baselineConfigManager;

	@Inject
	protected BaselineService m_baselineService;

	private static final int POINT_NUMBER = 60 * 24;

	private static enum METRIC_TYPE {
		COUNT, AVG, SUM
	};

	@Override
	public boolean buildDailyReport(String reportName, String metricID, Date reportPeriod) {
		MetricItemConfig metricConfig = m_configManager.getMetricConfig().getMetricItemConfigs().get(metricID);
		String metricKey = metricConfig.getMetricKey();
		String metricDomain = metricConfig.getDomain();
		String productLine = m_productLineConfigManager.queryProductLineByDomain(metricDomain);
		for (METRIC_TYPE type : METRIC_TYPE.values()) {
			String key = reportName + "+" + metricID + "+" + type;
			BaselineConfig baselineConfig = m_baselineConfigManager.queryBaseLineConfig(key);
			List<Integer> days = baselineConfig.getDays();
			List<Double> weights = baselineConfig.getWeights();
			Date targetDate = new Date(reportPeriod.getTime() + baselineConfig.getTargetDate() * TimeUtil.ONE_DAY);
			List<double[]> values = new ArrayList<double[]>();
			for (Integer day : days) {
				List<MetricItem> reports = new ArrayList<MetricItem>();
				Date relatedHour = new Date(reportPeriod.getTime() + day * TimeUtil.ONE_DAY);
				for (int i = 0; i < 24; i++) {
					Date hourEnd = new Date(relatedHour.getTime() + TimeUtil.ONE_HOUR);
					MetricReport report = m_reportService.queryMetricReport(productLine, relatedHour, hourEnd);
					MetricItem reportItem = report.getMetricItems().get(metricKey);
					relatedHour = hourEnd;
					reports.add(reportItem);
				}
				double[] oneDayValue = getOneDayValues(reports, type);
				values.add(oneDayValue);
			}

			double[] result = m_baselineCreator.createBaseLine(values, weights, new HashSet<Integer>(), POINT_NUMBER);
			Baseline baseline = new Baseline();
			baseline.setDataInDoubleArray(result);
			baseline.setIndexKey(key);
			baseline.setReportName(reportName);
			baseline.setReportPeriod(targetDate);
			m_baselineService.insertBaseline(baseline);
		}

		return true;
	}

	private double[] getOneDayValues(List<MetricItem> reports, METRIC_TYPE type) {
		double[] values = new double[POINT_NUMBER];
		for (int i = 0; i < POINT_NUMBER; i++) {
			values[i] = -1;
		}
		int hour = 0;
		for (MetricItem report : reports) {
			try {
				Map<Integer, Point> map = report.getAbtests().get("-1").getGroups().get("").getPoints();
				for (Integer minute : map.keySet()) {
					int index = hour * 60 + minute;
					if (index >= 0 && index < POINT_NUMBER) {
						Point point = map.get(minute);
						if (type == METRIC_TYPE.AVG) {
							values[index] = point.getAvg();
						} else if (type == METRIC_TYPE.COUNT) {
							values[index] = (double) point.getCount();
						} else if (type == METRIC_TYPE.SUM) {
							values[index] = point.getSum();
						}
					}
				}
			} catch (NullPointerException e) {
				// Do Nothing
			}
			hour++;
		}
		return values;
	}

	@Override
	public boolean buildHourReport(String reportName, String reportDomain, Date reportPeriod) {
		throw new RuntimeException("Metric base line report don't support hourly report!");
	}

	@Override
	public boolean buildMonthReport(String reportName, String reportDomain, Date reportPeriod) {
		throw new RuntimeException("Metric base line report don't support monthly report!");
	}

	@Override
	public boolean buildWeeklyReport(String reportName, String reportDomain, Date reportPeriod) {
		throw new RuntimeException("Metric base line report don't support weekly report!");
	}

}
