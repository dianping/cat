package com.dianping.cat.consumer.advanced;

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
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.DefaultMetric;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class MetricAnalyzerTest extends ComponentTestCase {
	private long m_timestamp;

	private MetricAnalyzer m_analyzer;

	private String m_domain = "group";

	private final int MINITE = 60 * 1000;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
		long currentTimeMillis = System.currentTimeMillis();

		m_timestamp = currentTimeMillis - currentTimeMillis % (3600 * 1000);

		@SuppressWarnings("unused")
		MetricConfigManager manager = lookup(MetricConfigManager.class);

		m_analyzer = (MetricAnalyzer) lookup(MessageAnalyzer.class, MetricAnalyzer.ID);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm");
		Date date = sdf.parse("20120101 00:00");

		m_analyzer.initialize(date.getTime(), Constants.HOUR, Constants.MINUTE * 5);
	}

	@Test
	public void testProcess() throws Exception {
		for (int i = 1; i <= 60; i++) {
			MessageTree tree = generateMessageTree(i);

			m_analyzer.process(tree);
		}

		MetricReport report = m_analyzer.getReport("Default");

		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("metric_analyzer.xml"), "utf-8");
		Assert.assertEquals(expected.replaceAll("\r", ""), report.toString().replaceAll("\r", ""));
	}

	protected MessageTree generateMessageTree(int i) {
		MessageTree tree = new DefaultMessageTree();

		tree.setMessageId("" + i);
		tree.setDomain(m_domain);
		tree.setHostName("group001");
		tree.setIpAddress("192.168.1.1");

		DefaultTransaction t;

		if (i % 2 == 0) {
			t = new DefaultTransaction("URL", "TuanGouWeb", null);
			t.setTimestamp(m_timestamp + i * MINITE);
			DefaultEvent event = new DefaultEvent("URL", "ABTest");
			
			event.addData("1=ab:A");
			
			t.addChild(event);
		} else {
			t = new DefaultTransaction("Metric", "TuanGouWeb", null);
			t.setTimestamp(m_timestamp + 1000);
			DefaultMetric metric = new DefaultMetric("City", "/shanghai");

			metric.setTimestamp(m_timestamp + i * MINITE);
			metric.setStatus("C");
			metric.addData("10");
			
			t.addChild(metric);
		}

		t.complete();
		t.setDurationInMillis(i * 2);
		tree.setMessage(t);

		return tree;
	}

}
