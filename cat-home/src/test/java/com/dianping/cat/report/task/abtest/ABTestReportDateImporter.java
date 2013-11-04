package com.dianping.cat.report.task.abtest;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.transform.DefaultSaxParser;
import com.dianping.cat.home.dal.abtest.AbtestReportDao;
import com.dianping.cat.home.dal.abtest.AbtestReportEntity;
import com.dianping.cat.report.abtest.entity.AbtestReport;

public class ABTestReportDateImporter extends ComponentTestCase{

	private AbtestReportDao m_abtestReportDao;
	
	@Before
	public void prepare() throws Exception{
		m_abtestReportDao = lookup(AbtestReportDao.class);
	}

	@Test
	public void test() throws Exception {
		String xml = Files.forIO().readFrom(getClass().getResourceAsStream("metricReport.xml"), "utf-8");

		MetricReport metricReport = DefaultSaxParser.parse(xml);

		MetricReportForABTestVisitor visitor = new MetricReportForABTestVisitor();

		metricReport.accept(visitor);

		Map<Integer, AbtestReport> result = visitor.getReportMap();

		Calendar calendar = Calendar.getInstance();
		Date now = new Date();
		calendar.setTime(now);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.roll(Calendar.DAY_OF_MONTH, -14);

		for (AbtestReport ar : result.values()) {
			if (ar.getRunId() != -1) {
				System.out.println(ar.toString());
				for (int i = 0; i < 14; i++) {
					for (int j = 0; j < 24; j++) {
						Date begin = calendar.getTime();

						calendar.roll(Calendar.HOUR_OF_DAY, true);
						Date end = calendar.getTime();

						System.out.println("Import new report for " + begin);
						ar.setStartTime(begin);
						ar.setEndTime(end);
						
						com.dianping.cat.home.dal.abtest.AbtestReport report = new com.dianping.cat.home.dal.abtest.AbtestReport();
						report.setPeriod(begin);
						report.setRunId(ar.getRunId());
						report.setContent(ar.toString());
						
						m_abtestReportDao.insert(report);
					}
					calendar.roll(Calendar.DAY_OF_MONTH, true);
				}
			}
		}
	}
	
	@Test
	@Ignore
	public void testSelect() throws Exception{
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, 15);
		
		Date begin = calendar.getTime();
		
		System.out.println(begin);
		calendar.set(Calendar.DAY_OF_MONTH, 20);

		Date end = calendar.getTime();
		
		System.out.println(end);
		
		List<com.dianping.cat.home.dal.abtest.AbtestReport> reports = m_abtestReportDao.findByRunIdDuration(152, null, end, AbtestReportEntity.READSET_FULL);
		
		System.out.println(reports.size());
	}
}
