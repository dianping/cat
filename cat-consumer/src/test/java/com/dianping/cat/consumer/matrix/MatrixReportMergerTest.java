package com.dianping.cat.consumer.matrix;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.matrix.model.transform.DefaultSaxParser;

public class MatrixReportMergerTest {
	@Ignore
	@Test
	public void testMatrixReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("matrix_analyzer.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("matrix_analyzer.xml"), "utf-8");
		MatrixReport reportOld = DefaultSaxParser.parse(oldXml);
		MatrixReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("matrix_analyzer_merger.xml"), "utf-8");
		MatrixReportMerger merger = new MatrixReportMerger(new MatrixReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), merger.getMatrixReport().toString()
		      .replace("\r", ""));
		Assert.assertEquals("Source report is changed!", newXml.replace("\r", ""), reportNew.toString().replace("\r", ""));
	}
}
