package com.dianping.cat.report.analyzer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.lookup.annotation.Inject;

import com.dainping.cat.consumer.dal.report.Report;
import com.dainping.cat.consumer.dal.report.ReportDao;
import com.dainping.cat.consumer.dal.report.ReportEntity;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.report.DailyreportDao;
import com.site.helper.Files;

public class UrlTransasctionBugTest extends ComponentTestCase {

	@Inject
	private DailyreportDao m_dailyreportDao;

	@Inject
	private ReportDao m_reportDao;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		m_dailyreportDao = lookup(DailyreportDao.class);
		m_reportDao = lookup(ReportDao.class);
	}

	@Test
	public void test() throws Exception {
		//fix the transaction xml parse builder
		String dateStr = "2012-12-14 19:00:00";
		Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateStr);

		List<Report> reports = m_reportDao.findAllByDomainNameDuration(date,
		      new Date(date.getTime() + TimeUtil.ONE_HOUR), "GroupWeb", "transaction", ReportEntity.READSET_FULL);
		File file = new File("text.txt");
		for (Report report : reports) {
			try {
				DefaultSaxParser.parse(report.getContent());
			} catch (Exception e) {
				Files.forIO().writeTo(file, report.getContent());
			}
		}
	}
}