package com.dianping.cat.report.task.abtest;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;
import org.unidal.helper.Files;
import org.xml.sax.SAXException;

import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.transform.DefaultSaxParser;
import com.dianping.cat.report.abtest.entity.AbtestReport;

public class MetricReportForABTestVisitorTest {
	
	@Test
	public void test() {
		MetricReport metricReport = null;
		try {
			String xml = Files.forIO().readFrom(getClass().getResourceAsStream("metricReport.xml"), "utf-8");
			metricReport = DefaultSaxParser.parse(xml);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		
		if(metricReport != null){
			MetricReportForABTestVisitor visitor = new MetricReportForABTestVisitor();
			
			metricReport.accept(visitor);
			
			Map<Integer, AbtestReport> result = visitor.getReportMap();
			
			for(AbtestReport ar : result.values()){
				System.out.println(ar);
			}
		}
	}

}
