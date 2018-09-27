package com.dianping.cat.server;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;
import org.xml.sax.SAXException;

import com.dianping.cat.config.server.ServerConfigVisitor;
import com.dianping.cat.configuration.server.entity.Server;
import com.dianping.cat.configuration.server.entity.ServerConfig;
import com.dianping.cat.configuration.server.transform.DefaultSaxParser;

public class ServerConfigVisitorTest {

	@Test
	public void test() throws IOException, SAXException {
		String server = Files.forIO().readFrom(getClass().getResourceAsStream("server.xml"), "utf-8");
		ServerConfig serverConfig01 = DefaultSaxParser.parse(server);
		ServerConfig serverConfig02 = DefaultSaxParser.parse(server);
		
		Server default01 = serverConfig01.findServer("default");
		Server default02 = serverConfig02.findServer("default");
		Server server01 = serverConfig01.findServer("server01");
		Server server02 = serverConfig02.findServer("server02");

		ServerConfigVisitor visitor01 = new ServerConfigVisitor(server01);
		visitor01.visitServer(default01);

		ServerConfigVisitor visitor02 = new ServerConfigVisitor(server02);
		visitor02.visitServer(default02);

		String expected01 = Files.forIO().readFrom(getClass().getResourceAsStream("server01.xml"), "utf-8");
		Assert.assertEquals(expected01.replace("\r", ""), default01.toString().replace("\r", ""));

		String expected02 = Files.forIO().readFrom(getClass().getResourceAsStream("server02.xml"), "utf-8");
		Assert.assertEquals(expected02.replace("\r", ""), default02.toString().replace("\r", ""));
	}

}
