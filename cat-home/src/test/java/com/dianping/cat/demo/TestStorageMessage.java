package com.dianping.cat.demo;

import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class TestStorageMessage {

	@Test
	public void testCross() throws Exception {
		String serverIp = "10.10.10.";

		for (int i = 0; i < 10; i++) {
			sendCacheMsg("Cat-Call", "cat", serverIp + i, "1000", "catServer1", serverIp + ":8080");
			sendCacheMsg("Cat-Call", "cat", serverIp + i, "1000", "catServer2", serverIp + ":8081");
			sendCacheMsg("Cat-Call", "cat", serverIp + i, "1000", "catServer1", serverIp + ":8080");
			sendCacheMsg("Cat-Call", "cat", serverIp + i, "1000", "catServer2", serverIp + ":8081");
		}
		Thread.sleep(10000);
	}

	private void sendCacheMsg(String method, String client, String clientIp, String port, String server, String serverIp) {
		Transaction t = Cat.newTransaction("Cache.memcached", "oUserAuthLevel:" + method);

		Cat.logEvent("PigeonCall.server", serverIp);
		Cat.logEvent("PigeonCall.app", server);
		Cat.logEvent("PigeonCall.port", port);

		MessageTree tree = Cat.getManager().getThreadLocalMessageTree();

		((DefaultMessageTree) tree).setDomain(client);
		((DefaultMessageTree) tree).setIpAddress(clientIp);
		t.setStatus(Transaction.SUCCESS);
		t.complete();
	}
}
