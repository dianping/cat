package com.dianping.cat.report;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.Date;

import com.dianping.cat.report.tool.Constants;
import com.dianping.cat.report.tool.DateUtils;
import com.site.helper.Files;

public class ReportManage {

	public String getRemotePageContent(String urlStr) {
		try {
			URL url = new URL(urlStr);
			URLConnection URLconnection = url.openConnection();
			HttpURLConnection httpConnection = (HttpURLConnection) URLconnection;
			int responseCode = httpConnection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				InputStream input = httpConnection.getInputStream();
				return Files.forIO().readFrom(input, "utf-8");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}

	public String getReportStartType(long currentHourStart, long reportRealStart) {
		if (reportRealStart == currentHourStart) {
			return Constants.MEMORY_CURRENT;
		} else if (reportRealStart == currentHourStart - DateUtils.HOUR) {
			return Constants.MEMORY_LAST;
		}
		return Constants.FILE;
	}

	public long computeReportStartHour(long currentHourStart, String inputStart, int changeValue) {
		long startLong = currentHourStart;
		if (inputStart != null) {
			try {
				Date reportStartDate = DateUtils.SDF_URL.parse(inputStart);
				startLong = reportStartDate.getTime();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else {
			inputStart = DateUtils.SDF_URL.format(currentHourStart);
		}

		long reportStart = startLong + changeValue * DateUtils.HOUR;
		if (reportStart > currentHourStart) {
			reportStart = currentHourStart;
		}
		return reportStart;
	}
}