package com.dianping.cat.report.page.transaction;

import java.util.Date;

import com.dianping.cat.report.ReportManager;
import com.dianping.cat.report.tool.DateUtils;

public class TransactionManager extends ReportManager{

	public String getConnectionUrl(String server,String domain, String duration){
		StringBuffer result = new StringBuffer("http://").append(server).append("/cat/r/service?model=transaction");
		if (domain != null && domain.length() > 0) {
			result.append("&").append("domain=").append(domain);
		}
		if (duration != null && duration.length() > 0) {
			result.append("&").append("index=").append(duration);
		}
		return result.toString();
	}

	public String getReportStoreFile (long startHour, String domain){
		long hour = DateUtils.HOUR;
		long second = DateUtils.SECOND;
		StringBuilder result = new StringBuilder();

		result.append(domain).append("-").append(DateUtils.SDF_URL.format(new Date(startHour))).append("-")
		      .append(DateUtils.SDF_URL.format(new Date(startHour + hour - second * 60))).append(".html");
		return result.toString();
	}

	public String getReportDisplayTitle (String domain,  long startHour){
		long currentTimeMillis = System.currentTimeMillis();
		long end = startHour + DateUtils.HOUR - DateUtils.SECOND;
		
		if (end > currentTimeMillis) {
			end = currentTimeMillis;
		}
		StringBuilder title = new StringBuilder().append("Domain:").append(domain);
		title.append("  From ").append(DateUtils.SDF_SEG.format(new Date(startHour))).append(" To ").append(
		      DateUtils.SDF_SEG.format(new Date(end)));
		return title.toString();
	}
	
	public String getBaseUrl(String currentDomain, String reportCurrentTime) {
	   StringBuffer urlPrefix = new StringBuffer("?domain=");
		urlPrefix.append(currentDomain).append("&current=").append(reportCurrentTime).append("&method=");
	   return urlPrefix.toString();
   }
}

