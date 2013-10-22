package com.dianping.cat.consumer.transaction;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.consumer.MockReportManager;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.dianping.cat.service.ReportDelegate;
import com.dianping.cat.service.ReportManager;

public class TransactionAnalyzerTest extends ComponentTestCase {
	private long m_timestamp;

	private TransactionAnalyzer m_analyzer;

	private String m_domain = "group";

	@Before
	public void init() {
		m_timestamp = System.currentTimeMillis() - System.currentTimeMillis() % (3600 * 1000);

		ReportDelegate<TransactionReport> transactionDelegate = new TransactionDelegate();
		ServerConfigManager serverConfigManager = new ServerConfigManager();
		ReportManager<TransactionReport> reportManager = new MockReportManager<TransactionReport>(transactionDelegate,
		      m_domain);

		m_analyzer = new TransactionAnalyzer();
		m_analyzer.setDelegate((TransactionDelegate) transactionDelegate);
		m_analyzer.setReportManager(reportManager);
		m_analyzer.setServerConfigManager(serverConfigManager);
	}

	@Test
	public void testProcessTransaction() throws Exception {

		for (int i = 1; i <= 1000; i++) {
			MessageTree tree = newMessageTree(i);

			m_analyzer.process(tree);
		}

		TransactionReport report = m_analyzer.getReport(m_domain);

		report.accept(new TransactionStatisticsComputer());

		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionAnalyzerTest.xml"), "utf-8");
		Assert.assertEquals(expected.replaceAll("\\s*", ""), report.toString().replaceAll("\\s*", ""));
	}

	protected MessageTree newMessageTree(int i) {
		MessageTree tree = new DefaultMessageTree();

		tree.setMessageId("" + i);
		tree.setDomain(m_domain);
		tree.setHostName("group001");
		tree.setIpAddress("192.168.1.1");

		DefaultTransaction t = new DefaultTransaction("A", "n" + i % 2, null);
		DefaultTransaction t2 = new DefaultTransaction("A-1", "n" + i % 3, null);

		if (i % 2 == 0) {
			t2.setStatus("ERROR");
		} else {
			t2.setStatus(Message.SUCCESS);
		}

		t2.complete();
		t2.setDurationInMillis(i);

		t.addChild(t2);

		if (i % 2 == 0) {
			t.setStatus("ERROR");
		} else {
			t.setStatus(Message.SUCCESS);
		}

		t.complete();
		t.setDurationInMillis(i * 2);
		t.setTimestamp(m_timestamp + 1000);
		t2.setTimestamp(m_timestamp + 2000);
		tree.setMessage(t);

		return tree;
	}
}
