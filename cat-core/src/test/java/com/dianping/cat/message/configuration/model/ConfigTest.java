package com.dianping.cat.message.configuration.model;

import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.configuration.model.entity.Config;
import com.dianping.cat.configuration.model.transform.DefaultParser;
import com.site.helper.Files;

public class ConfigTest {
	@Test
	public void testClient() throws Exception {
		InputStream in = getClass().getResourceAsStream("client.xml");
		String xml = Files.forIO().readFrom(in, "utf-8");
		Config config = new DefaultParser().parse(xml);

		Assert.assertEquals("client", config.getMode());
		Assert.assertEquals("Review", config.getApp().getDomain());
		Assert.assertEquals("192.168.8.1", config.getApp().getIp());

		Assert.assertEquals(3, config.getServers().size());
		Assert.assertEquals(2280, config.getServers().get(0).getPort().intValue());
		Assert.assertEquals(true, config.getServers().get(0).isEnabled());
		Assert.assertEquals(2281, config.getServers().get(1).getPort().intValue());
		Assert.assertEquals(false, config.getServers().get(1).isEnabled());
		Assert.assertEquals(2280, config.getServers().get(2).getPort().intValue());
		Assert.assertEquals(true, config.getServers().get(2).isEnabled());
	}

	@Test
	public void testServer() throws Exception {
		InputStream in = getClass().getResourceAsStream("server.xml");
		String xml = Files.forIO().readFrom(in, "utf-8");
		Config config = new DefaultParser().parse(xml);

		Assert.assertEquals("server", config.getMode());
		Assert.assertEquals("192.168.8.21", config.getBind().getIp());
		Assert.assertEquals(2280, config.getBind().getPort().intValue());

		Assert.assertEquals("[Review, Group]", config.getFilter().getDomains().toString());
	}
}
