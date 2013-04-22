package com.dianping.cat.consumer.advanced;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.matrix.model.transform.DefaultSaxParser;

public class MatrixModelTest {
	@Test
	public void testModel() throws Exception {
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("matrix.xml"), "utf-8");
		MatrixReport report = DefaultSaxParser.parse(source);
		MatrixReportFilter f1 = new MatrixReportFilter(1);

		report.accept(f1);
		String expected1 = Files.forIO().readFrom(getClass().getResourceAsStream("matrix-result.xml"), "utf-8");

		Assert.assertEquals(expected1.replaceAll("\\s*", ""), report.toString().replaceAll("\\s*", ""));
	}
}
