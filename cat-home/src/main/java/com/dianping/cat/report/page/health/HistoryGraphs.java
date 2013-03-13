package com.dianping.cat.report.page.health;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.health.model.entity.HealthReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.HistoryGraphItem;
import com.dianping.cat.report.service.ReportService;

public class HistoryGraphs {

	@Inject
	private ReportService m_reportService;

	public HistoryGraphItem buildHistoryGraph(String domain, Date start, Date end, String reportType, String key) {
		if (reportType.equalsIgnoreCase("day")) {
			return getDateFromHourlyReport(domain, start, end, key);
		} else {
			return getDateFromDailyReport(domain, start, end, key);
		}
	}

	private HistoryGraphItem getDateFromDailyReport(String domain, Date start, Date end, String key) {
		List<HealthReport> reports = new ArrayList<HealthReport>();
		for (long date = start.getTime(); date < end.getTime(); date = date + TimeUtil.ONE_DAY) {
			HealthReport report = getHistoryReport(new Date(date), new Date(date + TimeUtil.ONE_DAY), domain);
			reports.add(report);
		}
		int day = (int) ((end.getTime() - start.getTime()) / TimeUtil.ONE_DAY);
		HistoryGraphItem item = new HistoryGraphItem();
		item.setStart(start).setSize(day).setTitles(key);
		item.addValue(getDateFromReports(reports, day, key));
		return item;
	}

	private HistoryGraphItem getDateFromHourlyReport(String domain, Date start, Date end, String key) {
		List<HealthReport> reports = new ArrayList<HealthReport>();
		for (long date = start.getTime(); date < end.getTime(); date = date + TimeUtil.ONE_HOUR) {
			HealthReport report = getHourlyReport(date, domain);
			reports.add(report);
		}
		HistoryGraphItem item = new HistoryGraphItem();
		item.setStart(start).setSize(24).setTitles(key);
		item.addValue(getDateFromReports(reports, 24, key));
		return item;
	}

	private double[] getDateFromReports(List<HealthReport> reports, int size, String key) {
		double[] result = new double[size];
		int length = reports.size();

		for (int i = 0; i < length; i++) {
			HealthReport report = reports.get(i);
			try {
				if (key.equalsIgnoreCase("UrlResponseTime")) {
					result[i] = report.getUrl().getBaseInfo().getResponseTime();
				} else if (key.equalsIgnoreCase("UrlTotal")) {
					result[i] = report.getUrl().getBaseInfo().getTotal();
				} else if (key.equalsIgnoreCase("UrlErrorTotal")) {
					result[i] = report.getUrl().getBaseInfo().getErrorTotal();
				} else if (key.equalsIgnoreCase("UrlSuccessPercent")) {
					result[i] = report.getUrl().getBaseInfo().getSuccessPercent();
				} else if (key.equalsIgnoreCase("ServiceResponseTime")) {
					result[i] = report.getService().getBaseInfo().getResponseTime();
				} else if (key.equalsIgnoreCase("ServiceTotal")) {
					result[i] = report.getService().getBaseInfo().getTotal();
				} else if (key.equalsIgnoreCase("ServiceErrorTotal")) {
					result[i] = report.getService().getBaseInfo().getErrorTotal();
				} else if (key.equalsIgnoreCase("ServiceSuccessPercent")) {
					result[i] = report.getService().getBaseInfo().getSuccessPercent();
				} else if (key.equalsIgnoreCase("ClientServiceResponseTime")) {
					result[i] = report.getClientService().getBaseInfo().getResponseTime();
				} else if (key.equalsIgnoreCase("ClientServiceTotal")) {
					result[i] = report.getClientService().getBaseInfo().getTotal();
				} else if (key.equalsIgnoreCase("ClientServiceErrorTotal")) {
					result[i] = report.getClientService().getBaseInfo().getErrorTotal();
				} else if (key.equalsIgnoreCase("ClientServiceSuccessPercent")) {
					result[i] = report.getClientService().getBaseInfo().getSuccessPercent();
				} else if (key.equalsIgnoreCase("CallResponseTime")) {
					result[i] = report.getCall().getBaseInfo().getResponseTime();
				} else if (key.equalsIgnoreCase("CallTotal")) {
					result[i] = report.getCall().getBaseInfo().getTotal();
				} else if (key.equalsIgnoreCase("CallErrorTotal")) {
					result[i] = report.getCall().getBaseInfo().getErrorTotal();
				} else if (key.equalsIgnoreCase("CallSuccessPercent")) {
					result[i] = report.getCall().getBaseInfo().getSuccessPercent();
				} else if (key.equalsIgnoreCase("SqlResponseTime")) {
					result[i] = report.getSql().getBaseInfo().getResponseTime();
				} else if (key.equalsIgnoreCase("SqlTotal")) {
					result[i] = report.getSql().getBaseInfo().getTotal();
				} else if (key.equalsIgnoreCase("SqlErrorTotal")) {
					result[i] = report.getSql().getBaseInfo().getErrorTotal();
				} else if (key.equalsIgnoreCase("SqlSuccessPercent")) {
					result[i] = report.getSql().getBaseInfo().getSuccessPercent();
				} else if (key.equalsIgnoreCase("MemResponseTime")) {
					result[i] = report.getMemCache().getBaseCacheInfo().getResponseTime();
				} else if (key.equalsIgnoreCase("MemTotal")) {
					result[i] = report.getMemCache().getBaseCacheInfo().getTotal();
				} else if (key.equalsIgnoreCase("MemHitPercent")) {
					result[i] = report.getMemCache().getBaseCacheInfo().getHitPercent();
				} else if (key.equalsIgnoreCase("KvdbResponseTime")) {
					result[i] = report.getKvdbCache().getBaseCacheInfo().getResponseTime();
				} else if (key.equalsIgnoreCase("KvdbTotal")) {
					result[i] = report.getKvdbCache().getBaseCacheInfo().getTotal();
				} else if (key.equalsIgnoreCase("KvdbHitPercent")) {
					result[i] = report.getKvdbCache().getBaseCacheInfo().getHitPercent();
				} else if (key.equalsIgnoreCase("WebResponseTime")) {
					result[i] = report.getWebCache().getBaseCacheInfo().getResponseTime();
				} else if (key.equalsIgnoreCase("WebTotal")) {
					result[i] = report.getWebCache().getBaseCacheInfo().getTotal();
				} else if (key.equalsIgnoreCase("WebHitPercent")) {
					result[i] = report.getWebCache().getBaseCacheInfo().getHitPercent();
				} else if (key.equalsIgnoreCase("Exceptions")) {
					result[i] = report.getProblemInfo().getExceptions();
				} else if (key.equalsIgnoreCase("LongUrls")) {
					result[i] = report.getProblemInfo().getLongUrls();
				} else if (key.equalsIgnoreCase("LongUrlPercent")) {
					result[i] = report.getProblemInfo().getLongUrlPercent();
				} else if (key.equalsIgnoreCase("LongServices")) {
					result[i] = report.getProblemInfo().getLongServices();
				} else if (key.equalsIgnoreCase("LongServicePercent")) {
					result[i] = report.getProblemInfo().getLongServicePercent();
				} else if (key.equalsIgnoreCase("LongCaches")) {
					result[i] = report.getProblemInfo().getLongCaches();
				} else if (key.equalsIgnoreCase("LongCachePercent")) {
					result[i] = report.getProblemInfo().getLongCachePercent();
				} else if (key.equalsIgnoreCase("LongSqls")) {
					result[i] = report.getProblemInfo().getLongSqls();
				} else if (key.equalsIgnoreCase("LongSqlPercent")) {
					result[i] = report.getProblemInfo().getLongSqlPercent();
				} else if (key.equalsIgnoreCase("MahineNumbers")) {
					result[i] = report.getMachineInfo().getNumbers();
				} else if (key.equalsIgnoreCase("MahineAvgLoad")) {
					result[i] = report.getMachineInfo().getAvgLoad();
				} else if (key.equalsIgnoreCase("MahineAvgMaxLoad")) {
					result[i] = report.getMachineInfo().getAvgMaxLoad();
				} else if (key.equalsIgnoreCase("MahineAvgOldgc")) {
					result[i] = report.getMachineInfo().getAvgOldgc();
				} else if (key.equalsIgnoreCase("MahineAvgMaxOldgc")) {
					result[i] = report.getMachineInfo().getAvgMaxOldgc();
				} else if (key.equalsIgnoreCase("MahineAvgHttp")) {
					result[i] = report.getMachineInfo().getAvgHttp();
				} else if (key.equalsIgnoreCase("MahineAvgMaxHttp")) {
					result[i] = report.getMachineInfo().getAvgMaxHttp();
				} else if (key.equalsIgnoreCase("MahineAvgPigeon")) {
					result[i] = report.getMachineInfo().getAvgPigeon();
				} else if (key.equalsIgnoreCase("MahineAvgMaxPigeon")) {
					result[i] = report.getMachineInfo().getAvgMaxPigeon();
				} else if (key.equalsIgnoreCase("MahineMemoryUsed")) {
					result[i] = report.getMachineInfo().getAvgMemoryUsed();
				} else if (key.equalsIgnoreCase("MahineMaxMemoryUsed")) {
					result[i] = report.getMachineInfo().getAvgMaxMemoryUsed();
				} else {
					result[i] = -1;
				}
			} catch (NullPointerException e) {
				// ignore
			}
		}
		return result;
	}

	private HealthReport getHistoryReport(Date startDate, Date endDate, String domain) {
		return m_reportService.queryHealthReport(domain, startDate, endDate);
	}

	private HealthReport getHourlyReport(long date, String domain) {
		return m_reportService.queryHealthReport(domain, new Date(date), new Date(date + TimeUtil.ONE_HOUR));
	}
}
