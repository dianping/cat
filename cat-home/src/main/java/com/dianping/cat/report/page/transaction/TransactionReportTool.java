package com.dianping.cat.report.page.transaction;

import java.util.List;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultXmlParser;

public class TransactionReportTool {
	public static TransactionReport parseXML(String xml) {
		DefaultXmlParser parser = new DefaultXmlParser();
		try {
			return parser.parse(xml);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static TransactionReport merge(List<TransactionReport> reports) {
		TransactionReport result = reports.get(0);
		for (int i = 1; i < reports.size(); i++) {
			merge(result, reports.get(i));
		}
		return result;
	}

	private static void merge(TransactionReport targetReport, TransactionReport mergeReport) {
		
	}
}
