package com.dianping.cat.consumer.failure;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.consumer.AnalyzerFactory;
import com.dianping.cat.consumer.failure.model.entity.Entry;
import com.dianping.cat.consumer.failure.model.entity.FailureReport;
import com.dianping.cat.consumer.failure.model.entity.Segment;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class FailureAnalyzerTest extends ComponentTestCase {
	private String m_domain = "domain1";

	private String m_host = "127.0.0.1";

	private static final long HOUR = 1000L * 60 * 60;

	@Test
	public void testFailureHandler() throws Exception {
		long current = System.currentTimeMillis();
		long duration = 60 * 60 * 1000;
		long extraTime = 5 * 60 * 1000;
		long start = current - current % (60 * 60 * 1000) - HOUR;

		AnalyzerFactory factory = lookup(AnalyzerFactory.class);
		FailureReportAnalyzer analyzer = (FailureReportAnalyzer) factory.create("failure", start, duration, m_domain,
		      extraTime);
		// Just for one hour
		int number = 60 * 10;
		int threadNumber = 10;

		DefaultEvent e11 = new DefaultEvent("Error", "testError");
		DefaultEvent e21 = new DefaultEvent("Exception", "testException1");
		DefaultEvent e31 = new DefaultEvent("RuntimeException", "testRuntimeException1");
		DefaultEvent e22 = new DefaultEvent("Exception", "testException2");
		DefaultEvent e32 = new DefaultEvent("RuntimeException", "testRuntimeException2");
		MessageTree tree = new DefaultMessageTree();
		tree.setMessageId("xx0001");
		tree.setDomain(m_domain);
		tree.setHostName(m_host);
		tree.setIpAddress(m_host);
		DefaultTransaction t1 = new DefaultTransaction("T1", "N1", null);
		DefaultTransaction t2 = new DefaultTransaction("T2", "N2", null);
		DefaultTransaction t3 = new DefaultTransaction("T3", "N3", null);
		t2.addChild(t3);
		t2.addChild(e21);
		t2.addChild(e22);
		t3.addChild(e31);
		t3.addChild(e32);
		t2.setStatus("ERROR");
		t2.complete();
		t1.setStatus(Message.SUCCESS);
		t1.complete();
		t1.addChild(t2);
		t1.addChild(e11);
		tree.setMessage(t1);

		long ct = System.nanoTime();
		for (int i = 0; i < number; i++) {
			if (i == 1) {
				ct = System.nanoTime();
			}
			long addTime = 6 * 1000 * i;
			long timestamp = start + addTime;
			t1.setTimestamp(timestamp);
			t2.setTimestamp(timestamp);
			t3.setTimestamp(timestamp);
			e11.setTimestamp(timestamp);
			e21.setTimestamp(timestamp);
			e22.setTimestamp(timestamp);
			e31.setTimestamp(timestamp);
			e32.setTimestamp(timestamp);
			tree.setThreadId("thread" + i % threadNumber);
			analyzer.process(tree);
		}

		long time = System.nanoTime() - ct;

		System.out.println("time: " + time / 1e6 + " ms," + (time / 1e6 / number) + " ms each");

		FailureReport report = analyzer.generateByDomainAndIp(m_domain, m_host);

		assertEquals("Check the domain", report.getDomain(), "domain1");
		assertEquals("Check the machines", m_host, report.getMachine());
		assertEquals("Check the threads", threadNumber, report.getThreads().getThreads().size());

		Date startDate = report.getStartTime();
		Date endDate = report.getEndTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String startStr = sdf.format(startDate);
		String endStr = sdf.format(endDate);

		Date realStartDate = new Date(start);
		Date realEndDate = new Date(start + duration - 60 * 1000);

		assertEquals("Check the report start time", sdf.format(realStartDate), startStr);
		assertEquals("Check the report end time", sdf.format(realEndDate), endStr);

		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		Map<String, Segment> segments = report.getSegments();
		for (int i = 0; i < number / 10; i++) {
			String minuteStr = sdf2.format(startDate);
			Segment temp = segments.get(minuteStr);

			List<Entry> entries = temp.getEntries();

			if (entries == null) {
				System.out.println(minuteStr);
			} else {
				assertEquals("Check the segment size ", 50, entries.size());
			}

			startDate.setTime(startDate.getTime() + 1000 * 60);
		}
	}

	@Test
	public void testLongUrlHander() throws Exception {
		long current = System.currentTimeMillis();
		long duration = 60 * 60 * 1000;
		long extraTime = 5 * 60 * 1000;
		long start = current - current % (60 * 60 * 1000) - HOUR;
		String domain = "domain1";

		AnalyzerFactory factory = lookup(AnalyzerFactory.class);
		FailureReportAnalyzer analyzer = (FailureReportAnalyzer) factory.create("failure", start, duration, domain,
		      extraTime);
		int number = 60;
		for (int i = 0; i < number; i++) {
			DefaultTransaction t = new DefaultTransaction("A1", "B1", null);
			MessageTree tree = new DefaultMessageTree();
			tree.setMessageId("thread0001");
			tree.setDomain(m_domain);
			tree.setHostName("middleware");
			tree.setIpAddress(m_host);
			tree.setMessage(t);
			t.setDuration(3 * 1000);
			t.setTimestamp(start + 1000L * 60 * i);
			analyzer.process(tree);
			// analyzer.process(tree);
		}
		FailureReport report = analyzer.generateByDomainAndIp(domain, m_host);

		assertEquals("Check the machines", m_host, report.getMachine());
		assertEquals("Check the domain", report.getDomain(), "domain1");

		Date startDate = report.getStartTime();
		Date endDate = report.getEndTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String startStr = sdf.format(startDate);
		String endStr = sdf.format(endDate);

		Date realStartDate = new Date(start);
		Date realEndDate = new Date(start + duration - 60 * 1000);

		assertEquals("Check the report start time", sdf.format(realStartDate), startStr);
		assertEquals("Check the report end time", sdf.format(realEndDate), endStr);

		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		Map<String, Segment> segments = report.getSegments();
		for (int i = 0; i < 60; i++) {
			String minuteStr = sdf2.format(startDate);
			Segment temp = segments.get(minuteStr);

			assertEquals("Check the segment size ", temp.getEntries().size(), 1);
			startDate.setTime(startDate.getTime() + 1000 * 60);
		}
	}

	@Test
	public void testManyDomainAndIp() throws Exception {
		long current = System.currentTimeMillis();
		long duration = 60 * 60 * 1000;
		long extraTime = 5 * 60 * 1000;
		long start = current - current % (60 * 60 * 1000) - HOUR;
		String baseDaomain = "domain";
		String baseIp = "192.168.1.";

		AnalyzerFactory factory = lookup(AnalyzerFactory.class);
		FailureReportAnalyzer analyzer = (FailureReportAnalyzer) factory
		      .create("failure", start, duration, "", extraTime);
		int number = 60;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				for (int index = 0; index < number; index++) {
					DefaultTransaction t = new DefaultTransaction("A1", "B1", null);
					MessageTree tree = new DefaultMessageTree();
					tree.setMessageId("thread0001");
					tree.setDomain(baseDaomain + i);
					tree.setIpAddress(baseIp + j);
					tree.setMessage(t);
					t.setDuration(3 * 1000);
					t.setTimestamp(start + 1000L * 60 * index);
					analyzer.process(tree);
				}
			}
		}

		Map<String, FailureReport> reports = analyzer.getReports();
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				FailureReport report = reports.get(baseDaomain + i + ":" + baseIp + j);
				assertEquals("Check the report is not null", report == null, false);
				assertEquals("Check the machine", baseIp + j, report.getMachine());
				assertEquals("Check the domain", baseDaomain + i, report.getDomain());

				Date startDate = report.getStartTime();
				Date endDate = report.getEndTime();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String startStr = sdf.format(startDate);
				String endStr = sdf.format(endDate);

				Date realStartDate = new Date(start);
				Date realEndDate = new Date(start + duration - 60 * 1000);

				assertEquals("Check the report start time", sdf.format(realStartDate), startStr);
				assertEquals("Check the report end time", sdf.format(realEndDate), endStr);

				SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

				Map<String, Segment> segments = report.getSegments();
				for (int k = 0; k < 60; k++) {
					String minuteStr = sdf2.format(startDate);
					Segment temp = segments.get(minuteStr);

					assertEquals("Check the segment size ",temp.getEntries().size(), 1);
					startDate.setTime(startDate.getTime() + 1000 * 60);
				}
			}
		}
	}
}
