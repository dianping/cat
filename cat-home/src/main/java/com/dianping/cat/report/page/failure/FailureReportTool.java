package com.dianping.cat.report.page.failure;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dianping.cat.consumer.failure.model.entity.Entry;
import com.dianping.cat.consumer.failure.model.entity.FailureReport;
import com.dianping.cat.consumer.failure.model.entity.Segment;
import com.dianping.cat.consumer.failure.model.entity.Threads;
import com.dianping.cat.consumer.failure.model.transform.DefaultParser;

public class FailureReportTool {
	
	public static FailureReport parseXML(String xml) {
		DefaultParser parser = new DefaultParser();
		try {
			return parser.parse(xml);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static FailureReport merge(List<FailureReport> reports) {
		FailureReport result = reports.get(0);
		for (int i = 1; i < reports.size(); i++) {
			merge(result, reports.get(i));
		}
		return result;
	}

	private static void merge(FailureReport targetReport, FailureReport mergeReport) {
		Threads mergeThreads = mergeReport.getThreads();
		Threads targetTheads = targetReport.getThreads();
		Map<String, Segment> mergeSegments = mergeReport.getSegments();
		Map<String, Segment> targetSegments = targetReport.getSegments();

		if (mergeThreads != null) {
			Set<String> threadSet = mergeThreads.getThreads();
			Set<String> targetThreadSets = targetTheads.getThreads();
			for (String temp : threadSet) {
				targetThreadSets.add(temp);
			}
		}
		Iterator<String> keyItertor = mergeSegments.keySet().iterator();

		while (keyItertor.hasNext()) {
			String key = keyItertor.next();
			Segment mergeSegment = mergeSegments.get(key);
			Segment targetSegment = targetSegments.get(key);
			List<Entry> mergeEntrys = mergeSegment.getEntries();
			List<Entry> targetEntrys = targetSegment.getEntries();

			for (Entry entry : mergeEntrys) {
				targetEntrys.add(entry);
			}
		}
	}
}
