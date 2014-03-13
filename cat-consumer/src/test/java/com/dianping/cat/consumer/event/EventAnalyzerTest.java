package com.dianping.cat.consumer.event;

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Constants;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class EventAnalyzerTest extends ComponentTestCase {

	private long m_timestamp;

	private EventAnalyzer m_analyzer;

	private String m_domain = "group";

	@Before
	public void setUp() throws Exception {
		super.setUp();
		long currentTimeMillis = System.currentTimeMillis();

		m_timestamp = currentTimeMillis - currentTimeMillis % (3600 * 1000);

		m_analyzer = (EventAnalyzer) lookup(MessageAnalyzer.class, EventAnalyzer.ID);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm");
		Date date = sdf.parse("20120101 00:00");

		m_analyzer.initialize(date.getTime(), Constants.HOUR, Constants.MINUTE * 5);
	}

	@Test
	public void testProcess() throws Exception {
		for (int i = 1; i <= 1000; i++) {
			MessageTree tree = generateMessageTree(i);

			m_analyzer.process(tree);
		}

		EventReport report = m_analyzer.getReport(m_domain);

		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("event_analyzer.xml"), "utf-8");
		Assert.assertEquals(expected.replaceAll("\r", ""), report.toString().replaceAll("\r", ""));
	}

	protected MessageTree generateMessageTree(int i) {
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

		DefaultEvent event1 = new DefaultEvent("test2", "fail");
		event1.setTimestamp(m_timestamp + 5 * 60 * 1000);

		t2.addChild(event1);
		t2.complete();
		t2.setDurationInMillis(i);

		t.addChild(t2);

		if (i % 2 == 0) {
			t.setStatus("ERROR");
		} else {
			t.setStatus(Message.SUCCESS);
		}

		DefaultEvent event = new DefaultEvent("test1", "success");
		event.setTimestamp(m_timestamp + 5 * 60 * 1000);
		event.setStatus(Message.SUCCESS);
		t.addChild(event);

		t.complete();
		t.setDurationInMillis(i * 2);
		t.setTimestamp(m_timestamp + 1000);
		t2.setTimestamp(m_timestamp + 2000);
		tree.setMessage(t);

		return tree;
	}

}
