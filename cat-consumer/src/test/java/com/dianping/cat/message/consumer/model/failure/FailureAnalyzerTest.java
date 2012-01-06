package com.dianping.cat.message.consumer.model.failure;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.consumer.model.failure.entity.FailureReport;
import com.dianping.cat.consumer.model.failure.entity.Segment;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.consumer.impl.AnalyzerFactory;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class FailureAnalyzerTest extends ComponentTestCase {

	@Test
	public void testFailureHandler() throws Exception {
		long current = System.currentTimeMillis();
		long duration = 60 * 60 * 1000;
		long extraTime = 5 * 60 * 1000;
		long start = current - current % (60 * 60 * 1000);

		AnalyzerFactory factory = lookup(AnalyzerFactory.class);
		FailureReportAnalyzer analyzer = (FailureReportAnalyzer) factory
				.create("failure", start, duration, "all-domain", extraTime);

		for (int i = 0; i < 60; i++) {
			DefaultEvent e11 = new DefaultEvent("Error", "testError");
			DefaultEvent e21 = new DefaultEvent("Exception", "testException");
			DefaultEvent e31 = new DefaultEvent("RuntimeException",
					"testRuntimeException");
			DefaultEvent e22 = new DefaultEvent("Exception", "testException");
			DefaultEvent e32 = new DefaultEvent("RuntimeException",
					"testRuntimeException");
			MessageTree tree = new DefaultMessageTree();
			tree.setMessageId("xx0001");
			tree.setDomain("group");
			tree.setHostName("group001");
			tree.setIpAddress("192.168.1.1");
			DefaultTransaction t1 = new DefaultTransaction("Error",
					"OutOfMemory", null);
			DefaultTransaction t2 = new DefaultTransaction("Exception",
					"NullPointException", null);
			DefaultTransaction t3 = new DefaultTransaction("RuntimeException",
					"RuntimeException", null);
			t2.addChild(t3);
			t2.addChild(e21);
			t2.addChild(e22);
			t3.addChild(e31);
			t3.addChild(e32);
			t2.setStatus("ERROR");
			t2.complete();
			t2.setDuration(i);
			t1.addChild(t2);
			t1.setStatus(Message.SUCCESS);
			t1.complete();
			t1.addChild(e11);
			t1.setDuration(i * 2);
			tree.setMessage(t1);
			t1.setTimestamp(start + 60 * 1000 * i);
			t2.setTimestamp(start + 60 * 1000 * i);
			t3.setTimestamp(start + 60 * 1000 * i);
			e11.setTimestamp(start + 60 * 1000 * i);
			e21.setTimestamp(start + 60 * 1000 * i);
			e22.setTimestamp(start + 60 * 1000 * i);
			e31.setTimestamp(start + 60 * 1000 * i);
			e32.setTimestamp(start + 60 * 1000 * i);
			analyzer.process(tree);
			analyzer.process(tree);
			analyzer.process(tree);
		}

		FailureReport report = analyzer.generate();
		
		assertEquals("Check the Machines",report.getMachines().getMachines().size(), 3);
		assertEquals("Check the domain", report.getDomain(), "all-domain");
		
		Date startDate = report.getStartTime();
		Date endDate = report.getEndTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String startStr = sdf.format(startDate);
		String endStr = sdf.format(endDate);

		Date realStartDate = new Date(start);
		Date realEndDate = new Date(start + duration - 60 * 1000);

		assertEquals("Check the report start time",sdf.format(realStartDate), startStr);
		assertEquals("Check the report end time",sdf.format(realEndDate), endStr);

		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd hh:mm");

		Map<String, Segment> segments = report.getSegments();
		for (int i = 0; i < 60; i++) {
			String minuteStr = sdf2.format(startDate);
			Segment temp = segments.get(minuteStr);
			assertEquals("Check the segment size ",temp.getEntries().size(), 8*3);
			startDate.setTime(startDate.getTime()+ 1000 * 60);
		}
		System.out.println(report.toString());
	}

	@Test
	public void testLongUrlHander() throws Exception {

		long current = System.currentTimeMillis();
		long duration = 60 * 60 * 1000;
		long extraTime = 5 * 60 * 1000;
		long start = current - current % (60 * 60 * 1000);
		AnalyzerFactory factory = lookup(AnalyzerFactory.class);
		FailureReportAnalyzer analyzer = (FailureReportAnalyzer) factory
				.create("failure", start, duration, "all-domain", extraTime);

		for (int i = 0; i < 60; i++) {
			DefaultTransaction t = new DefaultTransaction("A1", "B1", null);
			MessageTree tree = new DefaultMessageTree();
			tree.setMessageId("thread0001");
			tree.setDomain("middleware");
			tree.setHostName("middleware");
			tree.setIpAddress("127.0.0.1");
			tree.setMessage(t);
			t.setDuration(3 *1000);
			t.setTimestamp(start + 1000 * 60 * i);
			analyzer.process(tree);
			analyzer.process(tree);
		}
		FailureReport report = analyzer.generate();
		
		assertEquals("Check the Machines",report.getMachines().getMachines().size(), 3);
		assertEquals("Check the domain", report.getDomain(), "all-domain");
		
		Date startDate = report.getStartTime();
		Date endDate = report.getEndTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String startStr = sdf.format(startDate);
		String endStr = sdf.format(endDate);

		Date realStartDate = new Date(start);
		Date realEndDate = new Date(start + duration - 60 * 1000);

		assertEquals("Check the report start time",sdf.format(realStartDate), startStr);
		assertEquals("Check the report end time",sdf.format(realEndDate), endStr);

		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd hh:mm");

		System.out.println(report.toString());
		Map<String, Segment> segments = report.getSegments();
		for (int i = 0; i < 60; i++) {
			String minuteStr = sdf2.format(startDate);
			Segment temp = segments.get(minuteStr);
			assertEquals("Check the segment size ",temp.getEntries().size(), 2);
			startDate.setTime(startDate.getTime()+ 1000 * 60);
		}
	}
}
