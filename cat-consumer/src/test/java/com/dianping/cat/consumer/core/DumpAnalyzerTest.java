package com.dianping.cat.consumer.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzerManager;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.io.DefaultMessageQueue;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

@RunWith(JUnit4.class)
public class DumpAnalyzerTest extends ComponentTestCase {
	@Test
	public void test() throws Exception {
		MessageAnalyzerManager manager = lookup(MessageAnalyzerManager.class);
		long now = 1334041324150L;
		int num = 1000000;
		DefaultMessageQueue queue = new DefaultMessageQueue(num);


		for (int i = 0; i < num; i++) {
			queue.offer(newMessageTree(i, now + i * 10L));
		}

		MessageAnalyzer analyzer = manager.getAnalyzer("dump", now);

		analyzer.analyze(queue);
		analyzer.doCheckpoint(true);
	}

	private MessageTree newMessageTree(int i, long timestamp) {
		MessageTree tree = new DefaultMessageTree();

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
