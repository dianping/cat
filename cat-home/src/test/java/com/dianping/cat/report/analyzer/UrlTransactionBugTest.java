package com.dianping.cat.report.analyzer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.lookup.annotation.Inject;

import com.dainping.cat.consumer.core.dal.Report;
import com.dainping.cat.consumer.core.dal.ReportDao;
import com.dainping.cat.consumer.core.dal.ReportEntity;
import com.dianping.cat.Cat;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.TimeUtil;

public class UrlTransactionBugTest extends ComponentTestCase {
	@Inject
	private ReportDao m_reportDao;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		m_reportDao = lookup(ReportDao.class);
	}

	public void test() throws Exception {
		// fix the transaction xml parse builder
		String dateStr = "2012-12-16 07:00:00";
		Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateStr);

		List<Report> reports = m_reportDao.findAllByDomainNameDuration(date, new Date(date.getTime() + TimeUtil.ONE_HOUR), "ShopWeb",
		      "transaction", ReportEntity.READSET_FULL);
		File file = new File("text.txt");
		for (Report report : reports) {
			try {
				DefaultSaxParser.parse(report.getContent());
			} catch (Exception e) {
				System.out.println(e);
				Files.forIO().writeTo(file, report.getContent());
			}
		}
	}

	public void insert() throws Exception {
		String xml = Files.forIO().readFrom(new File("text.xml"), "utf-8");
		System.out.println(xml.length());
		try {
			Report r = m_reportDao.createLocal();
			// String xml = new TransactionReportUrlFilter().buildXml(report);
			String domain = "CatTest";

			r.setName("transaction");
			r.setDomain(domain);
			r.setPeriod(TimeUtil.getCurrentDay());
			r.setIp("127.0.0.1");
			r.setType(1);
			r.setContent(xml);
			System.out.println("insertBefore");
			m_reportDao.insert(r);

			System.out.println("insert");
		} catch (Throwable e) {
			System.out.println(e);
			Cat.getProducer().logError(e);
		}

		List<Report> temp = m_reportDao.findAllByPeriodDomainName(TimeUtil.getCurrentDay(), "CatTest", "transaction",
		      ReportEntity.READSET_FULL);
		System.out.println(temp.size());
		for (Report re : temp) {
			TransactionReport report = DefaultSaxParser.parse(re.getContent());
			new Vistor().visitTransactionReport(report);
		}
	}

	static class Vistor extends com.dianping.cat.consumer.transaction.model.transform.BaseVisitor {

		@Override
		public void visitType(TransactionType type) {
			if ("URL".equals(type.getId())) {
				super.visitType(type);
			}
		}

		@Override
		public void visitName(TransactionName name) {
			if (name.getId().indexOf("2529898") > -1) {
				System.out.println(name.getId());
			}
			super.visitName(name);
		}
	}

	@Test
	public void testDataBaseOnLine() throws Exception {
		byte[] data = Files.forIO().readFrom(new File("report-transaction"));
		String all = new String(data, 1946326, 11610469 - 1946326);

		String shopWeb = all;

		System.out.println(shopWeb.substring(7, 100));
		int length = shopWeb.length();
		System.out.println(shopWeb.substring(length - 100, length));

		System.out.println("Old Length" + shopWeb.length());
		String domain = System.currentTimeMillis() + "";
		try {
			Report r = m_reportDao.createLocal();
			// String xml = new TransactionReportUrlFilter().buildXml(report);
			r.setName("transaction");
			r.setDomain(domain);
			r.setPeriod(TimeUtil.getCurrentDay());
			r.setIp("127.0.0.1");
			r.setType(1);
			r.setContent(shopWeb);
			m_reportDao.insert(r);

		} catch (Throwable e) {
			System.out.println(e);
			Cat.getProducer().logError(e);
		}

		Files.forIO().writeTo(new File("sfs.xml"), shopWeb.substring(7, shopWeb.length()).trim());
		TransactionReport report1 = DefaultSaxParser.parse(shopWeb);
		System.out.println(">>>>>>>" + report1.getMachines().size());
		List<Report> temp = m_reportDao.findAllByPeriodDomainName(TimeUtil.getCurrentDay(), domain, "transaction",
		      ReportEntity.READSET_FULL);
		System.out.println("temp size" + temp.size());
		for (Report re : temp) {
			String content = re.getContent();
			System.out.println("New Length:" + content.length());

			System.out.println(content.substring(0, 100));
			System.out.println(content.substring(length - 100, length));

			TransactionReport report = DefaultSaxParser.parse(content);
			new Vistor().visitTransactionReport(report);
		}
	}

}