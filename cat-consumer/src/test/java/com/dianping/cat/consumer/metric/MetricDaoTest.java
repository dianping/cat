package com.dianping.cat.consumer.metric;

import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.lookup.annotation.Inject;

import com.dainping.cat.consumer.dal.report.BusinessReport;
import com.dainping.cat.consumer.dal.report.BusinessReportDao;
import com.dainping.cat.consumer.dal.report.BusinessReportEntity;
import com.dainping.cat.consumer.dal.report.Report;
import com.dainping.cat.consumer.dal.report.ReportDao;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.metric.model.transform.DefaultNativeParser;

public class MetricDaoTest extends ComponentTestCase {

	@Inject
	private BusinessReportDao m_businessReportDao;

	@Inject
	private ReportDao m_reportDao;

	@Test
	public void test() throws Exception {
		m_businessReportDao = lookup(BusinessReportDao.class);

		BusinessReport r = m_businessReportDao.createLocal();
		MetricReport metricReport = new MetricReport();
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
		m_reportDao = lookup(ReportDao.class);
		Report report = m_reportDao.createLocal();
		String domain = "group";

		report.setName("metric");
		report.setDomain(domain);
		report.setPeriod(new Date());
		report.setIp("127.0.0.1");
		report.setType(1);
		// r.setBinaryContent("metric".getBytes());
		report.setContent("metic");
		report.setCreationDate(new Date());

		m_reportDao.insert(report);
	}
	
}
