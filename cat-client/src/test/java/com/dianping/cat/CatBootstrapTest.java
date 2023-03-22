package com.dianping.cat;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dianping.cat.configuration.ConfigureManager;

public class CatBootstrapTest extends ComponentTestCase {
	@After
	public void after() {
	}

	@Before
	public void before() {
	}

	@Test
	public void testInitializeByDomain() {
		Cat.getBootstrap().initializeByDomain("MyDomain");

		ConfigureManager manager = context().lookup(ConfigureManager.class);

		Assert.assertEquals("MyDomain", manager.getDomain());
	}

	@Test
	public void testInitializeByDomainAndServers() {
		Cat.getBootstrap().initializeByDomain("MyDomain", "server1", "server2");

		ConfigureManager manager = context().lookup(ConfigureManager.class);

		Assert.assertEquals("MyDomain", manager.getDomain());
		Assert.assertEquals(2, manager.getServers().size());
		Assert.assertEquals("server1", manager.getServers().get(0).getIp());
		Assert.assertEquals("server2", manager.getServers().get(1).getIp());
	}

	@Test
	public void testInitializeByServers() {
		Cat.getBootstrap().initialize("server1", "server2");

		ConfigureManager manager = context().lookup(ConfigureManager.class);

		Assert.assertEquals(2, manager.getServers().size());
		Assert.assertEquals("server1", manager.getServers().get(0).getIp());
		Assert.assertEquals("server2", manager.getServers().get(1).getIp());
	}

	@Test
	public void testLazyInitialization() {
		Assert.assertEquals(false, Cat.getBootstrap().isInitialized());

		// CAT API call will trigger lazy initialization
		Cat.newTransaction("Type", "Name").success().complete();

		Assert.assertEquals(true, Cat.getBootstrap().isInitialized());
	}
}
