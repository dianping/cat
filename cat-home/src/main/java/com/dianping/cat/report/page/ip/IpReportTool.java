package com.dianping.cat.report.page.ip;

import java.util.List;

import com.dianping.cat.consumer.ip.model.entity.IpReport;
import com.dianping.cat.consumer.ip.model.transform.DefaultParser;

public class IpReportTool {
	
	public static IpReport parseXML(String xml) {
		DefaultParser parser = new DefaultParser();
		try {
			return parser.parse(xml);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static IpReport merge(List<IpReport> reports) {
		IpReport result = reports.get(0);
		for (int i = 1; i < reports.size(); i++) {
			merge(result, reports.get(i));
		}
		return result;
	}

	private static void merge(IpReport targetReport, IpReport mergeReport) {
		
	}
}
