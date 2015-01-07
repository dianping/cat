package com.dianping.cat.report.task.system;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.productline.ProductLineConfigManager;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.system.entity.SystemReport;
import com.dianping.cat.home.system.transform.DefaultNativeBuilder;
import com.dianping.cat.report.graph.metric.CachedMetricReportService;
import com.dianping.cat.report.page.model.metric.MetricReportMerger;
import com.dianping.cat.report.service.ReportServiceManager;
import com.dianping.cat.report.task.spi.TaskBuilder;

public class SystemReportBuilder implements TaskBuilder {

	@Inject
	private ReportServiceManager m_reportService;

	@Inject
	private ProductLineConfigManager m_configManager;

	@Inject
	protected CachedMetricReportService m_metricReportService;

	public static final String ID = Constants.REPORT_SYSTEM;

	public static List<String> KEYS = Arrays.asList("sysCpu", "userCpu", "cpuUsage");

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		SystemReport report = buildSystemReport(period);
		DailyReport dailyReport = new DailyReport();

		dailyReport.setContent("");
		dailyReport.setIp("");
		dailyReport.setDomain(Constants.CAT);
		dailyReport.setCreationDate(new Date());
		dailyReport.setName(name);
		dailyReport.setPeriod(period);
		dailyReport.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(report);

		return m_reportService.insertDailyReport(dailyReport, binaryContent);
	}

	private SystemReport buildSystemReport(Date startTime) {
		Date endTime = TimeHelper.addDays(startTime, 1);
		long start = startTime.getTime();
		long end = endTime.getTime();
		SystemReport report = new SystemReport();

		report.setStartTime(startTime);
		report.setEndTime(endTime);

		buildReport(start, end, report, true);
		buildReport(start + TimeHelper.ONE_HOUR * 16, start + TimeHelper.ONE_HOUR * 18, report, false);
		return report;
	}

	private void buildReport(long start, long end, SystemReport report, boolean dayReport) {
		SystemReportStatistics statistics = new SystemReportStatistics(report, dayReport, KEYS);
		Map<String, String> properties = new HashMap<String, String>();

		properties.put("type", "system");
		properties.put("ip", Constants.ALL);
		properties.put("metricType", Constants.METRIC_SYSTEM_MONITOR);

		for (String productLine : m_configManager.querySystemProductLines().keySet()) {
			MetricReport productReport = new MetricReport(productLine);
			MetricReportMerger merger = new MetricReportMerger(productReport);

			for (long s = start; s < end; s += TimeHelper.ONE_HOUR) {
				MetricReport r = m_metricReportService.querySystemReport(productLine, properties, new Date(s));
				r.accept(merger);
			}
			productReport.accept(statistics);
		}
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		throw new RuntimeException(getID() + " don't support hourly update");
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		throw new RuntimeException(getID() + " don't support monthly update");
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		throw new RuntimeException(getID() + " don't support weekly update");
	}

	private String getID() {
		return ID;
	}

}
