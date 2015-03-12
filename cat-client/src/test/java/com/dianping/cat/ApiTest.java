package com.dianping.cat;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.dianping.cat.message.Transaction;

public class ApiTest {

	public Map<String, String> maps = new HashMap<String, String>();

	public Cat.Context context;

	@Before
	public void before() {
		context = new Cat.Context() {
			@Override
			public void addProperty(String key, String value) {
				maps.put(key, value);
			}

			@Override
			public String getProperty(String key) {
				return maps.get(key);
			}
		};
	}

	@Test
	public void testNoMessgeId() {
		Assert.assertEquals(null, Cat.getManager().getThreadLocalMessageTree().getMessageId());
		Transaction parent = Cat.newTransaction("Test", "test");
		Assert.assertEquals(null, Cat.getManager().getThreadLocalMessageTree().getMessageId());
		Cat.logRemoteCallClient(context);
		parent.complete();
	}

	@Test
	public void testRemoteCall() {
		Cat.getManager().reset();
		String parentMesageId = null;
		Transaction parent = Cat.newTransaction("Test", "test");
		Assert.assertEquals(null, Cat.getManager().getThreadLocalMessageTree().getMessageId());
		Cat.logRemoteCallClient(context);
		parentMesageId = Cat.getManager().getThreadLocalMessageTree().getMessageId();
		parent.complete();

		Transaction child = Cat.newTransaction("child", "child");
		Assert.assertEquals(null, Cat.getManager().getThreadLocalMessageTree().getMessageId());

		Cat.logRemoteCallServer(context);
		Cat.logRemoteCallClient(context);

		Assert.assertEquals(parentMesageId, Cat.getManager().getThreadLocalMessageTree().getParentMessageId());
		Assert.assertEquals(parentMesageId, Cat.getManager().getThreadLocalMessageTree().getRootMessageId());
		child.complete();
	}

}
