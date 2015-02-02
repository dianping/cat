package com.dianping.cat.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;
import org.xml.sax.SAXException;

import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.configuration.client.transform.DefaultSaxParser;
import com.dianping.cat.configuration.client.transform.DefaultXmlBuilder;

public class ConfigTest {
	@Test
	public void testClient() throws Exception {
		ClientConfig clientConfig = loadConfig("client-config.xml");
		ClientConfig globalConfig = loadConfig("global-config.xml");

		Assert.assertEquals("client", clientConfig.getMode());

		globalConfig.accept(new ClientConfigMerger(clientConfig));
		clientConfig.accept(new ClientConfigValidator());

		List<Server> servers = clientConfig.getServers();

		Assert.assertEquals(3, servers.size());
		Assert.assertEquals(2280, servers.get(0).getPort().intValue());
		Assert.assertEquals(true, servers.get(0).isEnabled());
		Assert.assertEquals(2281, servers.get(1).getPort().intValue());
		Assert.assertEquals(false, servers.get(1).isEnabled());
		Assert.assertEquals(2280, servers.get(2).getPort().intValue());
		Assert.assertEquals(true, servers.get(2).isEnabled());
	}

	private ClientConfig loadConfig(String configXml) throws IOException, SAXException {
		InputStream in = getClass().getResourceAsStream(configXml);
		String xml = Files.forIO().readFrom(in, "utf-8");
		ClientConfig clientConfig = DefaultSaxParser.parse(xml);

		return clientConfig;
	}

	@Test
	public void testConfig() throws Exception {
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("config.xml"), "utf-8");
		ClientConfig root = DefaultSaxParser.parse(source);
		String xml = new DefaultXmlBuilder().buildXml(root);
		String expected = source;

		Assert.assertEquals("XML is not well parsed!", expected.replace("\r", ""), xml.replace("\r", ""));
	}

}
