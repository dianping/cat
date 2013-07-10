package com.dianping.cat.report.task.metric;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.home.dal.report.Baseline;
import com.dianping.cat.report.baseline.BaselineService;

public class MetricBaselineReportBuilderTest  extends ComponentTestCase {
	
	private MetricBaselineReportBuilder getReportBuilder() throws Exception{
		MetricBaselineReportBuilder builder = lookup(MetricBaselineReportBuilder.class);
		builder.m_baselineService = new MyBaselineService();
		return builder;
	}
	
	private class MyBaselineService implements BaselineService{

		@Override
      public double[] queryDailyBaseline(String reportName, String key, Date reportPeriod) {
	      return null;
      }

		@Override
      public double[] queryHourlyBaseline(String reportName, String key, Date reportPeriod) {
	      return null;
      }

		@Override
      public void insertBaseline(Baseline baseline) {
			System.out.println(baseline.toString());
			System.out.print(baseline.getDataInDoubleArray().length + ":");

			for(double item:baseline.getDataInDoubleArray()){
				System.out.print(item + ",");
			}
			System.out.println();
      }
	}

	
	@Test
	public void testBuildDailyReport() throws Exception{
		MetricBaselineReportBuilder builder = getReportBuilder();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		builder.buildDailyTask("metirc", "TuanGouWeb:URL:/index", format.parse("2013-07-01 00:00:00"));
	}

}
