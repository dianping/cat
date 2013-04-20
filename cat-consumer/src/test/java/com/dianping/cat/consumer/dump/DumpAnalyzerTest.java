package com.dianping.cat.consumer.dump;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.consumer.MessageAnalyzerFactory;
import com.dianping.cat.consumer.MessageAnalyzer;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.io.DefaultMessageQueue;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import org.unidal.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class DumpAnalyzerTest extends ComponentTestCase {
	@Test
	public void test() throws Exception {
		MessageAnalyzerFactory factory = lookup(MessageAnalyzerFactory.class);
		long now = 1334041324150L;
		DefaultMessageQueue queue = new DefaultMessageQueue();
		int num = 1000000;

		queue.setSize(num);
		queue.initialize();

		for (int i = 0; i < num; i++) {
			queue.offer(newMessageTree(i, now + i * 10L));
		}

		MessageAnalyzer analyzer = factory.create("dump", now, 10 * 1000L, 10 * 1000L);

		analyzer.analyze(queue);

		analyzer.doCheckpoint(true);
	}

	private DefaultMessageTree newMessageTree(int i, long timestamp) {
		DefaultMessageTree tree = new DefaultMessageTree();

		tree.setDomain("domain");
		tree.setHostName("hostName" + i);
		tree.setIpAddress("ipAddress" + i);
		tree.setMessageId(String.valueOf(i));
		tree.setParentMessageId("parentMessageId" + i);
		tree.setRootMessageId("rootMessageId" + i);
		tree.setSessionToken("sessionToken");
		tree.setThreadGroupName("threadGroupName");
		tree.setThreadId("threadId" + i);
		tree.setThreadName("threadName");

		tree.setMessage(newTransaction("type", "name" + i, timestamp, "0", 123456 + i, "data" + i));
		return tree;
	}

	private Transaction newTransaction(String type, String name, long timestamp, String status, int duration, String data) {
		DefaultTransaction transaction = new DefaultTransaction(type, name, null);

		transaction.setStatus(status);
		transaction.addData(data);
		transaction.complete();
		transaction.setTimestamp(timestamp);
		transaction.setDurationInMillis(duration);
		return transaction;
	}
}
