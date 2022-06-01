package com.dianping.cat;

import org.junit.Test;

public class CatBootstrapTest {
	@Test
	public void testLazyInitialize() {
		Cat.newTransaction("Type", "Name");

	}

	@Test
	public void testInitializeByServer() {
		Cat.getBootstrap().initialize("localhost");
	}
}
