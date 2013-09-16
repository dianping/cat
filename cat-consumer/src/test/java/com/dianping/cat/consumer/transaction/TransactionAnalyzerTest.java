package com.dianping.cat.consumer.transaction;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

@Ignore
public class TransactionAnalyzerTest extends ComponentTestCase {
	private long timestamp;

	@Test
	public void testProcessTransaction() throws Exception {
		timestamp = System.currentTimeMillis() - System.currentTimeMillis() % (3600 * 1000);

		TransactionAnalyzer analyzer = (TransactionAnalyzer) lookup(MessageAnalyzer.class, TransactionAnalyzer.ID);
		TransactionReport report = new TransactionReport("Test");

		for (int i = 1; i <= 1000; i++) {
			MessageTree tree = newMessageTree(i);
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
			t.setTimestamp(timestamp + 1000);
			t2.setTimestamp(timestamp + 2000);
			tree.setMessage(t);

			analyzer.processTransaction(report, tree, t);
		}

		report.accept(new TransactionStatisticsComputer());

		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionAnalyzerTest.xml"), "utf-8");
		Assert.assertEquals(expected.replaceAll("\\s*", ""), report.toString().replaceAll("\\s*", ""));
	}

	protected MessageTree newMessageTree(int i) {
		MessageTree tree = new DefaultMessageTree();

		tree.setMessageId("" + i);
		tree.setDomain("group");
		tree.setHostName("group001");
		tree.setIpAddress("192.168.1.1");
		return tree;
	}
}
