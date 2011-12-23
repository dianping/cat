package com.dianping.cat.transport;

import junit.framework.Assert;

import org.junit.Test;

public class TransportManagerTest {
	@Test
	public void testInitailized() {
		new TransportManager().setTransport(new InMemoryTransport());

		Assert.assertNotNull(TransportManager.getTransport());

		new TransportManager().setTransport(null);
	}

	@Test(expected = RuntimeException.class)
	public void testNotInitailized() {
		Assert.assertNotNull(TransportManager.getTransport());
	}
}
