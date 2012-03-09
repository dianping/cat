package com.dianping.cat.consumer.transaction;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultJsonBuilder;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.site.helper.Files;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class TransactionAnalyzerTest extends ComponentTestCase {
	@Test
	public void testProcessTransaction() throws Exception {
		TransactionAnalyzer analyzer = lookup(TransactionAnalyzer.class);
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
			t2.setDuration(i);

			t.addChild(t2);

			if (i % 2 == 0) {
				t.setStatus("ERROR");
			} else {
				t.setStatus(Message.SUCCESS);
			}

			t.complete();
			t.setDuration(i * 2);

			tree.setMessage(t);

			analyzer.processTransaction(report, tree, t);
		}

		report.accept(new StatisticsComputer());

		String json = new DefaultJsonBuilder().buildJson(report);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionAnalyzerTest.json"), "utf-8");

		Assert.assertEquals(expected.replace("\r", ""), json.replace("\r", ""));
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
