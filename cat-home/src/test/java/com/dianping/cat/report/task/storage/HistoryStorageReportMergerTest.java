package com.dianping.cat.report.task.storage;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.storage.StorageReportMerger;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.consumer.storage.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.storage.model.transform.DefaultXmlBuilder;
import com.dianping.cat.report.page.storage.task.HistoryStorageReportMerger;

public class HistoryStorageReportMergerTest {
	@Test
	public void testMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("storage.xml"), "utf-8");
		StorageReport report1 = DefaultSaxParser.parse(oldXml);
		StorageReport report2 = DefaultSaxParser.parse(oldXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("result.xml"), "utf-8");
		StorageReportMerger merger = new HistoryStorageReportMerger(new StorageReport(report1.getId()));

		report1.accept(merger);
		report2.accept(merger);

		String actual = new DefaultXmlBuilder().buildXml(merger.getStorageReport());

		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), actual.replace("\r", ""));
	}
}
