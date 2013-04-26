package com.dianping.cat.report.page.event;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.helper.CatString;

public class EventMergeManager {

	public EventReport mergerAll(EventReport report, String ipAddress, String allName) {
		EventReport temp = mergerAllIp(report, ipAddress);

		return mergerAllName(temp, allName);
	}

	public EventReport mergerAllIp(EventReport report, String ipAddress) {
		if (CatString.ALL.equalsIgnoreCase(ipAddress)) {
			MergeAllMachine all = new MergeAllMachine();

			all.visitEventReport(report);
			report = all.getReport();
		}
		return report;
	}

	private EventReport mergerAllName(EventReport report, String allName) {
		if (CatString.ALL.equalsIgnoreCase(allName)) {
			MergeAllName all = new MergeAllName();

			all.visitEventReport(report);
			report = all.getReport();
		}
		return report;
	}

}
