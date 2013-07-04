package com.dianping.cat.report.task.metric;

import java.util.Date;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.consumer.advanced.MetricConfigManager;
import com.dianping.cat.home.dal.report.Baseline;
import com.dianping.cat.report.baseline.BaselineConfigManager;
import com.dianping.cat.report.baseline.BaselineService;
import com.dianping.cat.report.baseline.impl.DefaultBaselineCreator;
import com.dianping.cat.report.service.ReportService;

public class MetricBaselineReportBuilderTest  extends ComponentTestCase {
	
	private MetricBaselineReportBuilder getReportBuilder() throws Exception{
		MetricBaselineReportBuilder builder = new MetricBaselineReportBuilder();
		builder.m_baselineConfigManager = new BaselineConfigManager();
		builder.m_baselineCreator = new DefaultBaselineCreator();
		builder.m_reportService = lookup(ReportService.class);
		builder.m_baselineService = new MyBaselineService();
	
//		builder.m_baselineService = lookup(BaselineService.class);

		builder.m_configManager = lookup(MetricConfigManager.class);
		return builder;
	}
	
	private class MyBaselineService implements BaselineService{

		@Override
      public double[] queryDailyBaseline(String reportName, String key, Date reportPeriod) {
	      // TODO Auto-generated method stub
	      return null;
      }

		@Override
      public double[] queryHourlyBaseline(String reportName, String key, Date reportPeriod) {
	      // TODO Auto-generated method stub
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
			
			throw new RuntimeException("END");
      }
	}

	
	@Test
	public void testBuildDailyReport() throws Exception{
		MetricBaselineReportBuilder builder = getReportBuilder();
		builder.buildDailyReport("metirc", "TuanGou", new Date(new Date().getTime() - 24* 3600* 1000));
	}

}
