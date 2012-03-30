package com.dianping.cat.job.hdfs;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageStorage;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.dianping.cat.server.configuration.ServerConfigManager;

@RunWith(JUnit4.class)
public class HdfsMessageStorageTest extends CatTestCase {
	private DefaultMessageTree newMessageTree(String id) {
		DefaultMessageTree tree = new DefaultMessageTree();
		tree.setDomain("domain");
		tree.setHostName("hostName");
		tree.setIpAddress("ipAddress");
		tree.setMessageId(id);
		tree.setParentMessageId("parentMessageId");
		tree.setRootMessageId("rootMessageId");
		tree.setSessionToken("sessionToken");
		tree.setThreadId("threadId");
		tree.setThreadName("threadName");
		return tree;
	}

	@Before
	public void before() throws Exception {
		ServerConfigManager configManager = lookup(ServerConfigManager.class);

		configManager.initialize(new File("/data/appdatas/cat/server.xml"));
	}

	@Test
	public void test() throws Exception {
		MessageStorage storage = lookup(MessageStorage.class, "hdfs");
		MessageManager manager = lookup(MessageManager.class);
		MessageTree tree = newMessageTree("abcdef");
		Transaction t = new DefaultTransaction("t", "n", manager);

		tree.setMessage(t);
		storage.store(tree);
		// MessageTree actual = storage.get(tree.getMessageId());
		// Assert.assertEquals(tree, actual);
		((HdfsMessageStorage) storage).dispose();
	}
}
