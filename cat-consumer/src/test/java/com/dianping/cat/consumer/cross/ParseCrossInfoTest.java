package com.dianping.cat.consumer.cross;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.consumer.cross.CrossAnalyzer.CrossInfo;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.DefaultEvent;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class ParseCrossInfoTest {

	@Test
	public void testParsePigeonClientTransaction() {
		CrossAnalyzer analyzer = new CrossAnalyzer();

		DefaultTransaction t = new DefaultTransaction("Call", "method1", null);
		MessageTree tree = buildMockMessageTree();
		CrossInfo info = analyzer.parseCorssTransaction(t, tree);

		Assert.assertEquals(info.getLocalAddress(), "192.168.0.1");
		Assert.assertEquals(info.getRemoteAddress(), "UnknownIp");

		Message message = new DefaultEvent("PigeonCall.server", "10.1.1.1", null);
		t.addChild(message);

		info = analyzer.parseCorssTransaction(t, tree);

		Assert.assertEquals(info.getLocalAddress(), "192.168.0.1");
		Assert.assertEquals(info.getRemoteAddress(), "10.1.1.1");
		Assert.assertEquals(info.getDetailType(), "PigeonCall");
		Assert.assertEquals(info.getRemoteRole(), "Pigeon.Server");
	}

	@Test
	public void testParsePigeonServerTransaction() {
		CrossAnalyzer analyzer = new CrossAnalyzer();

		DefaultTransaction t = new DefaultTransaction("Service", "method1", null);
		MessageTree tree = buildMockMessageTree();
		CrossInfo info = analyzer.parseCorssTransaction(t, tree);

		Assert.assertEquals(info.getLocalAddress(), "192.168.0.1");
		Assert.assertEquals(info.getRemoteAddress(), "192.168.7.70");

		Message message = new DefaultEvent("PigeonCall.client", "192.168.7.71", null);
		t.addChild(message);

		info = analyzer.parseCorssTransaction(t, tree);

		Assert.assertEquals(info.getLocalAddress(), "192.168.0.1");
		Assert.assertEquals(info.getRemoteAddress(), "192.168.7.71");
		Assert.assertEquals(info.getDetailType(), "PigeonService");
		Assert.assertEquals(info.getRemoteRole(), "Pigeon.Client");
	}
	
	@Test 
	public void testParsePigeonServerTransactionWithPort(){
		CrossAnalyzer analyzer = new CrossAnalyzer();

		DefaultTransaction t = new DefaultTransaction("Service", "method1", null);
		MessageTree tree = buildMockMessageTree();
		CrossInfo info = analyzer.parseCorssTransaction(t, tree);

		Message message = new DefaultEvent("PigeonCall.client", "192.168.7.71:29987", null);
		t.addChild(message);

		info = analyzer.parseCorssTransaction(t, tree);

		Assert.assertEquals(info.getLocalAddress(), "192.168.0.1");
		Assert.assertEquals(info.getRemoteAddress(), "192.168.7.71");
		Assert.assertEquals(info.getDetailType(), "PigeonService");
		Assert.assertEquals(info.getRemoteRole(), "Pigeon.Client");
	}
	
	@Test
	public void testParseOtherTransaction() {
		CrossAnalyzer analyzer = new CrossAnalyzer();

		DefaultTransaction t = new DefaultTransaction("Other", "method1", null);
		MessageTree tree = buildMockMessageTree();
		CrossInfo info = analyzer.parseCorssTransaction(t, tree);

		Assert.assertEquals(true,info==null);
	}
	

	public MessageTree buildMockMessageTree() {
		MessageTree tree = new DefaultMessageTree();
		tree.setMessageId("Cat-c0a80746-373452-6");//192.168.7.70 machine logview
		tree.setIpAddress("192.168.0.1");
		return tree;
	}
}
