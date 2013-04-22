package com.dianping.cat.report.page.metric;

import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.lookup.annotation.Inject;
import org.unidal.webres.helper.Files;

import com.dainping.cat.consumer.advanced.dal.BusinessReport;
import com.dainping.cat.consumer.advanced.dal.BusinessReportDao;
import com.dainping.cat.consumer.advanced.dal.BusinessReportEntity;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.metric.model.transform.DefaultNativeParser;
import com.dianping.cat.consumer.metric.model.transform.DefaultSaxParser;

public class MetricDaoTest extends ComponentTestCase {
	@Inject
	private BusinessReportDao m_businessReportDao;

	private MetricReport getReport() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("metric-report.xml"), "utf-8");
		MetricReport report = DefaultSaxParser.parse(oldXml);

		return report;
	}

	@Test
	public void test() throws Exception {
		m_businessReportDao = lookup(BusinessReportDao.class);

		BusinessReport r = m_businessReportDao.createLocal();
		MetricReport metricReport = getReport();
		Date now = new Date();

		metricReport.setEndTime(now);
		metricReport.setStartTime(now);
		metricReport.setGroup("Cat");

		String group = "group";

		r.setName("metric");
		r.setProductLine(group);
		r.setPeriod(now);
		r.setIp("127.0.0.1");
		r.setType(1);
		// r.setBinaryContent("metric".getBytes());
		byte[] realBytes = DefaultNativeBuilder.build(metricReport);
		r.setContent(realBytes);
		r.setCreationDate(now);

		m_businessReportDao.insert(r);
		int id = r.getId();

		BusinessReport query = m_businessReportDao.findByPK(id, BusinessReportEntity.READSET_FULL);

		byte[] bytes = query.getContent();

		MetricReport queryReport = DefaultNativeParser.parse(bytes);
		Assert.assertEquals(queryReport.getGroup(), metricReport.getGroup());
		Assert.assertEquals(queryReport.getStartTime(), metricReport.getStartTime());
		Assert.assertEquals(queryReport.getEndTime(), metricReport.getEndTime());

	}

	@Test
	public void test2() throws Exception {
		for (int id = 13; id < 16; id++) {
			m_businessReportDao = lookup(BusinessReportDao.class);

			BusinessReport query = m_businessReportDao.findByPK(id, BusinessReportEntity.READSET_FULL);

			byte[] bytes = query.getContent();

			MetricReport queryReport = DefaultNativeParser.parse(bytes);
			System.out.println(queryReport);
		}
	}

}
