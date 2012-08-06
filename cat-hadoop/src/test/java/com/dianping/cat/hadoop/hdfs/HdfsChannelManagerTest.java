package com.dianping.cat.hadoop.hdfs;

import java.io.File;

import junit.framework.Assert;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.hadoop.hdfs.InputChannel;
import com.dianping.cat.hadoop.hdfs.InputChannelManager;
import com.dianping.cat.hadoop.hdfs.OutputChannel;
import com.dianping.cat.hadoop.hdfs.OutputChannelManager;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

@RunWith(JUnit4.class)
public class HdfsChannelManagerTest extends CatTestCase {
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
	public void testOutputAndInput() throws Exception {
		OutputChannelManager ocm = lookup(OutputChannelManager.class);
		String testid = "" + System.currentTimeMillis();
		DefaultMessageTree tree = newMessageTree(testid);
		String path = "20120321/11/Cat-192.168.63.36-1332299450675";
		OutputChannel oc = ocm.openChannel("dump", path, true);
		oc.write(tree);
		ocm.closeChannel(oc);

		InputChannelManager icm = lookup(InputChannelManager.class);
		InputChannel ic = icm.openChannel("logview", path);
		MessageCodec codec = lookup(MessageCodec.class, "plain-text");
		ChannelBuffer buf = ChannelBuffers.dynamicBuffer(8192);
		codec.encode(tree, buf);
		MessageTree actual = ic.read(0, buf.writerIndex());
		Assert.assertEquals(tree.getMessageId(), actual.getMessageId());
	}
}
