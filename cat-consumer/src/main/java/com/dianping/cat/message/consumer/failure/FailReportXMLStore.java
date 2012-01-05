package com.dianping.cat.message.consumer.failure;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.dianping.cat.consumer.model.failure.entity.FailureReport;
import com.dianping.cat.consumer.model.failure.transform.DefaultParser;
import com.site.helper.Files;

public class FailReportXMLStore implements FailReportStore {
	@Override
	public void storeFailureReport(FailureReport report) {
		String xml;
		try {
			xml = Files.forIO().readFrom(
					getClass().getResourceAsStream("/logView.xml"), "utf-8");
			report = new DefaultParser().parse(xml);
			System.out.println(report);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
		}
	}
}
