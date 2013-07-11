package com.dianping.cat.report.task.metric;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.advanced.MetricConfigManager;
import com.dianping.cat.consumer.core.ProductLineConfigManager;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.report.Baseline;
import com.dianping.cat.report.baseline.BaselineConfig;
import com.dianping.cat.report.baseline.BaselineConfigManager;
import com.dianping.cat.report.baseline.BaselineCreator;
import com.dianping.cat.report.baseline.BaselineService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.spi.ReportTaskBuilder;

public class MetricBaselineReportBuilder implements ReportTaskBuilder {
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

	@Inject
	protected MetricPointParser m_parser;

	private static final int POINT_NUMBER = 60 * 24;

	@Override
	public boolean buildDailyTask(String reportName, String domain, Date reportPeriod) {
		Map<String, MetricReport> reports = new HashMap<String, MetricReport>();
		for (String metricID : m_configManager.getMetricConfig().getMetricItemConfigs().keySet()) {
			buildDailyReportInternal(reports, reportName, metricID, reportPeriod);
		}
		return true;
	}

	protected void buildDailyReportInternal(Map<String, MetricReport> reportMap, String reportName, String metricID,
	      Date reportPeriod) {
		MetricItemConfig metricConfig = m_configManager.getMetricConfig().getMetricItemConfigs().get(metricID);
		String metricKey = metricConfig.getMetricKey();
		String metricDomain = metricConfig.getDomain();
		String productLine = m_productLineConfigManager.queryProductLineByDomain(metricDomain);
		for (MetricType type : MetricType.values()) {
			String key = metricID + ":" + type;
			BaselineConfig baselineConfig = m_baselineConfigManager.queryBaseLineConfig(key);
			List<Integer> days = baselineConfig.getDays();
			List<Double> weights = baselineConfig.getWeights();
			Date targetDate = new Date(reportPeriod.getTime() + baselineConfig.getTargetDate() * TimeUtil.ONE_DAY);
			List<double[]> values = new ArrayList<double[]>();

			for (Integer day : days) {
				List<MetricItem> reports = new ArrayList<MetricItem>();
				Date currentDate = new Date(reportPeriod.getTime() + day * TimeUtil.ONE_DAY);
				for (int i = 0; i < 24; i++) {
					Date start = new Date(currentDate.getTime() + i * TimeUtil.ONE_HOUR);
					Date end = new Date(start.getTime() + TimeUtil.ONE_HOUR);
					String metricReportKey = productLine + ":" + start.getTime();
					MetricReport report = reportMap.get(metricReportKey);
					
					if (report == null) {
						report = m_reportService.queryMetricReport(productLine, start, end);
						reportMap.put(metricReportKey, report);
					}
					MetricItem reportItem = report.findOrCreateMetricItem(metricKey);
					
					reports.add(reportItem);
				}
				double[] oneDayValue = m_parser.queryOneDayData(reports, type);
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
	}

	@Override
	public boolean buildHourlyTask(String reportName, String reportDomain, Date reportPeriod) {
		throw new RuntimeException("Metric base line report don't support hourly report!");
	}

	@Override
	public boolean buildMonthlyTask(String reportName, String reportDomain, Date reportPeriod) {
		throw new RuntimeException("Metric base line report don't support monthly report!");
	}

	@Override
	public boolean buildWeeklyTask(String reportName, String reportDomain, Date reportPeriod) {
		throw new RuntimeException("Metric base line report don't support weekly report!");
	}

}
