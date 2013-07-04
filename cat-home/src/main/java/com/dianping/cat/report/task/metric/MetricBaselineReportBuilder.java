package com.dianping.cat.report.task.metric;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.advanced.MetricConfigManager;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Point;
import com.dianping.cat.home.dal.report.Baseline;
import com.dianping.cat.report.baseline.BaselineConfig;
import com.dianping.cat.report.baseline.BaselineConfigManager;
import com.dianping.cat.report.baseline.BaselineCreator;
import com.dianping.cat.report.baseline.BaselineService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.AbstractReportBuilder;
import com.dianping.cat.report.task.spi.ReportBuilder;

public class MetricBaselineReportBuilder extends AbstractReportBuilder implements ReportBuilder {
	@Inject
	protected ReportService m_reportService;

	@Inject
	protected BaselineCreator m_baselineCreator;

	@Inject
	protected BaselineConfigManager m_baselineConfigManager;

	@Inject
	protected BaselineService m_baselineService;
	
	@Inject
	protected MetricConfigManager m_configManager;

	private static final long ONE_HOUR_IN_MILISECONDS = 3600 * 1000;

	private static final long ONE_DAY_IN_MILISECONDS = 3600 * 1000;

	private static final int POINT_NUMBER = 60 * 24;

	private static enum METRIC_TYPE {
		COUNT, AVG, SUM
	};

	@Override
	public boolean buildDailyReport(String reportName, String reportDomain, Date reportPeriod) {
		reportPeriod = TaskHelper.yesterdayZero(reportPeriod);
		for(MetricItemConfig metricConfig:m_configManager.getMetricConfig().getMetricItemConfigs().values()){
			String metricKey = metricConfig.getMetricKey();
			for (METRIC_TYPE type : METRIC_TYPE.values()) {
				String key = reportName + "+" + reportDomain + "+" + metricKey + "+" + type;
				BaselineConfig baselineConfig = m_baselineConfigManager.queryBaseLineConfig(key);
				List<Integer> days = baselineConfig.getDays();
				List<Double> weights = baselineConfig.getWeights();
				Date targetDate = new Date(reportPeriod.getTime() + baselineConfig.getTargetDate() * ONE_DAY_IN_MILISECONDS);
				List<double[]> valueList = new ArrayList<double[]>();
				for (Integer day : days) {
					List<MetricItem> reports = new ArrayList<MetricItem>();
					Date relatedHour = new Date(reportPeriod.getTime() + day * ONE_DAY_IN_MILISECONDS);
					for (int i = 0; i < 24; i++) {
						Date hourEnd = new Date(relatedHour.getTime() + ONE_HOUR_IN_MILISECONDS);
						MetricReport report = m_reportService.queryMetricReport(reportDomain, relatedHour, hourEnd);
						MetricItem reportItem = report.getMetricItems().get(metricKey);
						relatedHour = hourEnd;
						reports.add(reportItem);
					}
					double[] oneDayValue = getOneDayValues(reports, type);
					valueList.add(oneDayValue);
				}

				double[] result = m_baselineCreator.createBaseLine(valueList, weights, new HashSet<Integer>(),POINT_NUMBER);
				Baseline baseline = new Baseline();
				baseline.setDataInDoubleArray(result);
				baseline.setIndexKey(key);
				baseline.setReportName(reportName);
				baseline.setReportPeriod(targetDate);
				m_baselineService.insertBaseline(baseline);
			}
		}
		return true;
	}

	private double[] getOneDayValues(List<MetricItem> reports, METRIC_TYPE type) {
		double[] values = new double[POINT_NUMBER];
		int hour = 0;
		for (MetricItem report : reports) {
//			System.out.println(report.getAbtests() + "==" + report.getAbtests().get("-1") 
//					+ "==" + report.getAbtests().get("-1").getGroups()
//					+ "==" + report.getAbtests().get("-1").getGroups().get(""));
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
			hour ++;
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
