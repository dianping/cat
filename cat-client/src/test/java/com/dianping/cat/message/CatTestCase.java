package com.dianping.cat.message;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import org.junit.After;
import org.junit.Before;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.entity.Server;

public abstract class CatTestCase extends ComponentTestCase {
	protected File getConfigurationFile() {
		if (isCatServerAlive()) {
			try {
				ClientConfig config = new ClientConfig();

				config.setMode("client");
				config.addDomain(new Domain("cat"));
				config.addServer(new Server("localhost").setPort(2280));

				File file = new File("target/cat-config.xml");

				Files.forIO().writeTo(file, config.toString());
				return file;
			} catch (IOException e) {
				return null;
			}
		}

		return null;
	}

	protected boolean isCatServerAlive() {
		// detect if a CAT server listens on localhost:2280
		try {
			SocketChannel channel = SocketChannel.open(new InetSocketAddress("localhost", 2280));

			channel.close();
			return true;
		} catch (Exception e) {
			// ignore it
		}

		return false;
	}

	@Before
	public void setup() throws Exception {
		Cat.initialize(getContainer(), getConfigurationFile());
	}

	@After
	public void teardown() throws Exception {
	}
}