package com.dianping.cat.report.page.failure;

import java.util.Date;

import com.dianping.cat.report.ReportManage;
import com.dianping.cat.report.tool.DateUtils;

public class FailureManage extends ReportManage{

	public String getConnectionUrl(String server,String domain, String ip, String duration){
		StringBuffer result = new StringBuffer("http://").append(server).append("/cat/r/service?model=failure");
		if (domain != null && domain.length() > 0) {
			result.append("&").append("domain=").append(domain);
		}
		if (duration != null && duration.length() > 0) {
			result.append("&").append("index=").append(duration);
		}
		if (ip != null && ip.length() > 0) {
				result.append("&").append("ip=").append(ip);
		}
		return result.toString();
	}

	public String getReportStoreFile (long startHour, String domain, String ip){
		long hour = DateUtils.HOUR;
		long second = DateUtils.SECOND;
		StringBuilder result = new StringBuilder();

		result.append(domain).append(ip).append("-").append(DateUtils.SDF_URL.format(new Date(startHour))).append("-")
		      .append(DateUtils.SDF_URL.format(new Date(startHour + hour - second * 60))).append(".html");
		return result.toString();
	}

	public String getReportDisplayTitle (String domain, String ip, long startHour){
		long currentTimeMillis = System.currentTimeMillis();
		long end = startHour + DateUtils.HOUR - DateUtils.SECOND;
		
		if (end > currentTimeMillis) {
			end = currentTimeMillis;
		}
		StringBuilder title = new StringBuilder().append("Domain:").append(domain).append("  IP ").append(ip);
		title.append("  From ").append(DateUtils.SDF_SEG.format(new Date(startHour))).append(" To ").append(
		      DateUtils.SDF_SEG.format(new Date(end)));
		return title.toString();
	}
	
	public String getBaseUrl(String currentDomain, String currentIp, String reportCurrentTime) {
	   StringBuffer urlPrefix = new StringBuffer("?domain=");
		urlPrefix.append(currentDomain).append("&current=").append(reportCurrentTime).append("&ip=").append(currentIp)
		      .append("&method=");
	   return urlPrefix.toString();
   }
}

