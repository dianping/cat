package com.dianping.cat.report.page.statistics.task.system;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.consumer.config.ProductLineConfigManager;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.system.entity.SystemReport;
import com.dianping.cat.home.system.transform.DefaultNativeBuilder;
import com.dianping.cat.report.page.metric.service.MetricReportService;
import com.dianping.cat.report.page.statistics.service.SystemReportService;
import com.dianping.cat.report.task.TaskBuilder;

public class SystemReportBuilder implements TaskBuilder {

	@Inject
	private SystemReportService m_reportService;

	@Inject
	protected MetricReportService m_metricReportService;
	
	@Inject
	private ProductLineConfigManager m_configManager;

	public static final String ID = Constants.REPORT_SYSTEM;

	public static List<String> KEYS = Arrays.asList("cpuUsage");

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		SystemReport report = buildSystemReport(period);
		DailyReport dailyReport = new DailyReport();

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
		long start = startTime.getTime();
		Date endTime = TimeHelper.addDays(startTime, 1);
		long end = endTime.getTime();
		SystemReport report = new SystemReport();

		report.setStartTime(startTime);
		report.setEndTime(endTime);

		SystemReportStatistics statistics = new SystemReportStatistics(start, report, KEYS);

		for (String productLine : m_configManager.querySystemProductLines().keySet()) {
			for (long s = start; s < end; s += TimeHelper.ONE_HOUR) {
				Date sDate = new Date(s);
				Date eDate = new Date(s + TimeHelper.ONE_HOUR);

				try {
					MetricReport r = m_metricReportService.queryReport(productLine, sDate, eDate);

					statistics.visitMetricReport(r);
				} catch (Exception e) {
					Cat.logError(productLine + " system report visitor error", e);
				}
			}
		}
		return report;
	}

	public MetricReport querySystemReport(String product, Map<String, String> properties, Date start) {
		long time = start.getTime();
		Date end = new Date(time + TimeHelper.ONE_HOUR);
		MetricReport report = m_metricReportService.queryReport(product, start, end);

		return report;
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
