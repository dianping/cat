package com.dianping.cat.message.configuration;

import java.io.InputStream;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.configuration.model.entity.Config;
import com.dianping.cat.configuration.model.entity.Server;
import com.dianping.cat.configuration.model.transform.DefaultXmlParser;
import com.site.helper.Files;

public class ConfigTest {
	@Test
	public void testClient() throws Exception {
		InputStream in = getClass().getResourceAsStream("client.xml");
		String xml = Files.forIO().readFrom(in, "utf-8");
		Config config = new DefaultXmlParser().parse(xml);

		Assert.assertEquals("client", config.getMode());
		Assert.assertEquals("Review", config.getApp().getDomain());
		Assert.assertEquals("192.168.8.1", config.getApp().getIp());

		List<Server> servers = config.getServers();
		
		Assert.assertEquals(3, servers.size());
		Assert.assertEquals(2280, servers.get(0).getPort().intValue());
		Assert.assertEquals(true, servers.get(0).isEnabled());
		Assert.assertEquals(2281, servers.get(1).getPort().intValue());
		Assert.assertEquals(false, servers.get(1).isEnabled());
		Assert.assertEquals(2280, servers.get(2).getPort().intValue());
		Assert.assertEquals(true, servers.get(2).isEnabled());
	}

	@Test
	public void testServer() throws Exception {
		InputStream in = getClass().getResourceAsStream("server.xml");
		String xml = Files.forIO().readFrom(in, "utf-8");
		Config config = new DefaultXmlParser().parse(xml);

		Assert.assertEquals("server", config.getMode());
		Assert.assertEquals("192.168.8.21", config.getBind().getIp());
		Assert.assertEquals(2280, config.getBind().getPort().intValue());

		Assert.assertEquals("[Review, Group]", config.getFilter().getDomains().toString());
	}
}
