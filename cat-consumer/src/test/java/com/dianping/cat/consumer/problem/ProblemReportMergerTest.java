package com.dianping.cat.consumer.problem;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;

public class ProblemReportMergerTest {
	@Test
	public void testProblemReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("ProblemReportOld.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("ProblemReportNew.xml"), "utf-8");
		ProblemReport reportOld = DefaultSaxParser.parse(oldXml);
		ProblemReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("ProblemReportMergeResult.xml"), "utf-8");
		ProblemReportMerger merger = new ProblemReportMerger(new ProblemReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		Assert.assertEquals("Source report is not changed!", newXml.replace("\r", ""), reportNew.toString().replace("\r", ""));
		Assert.assertEquals("Source report is not changed!", oldXml.replace("\r", ""), reportOld.toString().replace("\r", ""));
		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), merger.getProblemReport().toString()
		      .replace("\r", ""));

	}

	@Test
	public void testMergeList() {
		ProblemReportMerger merger = new ProblemReportMerger(new ProblemReport("cat"));
		List<String> list1 = buildList1();
		merger.mergeList(list1, buildList2(), 10);
		Assert.assertEquals(10, list1.size());

		list1 = buildList1();
		merger.mergeList(list1, buildList2(), 25);
		Assert.assertEquals(25, list1.size());

		list1 = buildList1();
		merger.mergeList(list1, buildList2(), 30);
		Assert.assertEquals(30, list1.size());
		
		list1 = buildList1();
		merger.mergeList(list1, buildList2(), 40);
		Assert.assertEquals(30, list1.size());
	}

	private List<String> buildList1() {
		List<String> list = new ArrayList<String>();

		for (int i = 0; i < 10; i++) {
			list.add("Str" + i);
		}
		return list;
	}

	private List<String> buildList2() {
		List<String> list = new ArrayList<String>();

		for (int i = 0; i < 20; i++) {
			list.add("Str" + i);
		}

		return list;
	}

}
