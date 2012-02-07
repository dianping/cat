package com.dianping.cat.report.page.failure;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import com.dianping.cat.consumer.failure.FailureReportAnalyzer;
import com.dianping.cat.consumer.failure.model.entity.FailureReport;
import com.dianping.cat.consumer.failure.model.entity.Segment;
import com.dianping.cat.consumer.failure.model.entity.Threads;
import com.dianping.cat.consumer.failure.model.transform.DefaultJsonBuilder;
import com.dianping.cat.tool.DateUtil;
import com.site.helper.Files;

public class FailureData {

	private static final int CURRENT=1;
	
	private static  String getFailureJsonDate(FailureReport report) {
		DefaultJsonBuilder builder = new DefaultJsonBuilder();
		report.accept(builder);
		return builder.getString();
	}

	public static String getFailureDataFromMemory(FailureReportAnalyzer analyzer, String domain, String ip) {
		FailureReport report = analyzer.generateByDomainAndIp(domain, ip);
		return getFailureJsonDate(report);
	}

	public static String getFailureDataFromFile(String basePath, String file) {
		String result = "";
		try {
			result = Files.forIO().readFrom(new File(basePath + file), "utf-8");
			result = result.substring(result.indexOf("<body>") + 6, result.indexOf("</body>"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	
	public static String getFailureDataByNew(int pos, String domain, String ip) {
		long currentTime = System.currentTimeMillis();
		long currentStart = currentTime - currentTime % DateUtil.HOUR;
		long lastStart = currentTime - currentTime % DateUtil.HOUR - DateUtil.HOUR;
		Date date = new Date();
		if (pos == CURRENT) {
			date.setTime(currentStart);
		} else {
			date.setTime(lastStart);
		}
		FailureReport report = new FailureReport();
		
		report.setMachine(ip);
		report.setThreads(new Threads());
		report.setStartTime(date);
		report.setEndTime(new Date(date.getTime() + DateUtil.HOUR - DateUtil.MINUTE));
		report.setDomain(domain);
		long start = report.getStartTime().getTime();
		long endTime = report.getEndTime().getTime();
		Map<String, Segment> segments = report.getSegments();
		for (; start <= endTime; start = start + 60 * 1000) {
			String minute = DateUtil.SDF_SEG.format(new Date(start));
			segments.put(minute, new Segment(minute));
		}
		return getFailureJsonDate(report);
	}

	public String getFailureDataFromRemote() {
		return "";
	}
}
