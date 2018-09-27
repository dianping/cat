package com.dianping.cat.message;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.Cat.Context;

public class RpcLogviewTest {

	@Before
	public void setUp() {
		new File("/data/appdatas/cat/cat-cat.mark").delete();
	}

	@Test
	public void testClientMessage() throws InterruptedException {
		for (int i = 0; i < 1000; i++) {
			Transaction t = Cat.newTransaction("test", "test");
			final Map<String, String> map = new HashMap<String, String>();
			Context ctx = new Context() {

				@Override
				public String getProperty(String key) {
					return map.get(key);
				}

				@Override
				public void addProperty(String key, String value) {
					map.put(key, value);
				}
			};
			Cat.logRemoteCallClient(ctx);

			System.out.println(Cat.getManager().getThreadLocalMessageTree());
			t.complete();
		}

		Thread.sleep(1000);
	}
	
	
	@Test
	public void testServerMessage() throws InterruptedException {
		for (int i = 0; i < 1000; i++) {
			Transaction t = Cat.newTransaction("test", "test");
			final Map<String, String> map = new HashMap<String, String>();
			final String msgId = Cat.getCurrentMessageId();
			Context ctx = new Context() {

				@Override
				public String getProperty(String key) {
					return msgId;
				}

				@Override
				public void addProperty(String key, String value) {
					map.put(key, value);
				}
			};
			Cat.logRemoteCallServer(ctx);

			System.out.println(Cat.getManager().getThreadLocalMessageTree());
			t.complete();
		}

		Thread.sleep(1000);
	}
	
}
