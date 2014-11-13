package com.dianping.cat.consumer.dependency;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Constants;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class DependencyAnalyzerTest extends ComponentTestCase {

	private long m_timestamp;

	private DependencyAnalyzer m_analyzer;

	private String m_domain = "group";

	@Before
	public void setUp() throws Exception {
		super.setUp();
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
		long currentTimeMillis = System.currentTimeMillis();

		m_timestamp = currentTimeMillis - currentTimeMillis % (3600 * 1000);

		m_analyzer = (DependencyAnalyzer) lookup(MessageAnalyzer.class, DependencyAnalyzer.ID);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm");
		Date date = sdf.parse("20120101 00:00");

		m_analyzer.initialize(date.getTime(), Constants.HOUR, Constants.MINUTE * 5);
	}

	@Test
	public void testProcess() throws Exception {
		for (int i = 1; i <= 100; i++) {
			MessageTree tree = generateMessageTree(i);

			m_analyzer.process(tree);
		}

		DependencyReport report = m_analyzer.getReport(m_domain);

		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("dependency_analyzer.xml"), "utf-8");
		Assert.assertEquals(expected.replaceAll("\r", ""), report.toString().replaceAll("\r", ""));
	}

	protected MessageTree generateMessageTree(int i) {
		MessageTree tree = new DefaultMessageTree();

		tree.setMessageId("" + i);
		tree.setDomain(m_domain);
		tree.setHostName("group001");
		tree.setIpAddress("192.168.1.1");

		DefaultTransaction t;
		DefaultEvent event;
		DefaultEvent event2;

		if (i % 3 == 0) {
			t = new DefaultTransaction("Call", "Cat-Test-Call", null);
			event = new DefaultEvent("Exception", "192.168.1.0:3000:class:method1", null);
			event2 = new DefaultEvent("PigeonCall.app", "CatServer", null);
		} else if (i % 3 == 1) {
			t = new DefaultTransaction("PigeonCall", "Cat-Test-Call", null);
			event = new DefaultEvent("PigeonCall.ip", "CatServer", null);
			event2 = new DefaultEvent("PigeonCall.app", "CatServer", null);
		} else {
			t = new DefaultTransaction("SQL", "Cat-Test-SQL", null);
			event = new DefaultEvent("SQL.Database", "jdbc:mysql://127.0.0.1:3306?cat", null);
			event2 = new DefaultEvent("SQL.name", "select * from test", null);
		}

		event.setTimestamp(m_timestamp + 5 * 60 * 1000);
		event2.setTimestamp(m_timestamp + 5 * 60 * 1000);
		event.setStatus(Message.SUCCESS);
		event2.setStatus(Message.SUCCESS);
		t.setDurationInMillis(i);
		t.addChild(event);
		t.addChild(event2);

		t.complete();
		t.setDurationInMillis(i * 2);
		t.setTimestamp(m_timestamp + 1000);
		tree.setMessage(t);

		return tree;
	}

}
