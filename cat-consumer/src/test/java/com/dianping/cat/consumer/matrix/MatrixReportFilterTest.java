package com.dianping.cat.consumer.matrix;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.matrix.model.transform.DefaultDomParser;

public class MatrixReportFilterTest {

	@Test
	public void test() throws Exception {
		DefaultDomParser parser = new DefaultDomParser();
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("matrix.xml"), "utf-8");
		MatrixReport report = parser.parse(source);

		MatrixReportFilter f1 = new MatrixReportFilter(1);
		report.accept(f1);
		String expected1 = Files.forIO().readFrom(getClass().getResourceAsStream("matrix-result.xml"), "utf-8");

		Assert.assertEquals(expected1.replaceAll("\\s*", ""), report.toString().replaceAll("\\s*", ""));
	}
	
	@Test
	public void test1(){
		Long i = Long.MAX_VALUE;
		long ii=Long.MAX_VALUE;
		System.out.println(i.toString());
		System.out.println(ii);
	}
}
