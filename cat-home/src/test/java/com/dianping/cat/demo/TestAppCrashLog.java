package com.dianping.cat.demo;

import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class TestAppCrashLog {

	@Test
	public void test() throws InterruptedException {
		while (true) {
			for (int i = 0; i < 10; i++) {
				Transaction t = Cat.newTransaction("AndroidCrashLog", "crashLog");

				Cat.logError(new RuntimeException("AndroidCrashLogTest1"));
				Cat.logError(new Exception("AndroidCrashLogTest2"));
				Cat.logError(new RuntimeException("AndroidCrashLogTest1"));
				Cat.logError(new Exception("AndroidCrashLogTest2"));

				String plateform = getPlateform("Android", i);
				String version = getVersion("Android", i);
				String moudle = getModule("Android", i);
				String level = getLevel("Android", i);

				MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
				((DefaultMessageTree) tree).setIpAddress(version + ":" + plateform + ":" + moudle + ":" + level);
				((DefaultMessageTree) tree).setDomain("AndroidCrashLog");
				t.complete();

				Transaction t2 = Cat.newTransaction("iOSCrashLog", "crashLog");

				Cat.logError(new RuntimeException("iOSCrashLogTest1"));
				Cat.logError(new Exception("AndroidCrashLogTest2"));

				String plateform2 = getPlateform("iOS", i);
				String version2 = getVersion("iOS", i);
				String moudle2 = getModule("iOS", i);
				String level2 = getLevel("iOS", i);

				MessageTree tree2 = Cat.getManager().getThreadLocalMessageTree();
				((DefaultMessageTree) tree2).setIpAddress(version2 + ":" + plateform2 + ":" + moudle2 + ":" + level2);
				((DefaultMessageTree) tree2).setDomain("iOSCrashLog");
				t2.complete();
			}
			Thread.sleep(10000);
			// code nullpoint
		}
	}

	private String getLevel(String platform, int index) {
		return platform + "level" + index % 3;
	}

	private String getModule(String platform, int index) {
		return platform + "module" + index % 3;
	}

	private String getPlateform(String platform, int index) {
		return platform + index % 3;
	}

	private String getVersion(String platform, int index) {
		return platform + "version" + index % 3;
	}

}
