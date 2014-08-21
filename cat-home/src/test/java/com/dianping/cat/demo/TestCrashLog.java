package com.dianping.cat.demo;

import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class TestCrashLog {

	@Test
	public void test() throws InterruptedException {
		for (int i = 0; i < 100; i++) {
			Transaction t = Cat.newTransaction("url", "crash");

			String message = parse(i);

			Cat.logEvent("Exception", message, "ERROR", "sdf");

			String plateform = getPlateform(i);
			String version = getVersion(i);

			MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
			((DefaultMessageTree) tree).setIpAddress(plateform + ":" + version);
			((DefaultMessageTree) tree).setDomain("CrashLogWeb");
			t.complete();
		}
		Thread.sleep(10000);
		// code nullpoint
	}

	private String parse(int index) {
		return "message" + index % 3;
	}

	private String getPlateform(int index) {
		return "andriod" + index % 4;
	}

	private String getVersion(int index) {
		return "version" + index % 3;
	}

}
