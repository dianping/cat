package com.dianping.cat.consumer.problem;

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Constants;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class ProblemAnalyzerTest extends ComponentTestCase {

	private long m_timestamp;

	private ProblemAnalyzer m_analyzer;

	private String m_domain = "group";

	@Before
	public void setUp() throws Exception {
		super.setUp();
		long currentTimeMillis = System.currentTimeMillis();

		m_timestamp = currentTimeMillis - currentTimeMillis % (3600 * 1000);

		m_analyzer = (ProblemAnalyzer) lookup(MessageAnalyzer.class, ProblemAnalyzer.ID);
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

		ProblemReport report = m_analyzer.getReport(m_domain);

		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("problem_analyzer.xml"), "utf-8");
		Assert.assertEquals(expected.replaceAll("\r", ""), report.toString().replaceAll("\r", ""));
	}

	protected MessageTree generateMessageTree(int i) {
		MessageTree tree = new DefaultMessageTree();

		tree.setMessageId("" + i);
		tree.setDomain(m_domain);
		tree.setHostName("group001");
		tree.setIpAddress("192.168.1.1");
		tree.setThreadGroupName("Cat");
		tree.setThreadName("Cat-ProblemAnalyzer-Test");

		DefaultTransaction t = new DefaultTransaction("A", "n" + i % 2, null);

		t.setTimestamp(m_timestamp);
		t.setDurationInMillis(i * 50);

		switch (i % 7) {
		case 0:
			t.setType("URL");
			break;
		case 1:
			t.setType("Call");
			break;
		case 2:
			t.setType("Cache.");
			t.setDurationInMillis(i * 5);
			break;
		case 3:
			t.setType("SQL");
			break;
		case 4:
			t.setType("PigeonCall");
			break;
		case 5:
			t.setType("Service");
			break;
		case 6:
			t.setType("PigeonService");
			break;
		}

		tree.setMessage(t);

		return tree;
	}

}
