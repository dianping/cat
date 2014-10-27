package com.dianping.cat.report.page.cross;

import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class CrossTest {
	@Test
	public void test() throws InterruptedException {
		while (true) {
			Transaction tClient = Cat.newTransaction("PigeonCall", "Cat-Test-Call");
			MessageTree tree1 = Cat.getManager().getThreadLocalMessageTree();
			((DefaultMessageTree) tree1).setDomain("cat");
			((DefaultMessageTree) tree1).setIpAddress("10.1.2.15");
			Cat.logEvent("PigeonCall.server", "10.1.2.17:3000");
			Cat.logEvent("PigeonCall.app", "catServer");
			tClient.setStatus(Transaction.SUCCESS);
			Thread.sleep(100);
			tClient.complete();
			
			Transaction tClient3 = Cat.newTransaction("PigeonCall", "new-call");
			MessageTree tree5 = Cat.getManager().getThreadLocalMessageTree();
			((DefaultMessageTree) tree5).setDomain("cat");
			((DefaultMessageTree) tree5).setIpAddress("10.1.2.15");
			Cat.logEvent("PigeonCall.server", "10.1.2.17:3000");
			Cat.logEvent("PigeonCall.app", "catServer");
			tClient3.setStatus(Transaction.SUCCESS);
			Thread.sleep(100);
			tClient3.complete();
			
			Transaction tServer = Cat.newTransaction("PigeonService", "Cat-Test-Call");
			MessageTree tree2 = Cat.getManager().getThreadLocalMessageTree();
			((DefaultMessageTree) tree2).setDomain("cat");
			((DefaultMessageTree) tree2).setIpAddress("10.1.2.15");
			Cat.logEvent("PigeonService.client", "10.1.2.16:3000");
			Cat.logEvent("PigeonService.app", "catClient1");
			tServer.setStatus(Transaction.SUCCESS);
			tServer.complete();

			Transaction tClient2 = Cat.newTransaction("PigeonCall", "Cat-Test-Call");
			MessageTree tree3 = Cat.getManager().getThreadLocalMessageTree();
			((DefaultMessageTree) tree3).setDomain("catServer");
			((DefaultMessageTree) tree3).setIpAddress("10.1.2.17");
			Cat.logEvent("PigeonCall.server", "10.1.2.18:3000");
			Cat.logEvent("PigeonCall.app", "Server");
			tClient2.setStatus(Transaction.SUCCESS);
			tClient2.complete();

			Transaction tServer2 = Cat.newTransaction("PigeonService", "Cat-Test-Call");
			MessageTree tree4 = Cat.getManager().getThreadLocalMessageTree();
			((DefaultMessageTree) tree4).setDomain("catServer");
			((DefaultMessageTree) tree4).setIpAddress("10.1.2.17");
			Cat.logEvent("PigeonService.client", "10.1.2.15");
			Cat.logEvent("PigeonService.app", "cat");
			tServer2.setStatus(Transaction.SUCCESS);
			tServer2.complete();
			
			Transaction tServer3 = Cat.newTransaction("PigeonService", "new-call");
			MessageTree tree6 = Cat.getManager().getThreadLocalMessageTree();
			((DefaultMessageTree) tree6).setDomain("catServer");
			((DefaultMessageTree) tree6).setIpAddress("10.1.2.17");
			Cat.logEvent("PigeonService.client", "10.1.2.15");
			Cat.logEvent("PigeonService.app", "cat");
			tServer3.setStatus(Transaction.SUCCESS);
			tServer3.complete();
			
			Transaction tClient6 = Cat.newTransaction("PigeonCall", "Cat-Test-Call");
			MessageTree tree7 = Cat.getManager().getThreadLocalMessageTree();
			((DefaultMessageTree) tree7).setDomain("Unipay");
			((DefaultMessageTree) tree7).setIpAddress("10.1.4.99");
			Cat.logEvent("PigeonCall.server", "10.1.2.17:3000");
			Cat.logEvent("PigeonCall.app", "catServer");
			tClient6.setStatus(Transaction.SUCCESS);
			Thread.sleep(100);
			tClient6.complete();
			
			Transaction tServer4 = Cat.newTransaction("PigeonService", "new-call-2");
			MessageTree tree8 = Cat.getManager().getThreadLocalMessageTree();
			((DefaultMessageTree) tree8).setDomain("catServer");
			((DefaultMessageTree) tree8).setIpAddress("10.1.2.17");
			Cat.logEvent("PigeonService.client", "10.1.4.99");
			Cat.logEvent("PigeonService.app", "cat");
			tServer4.setStatus(Transaction.SUCCESS);
			tServer4.complete();
	

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
