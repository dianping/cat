/**
 * 
 */
package com.dianping.cat.consumer.transaction;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.consumer.AnalyzerFactory;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.site.lookup.ComponentTestCase;

/**
 * @author sean.wang
 * @since Jan 5, 2012
 */
@RunWith(JUnit4.class)
public class TransactionReportMessageAnalyzerTest extends ComponentTestCase {

	/**
	 * Test method for
	 * {@link com.dianping.cat.consumer.transaction.TransactionReportAnalyzer#process(com.dianping.cat.message.spi.MessageTree)}
	 * .
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testCommonGenerate() throws Exception {
		long current = System.currentTimeMillis();
		long duration = 60 * 60 * 1000;
		long extraTime = 5 * 60 * 1000;
		long start = current - current % (60 * 60 * 1000) - 1000L * 60 * 60;

		AnalyzerFactory factory = lookup(AnalyzerFactory.class);
		TransactionAnalyzer analyzer = (TransactionAnalyzer) factory.create("transaction", start, duration, extraTime);

		for (int i = 1; i <= 1000; i++) {
			MessageTree tree = new DefaultMessageTree();
			tree.setMessageId("" + i);
			tree.setDomain("group");
			tree.setHostName("group001");
			tree.setIpAddress("192.168.1.1");
			DefaultTransaction t = new DefaultTransaction("A", "n1", null);

			DefaultTransaction t2 = new DefaultTransaction("A-1", "n2", null);
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

			tree.setMessage(t);
			analyzer.process(tree);
		}

		TransactionReport report = analyzer.getReport("group");
		TransactionType typeA = report.getMachines().get("192.168.1.1").getTypes().get("A");
		TransactionName n1 = typeA.getNames().get("n1");
		assertEquals(1000, n1.getTotalCount());
		assertEquals(500, n1.getFailCount());
		assertEquals(50.0, n1.getFailPercent());
		assertEquals(2.0, n1.getMin());
		assertEquals(2000.0, n1.getMax());
		assertEquals(1001.0, n1.getAvg());
		assertEquals(1001000.0, n1.getSum());

		TransactionType typeA1 = report.getMachines().get("192.168.1.1").getTypes().get("A-1");
		TransactionName n2 = typeA1.getNames().get("n2");
		assertEquals(1000, n2.getTotalCount());
		assertEquals(500, n2.getFailCount());
		assertEquals(50.0, n2.getFailPercent());
		assertEquals(1.0, n2.getMin());
		assertEquals(1000.0, n2.getMax());
		assertEquals(500.5, n2.getAvg());
		assertEquals(500500.0, n2.getSum());
	}

}
