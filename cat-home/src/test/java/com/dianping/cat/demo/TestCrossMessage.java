package com.dianping.cat.demo;

import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class TestCrossMessage {

	@Test
	public void testCross() throws Exception {
		String serverIp = "10.10.10.1";
		String clientIp = "10.10.10.2";

		for (int i = 0; i < 1000; i++) {
			sendClientMsg("Cat-Call", "catClient1", clientIp, "1000", "catServer1", serverIp + ":8080");
			sendClientMsg("Cat-Call", "catClient1", clientIp, "1000", "catServer2", serverIp + ":8081");
			sendClientMsg("Cat-Call", "catClient2", clientIp, "1001", "catServer1", serverIp + ":8080");
			sendClientMsg("Cat-Call", "catClient2", clientIp, "1001", "catServer2", serverIp + ":8081");
			sendServiceMsg("Cat-Call", "catServer1", serverIp, "catClient1", clientIp + ":1000");
			sendServiceMsg("Cat-Call", "catServer1", serverIp, "catClient2", clientIp + ":1001");
			sendServiceMsg("Cat-Call", "catServer2", serverIp, "catClient1", clientIp + ":1000");
			sendServiceMsg("Cat-Call", "catServer2", serverIp, "catClient2", clientIp + ":1001");
		}
		Thread.sleep(10000);
	}

	private void sendServiceMsg(String method, String server, String serverIp, String client, String clientIp) {
		Transaction t = Cat.newTransaction("PigeonService", method);

		Cat.logEvent("PigeonService.client", clientIp);
		Cat.logEvent("PigeonService.app", client);

		MessageTree tree = Cat.getManager().getThreadLocalMessageTree();

		((DefaultMessageTree) tree).setDomain(server);
		((DefaultMessageTree) tree).setIpAddress(serverIp);
		t.setStatus(Transaction.SUCCESS);
		t.complete();
	}

	private void sendClientMsg(String method, String client, String clientIp, String port, String server, String serverIp) {
		Transaction t = Cat.newTransaction("PigeonCall", method);

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
