package com.dianping.cat.tool;

import java.text.ParseException;
import java.util.Date;


public class BaseReportTool {

	public static String getReportIndex(long currentHourStart, long computeStart) {
		if (computeStart == currentHourStart) {
			return Constant.MEMORY_CURRENT;
		} else if (computeStart == currentHourStart - DateUtil.HOUR) {
			return Constant.MEMORY_LAST;
		}
		return Constant.FILE;
	}
	
	public static long computeReportStart(long currentHourStart, String inputStart, int method) {
		long hour = DateUtil.HOUR;
		long startLong = currentHourStart;
		if (inputStart != null) {
			try {
				Date reportStartDate = DateUtil.SDF_URL.parse(inputStart);
				startLong = reportStartDate.getTime();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else {
			inputStart = DateUtil.SDF_URL.format(currentHourStart);
		}

		long reportStart = startLong + method * hour;
		if (reportStart > currentHourStart) {
			reportStart = currentHourStart;
		}
		return reportStart;
	}

	// failure has the ip info, other have none
	public static String getConnectionUrl(String reportType, String server, String domain, String ip, String index) {
		StringBuffer result = new StringBuffer("http://").append(server).append("/cat/r/service?model=").append(
		      reportType);
		if (domain != null && domain.length() > 0) {
			result.append("&").append("domain=").append(domain);
		}
		if (index != null && index.length() > 0) {
			result.append("&").append("index=").append(index);
		}
		if (reportType.equals(Constant.FAILURE)) {
			if (ip != null && ip.length() > 0) {
				result.append("&").append("ip=").append(ip);
			}
		}
		return result.toString();
	}

	public static String getReportName(long computeStart, String domain, String ip) {
		long hour = DateUtil.HOUR;
		long second = DateUtil.SECOND;
		StringBuilder result = new StringBuilder();

		result.append(domain).append(ip).append("-").append(DateUtil.SDF_URL.format(new Date(computeStart))).append("-")
		      .append(DateUtil.SDF_URL.format(new Date(computeStart + hour - second * 60))).append(".html");
		return result.toString();
	}

	// failure has the ip info, other have none
	public static String getReportTitle(String reportType, String domain, String ip, long start) {
		long currentTimeMillis = System.currentTimeMillis();
		long end = start + DateUtil.HOUR - DateUtil.SECOND;
		if (end > currentTimeMillis) {
			end = currentTimeMillis;
		}

		StringBuilder title = new StringBuilder().append("Domain:").append(domain);
		if (reportType.equals(Constant.FAILURE)) {
			title.append("  IP ").append(ip);
		}
		title.append("  From ").append(DateUtil.SDF_SEG.format(new Date(start))).append(" To ").append(
		      DateUtil.SDF_SEG.format(new Date(end)));
		return title.toString();
	}
}
