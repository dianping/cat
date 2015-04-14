package com.dianping.cat.report.page.problem;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.problem.ProblemReportMerger;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.problem.task.HistoryProblemReportMerger;

public class ProblemReportMergerTest {

	@Test
	public void testProblemReportMergeAll() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("ProblemReportOld.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("ProblemReportNew.xml"), "utf-8");
		ProblemReport reportOld = DefaultSaxParser.parse(oldXml);
		ProblemReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("ProblemReportMergeAllResult.xml"),
		      "utf-8");
		ProblemReportMerger merger = new HistoryProblemReportMerger(new ProblemReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		Assert.assertEquals("Check the merge result!", expected.replaceAll("\r", ""), merger.getProblemReport()
		      .toString().replace("\r", ""));
		Assert.assertEquals("Source report is not changed!", newXml.replaceAll("\r", ""), reportNew.toString()
		      .replaceAll("\r", ""));
		Assert.assertEquals("Source report is not changed!", oldXml.replaceAll("\r", ""), reportOld.toString()
		      .replaceAll("\r", ""));
	}

	@Test
	public void testProblemReportMergerSize() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("ProblemMobile.xml"), "utf-8");
		ProblemReport reportOld = DefaultSaxParser.parse(oldXml);
		ProblemReportMerger merger = new HistoryProblemReportMerger(new ProblemReport(reportOld.getDomain()));

		for (int i = 0; i < 24; i++) {
			reportOld.accept(merger);
		}
		ProblemReport problemReport = merger.getProblemReport();
		for (Machine machine : problemReport.getMachines().values()) {
			List<Entry> entries = machine.getEntries();
			for (Entry entry : entries) {
				int size = entry.getThreads().size();
				Assert.assertEquals(0, size);
			}
		}
		Assert.assertEquals(true, (double) problemReport.toString().length() / 1024 / 1024 < 1);
	}
}
