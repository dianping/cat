package com.dianping.cat.report.task.system;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.productline.ProductLineConfigManager;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.system.entity.SystemReport;
import com.dianping.cat.home.system.transform.DefaultNativeBuilder;
import com.dianping.cat.report.graph.metric.CachedMetricReportService;
import com.dianping.cat.report.page.system.graph.SystemReportConvertor;
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

	public static List<String> KEYS = Arrays.asList("cpuUsage");

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
		long start = startTime.getTime();
		Date endTime = TimeHelper.addDays(startTime, 1);
		long end = endTime.getTime();
		SystemReport report = new SystemReport();

		report.setStartTime(startTime);
		report.setEndTime(endTime);

		SystemReportStatistics statistics = new SystemReportStatistics(start, report, KEYS);
		Map<String, String> properties = new HashMap<String, String>();

		properties.put("type", "system");
		properties.put("ip", Constants.ALL);

		for (String productLine : m_configManager.querySystemProductLines().keySet()) {
			for (long s = start; s < end; s += TimeHelper.ONE_HOUR) {
				MetricReport r = querySystemReport(productLine, properties, new Date(s));

				r.accept(statistics);
			}
		}
		return report;
	}

	public MetricReport querySystemReport(String product, Map<String, String> properties, Date start) {
		long time = start.getTime();
		Date end = new Date(time + TimeHelper.ONE_HOUR);
		MetricReport report = m_reportService.queryMetricReport(product, start, end);

		String type = properties.get("type");
		String ipAddrsStr = properties.get("ip");
		Set<String> ipAddrs = null;

		if (!Constants.ALL.equalsIgnoreCase(ipAddrsStr)) {
			String[] ipAddrsArray = ipAddrsStr.split("_");
			ipAddrs = new HashSet<String>(Arrays.asList(ipAddrsArray));
		}
		SystemReportConvertor convert = new SystemReportConvertor(type, ipAddrs);

		convert.visitMetricReport(report);
		return convert.getReport();
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
