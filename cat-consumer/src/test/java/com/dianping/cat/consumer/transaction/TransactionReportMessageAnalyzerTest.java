/**
 * 
 */
package com.dianping.cat.consumer.transaction;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.consumer.MessageAnalyzerFactory;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

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

		MessageAnalyzerFactory factory = lookup(MessageAnalyzerFactory.class);
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
		Assert.assertEquals(1000, n1.getTotalCount());
		Assert.assertEquals(500, n1.getFailCount());
		double e = 0.001;
		Assert.assertEquals(50.0, n1.getFailPercent(),e);
		Assert.assertEquals(2.0, n1.getMin(),e);
		Assert.assertEquals(2000.0, n1.getMax(),e);
		Assert.assertEquals(1001.0, n1.getAvg(),e);
		Assert.assertEquals(1001000.0, n1.getSum(),e);

		TransactionType typeA1 = report.getMachines().get("192.168.1.1").getTypes().get("A-1");
		TransactionName n2 = typeA1.getNames().get("n2");
		Assert.assertEquals(1000, n2.getTotalCount());
		Assert.assertEquals(500, n2.getFailCount());
		Assert.assertEquals(50.0, n2.getFailPercent(),e);
		Assert.assertEquals(1.0, n2.getMin(),e);
		Assert.assertEquals(1000.0, n2.getMax(),e);
		Assert.assertEquals(500.5, n2.getAvg(),e);
		Assert.assertEquals(500500.0, n2.getSum(),e);
	}

}
