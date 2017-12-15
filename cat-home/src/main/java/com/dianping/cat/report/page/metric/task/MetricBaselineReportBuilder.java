package com.dianping.cat.report.page.metric.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.logging.LogEnabled;
import org.unidal.lookup.logging.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.config.ProductLineConfigManager;
import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.consumer.metric.MetricConfigManager;
import com.dianping.cat.consumer.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.dal.report.Baseline;
import com.dianping.cat.report.alert.MetricType;
import com.dianping.cat.report.page.metric.service.BaselineService;
import com.dianping.cat.report.page.metric.service.MetricReportService;
import com.dianping.cat.report.task.TaskBuilder;

public class MetricBaselineReportBuilder implements TaskBuilder, LogEnabled {

	public static final String ID = MetricAnalyzer.ID;

	@Inject
	protected MetricReportService m_reportService;

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

	protected Logger m_logger;

	private static final int POINT_NUMBER = 60 * 24;

	protected void buildDailyReportInternal(Map<String, MetricReport> reports, String reportName, String metricId,
	      Date reportPeriod) {
		MetricItemConfig metricConfig = m_configManager.getMetricConfig().getMetricItemConfigs().get(metricId);
		String metricDomain = metricConfig.getDomain();
		String productLine = m_productLineConfigManager.queryProductLineByDomain(metricDomain);

		for (MetricType type : MetricType.values()) {
			String key = metricId + ":" + type;
			BaselineConfig baselineConfig = m_baselineConfigManager.queryBaseLineConfig(key);
			List<Integer> days = baselineConfig.getDays();
			List<Double> weights = baselineConfig.getWeights();
			Date targetDate = new Date(reportPeriod.getTime() + baselineConfig.getTargetDate() * TimeHelper.ONE_DAY);
			List<double[]> values = new ArrayList<double[]>();

			for (Integer day : days) {
				List<MetricItem> metricItems = new ArrayList<MetricItem>();
				Date currentDate = new Date(reportPeriod.getTime() + day * TimeHelper.ONE_DAY);

				for (int i = 0; i < 24; i++) {
					Date start = new Date(currentDate.getTime() + i * TimeHelper.ONE_HOUR);
					Date end = new Date(start.getTime() + TimeHelper.ONE_HOUR);
					String metricReportKey = productLine + ":" + start.getTime();
					MetricReport report = reports.get(metricReportKey);

					if (report == null) {
						report = m_reportService.queryReport(productLine, start, end);

						reports.put(metricReportKey, report);
					}
					MetricItem reportItem = report.findMetricItem(metricId);

					if (reportItem == null) {
						reportItem = new MetricItem(metricId);
					}
					metricItems.add(reportItem);
				}
				double[] oneDayValue = m_parser.buildDailyData(metricItems, type);

				values.add(oneDayValue);
			}

			double[] result = m_baselineCreator.createBaseLine(values, weights, POINT_NUMBER);
			Baseline baseline = new Baseline();
			baseline.setDataInDoubleArray(result);
			baseline.setIndexKey(key);
			baseline.setReportName(reportName);
			baseline.setReportPeriod(targetDate);
			m_baselineService.insertBaseline(baseline);

			Date tomorrow = new Date(reportPeriod.getTime() + TimeHelper.ONE_DAY);
			boolean exist = m_baselineService.hasDailyBaseline(reportName, key, tomorrow);

			if (!exist) {
				Baseline tomorrowBaseline = new Baseline();

				tomorrowBaseline.setDataInDoubleArray(result);
				tomorrowBaseline.setIndexKey(key);
				tomorrowBaseline.setReportName(reportName);
				tomorrowBaseline.setReportPeriod(tomorrow);
				m_baselineService.insertBaseline(tomorrowBaseline);
			}
		}
	}

	@Override
	public boolean buildDailyTask(String reportName, String domain, Date reportPeriod) {
		Map<String, MetricReport> reports = new HashMap<String, MetricReport>();
		for (String metricID : m_configManager.getMetricConfig().getMetricItemConfigs().keySet()) {
			try {
				buildDailyReportInternal(reports, reportName, metricID, reportPeriod);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return true;
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

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

}
