package com.dianping.cat.analyzer.matrix;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.analyzer.matrix.MatrixReportFilter;
import com.dianping.cat.matrix.model.entity.MatrixReport;
import com.dianping.cat.matrix.model.transform.DefaultSaxParser;

public class MatrixModelTest {
	@Test
	public void testModel() throws Exception {
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("matrix.xml"), "utf-8");
		MatrixReport report = DefaultSaxParser.parse(source);
		MatrixReportFilter filter = new MatrixReportFilter();

		filter.setMaxSize(10);
		report.accept(filter);
		String expected1 = Files.forIO().readFrom(getClass().getResourceAsStream("matrix_result.xml"), "utf-8");

		Assert.assertEquals(expected1.replaceAll("\r", ""), report.toString().replaceAll("\r", ""));
	}
}
