package com.dianping.cat.report.page.event.transform;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.event.model.entity.EventReport;

public class EventMergeHelper {

	public EventReport mergeAllIps(EventReport report, String ipAddress) {
		if (Constants.ALL.equalsIgnoreCase(ipAddress)) {
			AllMachineMerger all = new AllMachineMerger();

			all.visitEventReport(report);
			report = all.getReport();
		}
		return report;
	}

	private EventReport mergeAllNames(EventReport report, String allName) {
		if (Constants.ALL.equalsIgnoreCase(allName)) {
			AllNameMerger all = new AllNameMerger();

			all.visitEventReport(report);
			report = all.getReport();
		}
		return report;
	}

	public EventReport mergeAllNames(EventReport report, String ipAddress, String allName) {
		EventReport temp = mergeAllIps(report, ipAddress);

		return mergeAllNames(temp, allName);
	}

}
