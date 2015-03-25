package com.dianping.cat.hadoop.hdfs;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzerManager;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.io.DefaultMessageQueue;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

@RunWith(JUnit4.class)
public class DumpUploaderSample extends ComponentTestCase {
	@Before
	public void before() throws Exception {
		ServerConfigManager manager = lookup(ServerConfigManager.class);

		manager.initialize(new File("/data/appdatas/cat/server.xml"));
	}

	@Test
	public void testUpload() throws Exception {
		MessageAnalyzerManager manager = lookup(MessageAnalyzerManager.class);
		long now = System.currentTimeMillis();
		int num = 10000;
		DefaultMessageQueue queue = new DefaultMessageQueue(num);

		for (int i = 0; i < num; i++) {
			queue.offer(newMessageTree(i, now + i * 10L));
		}

		MessageAnalyzer analyzer = manager.getAnalyzer("dump", now);

		analyzer.analyze(queue);
		analyzer.doCheckpoint(true);

		Thread.sleep(30 * 100 * 1000);
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
